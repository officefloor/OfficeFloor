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
package net.officefloor.server.http;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.RequestHandler;
import net.officefloor.server.RequestServicer;
import net.officefloor.server.RequestServicerFactory;
import net.officefloor.server.ResponseWriter;
import net.officefloor.server.SocketServicer;
import net.officefloor.server.SocketServicerFactory;
import net.officefloor.server.http.impl.DateHttpHeaderClock;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link SocketServicerFactory} and {@link RequestServicerFactory} to use the
 * {@link HttpRequestParser} to produce {@link ServerHttpConnection} instances
 * for servicing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServicerFactory
		implements SocketServicerFactory<HttpRequestParser>, RequestServicerFactory<HttpRequestParser> {

	private static final byte[] SPACE = " ".getBytes(ServerHttpConnection.HTTP_CHARSET);
	private static byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);
	private static byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	private static final HttpHeaderName CONTENT_LENGTH_NAME = new HttpHeaderName("Content-Length");
	private static final HttpHeaderName CONTENT_TYPE_NAME = new HttpHeaderName("Content-Type");

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation;

	/**
	 * Indicates if over secure {@link Socket}.
	 */
	private final boolean isSecure;

	/**
	 * {@link HttpRequestParserMetaData}.
	 */
	private final HttpRequestParserMetaData metaData;

	/**
	 * {@link StreamBufferPool} for servicing requests to capture the response
	 * entity.
	 */
	private final StreamBufferPool<ByteBuffer> serviceBufferPool;

	/**
	 * Throttle active request threshold. When this number of requests are active,
	 * further requests a throttled down to allow the active requests to be
	 * processed.
	 */
	private final int throttleActiveRequestThreshold;

	/**
	 * <code>Server</code> {@link HttpHeaderValue}.
	 */
	private final HttpHeaderValue serverName;

	/**
	 * {@link DateHttpHeaderClock}.
	 */
	private final DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * Indicates if include the {@link Escalation} stack trace in the
	 * {@link HttpResponse}.
	 */
	private boolean isIncludeEscalationStackTrace;

	/**
	 * Services the {@link ProcessAwareServerHttpConnectionManagedObject}.
	 * 
	 * @param connection
	 *            {@link ProcessAwareServerHttpConnectionManagedObject}.
	 * @throws IOException
	 *             If IO failure.
	 * @throws HttpException
	 *             If HTTP failure.
	 */
	protected abstract void service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
			throws IOException, HttpException;

	/*
	 * =============== SocketServicerFactory =================
	 */

	@Override
	public SocketServicer<HttpRequestParser> createSocketServicer(RequestHandler<HttpRequestParser> requestHandler) {
		return new HttpServicer(requestHandler);
	}

	/*
	 * =============== RequestServicerFactory ================
	 */

	@Override
	public RequestServicer<HttpRequestParser> createRequestServicer(SocketServicer<HttpRequestParser> socketServicer) {
		return (HttpServicer) socketServicer;
	}

	/**
	 * Instantiate.
	 * 
	 * @param serverLocation
	 *            {@link HttpServerLocation}.
	 * @param isSecure
	 *            Indicates if over secure {@link Socket}.
	 * @param serviceBufferPool
	 *            {@link StreamBufferPool} used to service requests.
	 * @param metaData
	 *            {@link HttpRequestParserMetaData}.
	 * @param throttleActiveRequestThreshold
	 *            Threshold of active requests to start throttling new requests.
	 * @param serverName
	 *            <code>Server</code> {@link HttpHeaderValue}.
	 * @param dateHttpHeaderClock
	 *            {@link DateHttpHeaderClock}.
	 * @param isIncludeEscalationStackTrace
	 *            Indicates whether to include the {@link Escalation} stack trace in
	 *            {@link HttpResponse}.
	 */
	public AbstractHttpServicerFactory(HttpServerLocation serverLocation, boolean isSecure,
			HttpRequestParserMetaData metaData, StreamBufferPool<ByteBuffer> serviceBufferPool,
			int throttleActiveRequestThreshold, HttpHeaderValue serverName, DateHttpHeaderClock dateHttpHeaderClock,
			boolean isIncludeEscalationStackTrace) {
		this.serverLocation = serverLocation;
		this.isSecure = isSecure;
		this.metaData = metaData;
		this.serviceBufferPool = serviceBufferPool;
		this.throttleActiveRequestThreshold = throttleActiveRequestThreshold;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
	}

	/**
	 * HTTP servicer.
	 */
	private class HttpServicer extends HttpRequestParser
			implements SocketServicer<HttpRequestParser>, RequestServicer<HttpRequestParser> {

		/**
		 * {@link RequestHandler}.
		 */
		private final RequestHandler<HttpRequestParser> requestHandler;

		/**
		 * {@link HttpException} in attempting to parse {@link HttpRequest}.
		 */
		private HttpException parseFailure = null;

		/**
		 * Active requests.
		 */
		private final AtomicInteger activeRequests = new AtomicInteger(0);

		/**
		 * Instantiate.
		 * 
		 * @param requestHandler
		 *            {@link RequestHandler}.
		 */
		private HttpServicer(RequestHandler<HttpRequestParser> requestHandler) {
			super(AbstractHttpServicerFactory.this.metaData);
			this.requestHandler = requestHandler;
		}

		/*
		 * ===================== SocketServicer ======================
		 */

		@Override
		public void service(StreamBuffer<ByteBuffer> readBuffer, boolean isNewBuffer) {

			// Add the buffer
			this.appendStreamBuffer(readBuffer);

			// Parse out the requests
			try {
				while (this.parse()) {

					// New request, so increment active connections
					int active = this.activeRequests.incrementAndGet();

					// Slow incoming requests by allowing other threads to process active requests
					if (active > AbstractHttpServicerFactory.this.throttleActiveRequestThreshold) {
						Thread.yield();
					}

					// Create request from parser
					this.requestHandler.handleRequest(this);
				}
			} catch (HttpException ex) {
				// Failed to parse request
				this.parseFailure = ex;
				this.requestHandler.handleRequest(this);
			}
		}

		/*
		 * ===================== RequestServicer ====================
		 */

		@Override
		public void service(HttpRequestParser request, ResponseWriter responseWriter) {

			// Determine if parse failure
			if (this.parseFailure != null) {
				// Write parse failure
				responseWriter.write((responseHead, socketBufferPool) -> {
					this.parseFailure.writeHttpResponse(HttpVersion.HTTP_1_1,
							AbstractHttpServicerFactory.this.isIncludeEscalationStackTrace, responseHead,
							socketBufferPool);
				}, null);
				return;
			}

			// Request ready, so obtain details
			Supplier<HttpMethod> methodSupplier = this.getMethod();
			Supplier<String> requestUriSupplier = this.getRequestURI();
			HttpVersion version = this.getVersion();
			NonMaterialisedHttpHeaders requestHeaders = this.getHeaders();
			ByteSequence requestEntity = this.getEntity();

			// Create the HTTP response writer
			HttpResponseWriter<ByteBuffer> writer = (responseVersion, status, httpHeader, httpCookie, contentLength,
					contentType, content) -> {

				// Write the response
				responseWriter.write((responseHead, socketBufferPool) -> {

					// Active request serviced
					this.activeRequests.decrementAndGet();

					// Write the status line
					responseVersion.write(responseHead, socketBufferPool);
					StreamBuffer.write(SPACE, 0, SPACE.length, responseHead, socketBufferPool);
					status.write(responseHead, socketBufferPool);
					StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHead, socketBufferPool);

					// Write the headers
					if (contentType != null) {
						CONTENT_TYPE_NAME.write(responseHead, socketBufferPool);
						StreamBuffer.write(COLON_SPACE, 0, COLON_SPACE.length, responseHead, socketBufferPool);
						contentType.write(responseHead, socketBufferPool);
						StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHead, socketBufferPool);
					}
					if (contentLength >= 0) {
						CONTENT_LENGTH_NAME.write(responseHead, socketBufferPool);
						StreamBuffer.write(COLON_SPACE, 0, COLON_SPACE.length, responseHead, socketBufferPool);
						StreamBuffer.write(contentLength, responseHead, socketBufferPool);
						StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHead, socketBufferPool);
					}
					WritableHttpHeader header = httpHeader;
					while (header != null) {
						header.write(responseHead, socketBufferPool);
						header = header.next;
					}
					WritableHttpCookie cookie = httpCookie;
					while (cookie != null) {
						cookie.write(responseHead, socketBufferPool);
						cookie = cookie.next;
					}
					StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHead, socketBufferPool);

				}, content);
			};

			// Create the connection
			ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
					AbstractHttpServicerFactory.this.serverLocation, AbstractHttpServicerFactory.this.isSecure,
					methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity,
					AbstractHttpServicerFactory.this.serverName, AbstractHttpServicerFactory.this.dateHttpHeaderClock,
					AbstractHttpServicerFactory.this.isIncludeEscalationStackTrace, writer,
					AbstractHttpServicerFactory.this.serviceBufferPool);

			try {
				try {
					// Service the connection
					AbstractHttpServicerFactory.this.service(connection);

				} catch (IOException ex) {
					// Propagate as HTTP exception
					throw new HttpException(
							new HttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getMessage()));
				}
			} catch (HttpException ex) {
				// Send HTTP exception
				responseWriter.write((responseHead, socketBufferPool) -> {
					ex.writeHttpResponse(version, AbstractHttpServicerFactory.this.isIncludeEscalationStackTrace,
							responseHead, socketBufferPool);
				}, null);
			}
		}

	}

}