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
import java.util.function.Consumer;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.impl.HttpResponseWriter;
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
		for (int position = buffer.position(); position < buffer.limit(); position++) {
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
					boolean isWritten = false;
					try {
						ByteBuffer buffer = streamBuffer.pooledBuffer;
						long position = contentHeadStreamBuffer.fileBuffer.position;
						long count = contentHeadStreamBuffer.fileBuffer.count;
						int bytesRead;
						do {
							buffer.clear();

							// Read bytes
							bytesRead = contentHeadStreamBuffer.fileBuffer.file.read(buffer, position);
							position += bytesRead;

							// Setup for bytes
							buffer.flip();
							if (count >= 0) {
								count -= bytesRead;

								// Determine read further than necessary
								if (count < 0) {
									buffer.limit(buffer.limit() - (int) Math.abs(count));
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
						if (contentHeadStreamBuffer.fileBuffer.callback != null) {
							contentHeadStreamBuffer.fileBuffer.callback
									.complete(contentHeadStreamBuffer.fileBuffer.file, isWritten);
						}
					}
				}
				contentHeadStreamBuffer = contentHeadStreamBuffer.next;
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