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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpServicerFunction;
import net.officefloor.server.http.MockHttpServer;
import net.officefloor.server.http.protocol.HttpStatus;
import net.officefloor.server.http.source.HttpServerSocketManagedObjectSource;

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
	public HttpServicerFunction buildServicer(String managedObjectName, MockHttpServer server) throws Exception {

		// Register the servicer to process messages
		HttpServicer servicer = new HttpServicer(this.isServerSecure());
		ReflectiveFunctionBuilder functionBuilder = server.constructFunction(servicer, "service");
		functionBuilder.buildObject(managedObjectName);

		// Return the reference to the service function
		return new HttpServicerFunction("service");
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
			assertEquals("Incorrect response body for request " + i, "Hello World", this.doRequest(client, request));
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
		assertEquals("Should be server failure", HttpStatus.SC_INTERNAL_SERVER_ERROR,
				response.getStatusLine().getStatusCode());
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
	private String doRequest(HttpClient client, HttpUriRequest request) throws Exception {
		// Do the request and obtain the response
		HttpResponse response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();
		assertEquals("Incorrect status", 200, status);

		// Read in the body of the response
		String body = HttpClientTestUtil.getEntityBody(response);

		// Return the body
		return body;
	}

}