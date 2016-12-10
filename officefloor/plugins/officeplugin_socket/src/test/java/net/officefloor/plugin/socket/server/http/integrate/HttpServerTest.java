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

import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.http.server.HttpServicerTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Tests the {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerTest extends MockHttpServer {

	/*
	 * ================== HttpServicerBuilder ===============================
	 */

	@Override
	public HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {

		// Register team to do the work
		server.constructTeam("WORKER",
				MockTeamSource.createOnePersonTeam("WORKER"));

		// Register the work to process messages
		HttpWork work = new HttpWork(this.getLocalAddress(),
				this.isServerSecure());
		ReflectiveWorkBuilder workBuilder = server.constructWork(work,
				"servicer", "service");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("service",
				"WORKER");
		taskBuilder.buildObject(managedObjectName);

		// Return the reference to the service task
		return new HttpServicerTask("servicer", "service");
	}

	/*
	 * ================== Tests ===============================
	 */

	/**
	 * Ensures can handle a GET request.
	 */
	public void testGetRequest() throws Exception {

		// Create the request
		HttpGet method = new HttpGet(this.getServerUrl() + "/path");

		// Obtain the response
		String responseBody = this.doRequest(method);
		assertEquals("Incorrect response body", "Hello World", responseBody);

		// Output the response entity
		System.out.println("GET response body: " + responseBody);
	}

	/**
	 * Ensures can handle multiple GET requests.
	 */
	public void testMultipleGetRequests() throws Exception {

		// Create the HTTP client (keeping the connection open between requests)
		HttpClient client = this.createHttpClient();

		// Validate multiple tests
		for (int i = 0; i < 100; i++) {

			// Execute the particular request
			HttpUriRequest request = new HttpGet(this.getServerUrl() + "/path");
			assertEquals("Incorrect response body for request " + i,
					"Hello World", this.doRequest(client, request));
		}
	}

	/**
	 * Ensure can handle a POST request.
	 */
	public void testPostRequest() throws Exception {

		// Create the request
		HttpUriRequest request = new HttpPost(this.getServerUrl() + "/path");

		// Obtain the response body
		String responseBody = this.doRequest(request);
		assertEquals("Incorrect response body", "Hello World", responseBody);

		// Output the response entity
		System.out.println("POST response body: " + responseBody);
	}

	/**
	 * Ensure can handle an request causing internal server error.
	 */
	public void testInternalServerRequest() throws Exception {

		// Create the request
		HttpUriRequest request = new HttpGet(this.getServerUrl() + "/fail");

		// Send the request
		HttpClient client = this.createHttpClient();
		HttpResponse response = client.execute(request);

		// Ensure indicate server failure
		assertEquals("Should be server failure",
				HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine()
						.getStatusCode());
	}

	/**
	 * Does the {@link HttpUriRequest}.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @return Resulting body of response.
	 */
	private String doRequest(HttpUriRequest request) throws Exception {
		// Do the request
		return this.doRequest(this.createHttpClient(), request);
	}

	/**
	 * Does the {@link HttpUriRequest}.
	 * 
	 * @param client
	 *            {@link HttpClient}.
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @return Resulting body of response.
	 */
	private String doRequest(HttpClient client, HttpUriRequest request)
			throws Exception {
		// Do the request and obtain the response
		HttpResponse response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();
		assertEquals("Incorrect status", 200, status);

		// Read in the body of the response
		String body = HttpTestUtil.getEntityBody(response);

		// Return the body
		return body;
	}

}