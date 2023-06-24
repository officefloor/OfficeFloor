/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
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

	/**
	 * {@link HttpServletResponse}.
	 */
	private final HttpServletResponse response;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * Indicates if serviced.
	 */
	private boolean isServiced = false;

	/**
	 * Possible failure.
	 */
	private IOException failure = null;

	/**
	 * Instantiate.
	 * 
	 * @param response   {@link HttpServletResponse}.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public HttpServletHttpResponseWriter(HttpServletResponse response, StreamBufferPool<ByteBuffer> bufferPool) {
		this.response = response;
		this.bufferPool = bufferPool;
	}

	/**
	 * Indicates if serviced.
	 * 
	 * @return <code>true</code> if serviced.
	 * @throws IOException If fails to write response.
	 */
	public boolean isServiced() throws IOException {

		// Sync to servlet servicing thread
		synchronized (this.response) {

			// Notify of failure
			if (this.failure != null) {
				throw this.failure;
			}

			// Return whether serviced
			return this.isServiced;
		}
	}

	/*
	 * ==================== HttpResponseWriter ======================
	 */

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
			WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {

		// Determine if not handled
		switch (status.getStatusCode()) {
		case 404:
		case 405:
			// Not serviced
			return;
		}

		// Write the response (may be on another thread so synchronise)
		synchronized (this.response) {

			// Indicate serviced
			this.isServiced = true;

			// Load values to response
			this.response.setStatus(status.getStatusCode());

			// Load the headers
			WritableHttpHeader header = headHttpHeader;
			while (header != null) {
				this.response.setHeader(header.getName(), header.getValue());
				header = header.next;
			}

			// Load the cookies
			WritableHttpCookie nextCookie = headHttpCookie;
			while (nextCookie != null) {
				Cookie cookie = new Cookie(nextCookie.getName(), nextCookie.getValue());
				this.response.addCookie(cookie);
				nextCookie = nextCookie.next;
			}

			// Load the entity
			if (contentType != null) {
				this.response.setContentType(contentType.getValue());
			}

			try {
				// Write the entity content
				ServletOutputStream entity = this.response.getOutputStream();
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

			} catch (IOException ex) {
				// Capture failure
				this.failure = ex;
			}
		}
	}

}
