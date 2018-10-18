/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.stress;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.DateHttpHeaderClock;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 *
 * @author Daniel Sagenschneider
 */
public class ValidateHttpServerImplementation implements HttpServerImplementation {

	/**
	 * Creates the {@link Server} for the {@link HttpServerLocation}.
	 * 
	 * @param serverLocation {@link HttpServerLocation}.
	 * @param sslContext     {@link SSLContext}. May be <code>null</code> if no
	 *                       HTTPS required.
	 * @return Configured but not started {@link Server}.
	 */
	public static Server createServer(HttpServerLocation serverLocation, SSLContext sslContext) {

		// Create the server
		Server server = new Server();
		List<ServerConnector> connectors = new ArrayList<>(2);

		// Provide HTTP connector
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverLocation.getHttpPort());
		connectors.add(connector);

		// Provide HTTPS connector (if required)
		int httpsPort = serverLocation.getHttpsPort();
		if (httpsPort > 0) {

			// Create HTTPS connection
			HttpConfiguration https = new HttpConfiguration();
			https.addCustomizer(new SecureRequestCustomizer());

			// Use the provided SSL context
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setSslContext(sslContext);

			// Provide HTTPS connector
			ServerConnector sslConnector = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
			sslConnector.setPort(httpsPort);
			connectors.add(sslConnector);
		}

		// Configure the connectors
		server.setConnectors(connectors.toArray(new ServerConnector[connectors.size()]));

		// Return the server
		return server;
	}

	/*
	 * ===================== HttpServerImplementation ====================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	public void configureHttpServer(HttpServerImplementationContext context) {

		// Provide server
		Server server = createServer(context.getHttpServerLocation(), context.getSslContext());

		// Create the input
		ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input = context
				.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
						ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Obtain details for servicing
		HttpHeaderValue serverName = new HttpHeaderValue(context.getServerName() + " Jetty");
		HttpServerLocation serverLocation = context.getHttpServerLocation();
		DateHttpHeaderClock dateHttpHeaderClock = context.getDateHttpHeaderClock();

		// Create in memory buffer pool
		StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(() -> ByteBuffer.allocate(1024), 10,
				1000);

		// Create the byte buffer writer
		ByteBufferWriter byteBufferWriter = (buffer, outputStream) -> {
			for (int position = buffer.position(); position < buffer.limit(); position++) {
				outputStream.write(buffer.get());
			}
		};

		// Provide servicing via using input
		ServletHolder servlet = new ServletHolder("handler", new HttpServlet() {

			@Override
			protected void service(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {

				// Start asynchronous servicing
				AsyncContext asyncContext = request.startAsync();

				// Obtain the request / response
				HttpServletRequest asyncRequest = (HttpServletRequest) asyncContext.getRequest();
				HttpServletResponse asyncResponse = (HttpServletResponse) asyncContext.getResponse();

				// Create the writer of the response
				HttpResponseWriter<ByteBuffer> writer = (version, status, headHttpHeader, headHttpCookie, contentLength,
						contentType, contentHeadStreamBuffer) -> {

					// Load values to response
					asyncResponse.setStatus(status.getStatusCode());

					// Load constant headers
					asyncResponse.setHeader("Date", "NOW");
					asyncResponse.setHeader("Expires", "no");

					// Load the headers
					while (headHttpHeader != null) {
						asyncResponse.setHeader(headHttpHeader.getName(), headHttpHeader.getValue());
						headHttpHeader = headHttpHeader.next;
					}

					// Load the cookies
					while (headHttpCookie != null) {
						Cookie cookie = new Cookie(headHttpCookie.getName(), headHttpCookie.getValue());
						cookie.setMaxAge(-1);
						asyncResponse.addCookie(cookie);
						headHttpCookie = headHttpCookie.next;
					}

					// Load the entity
					if (contentType != null) {
						asyncResponse.setContentType(contentType.getValue());
					}

					try {
						// Write the entity content
						ServletOutputStream entity = asyncResponse.getOutputStream();
						while (contentHeadStreamBuffer != null) {
							if (contentHeadStreamBuffer.pooledBuffer != null) {
								// Write the pooled byte buffer
								contentHeadStreamBuffer.pooledBuffer.flip();
								byteBufferWriter.write(contentHeadStreamBuffer.pooledBuffer, entity);

							} else if (contentHeadStreamBuffer.unpooledByteBuffer != null) {
								// Write the unpooled byte buffer
								byteBufferWriter.write(contentHeadStreamBuffer.unpooledByteBuffer, entity);

							} else {
								// Write the file content
								StreamBuffer<ByteBuffer> streamBuffer = bufferPool.getPooledStreamBuffer();
								try {
									ByteBuffer buffer = streamBuffer.pooledBuffer;
									do {
										buffer.clear();
										contentHeadStreamBuffer.fileBuffer.file.read(buffer);
										buffer.flip();
										byteBufferWriter.write(buffer, entity);
									} while (buffer.position() > 0);
								} finally {
									streamBuffer.release();
								}
							}
							contentHeadStreamBuffer = contentHeadStreamBuffer.next;
						}

					} catch (IOException e) {
						throw new IllegalStateException("Should not get failure in writing response");
					}

					// Flag complete
					asyncContext.complete();
				};

				// Create listing of headers
				List<NonMaterialisedHttpHeader> headers = new ArrayList<>();
				Enumeration<String> headerNames = asyncRequest.getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String name = headerNames.nextElement();

					// Add header
					Enumeration<String> headerValues = asyncRequest.getHeaders(name);
					while (headerValues.hasMoreElements()) {
						String value = headerValues.nextElement();
						HttpHeader header = new SerialisableHttpHeader(name, value);
						headers.add(new NonMaterialisedHttpHeader() {

							@Override
							public CharSequence getName() {
								return header.getName();
							}

							@Override
							public HttpHeader materialiseHttpHeader() {
								return header;
							}
						});
					}
				}

				// Create the request headers
				NonMaterialisedHttpHeaders httpHeaders = new NonMaterialisedHttpHeaders() {

					@Override
					public Iterator<NonMaterialisedHttpHeader> iterator() {
						return headers.iterator();
					}

					@Override
					public int length() {
						return headers.size();
					}
				};

				// Create the entity content
				ByteSequence entity = new ByteArrayByteSequence(asyncRequest.getInputStream().readAllBytes());

				// Create the server HTTP connection
				ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
						serverLocation, request.isSecure(), () -> HttpMethod.getHttpMethod(request.getMethod()),
						() -> request.getRequestURI(), HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders,
						entity, serverName, dateHttpHeaderClock, false, writer, bufferPool);

				// Service request
				input.service(connection, connection.getServiceFlowCallback());
			}
		});
		servlet.setAsyncSupported(true);
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(servlet, "/");
		server.setHandler(handler);

		// Configure starting stopping server
		context.getOfficeFloorDeployer().addOfficeFloorListener(new OfficeFloorListener() {

			@Override
			public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
				server.start();
			}

			@Override
			public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
				server.stop();
			}
		});
	}

	/**
	 * Writes the {@link ByteBuffer} to the {@link ServletOutputStream}.
	 */
	@FunctionalInterface
	private static interface ByteBufferWriter {

		/**
		 * Writes the {@link ByteBuffer} to the {@link ServletOutputStream}.
		 * 
		 * @param buffer      {@link ByteBuffer}.
		 * @param outputSteam {@link ServletOutputStream}.
		 * @throws IOException If fails to write {@link IOException}.
		 */
		void write(ByteBuffer buffer, ServletOutputStream outputSteam) throws IOException;
	}

}