/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * <p>
 * HTTP {@link Exception}.
 * <p>
 * This is a {@link RuntimeException} as typically this is handled directly by
 * the {@link HttpServerImplementation} to send a {@link HttpResponse}. It is
 * typically not for application logic to handle.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link HttpHeader} space.
	 */
	private static byte[] SPACE = " ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link HttpHeader} end of line encoded bytes.
	 */
	private static byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link HttpHeader} name/value separation.
	 */
	private static byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * Content-Length {@link HttpHeaderName}.
	 */
	private static HttpHeaderName CONTENT_LENGTH = new HttpHeaderName("Content-Length");

	/**
	 * No {@link WritableHttpHeader} instances value.
	 */
	private static final WritableHttpHeader[] NO_HEADERS = new WritableHttpHeader[0];

	/**
	 * {@link HttpStatus}.
	 */
	private final HttpStatus status;

	/**
	 * Possible {@link WritableHttpHeader} instances for the {@link HttpResponse}.
	 */
	private final WritableHttpHeader[] headers;

	/**
	 * {@link HttpResponse} entity content.
	 */
	private final String entity;

	/**
	 * Instantiate.
	 * 
	 * @param status {@link HttpStatus}.
	 */
	public HttpException(HttpStatus status) {
		super(status.getStatusMessage());
		this.status = status;
		this.headers = NO_HEADERS;
		this.entity = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param status {@link HttpStatus}.
	 * @param cause  {@link Throwable} cause.
	 */
	public HttpException(HttpStatus status, Throwable cause) {
		super(cause);
		this.status = status;
		this.headers = NO_HEADERS;
		this.entity = null;
	}

	/**
	 * Convenience constructor for providing error message.
	 * 
	 * @param statusCode   Status code.
	 * @param errorMessage Error message.
	 */
	public HttpException(int statusCode, String errorMessage) {
		this(HttpStatus.getHttpStatus(statusCode), errorMessage);
	}

	/**
	 * Convenience constructor for providing error message.
	 * 
	 * @param status       {@link HttpStatus}.
	 * @param errorMessage Error message.
	 */
	public HttpException(HttpStatus status, String errorMessage) {
		super(new ErrorMessageException(errorMessage));
		this.status = status;
		this.headers = NO_HEADERS;
		this.entity = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param status  {@link HttpStatus}.
	 * @param headers {@link HttpHeader} instances. May be <code>null</code>.
	 * @param entity  Entity for the {@link HttpResponse}. May be <code>null</code>.
	 */
	public HttpException(HttpStatus status, WritableHttpHeader[] headers, String entity) {
		super(status.getStatusMessage());
		this.status = status;
		this.headers = (headers == null ? NO_HEADERS : headers);
		this.entity = entity;
	}

	/**
	 * Enable wrapping {@link Throwable} in a {@link HttpException} for
	 * {@link HttpStatus#INTERNAL_SERVER_ERROR}.
	 * 
	 * @param cause {@link Throwable} cause.
	 */
	public HttpException(Throwable cause) {
		this(HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}

	/**
	 * Obtains the {@link HttpStatus} for the {@link HttpResponse}.
	 * 
	 * @return {@link HttpStatus} for the {@link HttpResponse}.
	 */
	public HttpStatus getHttpStatus() {
		return this.status;
	}

	/**
	 * Obtains the {@link HttpHeader} instances.
	 * 
	 * @return {@link HttpHeader} instances.
	 */
	public HttpHeader[] getHttpHeaders() {
		return this.headers;
	}

	/**
	 * Obtains the entity for the {@link HttpResponse}.
	 * 
	 * @return Entity for the {@link HttpResponse}.
	 */
	public String getEntity() {
		return this.entity;
	}

	/**
	 * Writes the HTTP response for this {@link HttpException}.
	 * 
	 * @param <B>                 Buffer type.
	 * @param version             {@link HttpVersion}.
	 * @param isIncludeStackTrace Whether to include the stack trace.
	 * @param head                Head {@link StreamBuffer} of the linked list of
	 *                            {@link StreamBuffer} instances.
	 * @param bufferPool          {@link StreamBufferPool}.
	 */
	public <B> void writeHttpResponse(HttpVersion version, boolean isIncludeStackTrace, StreamBuffer<B> head,
			StreamBufferPool<B> bufferPool) {

		// Write the status line
		version.write(head, bufferPool);
		StreamBuffer.write(SPACE, head, bufferPool);
		this.status.write(head, bufferPool);
		StreamBuffer.write(HEADER_EOLN, head, bufferPool);

		// Write the headers
		for (int i = 0; i < this.headers.length; i++) {
			this.headers[i].write(head, bufferPool);
		}

		// Determine if include the stack trace
		if (!isIncludeStackTrace) {
			// Complete request without entity
			StreamBuffer.write(HEADER_EOLN, head, bufferPool);
			return;
		}

		// Generate the stack trace
		StringWriter stackTrace = new StringWriter();
		PrintWriter writer = new PrintWriter(stackTrace);
		this.printStackTrace(writer);
		writer.flush();
		byte[] stackTraceBytes = stackTrace.toString().getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

		// Write header for the content length
		CONTENT_LENGTH.write(head, bufferPool);
		StreamBuffer.write(COLON_SPACE, head, bufferPool);
		StreamBuffer.write(stackTraceBytes.length, head, bufferPool);
		StreamBuffer.write(HEADER_EOLN, head, bufferPool);

		// Write end of headers
		StreamBuffer.write(HEADER_EOLN, head, bufferPool);

		// Write the stack trace
		StreamBuffer.write(stackTraceBytes, head, bufferPool);
	}

	/**
	 * Allow error message.
	 */
	private static class ErrorMessageException extends Exception {
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiate with error message.
		 * 
		 * @param message Error message.
		 */
		private ErrorMessageException(String message) {
			super(message);
		}
	}

	/*
	 * ==================== Exception =====================
	 */

	@Override
	public String getMessage() {
		Throwable cause = this.getCause();
		if ((cause != null) && (cause instanceof ErrorMessageException)) {
			// Use just the error message
			return cause.getMessage();
		} else {
			// Provide full message
			return super.getMessage();
		}
	}

}
