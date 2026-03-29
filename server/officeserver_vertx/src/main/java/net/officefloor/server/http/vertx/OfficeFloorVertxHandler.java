/*-
 * #%L
 * Vertx HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http.vertx;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Supplier;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpServerConnection;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link Vertx} {@link Handler} to service with {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorVertxHandler implements Handler<HttpServerRequest> {

	/**
	 * {@link ProcessManager} key.
	 */
	private static final AttributeKey<ProcessManager> PROCESS_MANAGER_KEY = AttributeKey.valueOf("KEY");

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation;

	/**
	 * <code>Server</code> {@link HttpHeaderValue}.
	 */
	private final HttpHeaderValue serverName;

	/**
	 * {@link DateHttpHeaderClock}.
	 */
	private final DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * Indicates whether to include the stack trace.
	 */
	private final boolean isIncludeStackTrace;

	/**
	 * {@link ExternalServiceInput} to handle {@link Vertx}
	 * {@link HttpServerRequest} instances.
	 */
	@SuppressWarnings("rawtypes")
	private final ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput;

	/**
	 * Instantiate.
	 * 
	 * @param serverLocation      {@link HttpServerLocation}.
	 * @param serverName          <code>Server</code> {@link HttpHeaderValue}.
	 * @param dateHttpHeaderClock {@link DateHttpHeaderClock}.
	 * @param isIncludeStackTrace Indicates whether to include the stack trace.
	 * @param serviceInput        {@link ExternalServiceInput} to handle
	 *                            {@link Vertx} {@link HttpServerRequest} instances.
	 */
	@SuppressWarnings("rawtypes")
	public OfficeFloorVertxHandler(HttpServerLocation serverLocation, HttpHeaderValue serverName,
			DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeStackTrace,
			ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput) {
		this.serverLocation = serverLocation;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeStackTrace = isIncludeStackTrace;
		this.serviceInput = serviceInput;
	}

	/*
	 * ===================== Handler =========================
	 */

	@Override
	public void handle(HttpServerRequest request) {
		request.bodyHandler((requestEntity) -> {

			// Ensure flag process manager handling on channel
			HttpServerConnection vertxConnection = (HttpServerConnection) request.connection();
			Channel channel = vertxConnection.channel();
			Attribute<ProcessManager> attribute = channel.attr(PROCESS_MANAGER_KEY);
			if (attribute.get() == null) {

				// First request, so load the close listener
				channel.closeFuture().addListener((future) -> {
					ProcessManager processManager = channel.attr(PROCESS_MANAGER_KEY).get();
					processManager.cancel();
				});
			}

			// Supply the method
			Supplier<HttpMethod> methodSupplier = () -> {
				io.vertx.core.http.HttpMethod vertxHttpMethod = request.method();
				String method = vertxHttpMethod.name();
				switch (method) {
				case "GET":
					return HttpMethod.GET;
				case "POST":
					return HttpMethod.POST;
				case "PUT":
					return HttpMethod.PUT;
				case "OPTIONS":
					return HttpMethod.OPTIONS;
				case "DELETE":
					return HttpMethod.DELETE;
				case "CONNECT":
					return HttpMethod.CONNECT;
				case "HEAD":
					return HttpMethod.HEAD;
				default:
					return HttpMethod.getHttpMethod(method);
				}
			};

			// Supply the request URI
			Supplier<String> requestUriSupplier = () -> request.uri();

			// Obtain the version
			HttpVersion version;
			switch (request.version()) {
			case HTTP_1_0:
				version = HttpVersion.HTTP_1_0;
				break;
			case HTTP_1_1:
				version = HttpVersion.HTTP_1_1;
				break;
			default:
				version = new HttpVersion(request.version().alpnName());
				break;
			}

			// Obtain the request headers
			NonMaterialisedHttpHeaders requestHeaders = new NonMaterialisedHttpHeaders() {

				@Override
				public Iterator<NonMaterialisedHttpHeader> iterator() {
					Iterator<Entry<String, String>> iterator = request.headers().iterator();
					return new Iterator<NonMaterialisedHttpHeader>() {

						@Override
						public boolean hasNext() {
							return iterator.hasNext();
						}

						@Override
						public NonMaterialisedHttpHeader next() {
							Entry<String, String> entry = iterator.next();
							return new NonMaterialisedHttpHeader() {

								@Override
								public CharSequence getName() {
									return entry.getKey();
								}

								@Override
								public HttpHeader materialiseHttpHeader() {
									return new SerialisableHttpHeader(entry.getKey(), entry.getValue());
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

			// Obtain the request entity
			ByteSequence requestEntitySequence = new ByteSequence() {

				@Override
				public byte byteAt(int index) {
					return requestEntity.getByte(index);
				}

				@Override
				public int length() {
					return requestEntity.length();
				}
			};

			// Create the response buffer
			VertxBufferPool responseBuffer = new VertxBufferPool();

			// Handle response
			HttpResponseWriter<Buffer> responseWriter = (responseVersion, status, httpHeader, httpCookie, contentLength,
					contentType, content) -> {

				// Obtain the response
				HttpServerResponse response = request.response();

				// Specify the status
				response.setStatusCode(status.getStatusCode());
				response.setStatusMessage(status.getStatusMessage());

				// Write the content details
				if (contentLength > 0) {
					response.putHeader("Content-Length", String.valueOf(contentLength));
					if (contentType != null) {
						response.putHeader("Content-Type", contentType.getValue());
					}
				}

				// Load the remaining headers
				while (httpHeader != null) {
					response.putHeader(httpHeader.getName(), httpHeader.getValue());
					httpHeader = httpHeader.next;
				}

				// Load the cookies
				while (httpCookie != null) {
					response.putHeader("set-cookie", httpCookie.toResponseHeaderValue());
					httpCookie = httpCookie.next;
				}

				// Write entity and send response
				response.send(responseBuffer.pooledBuffer);
			};

			// Create the Server HTTP connection
			ProcessAwareServerHttpConnectionManagedObject<Buffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
					this.serverLocation, false, methodSupplier, requestUriSupplier, version, requestHeaders,
					requestEntitySequence, this.serverName, this.dateHttpHeaderClock, this.isIncludeStackTrace,
					responseWriter, responseBuffer);

			// Service the request
			ProcessManager processManager = this.serviceInput.service(connection, connection.getServiceFlowCallback());

			// Register for cancel handling on close
			attribute.set(processManager);
		});
	}

}
