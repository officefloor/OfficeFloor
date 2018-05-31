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
package net.officefloor.server.http.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.server.http.CleanupException;
import net.officefloor.server.http.HttpEscalationContext;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;
import net.officefloor.server.stream.impl.CloseHandler;
import net.officefloor.server.stream.impl.ProcessAwareServerOutputStream;
import net.officefloor.server.stream.impl.ProcessAwareServerWriter;

/**
 * {@link Serializable} {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponse<B> implements HttpResponse, CloseHandler {

	/**
	 * Binary <code>Content-Type</code>.
	 */
	private static final HttpHeaderValue BINARY_CONTENT = new HttpHeaderValue("application/octet-stream");

	/**
	 * Text <code>Content-Type</code>.
	 */
	private static final HttpHeaderValue TEXT_CONTENT = new HttpHeaderValue("text/plain");

	/**
	 * {@link ProcessAwareServerHttpConnectionManagedObject}.
	 */
	private final ProcessAwareServerHttpConnectionManagedObject<B> serverHttpConnection;

	/**
	 * Client {@link HttpVersion}.
	 */
	private final HttpVersion clientVersion;

	/**
	 * {@link HttpVersion}.
	 */
	private HttpVersion version;

	/**
	 * {@link HttpStatus}.
	 */
	private HttpStatus status = HttpStatus.OK;

	/**
	 * {@link ProcessAwareHttpResponseHeaders}.
	 */
	private ProcessAwareHttpResponseHeaders headers;

	/**
	 * {@link ProcessAwareHttpResponseCookies}.
	 */
	private ProcessAwareHttpResponseCookies cookies;

	/**
	 * {@link BufferPoolServerOutputStream}.
	 */
	private final BufferPoolServerOutputStream<B> bufferPoolOutputStream;

	/**
	 * {@link ProcessAwareServerOutputStream}.
	 */
	private final ProcessAwareServerOutputStream safeOutputStream;

	/**
	 * {@link ServerWriter}.
	 */
	private ServerWriter entityWriter = null;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext processAwareContext;

	/**
	 * {@link CleanupEscalation} instances. Will be <code>null</code> if successful
	 * clean up.
	 */
	private CleanupEscalation[] cleanupEscalations = null;

	/**
	 * {@link HttpEscalationHandler}.
	 */
	private HttpEscalationHandler escalationHandler = null;

	/**
	 * <code>Content-Type</code> value.
	 */
	private HttpHeaderValue contentType = null;

	/**
	 * {@link Charset} for the {@link ServerWriter}.
	 */
	private Charset charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;

	/**
	 * Indicates if this {@link HttpResponse} has been sent.
	 */
	private boolean isSent = false;

	/**
	 * Indicates if this {@link HttpResponse} has been written to the
	 * {@link HttpResponseWriter}.
	 */
	private boolean isWritten = false;

	/**
	 * Instantiate.
	 * 
	 * @param serverHttpConnection
	 *            {@link ServerHttpConnection}.
	 * @param version
	 *            {@link HttpVersion}.
	 * @param processAwareContext
	 *            {@link ProcessAwareContext}.
	 */
	public ProcessAwareHttpResponse(ProcessAwareServerHttpConnectionManagedObject<B> serverHttpConnection,
			HttpVersion version, ProcessAwareContext processAwareContext) {
		this.serverHttpConnection = serverHttpConnection;
		this.clientVersion = version;
		this.version = version;
		this.headers = new ProcessAwareHttpResponseHeaders(processAwareContext);
		this.cookies = new ProcessAwareHttpResponseCookies(processAwareContext);
		this.bufferPoolOutputStream = new BufferPoolServerOutputStream<>(this.serverHttpConnection.bufferPool, this);
		this.safeOutputStream = new ProcessAwareServerOutputStream(this.bufferPoolOutputStream, processAwareContext);
		this.processAwareContext = processAwareContext;
	}

	/**
	 * Flushes the {@link HttpResponse} to the {@link HttpResponseWriter}.
	 * 
	 * @param escalation
	 *            Possible escalation in servicing. Will be <code>null</code> if
	 *            successful.
	 * @throws IOException
	 *             If fails to flush {@link HttpResponse} to the
	 *             {@link HttpResponseWriter}.
	 */
	public void flushResponseToHttpResponseWriter(Throwable escalation) throws IOException {
		this.safe(() -> {
			this.unsafeFlushResponseToHttpResponseWriter(escalation);
			return null;
		});
	}

	/**
	 * Sets the {@link CleanupEscalation} instances.
	 * 
	 * @param cleanupEscalations
	 *            {@link CleanupEscalation} instances.
	 * @throws IOException
	 *             If fails to send {@link CleanupEscalation} details for this
	 *             {@link HttpResponse}.
	 */
	void setCleanupEscalations(CleanupEscalation[] cleanupEscalations) throws IOException {

		// Determine if have escalations
		if (cleanupEscalations.length == 0) {
			return; // no escalations
		}

		// Store the clean up escalations
		this.processAwareContext.run(() -> {
			this.cleanupEscalations = cleanupEscalations;
			return null;
		});
	}

	/**
	 * Flushes this {@link HttpResponse} to the {@link HttpResponseWriter}.
	 * 
	 * @param escalation
	 *            Possible escalation in servicing. Will be <code>null</code> if
	 *            successful.
	 * @throws IOException
	 *             If fails to flush {@link HttpResponse} to the
	 *             {@link HttpResponseWriter}.
	 */
	private void unsafeFlushResponseToHttpResponseWriter(Throwable escalation) throws IOException {

		// Determine if already written
		if (this.isWritten) {
			return; // already written
		}

		// Determine if clean up escalation
		if ((escalation == null) && (this.cleanupEscalations != null)) {
			// No escalation, but clean up escalation
			escalation = new CleanupException(this.cleanupEscalations);
		}

		// Handle escalation
		if (escalation != null) {

			// Clear sent to allow writing content
			this.isSent = false;

			// Reset the response to send an error
			this.unsafeReset();

			// Determine if HTTP escalation
			boolean isHttpEscalation = escalation instanceof HttpException;

			// Set up response for escalation
			if (isHttpEscalation) {
				// Provide the HTTP escalation
				HttpException httpEscalation = (HttpException) escalation;
				this.status = httpEscalation.getHttpStatus();
				HttpHeader[] escalationHeaders = httpEscalation.getHttpHeaders();
				for (int i = 0; i < escalationHeaders.length; i++) {
					HttpHeader escalationHeader = escalationHeaders[i];
					this.headers.addHeader(escalationHeader.getName(), escalationHeader.getValue());
				}
			} else {
				// Unknown escalation, so error
				this.status = HttpStatus.INTERNAL_SERVER_ERROR;
			}

			// Determine if escalation handler
			if (this.escalationHandler != null) {

				// Handle the escalation
				final Throwable finalEscalation = escalation;
				this.escalationHandler.handle(new HttpEscalationContext() {

					@Override
					public Throwable getEscalation() {
						return finalEscalation;
					}

					@Override
					public boolean isIncludeStacktrace() {
						return ProcessAwareHttpResponse.this.serverHttpConnection.isIncludeStackTraceOnEscalation;
					}

					@Override
					public ServerHttpConnection getServerHttpConnection() {
						return ProcessAwareHttpResponse.this.serverHttpConnection;
					}
				});

			} else {
				// Default send the escalation
				if (isHttpEscalation) {
					// Send the HTTP entity (if provided)
					HttpException httpEscalation = (HttpException) escalation;
					String entity = httpEscalation.getEntity();
					if (entity != null) {
						PrintWriter writer = new PrintWriter(this.bufferPoolOutputStream
								.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
						writer.write(entity);
						writer.flush();
					}

				} else if (this.serverHttpConnection.isIncludeStackTraceOnEscalation) {
					// Send escalation stack trace
					this.contentType = TEXT_CONTENT;
					PrintWriter writer = new PrintWriter(this.bufferPoolOutputStream
							.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
					escalation.printStackTrace(writer);
					writer.flush();
				}
			}
		}

		// Ensure send
		this.unsafeSend();

		// Determine content details
		long contentLength = this.bufferPoolOutputStream.getContentLength();
		HttpHeaderValue contentType = null;
		if (contentLength > 0) {
			// Have content so derive content type
			contentType = this.deriveContentType();
		} else if (this.status == HttpStatus.OK) {
			// No content, so update to no content
			this.status = HttpStatus.NO_CONTENT;
		}

		// Write the response (and consider written)
		this.isWritten = true;
		this.serverHttpConnection.httpResponseWriter.writeHttpResponse(this.version, this.status,
				this.headers.getWritableHttpHeaders(), this.cookies.getWritableHttpCookie(), contentLength, contentType,
				this.bufferPoolOutputStream.getBuffers());
	}

	/**
	 * Unsafe send {@link HttpResponse}.
	 * 
	 * @throws IOException
	 *             If fails to send.
	 */
	private void unsafeSend() throws IOException {

		// Determine if already sent
		if (this.isSent) {
			return; // already sent
		}

		// If writer, ensure flushed
		if (this.entityWriter != null) {
			this.entityWriter.flush();
		}

		// Consider sent
		this.isSent = true;
	}

	/**
	 * Unsafe reset {@link HttpResponse}.
	 * 
	 * @throws IOException
	 *             If fails to reset.
	 */
	private void unsafeReset() throws IOException {

		// Reset the response
		this.version = this.clientVersion;
		this.status = HttpStatus.OK;
		this.headers = new ProcessAwareHttpResponseHeaders(this.processAwareContext);
		this.cookies = new ProcessAwareHttpResponseCookies(this.processAwareContext);

		// Release writing content
		this.contentType = null;
		this.charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
		this.entityWriter = null;

		// Release the buffers
		if (this.entityWriter != null) {
			this.entityWriter.flush(); // clear content
		}
		this.bufferPoolOutputStream.clear();
	}

	/**
	 * Derives the <code>Content-Type</code>.
	 * 
	 * @return <code>Content-Type</code>
	 */
	private HttpHeaderValue deriveContentType() {

		// Determine if content-type specified
		if (this.contentType != null) {
			return this.contentType;
		} else {
			// Not specified, so determine based on state
			return (this.entityWriter == null ? BINARY_CONTENT : TEXT_CONTENT);
		}
	}

	/**
	 * Determines if can change the <code>Content-Type</code> and {@link Charset}.
	 * 
	 * @throws IOException
	 *             If not able to change.
	 */
	private void allowContentTypeChange() throws IOException {
		if (this.entityWriter != null) {
			throw new IOException("Can not change Content-Type. Committed to writing "
					+ this.deriveContentType().getValue() + " (charset " + this.charset.name() + ")");
		}
	}

	/**
	 * Undertakes a {@link ProcessSafeOperation}.
	 * 
	 * @param operation
	 *            {@link ProcessSafeOperation}.
	 * @return Result of {@link ProcessSafeOperation}.
	 * @throws T
	 *             Possible {@link Throwable} from {@link ProcessSafeOperation}.
	 */
	private <R, T extends Throwable> R safe(ProcessSafeOperation<R, T> operation) throws T {
		return this.processAwareContext.run(operation);
	}

	/*
	 * ===================== HttpResponse ========================
	 */

	@Override
	public HttpVersion getVersion() {
		return this.safe(() -> this.version);
	}

	@Override
	public void setVersion(HttpVersion version) {
		if (version == null) {
			throw new IllegalArgumentException("Must provide version");
		}
		this.safe(() -> this.version = version);
	}

	@Override
	public HttpStatus getStatus() {
		return this.safe(() -> this.status);
	}

	@Override
	public void setStatus(HttpStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("Must provide status");
		}
		this.safe(() -> this.status = status);
	}

	@Override
	public HttpResponseHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public HttpResponseCookies getCookies() {
		return this.cookies;
	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {
		this.safe(() -> {

			// Ensure can change Content-Type
			this.allowContentTypeChange();

			// Specify the charset (ensuring default)
			this.charset = (charset != null ? charset : ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

			// Specify the content type
			String nonNullContentType = (contentType != null ? contentType : TEXT_CONTENT.getValue());
			if (this.charset == ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET) {
				this.contentType = new HttpHeaderValue(contentType);
			} else {
				// Ensure have content type
				this.contentType = new HttpHeaderValue(nonNullContentType + "; charset=" + this.charset.name());
			}

			// Void return
			return null;
		});
	}

	@Override
	public void setContentType(HttpHeaderValue contentTypeAndCharsetValue, Charset charset) throws IOException {
		this.safe(() -> {

			// Ensure can change Content-Type
			this.allowContentTypeChange();

			// Specify the charset (ensuring default)
			this.charset = (charset != null ? charset : ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

			// Use content-type as is
			this.contentType = contentTypeAndCharsetValue;

			// Void return
			return null;
		});
	}

	@Override
	public String getContentType() {
		return this.safe(() -> this.deriveContentType().getValue());
	}

	@Override
	public Charset getContentCharset() {
		return this.safe(() -> this.charset);
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {
		return this.safeOutputStream;
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {
		return this.safe(() -> {
			if (this.entityWriter == null) {
				this.entityWriter = new ProcessAwareServerWriter(
						this.bufferPoolOutputStream.getServerWriter(this.charset), this.processAwareContext);
			}
			return this.entityWriter;
		});
	}

	@Override
	public HttpEscalationHandler getEscalationHandler() {
		return this.safe(() -> this.escalationHandler);
	}

	@Override
	public void setEscalationHandler(HttpEscalationHandler escalationHandler) {
		this.safe(() -> {
			this.escalationHandler = escalationHandler;
			return null;
		});
	}

	@Override
	public void reset() throws IOException {
		this.safe(() -> {

			// Ensure not written
			if (this.isSent) {
				throw new IOException("Already committed to send response");
			}

			// Reset the response
			this.unsafeReset();

			// Void return
			return null;
		});
	}

	@Override
	public void send() throws IOException {
		this.safe(() -> {

			// Send
			this.unsafeSend();

			// Void return
			return null;
		});
	}

	/*
	 * ======================= CloseHandler =================================
	 */

	@Override
	public boolean isClosed() {

		// Closed if sent
		return this.isSent;
	}

	@Override
	public void close() throws IOException {

		// Send on close
		this.send();
	}

}