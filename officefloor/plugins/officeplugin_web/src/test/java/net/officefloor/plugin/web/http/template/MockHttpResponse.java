/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.template;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.server.stream.MockServerOutputStream;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;

/**
 * Mock {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpResponse implements HttpResponse {

	/**
	 * Status.
	 */
	private int status = -1;

	/**
	 * Status message.
	 */
	private String statusMessage = null;

	/**
	 * {@link HttpVersion}.
	 */
	private HttpVersion version = null;

	/**
	 * Headers.
	 */
	private Properties headers = new Properties();

	/**
	 * Entity.
	 */
	private MockServerOutputStream entity = new MockServerOutputStream(UsAsciiUtil.US_ASCII);

	/**
	 * Flag indicating if sent.
	 */
	private boolean isSent = false;

	/**
	 * Obtains the header value.
	 * 
	 * @param name
	 *            Header name.
	 * @return Corresponding header value.
	 */
	public String getHeaderValue(String name) {
		return this.headers.getProperty(name);
	}

	/**
	 * Obtains the entity content.
	 * 
	 * @return Entity content.
	 */
	public byte[] getEntityContent() {
		return this.entity.getWrittenBytes();
	}

	/**
	 * Flags if this {@link HttpResponse} is sent.
	 * 
	 * @return <code>true</code> if this {@link HttpResponse} is sent.
	 */
	public boolean isSent() {
		return this.isSent;
	}

	/*
	 * =============== HttpResponse =========================
	 */

	@Override
	public void setHttpVersion(HttpVersion version) {
		this.version = version;
	}

	@Override
	public HttpVersion getHttpVersion() {
		return this.version;
	}

	@Override
	public void setHttpStatus(int status) {
		this.status = status;
	}

	@Override
	public int getHttpStatus() {
		return this.status;
	}

	@Override
	public void setStatus(int status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
	}

	@Override
	public String getStatusMessage() {
		return this.statusMessage;
	}

	@Override
	public void reset() throws IOException {
		throw new IllegalStateException("Can not reset " + MockHttpResponse.class.getSimpleName());
	}

	@Override
	public HttpHeader addHeader(String name, String value) {
		TestCase.assertFalse("Response already contains header '" + name + "'", this.headers.containsKey(name));
		this.headers.setProperty(name, value);
		return new HttpHeaderImpl(name, value);
	}

	@Override
	public HttpHeader getHeader(String name) {
		String value = this.headers.getProperty(name);
		return (value == null ? null : new HttpHeaderImpl(name, value));
	}

	@Override
	public HttpHeader[] getHeaders() {
		// Create the listing of headers
		List<HttpHeader> list = new LinkedList<HttpHeader>();
		for (String name : this.headers.stringPropertyNames()) {
			String value = this.headers.getProperty(name);
			list.add(new HttpHeaderImpl(name, value));
		}

		// Return the listing of headers
		return list.toArray(new HttpHeader[0]);
	}

	@Override
	public void removeHeader(HttpHeader header) {
		this.headers.remove(header.getName());
	}

	@Override
	public void removeHeaders(String name) {
		this.headers.remove(name);
	}

	@Override
	public ServerOutputStream getEntity() {
		return this.entity.getServerOutputStream();
	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {
		throw new IllegalStateException("Can not change Content-Type for " + MockHttpResponse.class.getSimpleName());
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public Charset getContentCharset() {
		return UsAsciiUtil.US_ASCII;
	}

	@Override
	public ServerWriter getEntityWriter() {
		return this.entity.getServerWriter();
	}

	@Override
	public void send() throws IOException {
		this.isSent = true;
	}

}