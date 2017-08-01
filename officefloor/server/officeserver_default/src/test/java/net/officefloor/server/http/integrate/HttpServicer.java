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
package net.officefloor.server.http.integrate;

import java.io.OutputStreamWriter;

import junit.framework.TestCase;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Provides for processing a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServicer {

	/**
	 * Flag indicating if expecting {@link ServerHttpConnection} to be secure.
	 */
	private final boolean expectIsSecure;

	/**
	 * Initiate.
	 * 
	 * @param expectIsSecure
	 *            Flag indicating if expecting {@link ServerHttpConnection} to
	 *            be secure.
	 */
	public HttpServicer(boolean expectIsSecure) {
		this.expectIsSecure = expectIsSecure;
	}

	/**
	 * Processes the {@link HttpRequest}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public void service(ServerHttpConnection connection) throws Throwable {

		// Validate whether secure channel
		TestCase.assertEquals("Incorrect secure channel indication", this.expectIsSecure, connection.isSecure());

		// Obtain the HTTP request
		HttpRequest request = connection.getHttpRequest();

		// Detail request being serviced
		String requestUri = request.getRequestURI();
		System.out.println(this.getClass().getSimpleName() + " serving request: " + request.getHttpMethod() + " "
				+ requestUri + " " + request.getVersion());

		// Determine if invalid cause failure
		final String failUri = "/fail";
		if (failUri.equals(requestUri)) {
			// Fail
			throw new Exception("Testing triggered failure due to request uri " + failUri);
		}

		// Obtain response for the request
		HttpResponse response = connection.getHttpResponse();

		// Write the body of the response
		String message = "Hello World";
		new OutputStreamWriter(response.getEntity()).append(message).flush();

		// Send response
		response.getEntity().close();
	}
}