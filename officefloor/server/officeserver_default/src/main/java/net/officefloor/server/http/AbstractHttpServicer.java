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
import net.officefloor.server.http.impl.WritableHttpHeader;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link SocketServicer} to use the {@link HttpRequestParser} to produce
 * {@link ServerHttpConnection} instances for {@link RequestServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpServicer extends HttpRequestParser
		implements SocketServicer<HttpRequestParser>, RequestServicer<HttpRequestParser> {

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

	/**
	 * Writes the {@link HttpException} response.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @param exception
	 *            {@link HttpException} response.
	 * @param writer
	 *            {@link ResponseWriter}.
	 */
	private void writeHttpException(HttpVersion version, HttpException exception, ResponseWriter writer) {
		try {
			// Write the response
			BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(this.bufferPool);
			ServerWriter headerWriter = outputStream.getServerWriter(ServerHttpConnection.HTTP_CHARSET);

			// Obtain the status
			HttpStatus status = exception.getHttpStatus();

			// Write the header
			headerWriter.write(version.getBytes());
			headerWriter.write(' ');
			headerWriter.write(status.getBytes());
			headerWriter.write('\r');
			headerWriter.write('\n');
			headerWriter.write('\r');
			headerWriter.write('\n');
			headerWriter.flush();

			// Obtain the buffers
			StreamBuffer<ByteBuffer> buffers = outputStream.getBuffers();

			writer.write(buffers);
		} catch (IOException ex) {

			// Provide means to close connection
			ex.printStackTrace();
		}
	}

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
		HttpResponseWriter<ByteBuffer> writer = (responseVersion, status, httpHeaders, contentLength, contentType,
				content) -> {
			try {
				try {
					// Write the response
					BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(
							this.bufferPool);
					ServerWriter headerWriter = outputStream.getServerWriter(ServerHttpConnection.HTTP_CHARSET);

					// Write the header
					headerWriter.write(responseVersion.getBytes());
					headerWriter.write(' ');
					headerWriter.write(status.getBytes());
					headerWriter.write('\r');
					headerWriter.write('\n');
					if (contentType != null) {
						CONTENT_TYPE_NAME.writeName(headerWriter);
						headerWriter.write(':');
						headerWriter.write(' ');
						contentType.writeValue(headerWriter);
						headerWriter.append('\r');
						headerWriter.append('\n');
					}
					if (contentLength > 0) {
						CONTENT_LENGTH_NAME.writeName(headerWriter);
						headerWriter.write(':');
						headerWriter.write(' ');
						headerWriter.write(String.valueOf(contentLength));
						headerWriter.append('\r');
						headerWriter.append('\n');
					}
					while (httpHeaders.hasNext()) {
						WritableHttpHeader header = httpHeaders.next();
						header.writeHttpHeader(headerWriter);
					}
					headerWriter.write('\r');
					headerWriter.write('\n');
					headerWriter.flush();

					// Obtain the buffers
					StreamBuffer<ByteBuffer> buffers = outputStream.getBuffers();

					// Append entity buffers
					StreamBuffer<ByteBuffer> lastHeader = buffers;
					while (lastHeader.next != null) {
						lastHeader = lastHeader.next;
					}
					lastHeader.next = content;

					// Write the response
					responseWriter.write(buffers);

				} catch (IOException ex) {
					// Propagate as HTTP exception
					throw new HttpException(
							new HttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getMessage()));
				}
			} catch (HttpException ex) {
				// Send HTTP exception
				this.writeHttpException(version, ex, responseWriter);
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
			this.writeHttpException(version, ex, responseWriter);
		}
	}

}