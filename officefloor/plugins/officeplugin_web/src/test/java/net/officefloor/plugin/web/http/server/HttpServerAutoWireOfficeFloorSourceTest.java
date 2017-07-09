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
package net.officefloor.plugin.web.http.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpApplicationStateful;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateful;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.HttpFileSenderManagedFunctionSource;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;

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
	private HttpServerAutoWireApplication source = new HttpServerAutoWireOfficeFloorSource();

	/**
	 * Value indicating that connection was expected to be refused.
	 */
	private static final int CONNECTION_REFUSED_STATUS = -1;

	/**
	 * Default not found file path.
	 */
	private final String DEFAULT_NOT_FOUND_PATH = HttpFileSenderManagedFunctionSource.DEFAULT_NOT_FOUND_FILE_PATH;

	/**
	 * {@link HttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil
			.createHttpClient(true);

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.close();

		} finally {
			// Ensure close
			AutoWireManagement.closeAllOfficeFloors();
		}
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
		this.assertHttpRequest("https://localhost:7979/index.html", 200,
				expected);

		// Ensure services request for default file
		this.assertHttpRequest("http://localhost:7878", 200, expected);
		this.assertHttpRequest("https://localhost:7979", 200, expected);

		// Ensure handles unknown resource
		this.assertHttpRequest("http://localhost:7878/unknown", 404,
				fileNotFound);
		this.assertHttpRequest("https://localhost:7979/unknown", 404,
				fileNotFound);
	}

	/**
	 * Ensure able to construct specifying HTTP port.
	 */
	public void testInitiateHttpPort() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Construct HTTP instance with specified port
		this.source = new HttpServerAutoWireOfficeFloorSource(PORT);
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by initiated HTTP port
		this.assertHttpRequest("http://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure able to construct specifying HTTPS port.
	 */
	public void testInitiateHttpsPort() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();
		final int SECURE_PORT = HttpTestUtil.getAvailablePort();

		// Construct HTTP instance with specified port
		this.source = new HttpServerAutoWireOfficeFloorSource(PORT,
				SECURE_PORT, HttpTestUtil.getSslEngineSourceClass());
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by initiated HTTPS port
		this.assertHttpRequest("https://localhost:" + SECURE_PORT, 200,
				expected);
	}

	/**
	 * Ensure specify HTTP port.
	 */
	public void testAddHttpSocket() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Specify the HTTP port
		this.source.addHttpServerSocket(PORT);
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by added HTTP port
		this.assertHttpRequest("http://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure specify HTTPS port.
	 */
	public void testAddHttpsSocket() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Specify the HTTPS port
		this.source.addHttpsServerSocket(PORT,
				HttpTestUtil.getSslEngineSourceClass());
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by added HTTP port
		this.assertHttpRequest("https://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure same {@link AutoWireObject} should HTTP port be added twice.
	 */
	public void testAddSameHttpSocketTwice() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Add the port twice
		AutoWireObject one = this.source.addHttpServerSocket(PORT);
		AutoWireObject two = this.source.addHttpServerSocket(PORT);
		assertSame("Ensure same object for same port", one, two);
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by added HTTP port
		this.assertHttpRequest("http://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure same {@link AutoWireObject} should HTTPS port be added twice.
	 */
	public void testAddSameHttpsSocketTwice() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Add the port twice
		AutoWireObject one = this.source.addHttpsServerSocket(PORT,
				HttpTestUtil.getSslEngineSourceClass());
		AutoWireObject two = this.source.addHttpsServerSocket(PORT,
				HttpTestUtil.getSslEngineSourceClass());
		assertSame("Ensure same object for same port", one, two);
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by added HTTP port
		this.assertHttpRequest("https://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure can provide {@link OfficeFloor} property to configure the HTTP
	 * port.
	 */
	public void testConfigureHttpPort() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Open on alternate port (via OfficeFloor configuration)
		this.source.getOfficeFloorCompiler().addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
				String.valueOf(PORT));
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by configured HTTP port
		this.assertHttpRequest("http://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure can provide {@link OfficeFloor} property to configure the HTTPS
	 * port.
	 */
	public void testConfigureHttpsPort() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Open on alternate port (via OfficeFloor configuration)
		OfficeFloorCompiler compiler = this.source.getOfficeFloorCompiler();
		compiler.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTPS_PORT,
				String.valueOf(PORT));
		compiler.addProperty(
				SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
				HttpTestUtil.getSslEngineSourceClass().getName());
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by configured HTTPS port
		this.assertHttpRequest("https://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure can provide {@link OfficeFloor} property to configure the HTTP
	 * port.
	 */
	public void testConfigureClusterHttpPort() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Open on alternate port (via OfficeFloor configuration)
		OfficeFloorCompiler compiler = this.source.getOfficeFloorCompiler();
		compiler.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
				String.valueOf(-1)); // should not be used as cluster HTTP port
		compiler.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_CLUSTER_HTTP_PORT,
				String.valueOf(PORT));
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by configured HTTP port
		this.assertHttpRequest("http://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure can provide {@link OfficeFloor} property to configure the HTTPS
	 * port.
	 */
	public void testConfigureClusterHttpsPort() throws Exception {

		final int PORT = HttpTestUtil.getAvailablePort();

		// Open on alternate port (via OfficeFloor configuration)
		OfficeFloorCompiler compiler = this.source.getOfficeFloorCompiler();
		compiler.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTPS_PORT,
				String.valueOf(-1)); // should not be used as cluster HTTPS port
		compiler.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_CLUSTER_HTTPS_PORT,
				String.valueOf(PORT));
		compiler.addProperty(
				SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
				HttpTestUtil.getSslEngineSourceClass().getName());
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure able to service by configured HTTPS port
		this.assertHttpRequest("https://localhost:" + PORT, 200, expected);
	}

	/**
	 * Ensure can listen on multiple HTTP ports.
	 */
	public void testListenOnMultipleHttpPorts() throws Exception {

		final int EXTRA_PORT_ONE = HttpTestUtil.getAvailablePort();
		final int EXTRA_PORT_TWO = HttpTestUtil.getAvailablePort();
		final int DEFAULT_PORT = 7878; // listen on default port as well

		// Listen on multiple HTTP Server socket
		this.source = new HttpServerAutoWireOfficeFloorSource(EXTRA_PORT_ONE);
		this.source.addHttpServerSocket(EXTRA_PORT_TWO);
		this.source.addHttpServerSocket(DEFAULT_PORT);
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
	 * Ensure can listen on multiple HTTPS ports.
	 */
	public void testListenOnMultipleHttpsPorts() throws Exception {

		final int EXTRA_PORT_ONE = HttpTestUtil.getAvailablePort();
		final int EXTRA_PORT_TWO = HttpTestUtil.getAvailablePort();
		final int DEFAULT_PORT = 7979; // listen on default port as well

		// Listen on multiple HTTPS Server socket
		this.source = new HttpServerAutoWireOfficeFloorSource(7878,
				EXTRA_PORT_ONE, HttpTestUtil.getSslEngineSourceClass());
		this.source.addHttpsServerSocket(EXTRA_PORT_TWO,
				HttpTestUtil.getSslEngineSourceClass());
		this.source.addHttpsServerSocket(DEFAULT_PORT,
				HttpTestUtil.getSslEngineSourceClass());
		this.source.openOfficeFloor();

		// Obtain the expected content
		String expected = this.getFileContents("PUBLIC/index.html");

		// Ensure listening on both ports
		this.assertHttpRequest("https://localhost:" + EXTRA_PORT_ONE, 200,
				expected);
		this.assertHttpRequest("https://localhost:" + EXTRA_PORT_TWO, 200,
				expected);
		this.assertHttpRequest("https://localhost:" + DEFAULT_PORT, 200,
				expected);
	}

	/**
	 * Ensure able to utilise the {@link HttpSession}.
	 */
	public void testHttpSession() throws Exception {

		// Add section that uses the HTTP Session
		AutoWireSection section = this.source
				.addSection("SECTION", ClassSectionSource.class.getName(),
						MockSection.class.getName());
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

	/**
	 * Ensure able to utilise the various states.
	 */
	public void testHttpState() throws Exception {

		// Obtain the template path
		String templatePath = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/HttpStateTemplate.ofp";

		// Add the template
		this.source.addHttpTemplate("template", templatePath,
				MockHttpStateTemplateLogic.class);

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Should provide content from each state type object
		this.assertHttpRequest("http://localhost:7878/template", 200,
				"Application Session Request");
	}

	/**
	 * Provides template logic for HTTP state test.
	 */
	public static class MockHttpStateTemplateLogic {

		public MockApplicationObject getApplication(MockApplicationObject object) {
			return object;
		}

		public MockSessionObject getSession(MockSessionObject object) {
			return object;
		}

		public MockRequestObject getRequest(MockRequestObject object) {
			return object;
		}
	}

	/**
	 * {@link HttpApplicationState} object.
	 */
	@HttpApplicationStateful
	public static class MockApplicationObject {
		public String getText() {
			return "Application";
		}
	}

	/**
	 * {@link HttpSession} object.
	 */
	@HttpSessionStateful
	public static class MockSessionObject implements Serializable {
		public String getText() {
			return "Session";
		}
	}

	/**
	 * {@link HttpRequestState} object.
	 */
	@HttpRequestStateful
	public static class MockRequestObject implements Serializable {
		public String getText() {
			return "Request";
		}
	}

	/**
	 * Asserts the HTTP request returns expected result.
	 * 
	 * @param url
	 *            URL to send the HTTP request.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseEntity
	 *            Expected response entity.
	 */
	private void assertHttpRequest(String url, int expectedResponseStatus,
			String expectedResponseEntity) {
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

			// Obtain the actual entity
			String actualResponseEntity = HttpTestUtil.getEntityBody(response);

			// Ensure correct response status
			assertEquals("Should be successful [" + actualResponseEntity + "]",
					expectedResponseStatus, response.getStatusLine()
							.getStatusCode());

			// Ensure obtained as expected
			assertEquals("Incorrect response for URL '" + url + "'",
					expectedResponseEntity, actualResponseEntity);

		} catch (Exception ex) {
			throw fail(ex);
		}
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
				.getEntity());
		writer.append(response);
		writer.flush();
	}

}