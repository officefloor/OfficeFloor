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
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.StreamBufferUtil;

/**
 * {@link HttpServlet} {@link HttpResponseWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

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
				StreamBufferUtil.write(contentHeadStreamBuffer, entity, this.bufferPool);

			} catch (IOException ex) {
				// Capture failure
				this.failure = ex;
			}
		}
	}

}
