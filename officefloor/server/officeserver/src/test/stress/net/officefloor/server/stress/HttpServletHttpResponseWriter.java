/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.stress;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * {@link HttpServlet} {@link HttpResponseWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

	/**
	 * {@link ByteBufferWriter}.
	 */
	private static final ByteBufferWriter byteBufferWriter = (buffer, outputStream) -> {
		for (int position = BufferJvmFix.position(buffer); position < BufferJvmFix.limit(buffer); position++) {
			outputStream.write(buffer.get());
		}
	};

	/**
	 * {@link AsyncContext}.
	 */
	private final AsyncContext asyncContext;

	/**
	 * Decorates the {@link HttpServletResponse}.
	 */
	private final Consumer<HttpServletResponse> responseDecorator;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * Instantiate.
	 * 
	 * @param asyncContext      {@link AsyncContext}.
	 * @param responseDecorator Decorates the {@link HttpServletResponse}.
	 * @param bufferPool        {@link StreamBufferPool}.
	 */
	public HttpServletHttpResponseWriter(AsyncContext asyncContext, Consumer<HttpServletResponse> responseDecorator,
			StreamBufferPool<ByteBuffer> bufferPool) {
		this.asyncContext = asyncContext;
		this.responseDecorator = responseDecorator;
		this.bufferPool = bufferPool;
	}

	/*
	 * ==================== HttpResponseWriter ======================
	 */

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
			WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {

		// Obtain the async response
		HttpServletResponse asyncResponse = (HttpServletResponse) this.asyncContext.getResponse();

		// Load values to response
		asyncResponse.setStatus(status.getStatusCode());

		// Decorate the response
		this.responseDecorator.accept(asyncResponse);

		// Load the headers
		WritableHttpHeader header = headHttpHeader;
		while (header != null) {
			asyncResponse.setHeader(header.getName(), header.getValue());
			header = header.next;
		}

		// Load the cookies
		WritableHttpCookie cookie = headHttpCookie;
		while (cookie != null) {
			asyncResponse.addCookie(new Cookie(headHttpCookie.getName(), headHttpCookie.getValue()));
			cookie = cookie.next;
		}

		// Load the entity
		if (contentType != null) {
			asyncResponse.setContentType(contentType.getValue());
		}

		try {
			// Write the entity content
			ServletOutputStream entity = asyncResponse.getOutputStream();
			StreamBuffer<ByteBuffer> stream = contentHeadStreamBuffer;
			while (stream != null) {
				if (stream.pooledBuffer != null) {
					// Write the pooled byte buffer
					BufferJvmFix.flip(stream.pooledBuffer);
					byteBufferWriter.write(stream.pooledBuffer, entity);

				} else if (stream.unpooledByteBuffer != null) {
					// Write the unpooled byte buffer
					byteBufferWriter.write(stream.unpooledByteBuffer, entity);

				} else {
					// Write the file content
					StreamBuffer<ByteBuffer> streamBuffer = bufferPool.getPooledStreamBuffer();
					boolean isWritten = false;
					try {
						ByteBuffer buffer = streamBuffer.pooledBuffer;
						long position = stream.fileBuffer.position;
						long count = stream.fileBuffer.count;
						int bytesRead;
						do {
							BufferJvmFix.clear(buffer);

							// Read bytes
							bytesRead = stream.fileBuffer.file.read(buffer, position);
							position += bytesRead;

							// Setup for bytes
							BufferJvmFix.flip(buffer);
							if (count >= 0) {
								count -= bytesRead;

								// Determine read further than necessary
								if (count < 0) {
									BufferJvmFix.limit(buffer, BufferJvmFix.limit(buffer) - (int) Math.abs(count));
									bytesRead = 0;
								}
							}

							// Write the buffer
							byteBufferWriter.write(buffer, entity);
						} while (bytesRead > 0);

						// As here, written file
						isWritten = true;

					} finally {
						streamBuffer.release();

						// Close the file
						if (stream.fileBuffer.callback != null) {
							stream.fileBuffer.callback.complete(stream.fileBuffer.file, isWritten);
						}
					}
				}
				stream = stream.next;
			}

		} catch (IOException e) {
			throw new IllegalStateException("Should not get failure in writing response");
		}

		// Flag complete
		asyncContext.complete();
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
