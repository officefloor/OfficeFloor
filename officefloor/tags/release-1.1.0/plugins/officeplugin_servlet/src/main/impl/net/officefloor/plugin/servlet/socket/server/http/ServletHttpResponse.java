/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.socket.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.outputstream.OutputStreamOutputBufferStream;

/**
 * {@link HttpResponse} wrapping a {@link HttpServletResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpResponse implements HttpResponse {

	/**
	 * {@link HttpServletResponse}.
	 */
	private final HttpServletResponse servletResponse;

	/**
	 * {@link HttpHeader} instances.
	 */
	private final List<HttpHeader> headers = new LinkedList<HttpHeader>();

	/**
	 * {@link OutputBufferStream} for the body.
	 */
	private OutputBufferStream body;

	/**
	 * Initiate.
	 * 
	 * @param servletResponse
	 *            {@link HttpServletResponse}.
	 */
	public ServletHttpResponse(HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
	}

	/*
	 * ========================= HttpResponse =================================
	 */

	@Override
	public synchronized HttpHeader addHeader(String name, String value) {

		// Create the HTTP header
		HttpHeader header = new HttpHeaderImpl(name, value);

		// Add the header
		this.servletResponse.addHeader(name, value);

		// Keep track of the header
		this.headers.add(header);

		// Return the header
		return header;
	}

	@Override
	public synchronized HttpHeader getHeader(String name) {

		// Find the first HTTP header
		for (HttpHeader header : this.headers) {
			if (header.getName().equalsIgnoreCase(name)) {
				// Found header
				return header;
			}
		}

		// As here, did not find HTTP header
		return null;
	}

	@Override
	public synchronized HttpHeader[] getHeaders() {
		return this.headers.toArray(new HttpHeader[this.headers.size()]);
	}

	@Override
	public synchronized void removeHeader(HttpHeader header) {

		// Remove the tracked header
		this.headers.remove(header);

		// Clear the header
		this.servletResponse.setHeader(header.getName(), null);
	}

	@Override
	public synchronized void removeHeaders(String name) {

		// Remove the tracked headers
		for (Iterator<HttpHeader> iterator = this.headers.iterator(); iterator
				.hasNext();) {
			HttpHeader header = iterator.next();
			if (header.getName().equalsIgnoreCase(name)) {
				// Remove header
				iterator.remove();
			}
		}

		// Clear the header
		this.servletResponse.setHeader(name, null);
	}

	@Override
	public synchronized void setStatus(int status) {
		this.servletResponse.setStatus(status);
	}

	@Override
	public void setStatus(int status, String statusMessage) {
		// Ignore message and always use Servlet container message
		this.setStatus(status);
	}

	@Override
	public void setVersion(String version) {
		// Do nothing as allow Servlet container to manage
	}

	@Override
	public synchronized OutputBufferStream getBody() {

		// Lazy load the body
		if (this.body == null) {

			// Obtain the output stream
			OutputStream outputStream;
			try {
				outputStream = this.servletResponse.getOutputStream();
			} catch (IOException ex) {
				outputStream = null; // should not occur
			}

			// Create the body stream
			this.body = new OutputStreamOutputBufferStream(outputStream);
		}

		// Return the body
		return this.body;
	}

	@Override
	public synchronized void send() throws IOException {
		this.servletResponse.flushBuffer();
	}

}