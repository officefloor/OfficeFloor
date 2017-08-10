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
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;

/**
 * {@link Serializable} {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponse<B> implements HttpResponse {

	/**
	 * <code>Content-Type</code> {@link HttpHeader} name.
	 */
	private static final String CONTENT_TYPE = "Content-Type";

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
	private final ProcessAwareHttpResponseHeaders headers = new ProcessAwareHttpResponseHeaders();

	/**
	 * {@link BufferPoolServerOutputStream}.
	 */
	private final BufferPoolServerOutputStream<B> outputStream;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext processAwareContext;

	/**
	 * {@link HttpResponseWriter}.
	 */
	private final HttpResponseWriter<B> responseWriter;

	/**
	 * {@link Charset} for the {@link ServerWriter}.
	 */
	private Charset charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;

	/**
	 * Instantiate.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @param bufferPool
	 *            {@link BufferPool}.
	 * @param processAwareContext
	 *            {@link ProcessAwareContext}.
	 * @param responseWriter
	 *            {@link HttpResponseWriter}.
	 */
	public ProcessAwareHttpResponse(HttpVersion version, BufferPool<B> bufferPool,
			ProcessAwareContext processAwareContext, HttpResponseWriter<B> responseWriter) {
		this.version = version;
		this.outputStream = new BufferPoolServerOutputStream<>(bufferPool, processAwareContext);
		this.processAwareContext = processAwareContext;
		this.responseWriter = responseWriter;
	}

	/*
	 * ===================== HttpResponse ========================
	 */

	@Override
	public HttpVersion getHttpVersion() {
		return this.processAwareContext.run(() -> this.version);
	}

	@Override
	public void setHttpVersion(HttpVersion version) {
		this.processAwareContext.run(() -> this.version = version);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.processAwareContext.run(() -> this.status);
	}

	@Override
	public void setHttpStatus(HttpStatus status) {
		this.processAwareContext.run(() -> this.status = status);
	}

	@Override
	public HttpResponseHeaders getHttpHeaders() {
		return this.headers;
	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {
		this.processAwareContext.run(() -> {

			// Specify the content type header
			this.headers.removeHeaders(CONTENT_TYPE);
			if (contentType != null) {
				this.headers.addHeader(CONTENT_TYPE, (charset == ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET ? ""
						: ";charset=" + charset.name()));
			}
			this.charset = (charset != null ? charset : ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

			// Void return
			return null;
		});
	}

	@Override
	public String getContentType() {
		return this.processAwareContext.run(() -> {
			HttpHeader header = this.headers.getHeader(CONTENT_TYPE);
			return (header == null ? null : header.getValue());
		});
	}

	@Override
	public Charset getContentCharset() {
		return this.processAwareContext.run(() -> this.charset);
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {
		return this.outputStream;
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {
		return this.outputStream.getServerWriter(this.charset);
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send() throws IOException {
		this.processAwareContext.run(() -> {

			// Write the response
			this.responseWriter.writeHttpResponse(this.version, this.status, this.headers.getWritableHttpHeaders(),
					this.outputStream.getBuffers());

			// Void return
			return null;
		});
	}

}