/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.work.http;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

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
	 * Version.
	 */
	private String version = null;

	/**
	 * Headers.
	 */
	private Properties headers = new Properties();

	/**
	 * Body.
	 */
	private BufferStream body = new BufferStreamImpl(
			new HeapByteBufferSquirtFactory(1024));

	/**
	 * Flag indicating if sent.
	 */
	private boolean isSent = false;

	/**
	 * Obtains the HTTP status.
	 *
	 * @return HTTP status.
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Obtains the HTTP status message.
	 *
	 * @return HTTP status message.
	 */
	public String getStatusMessage() {
		return this.statusMessage;
	}

	/**
	 * Obtains the HTTP version.
	 *
	 * @return HTTP version.
	 */
	public String getVersion() {
		return this.version;
	}

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
	 * Obtains the body content.
	 *
	 * @return Body content.
	 */
	public byte[] getBodyContent() {
		InputBufferStream input = this.body.getInputBufferStream();
		int availableBytes = (int) input.available();
		byte[] data = new byte[availableBytes];
		try {
			// Ensure read all data
			TestCase.assertEquals("Failed obtaining body content",
					availableBytes, input.read(data));
		} catch (IOException ex) {
			TestCase.fail("Should not fail on reading body data: "
					+ ex.getMessage());
		}
		return data;
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
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void setStatus(int status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public HttpHeader addHeader(String name, String value) {
		TestCase.assertFalse("Response already contains header '" + name + "'",
				this.headers.containsKey(name));
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
	public OutputBufferStream getBody() {
		return this.body.getOutputBufferStream();
	}

	@Override
	public void send() throws IOException {
		this.isSent = true;
	}

}