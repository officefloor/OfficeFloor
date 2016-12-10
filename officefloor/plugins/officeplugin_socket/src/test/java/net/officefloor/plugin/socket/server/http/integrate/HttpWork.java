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
package net.officefloor.plugin.socket.server.http.integrate;

import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Provides for processing a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpWork {

	/**
	 * Expected local {@link InetSocketAddress} for the
	 * {@link ServerHttpConnection}.
	 */
	private final InetSocketAddress expectedLocalAddress;

	/**
	 * Flag indicating if expecting {@link ServerHttpConnection} to be secure.
	 */
	private final boolean expectIsSecure;

	/**
	 * Initiate.
	 * 
	 * @param expectedLocalAddress
	 *            Expected local {@link InetSocketAddress} for the
	 *            {@link ServerHttpConnection}.
	 * @param expectIsSecure
	 *            Flag indicating if expecting {@link ServerHttpConnection} to
	 *            be secure.
	 */
	public HttpWork(InetSocketAddress expectedLocalAddress,
			boolean expectIsSecure) {
		this.expectedLocalAddress = expectedLocalAddress;
		this.expectIsSecure = expectIsSecure;
	}

	/**
	 * Processes the {@link HttpRequest}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public void service(ServerHttpConnection connection) throws Throwable {

		// Obtain expected local port
		int expectedLocalPort = this.expectedLocalAddress.getPort();

		// Validate the local address
		InetSocketAddress actualLocalAddress = connection.getLocalAddress();
		TestCase.assertNotNull("Must have local host",
				actualLocalAddress.getHostName());
		TestCase.assertEquals("Incorrect local port", expectedLocalPort,
				actualLocalAddress.getPort());

		// Validate the remote address (not same port as local)
		InetSocketAddress actualRemoteAddress = connection.getRemoteAddress();
		TestCase.assertNotNull("Must have remote host",
				actualRemoteAddress.getHostName());
		TestCase.assertTrue(
				"Remote address port to be different to local address port",
				expectedLocalPort != actualRemoteAddress.getPort());

		// Validate whether secure channel
		TestCase.assertEquals("Incorrect secure channel indication",
				this.expectIsSecure, connection.isSecure());

		// Obtain the HTTP request
		HttpRequest request = connection.getHttpRequest();

		// Detail request being serviced
		String requestUri = request.getRequestURI();
		System.out.println(this.getClass().getSimpleName()
				+ " serving request: " + request.getMethod() + " " + requestUri
				+ " " + request.getVersion());

		// Determine if invalid cause failure
		final String failUri = "/fail";
		if (failUri.equals(requestUri)) {
			// Fail
			throw new Exception("Testing triggered failure due to request uri "
					+ failUri);
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