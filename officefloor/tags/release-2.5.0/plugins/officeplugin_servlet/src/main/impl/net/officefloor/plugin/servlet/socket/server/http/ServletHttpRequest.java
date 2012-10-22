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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.ServerInputStream;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * {@link HttpRequest} wrapping a {@link HttpServletRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpRequest implements HttpRequest {

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest servletRequest;

	/**
	 * {@link HttpHeader} instances.
	 */
	private List<HttpHeader> headers = null;

	/**
	 * {@link ServerInputStream}.
	 */
	private ServerInputStream entity = null;

	/**
	 * Initiate.
	 * 
	 * @param servletRequest
	 *            {@link HttpServletRequest}.
	 */
	public ServletHttpRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	/*
	 * ======================= HttpRequest ===============================
	 */

	@Override
	public String getMethod() {
		return this.servletRequest.getMethod();
	}

	@Override
	public String getRequestURI() {
		String requestUri = this.servletRequest.getRequestURI();
		String queryString = this.servletRequest.getQueryString();
		if ((queryString == null) || (queryString.length() == 0)) {
			return requestUri; // no query string
		} else {
			return requestUri + "?" + queryString;
		}
	}

	@Override
	public String getVersion() {
		return this.servletRequest.getProtocol();
	}

	@Override
	public synchronized List<HttpHeader> getHeaders() {

		// Lazy load the headers
		if (this.headers == null) {
			this.headers = new ArrayList<HttpHeader>();

			// Iterate over the header names
			Enumeration<String> headerNames = this.servletRequest
					.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();

				// Iterate over values (including them)
				Enumeration<String> values = this.servletRequest
						.getHeaders(headerName);
				while (values.hasMoreElements()) {
					String value = values.nextElement();

					// Add the header
					this.headers.add(new HttpHeaderImpl(headerName, value));
				}
			}
		}

		// Return the headers
		return this.headers;
	}

	@Override
	public synchronized ServerInputStream getEntity() throws IOException {

		// Lazy obtain the entity
		if (this.entity == null) {

			/*
			 * Potential for lot of requests to be sent that do not provide all
			 * data causing OOM. This however is true on processing the requests
			 * and therefore expected to be handled by the Servlet Container.
			 */

			// Read the contents of the entity
			int contentLength = servletRequest.getContentLength();
			byte[] data = new byte[contentLength];
			InputStream inputStream = this.servletRequest.getInputStream();
			int bytesRead = inputStream.read(data);
			if (bytesRead < 0) {
				bytesRead = 0; // no data, and end of stream
			}

			// Create the server input entity (end index is length -1)
			ServerInputStreamImpl requestEntity = new ServerInputStreamImpl(
					this.servletRequest);
			requestEntity.inputData(data, 0, (bytesRead - 1), false);
			this.entity = requestEntity;
		}

		// Return the entity
		return this.entity;
	}

}