/*-
 * #%L
 * Undertow HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.xnio.channels.StreamSinkChannel;

import io.undertow.io.Receiver.FullBytesCallback;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerImplementationFactory;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Undertow {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class UndertowHttpServerImplementation extends AbstractUndertowHttpServer
		implements HttpServerImplementation, HttpServerImplementationFactory, OfficeFloorListener {

	/**
	 * HTTP Header name <code>Content-Length</code>.
	 */
	private static final HttpString CONTENT_LENGTH = new HttpString("Content-Length");

	/**
	 * HTTP Header name <code>Content-Type</code>.
	 */
	private static final HttpString CONTENT_TYPE = new HttpString("Content-Type");

	/**
	 * HTTP Header name <code>set-cookie</code>.
	 */
	private static final HttpString SET_COOKIE = new HttpString("set-cookie");

	/**
	 * {@link Executor} to dispatch request.
	 */
	private static final Executor DISPATCH_EXECUTOR = (runnable) -> runnable.run();

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
	 * {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput;

	/*
	 * ================== HttpServerImplementation ==================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {
		this.context = context;

		// Obtain the headers
		this.serverName = HttpServer.getServerHttpHeaderValue(context, "Undertow");
		this.dateHttpHeaderClock = context.getDateHttpHeaderClock();

		// Determine if include stack trace
		this.isIncludeStackTrace = context.isIncludeEscalationStackTrace();

		// Obtain the service input for handling requests
		this.serviceInput = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// TODO configure the buffer pooling
		int byteBufferCapacity = 1024;
		int maxThreadLocalPoolSize = Integer.MAX_VALUE;
		int maxCorePoolSize = Integer.MAX_VALUE;

		// Create stream buffer pool
		this.bufferPool = new ThreadLocalStreamBufferPool(() -> ByteBuffer.allocateDirect(byteBufferCapacity),
				maxThreadLocalPoolSize, maxCorePoolSize);

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

	@Override
	protected ProcessManager service(HttpServerExchange exchange) throws Exception {
		HttpServerExchangeServicer servicer = new HttpServerExchangeServicer();
		exchange.getRequestReceiver().receiveFullBytes(servicer);
		return servicer;
	}

	/**
	 * Services the {@link HttpServerExchange}.
	 */
	private class HttpServerExchangeServicer implements FullBytesCallback, ProcessManager {

		/**
		 * {@link ProcessManager}.
		 */
		private volatile ProcessManager processManager = null;

		/*
		 * ================== FullBytesCallback =========================
		 */

		@Override
		public void handle(HttpServerExchange exchange, byte[] message) {

			// Obtain the server location
			HttpServerLocation serverLocation = UndertowHttpServerImplementation.this.context.getHttpServerLocation();

			// Supply the method
			Supplier<HttpMethod> methodSupplier = () -> {
				HttpString undertowHttpMethod = exchange.getRequestMethod();
				if (undertowHttpMethod.equalToString("GET")) {
					return HttpMethod.GET;
				} else if (undertowHttpMethod.equalToString("POST")) {
					return HttpMethod.POST;
				} else if (undertowHttpMethod.equalToString("PUT")) {
					return HttpMethod.PUT;
				} else if (undertowHttpMethod.equalToString("OPTIONS")) {
					return HttpMethod.OPTIONS;
				} else if (undertowHttpMethod.equalToString("DELETE")) {
					return HttpMethod.DELETE;
				} else if (undertowHttpMethod.equalToString("CONNECT")) {
					return HttpMethod.CONNECT;
				} else if (undertowHttpMethod.equalToString("HEAD")) {
					return HttpMethod.HEAD;
				} else {
					return HttpMethod.getHttpMethod(undertowHttpMethod.toString());
				}
			};

			// Supply the request URI
			Supplier<String> requestUriSupplier = () -> {
				String requestUri = exchange.getRequestURI();
				String queryString = exchange.getQueryString();
				return ((queryString == null) || ("".equals(queryString))) ? requestUri
						: requestUri + "?" + queryString;
			};

			// Obtain the version
			HttpVersion version;
			String versionName = exchange.getProtocol().toString();
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

			// Obtain the request headers
			NonMaterialisedHttpHeaders requestHeaders = new NonMaterialisedHttpHeaders() {

				@Override
				public Iterator<NonMaterialisedHttpHeader> iterator() {
					Iterator<HeaderValues> nameIterator = exchange.getRequestHeaders().iterator();
					return new Iterator<NonMaterialisedHttpHeader>() {

						private HeaderValues currentHeader = null;

						private Iterator<String> currentValueIterator = null;

						@Override
						public boolean hasNext() {
							if ((currentValueIterator != null) && (currentValueIterator.hasNext())) {
								return true;
							}
							return nameIterator.hasNext();
						}

						@Override
						public NonMaterialisedHttpHeader next() {

							// Obtain first or no further values for name
							if ((currentHeader == null) || (!currentValueIterator.hasNext())) {
								currentHeader = nameIterator.next();
								currentValueIterator = currentHeader.iterator();
							}

							// Return the header for value
							HeaderValues name = currentHeader;
							String value = currentValueIterator.next();
							return new NonMaterialisedHttpHeader() {

								@Override
								public CharSequence getName() {
									return name.getHeaderName().toString();
								}

								@Override
								public HttpHeader materialiseHttpHeader() {
									return new SerialisableHttpHeader(name.getHeaderName().toString(), value);
								}
							};
						}
					};
				}

				@Override
				public int length() {
					return exchange.getRequestHeaders().size();
				}
			};

			// Create the byte sequence
			ByteSequence requestEntity = new ByteSequence() {

				@Override
				public byte byteAt(int index) {
					return message[index];
				}

				@Override
				public int length() {
					return message.length;
				}
			};

			// Handle response
			HttpResponseWriter<ByteBuffer> responseWriter = new UndertowHttpResponseWriter(exchange);

			// Create the Server HTTP connection
			ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
					serverLocation, false, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity,
					UndertowHttpServerImplementation.this.serverName,
					UndertowHttpServerImplementation.this.dateHttpHeaderClock,
					UndertowHttpServerImplementation.this.isIncludeStackTrace, responseWriter,
					UndertowHttpServerImplementation.this.bufferPool);

			// Service the request (dispatched to avoid threading issues)
			exchange.dispatch(DISPATCH_EXECUTOR, () -> {
				this.processManager = UndertowHttpServerImplementation.this.serviceInput.service(connection,
						connection.getServiceFlowCallback());
			});
		}

		/*
		 * =================== ProcessManager ===========================
		 */

		@Override
		public void cancel() {

			// Only able to cancel once started servicing
			if (this.processManager != null) {
				this.processManager.cancel();
			}
		}
	}

	private class UndertowHttpResponseWriter implements HttpResponseWriter<ByteBuffer>, Runnable {

		/**
		 * {@link HttpServerExchange}.
		 */
		private final HttpServerExchange exchange;

		/**
		 * Response {@link StreamSinkChannel}.
		 */
		private StreamSinkChannel responseChannel = null;

		/**
		 * Entity content.
		 */
		private StreamBuffer<ByteBuffer> content = null;

		/**
		 * Instantiate.
		 * 
		 * @param exchange {@link HttpServerExchange}.
		 */
		public UndertowHttpResponseWriter(HttpServerExchange exchange) {
			this.exchange = exchange;
		}

		/**
		 * Prepares the {@link StreamBuffer} for writing.
		 * 
		 * @param buffer {@link StreamBuffer} to write.
		 */
		private void prepareStreamBuffer(StreamBuffer<ByteBuffer> buffer) {
			if (buffer.pooledBuffer != null) {
				BufferJvmFix.flip(buffer.pooledBuffer);
			}
		}

		/*
		 * ==================== HttpResponseWriter ==========================
		 */

		@Override
		public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader httpHeader,
				WritableHttpCookie httpCookie, long contentLength, HttpHeaderValue contentType,
				StreamBuffer<ByteBuffer> content) {

			// Specify the status
			this.exchange.setStatusCode(status.getStatusCode());

			// Write the content details
			HeaderMap headers = this.exchange.getResponseHeaders();
			if (contentLength > 0) {
				headers.add(CONTENT_LENGTH, contentLength);
				if (contentType != null) {
					headers.add(CONTENT_TYPE, contentType.getValue());
				}
			}

			// Load the remaining headers
			while (httpHeader != null) {
				headers.add(new HttpString(httpHeader.getName()), httpHeader.getValue());
				httpHeader = httpHeader.next;
			}

			// Load the cookies
			while (httpCookie != null) {
				headers.add(SET_COOKIE, httpCookie.toResponseHeaderValue());
				httpCookie = httpCookie.next;
			}

			// Determine if entity to write
			if (content == null) {
				this.exchange.endExchange();
				return; // no entity, so done
			}

			// Write the entity and complete
			this.responseChannel = this.exchange.getResponseChannel();
			this.content = content;
			this.prepareStreamBuffer(this.content);
			this.run();
		}

		/*
		 * =================== Runnable =============================
		 */

		@Override
		public void run() {

			// Loop until complete write
			for (;;) {

				// Continue writing existing buffer
				boolean isFurtherBufferWrite;
				try {
					if (this.content.pooledBuffer != null) {
						// Write the pooled buffer
						ByteBuffer byteBuffer = this.content.pooledBuffer;
						this.responseChannel.write(byteBuffer);
						isFurtherBufferWrite = byteBuffer.hasRemaining();

					} else if (this.content.unpooledByteBuffer != null) {
						// Write the unpooled buffer
						ByteBuffer byteBuffer = this.content.unpooledByteBuffer;
						this.responseChannel.write(byteBuffer);
						isFurtherBufferWrite = byteBuffer.hasRemaining();

					} else if (this.content.fileBuffer != null) {
						// Write the file content
						FileBuffer writeBuffer = this.content.fileBuffer;

						// Determine the position and count
						long position = writeBuffer.position + writeBuffer.bytesWritten;
						long count = (writeBuffer.count < 0 ? writeBuffer.file.size() - writeBuffer.position
								: writeBuffer.count) - writeBuffer.bytesWritten;

						// Write the file content to the socket
						long bytesWritten = writeBuffer.file.transferTo(position, count, this.responseChannel);

						// Increment the number of bytes written
						writeBuffer.bytesWritten += bytesWritten;

						// Determine if written all bytes
						isFurtherBufferWrite = (bytesWritten < count);

						// If all written, the file complete
						if ((!isFurtherBufferWrite) && (writeBuffer.callback != null)) {
							writeBuffer.callback.complete(writeBuffer.file, true);
						}

					} else {
						throw new IllegalStateException("Unknown " + StreamBuffer.class.getSimpleName() + " type");
					}

				} catch (Throwable ex) {

					// Release all buffers, as failure in writing entity
					StreamBuffer<ByteBuffer> release;
					while (this.content != null) {
						release = this.content;
						this.content = this.content.next;
						release.release();
					}

					// Failure with connection, so close
					try {
						this.exchange.getConnection().close();
					} catch (IOException e) {
						// Best attempts made
					}

					// Nothing further
					return;
				}

				// If further to write from buffer, dispatch to handle
				if (isFurtherBufferWrite) {
					this.exchange.dispatch(this);
					return;
				}

				// Prepare the next buffer (releasing written buffer)
				StreamBuffer<ByteBuffer> release = this.content;
				this.content = this.content.next;
				release.release();
				if (this.content == null) {
					// No further content, so end exchange
					this.exchange.endExchange();
					return;
				}

				// Prepare next buffer (looping to write)
				this.prepareStreamBuffer(this.content);
			}
		}
	}

}