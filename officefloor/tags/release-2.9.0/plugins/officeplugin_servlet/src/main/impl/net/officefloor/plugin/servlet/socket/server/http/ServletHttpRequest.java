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
package net.officefloor.plugin.servlet.socket.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
	 * HTTP method.
	 */
	private final String method;

	/**
	 * Request URI.
	 */
	private final String requestUri;

	/**
	 * {@link HttpHeader} instances.
	 */
	private List<HttpHeader> headers = null;

	/**
	 * {@link ServerInputStream}.
	 */
	private ServerInputStreamImpl entity = null;

	/**
	 * Initiate.
	 * 
	 * @param servletRequest
	 *            {@link HttpServletRequest}.
	 */
	public ServletHttpRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;

		// Load state from servlet request
		this.method = null;
		this.requestUri = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param servletRequest
	 *            {@link HttpServletRequest}.
	 * @param momento
	 *            Momento for loading state.
	 */
	ServletHttpRequest(HttpServletRequest servletRequest, Serializable momento) {
		this.servletRequest = servletRequest;

		// Load state
		StateMomento state = (StateMomento) momento;
		this.method = state.method;
		this.requestUri = state.requestUri;
		this.headers = new ArrayList<HttpHeader>(state.headers);
		this.entity = new ServerInputStreamImpl(this.servletRequest,
				state.entityState);
	}

	/**
	 * Exports the state as a momento.
	 * 
	 * @return Momento of the current state.
	 * @throws IOException
	 *             If fails to export state.
	 */
	Serializable exportState() throws IOException {

		// Obtain the state
		String method = this.getMethod();
		String requestUri = this.getRequestURI();
		List<HttpHeader> headers = new ArrayList<HttpHeader>(this.getHeaders());
		Serializable entityState = this.getEntity().exportState();

		// Create and return the state
		return new StateMomento(method, requestUri, headers, entityState);
	}

	/*
	 * ======================= HttpRequest ===============================
	 */

	@Override
	public String getMethod() {
		return (this.method != null ? this.method : this.servletRequest
				.getMethod());
	}

	@Override
	public String getRequestURI() {

		// Determine if loaded request URI
		if (this.requestUri != null) {
			// Use loaded request URI
			return this.requestUri;
		}

		// Provide from Servlet Request
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
	public synchronized ServerInputStreamImpl getEntity() throws IOException {

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

	/**
	 * Momento of the state.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Method.
		 */
		private final String method;

		/**
		 * Request URI.
		 */
		private final String requestUri;

		/**
		 * {@link HttpHeader} instances.
		 */
		private final List<HttpHeader> headers;

		/**
		 * {@link ServerInputStream} state.
		 */
		private final Serializable entityState;

		public StateMomento(String method, String requestUri,
				List<HttpHeader> headers, Serializable entityState) {
			this.method = method;
			this.requestUri = requestUri;
			this.headers = headers;
			this.entityState = entityState;
		}
	}

}