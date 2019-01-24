/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.rapidoid;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.rapidoid.buffer.Buf;
import org.rapidoid.config.Conf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.impl.lowlevel.HttpIO;
import org.rapidoid.net.Server;
import org.rapidoid.net.ServerBuilder;
import org.rapidoid.net.TCP;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Rapidoid {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class RapidoidHttpServerImplementation extends AbstractHttpServer
		implements HttpServerImplementation, OfficeFloorListener {

	/**
	 * Name of {@link System} property to obtain the pooled {@link StreamBuffer}
	 * size.
	 */
	public static final String SYSTEM_PROPERTY_STREAM_BUFFER_SIZE = "rapidoid.socket.stream.buffer.size";

	/**
	 * Name of {@link System} property to obtain the maximum
	 * {@link ThreadLocalStreamBufferPool} {@link ThreadLocal} pool size.
	 */
	public static final String SYSTEM_PROPERTY_THREADLOCAL_BUFFER_POOL_MAX_SIZE = "rapidoid.socket.threadlocal.buffer.pool.max.size";

	/**
	 * Name of {@link System} property to obtain the maximum
	 * {@link ThreadLocalStreamBufferPool} core pool size.
	 */
	public static final String SYSTEM_PROPERTY_CORE_BUFFER_POOL_MAX_SIZE = "rapidoid.socket.core.buffer.pool.max.size";

	/**
	 * Obtains the {@link System} property value.
	 * 
	 * @param name
	 *            Name of the {@link System} property.
	 * @param defaultValue
	 *            Default value.
	 * @return {@link System} property value.
	 */
	private static int getSystemProperty(String name, int defaultValue) {
		String text = System.getProperty(name, null);
		if (CompileUtil.isBlank(text)) {
			// No value configured, so use default
			return defaultValue;

		} else {
			// Attempt to parse the configured value
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException ex) {
				// Invalid value
				throw new NumberFormatException(
						"Invalid system configured value for " + name + " '" + text + "'.  Must be an integer.");
			}
		}
	}

	/**
	 * {@link HttpServerImplementationContext}.
	 */
	private HttpServerImplementationContext context;

	/**
	 * <code>Server</code> {@link HttpHeaderValue}.
	 */
	private HttpHeaderValue serverName;

	/**
	 * {@link DateHttpHeaderClock}.
	 */
	private DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * Indicates whether to include the stack trace.
	 */
	private boolean isIncludeStackTrace;

	/**
	 * {@link StreamBufferPool}.
	 */
	private StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * HTTP {@link Server}.
	 */
	private Server httpServer;

	/**
	 * HTTPS {@link Server}.
	 */
	private Server httpsServer;

	/**
	 * {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput;

	/*
	 * ================= HttpServerImplementation ======================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {
		this.context = context;

		// Obtain the headers
		this.serverName = HttpServer.getServerHttpHeaderValue(context, "Rapidoid");
		this.dateHttpHeaderClock = context.getDateHttpHeaderClock();

		// Determine if include stack trace
		this.isIncludeStackTrace = context.isIncludeEscalationStackTrace();

		// Obtain the service input for handling requests
		this.serviceInput = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Hook into OfficeFloor life-cycle (to start/stop server)
		context.getOfficeFloorDeployer().addOfficeFloorListener(this);
	}

	/*
	 * ===================== OfficeFloorListener =======================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {

		// Create the buffer pool
		int streamBufferSize = getSystemProperty(SYSTEM_PROPERTY_STREAM_BUFFER_SIZE, 8192);
		int maxThreadLocalPoolSize = getSystemProperty(SYSTEM_PROPERTY_THREADLOCAL_BUFFER_POOL_MAX_SIZE,
				Integer.MAX_VALUE);
		int maxCorePoolSize = getSystemProperty(SYSTEM_PROPERTY_CORE_BUFFER_POOL_MAX_SIZE, Integer.MAX_VALUE);
		this.bufferPool = new ThreadLocalStreamBufferPool(() -> ByteBuffer.allocateDirect(streamBufferSize),
				maxThreadLocalPoolSize, maxCorePoolSize);

		// Create the initial server builder
		Supplier<ServerBuilder> initialBuilder = () -> TCP.server(Conf.HTTP).protocol(this).address("0.0.0.0")
				.syncBufs(false);

		// Start the HTTP server
		this.httpServer = initialBuilder.get().port(this.context.getHttpServerLocation().getClusterHttpPort()).build()
				.start();

		// Start the SSL server
		int httpsPort = this.context.getHttpServerLocation().getClusterHttpsPort();
		if (httpsPort > 0) {
			ServerBuilder httpsBuilder = initialBuilder.get().port(httpsPort);
			SSLContext sslContext = this.context.getSslContext();
			if (sslContext != null) {
				httpsBuilder = httpsBuilder.tlsContext(sslContext).tls(true);
			}
			this.httpsServer = httpsBuilder.build().start();
		}
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {

		// Stop the HTTP server
		this.httpServer.shutdown();

		// Stop the HTTPS server
		if (this.httpsServer != null) {
			this.httpsServer.shutdown();
		}
	}

	/*
	 * ==================== AbstractHttpServer =========================
	 */

	@Override
	protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper data) {

		// Obtain the server location
		HttpServerLocation serverLocation = this.context.getHttpServerLocation();

		// Supply the method
		HttpMethod httpMethod;
		if (data.isGet.value) {
			httpMethod = HttpMethod.GET;
		} else {
			String verb = data.verb.str(buf);
			httpMethod = HttpMethod.getHttpMethod(verb);
		}
		Supplier<HttpMethod> methodSupplier = () -> httpMethod;

		// Supply the request URI
		String uri = data.uri.str(buf);
		Supplier<String> requestUriSupplier = () -> uri;

		// Obtain the version
		HttpVersion version;
		String versionName = data.protocol.str(buf);
		switch (versionName) {
		case "HTTP/1.0":
			version = HttpVersion.HTTP_1_0;
			break;
		case "HTTP/1.1":
			version = HttpVersion.HTTP_1_1;
			break;
		default:
			version = new HttpVersion(versionName);
			break;
		}

		// Obtain the headers
		final int NAME_INDEX = 0;
		final int VALUE_INDEX = 1;
		HTTP_PARSER.parseHeadersIntoKV(buf, data.headers, data.headersKV.reset(), null, data);
		String[][] headers = new String[data.headersKV.count][2];
		for (int i = 0; i < data.headersKV.count; i++) {
			headers[i][NAME_INDEX] = data.headersKV.keys[i].str(buf);
			headers[i][VALUE_INDEX] = data.headersKV.values[i].str(buf);
		}
		NonMaterialisedHttpHeaders requestHeaders = new NonMaterialisedHttpHeaders() {

			@Override
			public Iterator<NonMaterialisedHttpHeader> iterator() {
				return new Iterator<NonMaterialisedHttpHeader>() {

					private int nextHeader = 0;

					@Override
					public boolean hasNext() {
						return this.nextHeader < headers.length;
					}

					@Override
					public NonMaterialisedHttpHeader next() {
						String name = headers[this.nextHeader][NAME_INDEX];
						String value = headers[this.nextHeader++][VALUE_INDEX];
						return new NonMaterialisedHttpHeader() {

							@Override
							public CharSequence getName() {
								return name;
							}

							@Override
							public HttpHeader materialiseHttpHeader() {
								return new SerialisableHttpHeader(name, value);
							}
						};
					}
				};
			}

			@Override
			public int length() {
				return headers.length;
			}
		};

		// Obtain the request entity content
		ByteSequence requestEntity = ByteSequence.EMPTY;
		if (data.body.length > 0) {
			int bodyStart = data.body.start;
			int bodyLength = data.body.length;
			requestEntity = new ByteSequence() {

				@Override
				public byte byteAt(int index) {
					return buf.get(bodyStart + index);
				}

				@Override
				public int length() {
					return bodyLength;
				}
			};
		}

		// Handle response
		HttpResponseWriter<ByteBuffer> responseWriter = (responseVersion, status, httpHeader, httpCookie, contentLength,
				contentType, content) -> {

			// Write the response status line
			ctx.write(responseVersion.getName());
			ctx.write(SPACE_);
			ctx.write(String.valueOf(status.getStatusCode()));
			ctx.write(SPACE_);
			ctx.write(status.getStatusMessage());
			ctx.write(CR_LF);

			// Write the content details
			if (contentLength > 0) {
				HttpIO.INSTANCE.writeContentLengthHeader(ctx, (int) contentLength);
				if (contentType != null) {
					ctx.write(CONTENT_TYPE_TXT);
					ctx.write(contentType.getValue());
					ctx.write(CR_LF);
				}
			}

			// Load the remaining headers
			while (httpHeader != null) {
				ctx.write(httpHeader.getName());
				ctx.write(": ");
				ctx.write(httpHeader.getValue());
				ctx.write(CR_LF);
				httpHeader = httpHeader.next;
			}

			// Load the cookies
			while (httpCookie != null) {
				ctx.write("set-cookie: ");
				ctx.write(httpCookie.toResponseHeaderValue());
				ctx.write(CR_LF);
				httpCookie = httpCookie.next;
			}

			ctx.write(CR_LF);

			// Write the entity
			while (content != null) {
				if (content.pooledBuffer != null) {
					// Write the pooled buffer
					content.pooledBuffer.flip();
					ctx.write(content.pooledBuffer);

				} else if (content.unpooledByteBuffer != null) {
					// Write the unpooled buffer
					ctx.write(content.unpooledByteBuffer);

				} else if (content.fileBuffer != null) {
					// Write the file content
					// Not change position to allow re-use of file channel
					FileBuffer fileBuffer = content.fileBuffer;
					StreamBuffer<ByteBuffer> transfer = this.bufferPool.getPooledStreamBuffer();
					try {
						long position = fileBuffer.position;
						long bytesToTransfer = fileBuffer.count;
						if (bytesToTransfer < 0) {
							bytesToTransfer = fileBuffer.file.size();
						}
						while (bytesToTransfer > 0) {
							int bytesRead = fileBuffer.file.read(transfer.pooledBuffer, position);
							long bytesToWrite = Math.min(bytesToTransfer, bytesRead);
							transfer.pooledBuffer.flip();
							ctx.write(transfer.pooledBuffer);
							bytesToTransfer -= bytesToWrite;
						}
						if (fileBuffer.callback != null) {
							fileBuffer.callback.complete(fileBuffer.file, true);
						}

					} catch (Exception ex) {
						// Send failure
						ctx.restart();
						ctx.write(HTTP_500);

					} finally {
						// Ensure buffer released
						transfer.release();
					}
				}
				content = content.next;
			}

			// Determine if keep alive
			ctx.send();
			ctx.closeIf(!data.isKeepAlive.value);
		};

		// Create the Server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				serverLocation, false, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity,
				this.serverName, this.dateHttpHeaderClock, this.isIncludeStackTrace, responseWriter, this.bufferPool);

		// Service the request
		this.serviceInput.service(connection, connection.getServiceFlowCallback());

		// Always asynchronous
		return HttpStatus.ASYNC;
	}

}