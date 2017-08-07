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

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link Serializable} {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpResponse implements HttpResponse {

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
	 * {@link SerialisableHttpResponseHeaders}.
	 */
	private final SerialisableHttpResponseHeaders headers = new SerialisableHttpResponseHeaders();

	/**
	 * {@link Charset} for the {@link ServerWriter}.
	 */
	private Charset charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
	
	/**
	 * Instantiate.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @param byteBufferPool
	 *            {@link BufferPool}.
	 */
	public SerialisableHttpResponse(HttpVersion version) {
		this.version = version;
	}

	/*
	 * ===================== HttpResponse ========================
	 */

	@Override
	public HttpVersion getHttpVersion() {
		return this.version;
	}

	@Override
	public void setHttpVersion(HttpVersion version) {
		this.version = version;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.status;
	}

	@Override
	public void setHttpStatus(HttpStatus status) {
		this.status = status;
	}

	@Override
	public HttpResponseHeaders getHttpHeaders() {
		return this.headers;
	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {
		this.headers.removeHeaders(CONTENT_TYPE);
		if (contentType != null) {
			this.headers.addHeader(CONTENT_TYPE,
					(charset == ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET ? "" : ";charset=" + charset.name()));
		}
		this.charset = charset;
	}

	@Override
	public String getContentType() {
		HttpHeader header = this.headers.getHeader(CONTENT_TYPE);
		return (header == null ? null : header.getValue());
	}

	@Override
	public Charset getContentCharset() {
		return this.charset;
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send() throws IOException {
		// TODO Auto-generated method stub

	}

}