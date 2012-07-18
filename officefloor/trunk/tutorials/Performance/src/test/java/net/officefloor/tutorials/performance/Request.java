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
package net.officefloor.tutorials.performance;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Type of request.
 * 
 * @author Daniel Sagenschneider
 */
public class Request {

	/**
	 * URI.
	 */
	private final String uri;

	/**
	 * {@link HttpUriRequest}.
	 */
	private final HttpUriRequest httpRequest;

	/**
	 * Expected response.
	 */
	private final char expectedResponse;

	/**
	 * Initiate.
	 * 
	 * @param serverLocation
	 *            Server location. May be <code>null</code> to not make
	 *            requests.
	 * @param uri
	 *            URI to request on the server.
	 * @param expectedResponse
	 *            Expected response.
	 */
	public Request(String serverLocation, String uri, char expectedResponse) {
		this.uri = (uri.startsWith("/") ? uri : "/" + uri);
		this.expectedResponse = expectedResponse;

		// Construct the HTTP request
		if (serverLocation == null) {
			this.httpRequest = null;
		} else {
			this.httpRequest = new HttpGet("http://" + serverLocation + uri);
		}
	}

	/**
	 * Obtains the URI.
	 * 
	 * @return URI.
	 */
	public String getUri() {
		return this.uri;
	}

	/**
	 * Obtains the {@link HttpUriRequest}.
	 * 
	 * @return {@link HttpUriRequest}.
	 */
	public HttpUriRequest getHttpRequest() {
		return this.httpRequest;
	}

	/**
	 * Obtains the expected response.
	 * 
	 * @return Expected response.
	 */
	public char getExpectedResponse() {
		return this.expectedResponse;
	}

}