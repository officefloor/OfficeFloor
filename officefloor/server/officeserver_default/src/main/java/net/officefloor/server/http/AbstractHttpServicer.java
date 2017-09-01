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
import java.util.function.Supplier;

import net.officefloor.server.RequestHandler;
import net.officefloor.server.RequestServicer;
import net.officefloor.server.ResponseWriter;
import net.officefloor.server.SocketServicer;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link SocketServicer} to use the {@link HttpRequestParser} to produce
 * {@link ServerHttpConnection} instances for {@link RequestServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServicer extends HttpRequestParser
		implements SocketServicer<HttpRequestParser>, RequestServicer<HttpRequestParser> {

	private static final byte[] SPACE = " ".getBytes(ServerHttpConnection.HTTP_CHARSET);
	private static byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);
	private static byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	private static final HttpHeaderName CONTENT_LENGTH_NAME = new HttpHeaderName("Content-Length");
	private static final HttpHeaderName CONTENT_TYPE_NAME = new HttpHeaderName("Content-Type");

	/**
	 * Indicates if over secure {@link Socket}.
	 */
	private final boolean isSecure;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * Instantiate.
	 * 
	 * @param isSecure
	 *            Indicates if over secure {@link Socket}.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @param metaData
	 *            {@link HttpRequestParserMetaData}.
	 */
	public AbstractHttpServicer(boolean isSecure, StreamBufferPool<ByteBuffer> bufferPool,
			HttpRequestParserMetaData metaData) {
		super(metaData);
		this.isSecure = isSecure;
		this.bufferPool = bufferPool;
	}

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
	 * ===================== SocketServicer ======================
	 */

	@Override
	public void service(StreamBuffer<ByteBuffer> readBuffer, RequestHandler<HttpRequestParser> requestHandler) {

		// Add the buffer
		this.appendStreamBuffer(readBuffer);

		// Parse out the requests
		while (this.parse()) {

			// Create request from parser
			requestHandler.handleRequest(this);
		}
	}

	/*
	 * ===================== RequestServicer ====================
	 */

	@Override
	public void service(HttpRequestParser request, ResponseWriter responseWriter) {

		// Request ready, so obtain details
		Supplier<HttpMethod> methodSupplier = this.getMethod();
		Supplier<String> requestUriSupplier = this.getRequestURI();
		HttpVersion version = this.getVersion();
		NonMaterialisedHttpHeaders requestHeaders = this.getHeaders();
		ByteSequence requestEntity = this.getEntity();

		// Create the HTTP response writer
		HttpResponseWriter<ByteBuffer> writer = (responseVersion, status, httpHeader, contentLength, contentType,
				content) -> {

			// Create buffer for response header
			StreamBuffer<ByteBuffer> responseHeader = this.bufferPool.getPooledStreamBuffer();
			try {
				// Write the response
				responseVersion.write(responseHeader, this.bufferPool);
				StreamBuffer.write(SPACE, 0, SPACE.length, responseHeader, this.bufferPool);
				status.write(responseHeader, this.bufferPool);
				StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHeader, bufferPool);

				// Write the header
				if (contentType != null) {
					CONTENT_TYPE_NAME.write(responseHeader, this.bufferPool);
					StreamBuffer.write(COLON_SPACE, 0, COLON_SPACE.length, responseHeader, this.bufferPool);
					contentType.write(responseHeader, this.bufferPool);
					StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHeader, this.bufferPool);
				}
				if (contentLength > 0) {
					CONTENT_LENGTH_NAME.write(responseHeader, this.bufferPool);
					StreamBuffer.write(COLON_SPACE, 0, COLON_SPACE.length, responseHeader, this.bufferPool);
					HttpHeaderValue.writeInteger(contentLength, responseHeader, this.bufferPool);
					StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHeader, this.bufferPool);
				}
				while (httpHeader != null) {
					httpHeader.write(responseHeader, this.bufferPool);
					httpHeader = httpHeader.next;
				}
				StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, responseHeader, bufferPool);

				// Append entity buffers
				StreamBuffer<ByteBuffer> lastHeader = responseHeader;
				while (lastHeader.next != null) {
					lastHeader = lastHeader.next;
				}
				lastHeader.next = content;

				// Write the response
				responseWriter.write(responseHeader);

			} catch (HttpException ex) {

				// Release the attempted response
				while (responseHeader != null) {
					responseHeader.release();
					responseHeader = responseHeader.next;
				}
				while (content != null) {
					content.release();
					content = content.next;
				}

				// Send HTTP exception
				StreamBuffer<ByteBuffer> response = this.bufferPool.getPooledStreamBuffer();
				ex.writeHttpResponse(version, true, response, this.bufferPool);
				responseWriter.write(response);
			}
		};

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				this.isSecure, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity, writer,
				this.bufferPool);

		try {
			try {
				// Service the connection
				this.service(connection);

			} catch (IOException ex) {
				// Propagate as HTTP exception
				throw new HttpException(
						new HttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getMessage()));
			}
		} catch (HttpException ex) {
			// Send HTTP exception
			StreamBuffer<ByteBuffer> response = this.bufferPool.getPooledStreamBuffer();
			ex.writeHttpResponse(version, true, response, this.bufferPool);
			responseWriter.write(response);
		}
	}

}