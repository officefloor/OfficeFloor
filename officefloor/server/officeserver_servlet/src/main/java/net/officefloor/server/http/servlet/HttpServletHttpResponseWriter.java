/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;

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
			WritableHttpCookie cookie = headHttpCookie;
			while (cookie != null) {
				this.response.addCookie(new Cookie(headHttpCookie.getName(), headHttpCookie.getValue()));
				cookie = cookie.next;
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
