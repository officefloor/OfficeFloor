/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.stream.servlet.ServletServerOutputStream;

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
	 * {@link Charset} for the {@link ServerWriter}.
	 */
	private Charset charset = null;

	/**
	 * {@link ServerOutputStream} for the entity.
	 */
	private ServerOutputStream entity;

	/**
	 * {@link ServerWriter} for the entity.
	 */
	private ServerWriter entityWriter;

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
	public synchronized ServerOutputStream getEntity() throws IOException {

		// Lazy load the entity
		if (this.entity == null) {

			// Obtain the output stream
			OutputStream outputStream = this.servletResponse.getOutputStream();

			// Create the body stream
			this.entity = new ServletServerOutputStream(outputStream);
		}

		// Return the entity
		return this.entity;
	}

	@Override
	public void setContentType(String contentType) {
		this.servletResponse.setContentType(contentType);
	}

	@Override
	public synchronized void setContentCharset(Charset charset,
			String charsetName) {
		this.charset = charset;
		this.servletResponse.setCharacterEncoding(charsetName == null ? charset
				.name() : charsetName);
	}

	@Override
	public synchronized ServerWriter getEntityWriter() throws IOException {

		// Lazy load the entity writer
		if (this.entityWriter == null) {

			// Obtain the charset
			Charset charset = (this.charset != null ? this.charset : Charset
					.forName(this.servletResponse.getCharacterEncoding()));

			// Obtain the entity
			ServerOutputStream entity = this.getEntity();

			// Create the entity writer
			this.entityWriter = new ServerWriter(entity, charset, this);
		}

		// Return the entity writer
		return this.entityWriter;
	}

	@Override
	public synchronized void send() throws IOException {
		this.servletResponse.flushBuffer();
	}

	@Override
	public void reset() throws IOException {
		this.servletResponse.reset();
	}

}