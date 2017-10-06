/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.netty;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Netty {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyHttpServerImplementation extends AbstractNettyHttpServer
		implements HttpServerImplementation, OfficeFloorListener {

	/**
	 * {@link HttpServerImplementationContext}.
	 */
	private HttpServerImplementationContext context;

	/**
	 * {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput;

	/*
	 * ================== HttpServerImplementation ==================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {
		this.context = context;

		// Obtain the service input for handling requests
		this.serviceInput = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Hook into OfficeFloor life-cycle (to start/stop server)
		context.getOfficeFloorDeployer().addOfficeFloorListener(this);
	}

	/*
	 * ===================== OfficeFloorListener ====================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
		HttpServerLocation serverLocation = this.context.getHttpServerLocation();
		this.startHttpServer(serverLocation.getHttpPort(), serverLocation.getHttpsPort(), this.context.getSslContext());
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
		this.stopHttpServer();
	}

	/**
	 * Services the {@link HttpRequest}.
	 * 
	 * @param context
	 *            {@link ChannelHandlerContext}.
	 * @param request
	 *            {@link HttpRequest}.
	 * @throws Exception
	 *             If fail servicing.
	 */
	@Override
	protected void service(ChannelHandlerContext context, HttpRequest request) throws Exception {

		// Obtain the server location
		HttpServerLocation serverLocation = this.context.getHttpServerLocation();

		// Supply the method
		Supplier<HttpMethod> methodSupplier = () -> {
			io.netty.handler.codec.http.HttpMethod nettyHttpMethod = request.method();
			AsciiString methodName = nettyHttpMethod.asciiName();
			if (methodName.contentEquals("GET")) {
				return HttpMethod.GET;
			} else if (methodName.contentEquals("POST")) {
				return HttpMethod.POST;
			} else if (methodName.contentEquals("PUT")) {
				return HttpMethod.PUT;
			} else if (methodName.contentEquals("OPTIONS")) {
				return HttpMethod.OPTIONS;
			} else if (methodName.contentEquals("DELETE")) {
				return HttpMethod.DELETE;
			} else if (methodName.contentEquals("CONNECT")) {
				return HttpMethod.CONNECT;
			} else if (methodName.contentEquals("HEAD")) {
				return HttpMethod.HEAD;
			} else {
				return new HttpMethod(methodName.toString());
			}
		};

		// Supply the request URI
		Supplier<String> requestUriSupplier = () -> request.uri();

		// Obtain the version
		HttpVersion version;
		String versionName = request.protocolVersion().text();
		switch (versionName) {
		case "HTTP/1.0":
			version = HttpVersion.HTTP_1_0;
		case "HTTP/1.1":
			version = HttpVersion.HTTP_1_1;
		default:
			version = new HttpVersion(versionName);
		}

		// Obtain the request headers
		NonMaterialisedHttpHeaders requestHeaders = new NonMaterialisedHttpHeaders() {

			@Override
			public Iterator<NonMaterialisedHttpHeader> iterator() {
				Iterator<Entry<CharSequence, CharSequence>> iterator = request.headers().iteratorCharSequence();
				return new Iterator<NonMaterialisedHttpHeader>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public NonMaterialisedHttpHeader next() {
						Entry<CharSequence, CharSequence> entry = iterator.next();
						return new NonMaterialisedHttpHeader() {

							@Override
							public CharSequence getName() {
								return entry.getKey();
							}

							@Override
							public HttpHeader materialiseHttpHeader() {
								return new SerialisableHttpHeader(entry.getKey().toString(),
										entry.getValue().toString());
							}
						};
					}
				};
			}

			@Override
			public int length() {
				return request.headers().size();
			}
		};

		// Obtain the request entity content
		ByteSequence requestEntity = null;
		if (request instanceof FullHttpRequest) {
			FullHttpRequest fullRequest = (FullHttpRequest) request;
			ByteBuf entityByteBuf = fullRequest.content();
			requestEntity = new ByteSequence() {

				@Override
				public byte byteAt(int index) {
					return entityByteBuf.getByte(index);
				}

				@Override
				public int length() {
					return entityByteBuf.capacity();
				}
			};
		}

		// Create the response
		FullHttpResponse response = new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK, false);

		// Handle response
		HttpResponseWriter<ByteBuf> responseWriter = (responseVersion, status, httpHeader, contentLength, contentType,
				content) -> {

			// Specify the status
			HttpResponseStatus nettyStatus = HttpResponseStatus.valueOf(status.getStatusCode());
			response.setStatus(nettyStatus);

			// Write the content details
			HttpHeaders headers = response.headers();
			if (contentLength > 0) {
				headers.addInt("Content-Length", (int) contentLength);
				if (contentType != null) {
					headers.add("Content-Type", contentType.getValue());
				}
			}

			// Load the remaining headers
			while (httpHeader != null) {
				headers.add(httpHeader.getName(), httpHeader.getValue());
				httpHeader = httpHeader.next;
			}

			// Send the response
			context.executor().execute(() -> {
				context.write(response);
				context.flush();
			});
		};

		// Create the Netty buffer pool
		NettyBufferPool bufferPool = new NettyBufferPool(response);

		// Create the Server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuf> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				serverLocation, false, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity,
				responseWriter, bufferPool);

		// Service the request
		NettyHttpServerImplementation.this.serviceInput.service(connection, connection.getServiceFlowCallback());
	}

}