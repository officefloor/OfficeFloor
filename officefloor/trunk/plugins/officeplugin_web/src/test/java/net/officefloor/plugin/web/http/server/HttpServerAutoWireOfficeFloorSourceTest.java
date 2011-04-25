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

package net.officefloor.plugin.web.http.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileSenderWorkSource;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link HttpServerAutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerAutoWireOfficeFloorSourceTest extends
		OfficeFrameTestCase {

	/**
	 * {@link HttpServerAutoWireOfficeFloorSource} to test.
	 */
	private final HttpServerAutoWireApplication source = new HttpServerAutoWireOfficeFloorSource();

	/**
	 * Value indicating that connection was expected to be refused.
	 */
	private static final int CONNECTION_REFUSED_STATUS = -1;

	/**
	 * Default not found file path.
	 */
	private final String DEFAULT_NOT_FOUND_PATH = ClasspathHttpFileSenderWorkSource.DEFAULT_NOT_FOUND_FILE_PATH;

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	@Override
	protected void tearDown() throws Exception {
		// Ensure close
		AutoWireAdministration.closeAllOfficeFloors();

		// Stop the client
		this.client.getConnectionManager().shutdown();
	}

	/**
	 * Ensure can run HTTP server from defaults.
	 */
	public void testDefaults() throws Exception {

		// Open on defaults
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");
		String fileNotFound = this.getFileContents(DEFAULT_NOT_FOUND_PATH);

		// Ensure services request for direct file
		this.assertHttpRequest("http://localhost:7878/index.html", 200,
				expected);

		// Ensure services request for default file
		this.assertHttpRequest("http://localhost:7878", 200, expected);

		// Ensure handles unknown resource
		this.assertHttpRequest("http://localhost:7878/unknown", 404,
				fileNotFound);
	}

	/**
	 * Ensure can override the {@link HttpServerSocketManagedObjectSource}.
	 */
	public void testOverrideHttpServerSocketManagedObjectSource()
			throws Exception {

		final int PORT = MockHttpServer.getAvailablePort();

		// Override the HTTP Server socket
		this.addHttpSocket(PORT);
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by overridden HTTP Server socket
		this.assertHttpRequest("http://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure can listen on multiple ports.
	 */
	public void testListenOnMultiplePorts() throws Exception {

		final int EXTRA_PORT_ONE = MockHttpServer.getAvailablePort();
		final int EXTRA_PORT_TWO = MockHttpServer.getAvailablePort();
		final int DEFAULT_PORT = 7878; // listen on default port as well

		// Listen on multiple HTTP Server socket
		this.addHttpSocket(EXTRA_PORT_ONE);
		this.addHttpSocket(EXTRA_PORT_TWO);
		this.addHttpSocket(DEFAULT_PORT);
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure listening on both ports
		this.assertHttpRequest("http://localhost:" + EXTRA_PORT_ONE, 200,
				expected);
		this.assertHttpRequest("http://localhost:" + EXTRA_PORT_TWO, 200,
				expected);
		this.assertHttpRequest("http://localhost:" + DEFAULT_PORT, 200,
				expected);
	}

	/**
	 * Ensure able to utilise the {@link HttpSession}.
	 */
	public void testHttpSession() throws Exception {

		// Add section that uses the HTTP Session
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class, MockSection.class.getName());
		this.source.linkUri("increment", section, "incrementCounter");

		// Ensure have the auto-wire object for HTTP Session
		assertNotNull("Must have HTTP Session auto-wire object",
				this.source.getHttpSessionAutoWireObject());

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// First requests sets up state
		this.assertHttpRequest("http://localhost:7878/increment", 200, "1");

		// State should be maintained across requests to get incremented count
		this.assertHttpRequest("http://localhost:7878/increment", 200, "2");
		this.assertHttpRequest("http://localhost:7878/increment", 200, "3");
	}

	/**
	 * Asserts the HTTP request returns expected result.
	 * 
	 * @param url
	 *            URL to send the HTTP request.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseBody
	 *            Expected response body.
	 */
	private void assertHttpRequest(String url, int expectedResponseStatus,
			String expectedResponseBody) {
		try {

			HttpResponse response;
			try {
				// Send the request
				HttpGet request = new HttpGet(url);
				response = this.client.execute(request);

			} catch (Exception ex) {
				// Determine if should refuse connection
				if (expectedResponseStatus == CONNECTION_REFUSED_STATUS) {
					assertTrue("Should be connect failure",
							ex instanceof HttpHostConnectException);
					assertEquals("Incorrect cause", "Connection to " + url
							+ " refused", ex.getMessage());
					return; // correctly had connection refused
				}

				// Propagate failure of request
				throw ex;
			}

			// Ensure correct response status
			assertEquals("Should be successful", expectedResponseStatus,
					response.getStatusLine().getStatusCode());

			// Ensure obtained as expected
			String actualResponseBody = MockHttpServer.getEntityBody(response);
			assertEquals("Incorrect response for URL '" + url + "'",
					expectedResponseBody, actualResponseBody);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Convenience method to add a {@link HttpServerSocketManagedObjectSource}.
	 * 
	 * @param port
	 *            Port to listen on.
	 */
	private void addHttpSocket(int port) {
		this.source
				.addHttpSocket(
						HttpServerSocketManagedObjectSource.class,
						HttpServerSocketManagedObjectSource
								.createManagedObjectSourceWirer(
										HttpServerAutoWireOfficeFloorSource.HANDLER_SECTION_NAME,
										HttpServerAutoWireOfficeFloorSource.HANDLER_INPUT_NAME))
				.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_PORT)
				.setValue(String.valueOf(port));
	}

	/**
	 * Obtains the content.
	 * 
	 * @param path
	 *            Path to the content.
	 * @return Content.
	 */
	private String getFileContents(String path) {
		try {
			return this.getFileContents(this.findFile(path));
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Writes the response.
	 * 
	 * @param response
	 *            Response.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	private static void writeResponse(String response,
			ServerHttpConnection connection) throws IOException {
		Writer writer = new OutputStreamWriter(connection.getHttpResponse()
				.getBody().getOutputStream());
		writer.append(response);
		writer.flush();
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		/**
		 * Increment counter handler.
		 * 
		 * @param session
		 *            {@link HttpSession}.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public void incrementCounter(HttpSession session,
				ServerHttpConnection connection) throws IOException {

			final String COUNTER_NAME = "counter";

			// Obtain the counter
			RequestCounter counter = (RequestCounter) session
					.getAttribute(COUNTER_NAME);
			if (counter == null) {
				counter = new RequestCounter();
				session.setAttribute(COUNTER_NAME, counter);
			}

			// Increment the counter for this request
			counter.count++;

			// Indicate the number of requests
			HttpServerAutoWireOfficeFloorSourceTest.writeResponse(
					String.valueOf(counter.count), connection);
		}
	}

	/**
	 * Request counter to be stored within the {@link HttpSession}.
	 */
	public static class RequestCounter implements Serializable {

		/**
		 * Count.
		 */
		public int count = 0;
	}

}