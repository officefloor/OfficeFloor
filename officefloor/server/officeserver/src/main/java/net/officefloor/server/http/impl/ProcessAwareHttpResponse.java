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
import java.io.Serializable;
import java.nio.charset.Charset;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.StreamBufferPool;
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
	 * {@link HttpResponseWriter}.
	 */
	private final HttpResponseWriter<B> responseWriter;

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
	 * Instantiate.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @param processAwareContext
	 *            {@link ProcessAwareContext}.
	 * @param responseWriter
	 *            {@link HttpResponseWriter}.
	 */
	public ProcessAwareHttpResponse(HttpVersion version, StreamBufferPool<B> bufferPool,
			ProcessAwareContext processAwareContext, HttpResponseWriter<B> responseWriter) {
		this.clientVersion = version;
		this.version = version;
		this.headers = new ProcessAwareHttpResponseHeaders(processAwareContext);
		this.bufferPoolOutputStream = new BufferPoolServerOutputStream<>(bufferPool, this);
		this.safeOutputStream = new ProcessAwareServerOutputStream(this.bufferPoolOutputStream, processAwareContext);
		this.processAwareContext = processAwareContext;
		this.responseWriter = responseWriter;
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
	 * Determines if can change the <code>Content-Type</code> and
	 * {@link Charset}.
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
	public HttpVersion getHttpVersion() {
		return this.safe(() -> this.version);
	}

	@Override
	public void setHttpVersion(HttpVersion version) {
		if (version == null) {
			throw new IllegalArgumentException("Must provide version");
		}
		this.safe(() -> this.version = version);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.safe(() -> this.status);
	}

	@Override
	public void setHttpStatus(HttpStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("Must provide status");
		}
		this.safe(() -> this.status = status);
	}

	@Override
	public HttpResponseHeaders getHttpHeaders() {
		return this.headers;
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
	public void reset() throws IOException {
		this.safe(() -> {

			// Ensure not sent
			if (this.isSent) {
				throw new IOException("Already committed to send response");
			}

			// Reset the response
			this.version = this.clientVersion;
			this.status = HttpStatus.OK;
			this.headers = new ProcessAwareHttpResponseHeaders(processAwareContext);

			// Release writing content
			this.contentType = null;
			this.charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
			this.entityWriter = null;

			// Release the buffers
			this.bufferPoolOutputStream.clear();

			// Void return
			return null;
		});
	}

	@Override
	public void send() throws IOException {
		this.safe(() -> {

			// Determine if already sent
			if (this.isSent) {
				return null; // already sent
			}

			// If writer, ensure flushed
			if (this.entityWriter != null) {
				this.entityWriter.flush();
			}

			// Determine content details
			long contentLength = this.bufferPoolOutputStream.getContentLength();
			HttpHeaderValue contentType = null;
			if (contentLength > 0) {
				contentType = this.deriveContentType();
			}

			// Write the response (and consider sent)
			this.isSent = true;
			this.responseWriter.writeHttpResponse(this.version, this.status, this.headers.getWritableHttpHeaders(),
					contentLength, contentType, this.bufferPoolOutputStream.getBuffers());

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