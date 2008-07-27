/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Tests the {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel
 */
public class HttpServerTest extends HttpServerStartup {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.HttpServerStartup#registerHttpServiceTask()
	 */
	@Override
	protected TaskReference registerHttpServiceTask() throws Exception {
		// Register team to do the work
		this.constructTeam("WORKER", new OnePersonTeam(100));

		// Register the work to process messages
		ReflectiveWorkBuilder workBuilder = this.constructWork(new HttpWork(),
				"servicer", "service");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("service",
				"WORKER");
		taskBuilder.buildObject("P-MO", "MO");

		// Return the reference to the service task
		return new TaskReference("servicer", "service");
	}

	/**
	 * Ensures can handle a GET request.
	 */
	public void testGetRequest() throws Exception {

		// Create the request
		HttpMethod method = new GetMethod(this.getServerUrl() + "/path");

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
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.connection-manager.class",
				new SimpleHttpConnectionManager());

		// Validate multiple tests
		for (int i = 0; i < 100; i++) {
			// Execute the particular request
			HttpMethod method = new GetMethod(this.getServerUrl() + "/path");
			assertEquals("Incorrect response body for request " + i,
					"Hello World", this.doRequest(client, method));
		}
	}

	/**
	 * Ensure can handle a POST request.
	 */
	public void testPostRequest() throws Exception {

		// Create the request
		HttpMethod method = new PostMethod(this.getServerUrl() + "/path");

		// Obtain the response body
		String responseBody = this.doRequest(method);
		assertEquals("Incorrect response body", "Hello World", responseBody);

		// Output the response entity
		System.out.println("POST response body: " + responseBody);
	}

	/**
	 * Does the {@link HttpMethod}.
	 * 
	 * @param request
	 *            {@link HttpMethod}.
	 * @return Resulting body of response.
	 */
	private String doRequest(HttpMethod method) throws Exception {
		// Do the request
		return this.doRequest(new HttpClient(), method);
	}

	/**
	 * Does the {@link HttpMethod}.
	 * 
	 * @param client
	 *            {@link HttpClient}.
	 * @param method
	 *            {@link HttpMethod}.
	 * @return Resulting body of response.
	 */
	private String doRequest(HttpClient client, HttpMethod method)
			throws Exception {
		try {
			// Do the request and obtain the response
			int status = client.executeMethod(method);
			assertEquals("Incorrect status", 200, status);

			// Return the response
			return method.getResponseBodyAsString();

		} finally {
			method.releaseConnection();
		}
	}

}
