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

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileSenderWorkSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

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
	 * Value indicating that connection was expected to be refused.
	 */
	private static final int CONNECTION_REFUSED_STATUS = -1;

	/**
	 * {@link HttpServerAutoWireOfficeFloorSource} to test.
	 */
	private final HttpServerAutoWireApplication source = new HttpServerAutoWireOfficeFloorSource();

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * Default not found file path.
	 */
	private final String DEFAULT_NOT_FOUND_PATH = ClasspathHttpFileSenderWorkSource.DEFAULT_NOT_FOUND_FILE_PATH;

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
	 * Ensure able to add HTTP template that is available via URI.
	 */
	public void testTemplateWithUri() throws Exception {

		final String SUBMIT_URI = "/uri.ofp.links/submit.task";

		// Add HTTP template (with URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				"template.ofp", MockTemplateLogic.class, "uri.ofp");
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "uri.ofp",
				section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class,
				section.getSectionSourceClass());
		assertEquals("Incorrect section location", "template.ofp",
				section.getSectionLocation());
		assertEquals("Incorrect template path", "template.ofp",
				section.getTemplatePath());
		assertEquals("Incorrect template URI", "uri.ofp",
				section.getTemplateUri());

		// Ensure template available
		this.assertHttpRequest("http://localhost:7878/uri.ofp", 200, SUBMIT_URI);
	}

	/**
	 * Ensure issue if attempt to add more than one HTTP template for a URI.
	 */
	public void testMultipleTemplatesWithSameUri() throws Exception {

		final String TEMPLATE_URI = "template.ofp";

		// Add HTTP template
		this.source.addHttpTemplate("template.ofp", MockTemplateLogic.class,
				TEMPLATE_URI);

		// Ensure indicates template already registered for URI
		try {
			this.source.addHttpTemplate("template.ofp",
					MockTemplateLogic.class, TEMPLATE_URI);
			fail("Should not successfully add template for duplicate URI");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"HTTP Template already added for URI '" + TEMPLATE_URI
							+ "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to add HTTP template that is NOT available via URI.
	 */
	public void testTemplateWithoutUri() throws Exception {

		String fileNotFound = this.getFileContents(DEFAULT_NOT_FOUND_PATH);

		// Add HTTP template (without URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				"template.ofp", MockTemplateLogic.class);
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "resource0",
				section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class,
				section.getSectionSourceClass());
		assertEquals("Incorrect section location", "template.ofp",
				section.getSectionLocation());
		assertEquals("Incorrect template path", "template.ofp",
				section.getTemplatePath());
		assertNull("Should not have a template URI", section.getTemplateUri());

		// Ensure template NOT available
		this.assertHttpRequest("http://localhost:7878/template.ofp", 404,
				fileNotFound);
	}

	/**
	 * Ensure able to request the template link on public template.
	 */
	public void testTemplateLinkWithUri() throws Exception {

		final String SUBMIT_URI = "/uri.ofp.links/submit.task";

		// Add HTTP template
		this.source.addHttpTemplate("template.ofp", MockTemplateLogic.class,
				"uri.ofp");
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		this.assertHttpRequest("http://localhost:7878" + SUBMIT_URI, 200,
				"submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure able to request the template link on private template.
	 */
	public void testTemplateLinkWithoutUri() throws Exception {

		final String SUBMIT_URI = "/resource0.links/submit.task";

		// Add HTTP template
		this.source.addHttpTemplate("template.ofp", MockTemplateLogic.class);
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		this.assertHttpRequest("http://localhost:7878" + SUBMIT_URI, 200,
				"submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure able to link URI to {@link OfficeSectionInput} for processing.
	 */
	public void testLinkUriToSectionInput() throws Exception {

		// Add section for handling request
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class, MockTemplateLogic.class.getName());
		this.source.linkUri("test", section, "submit");
		this.source.openOfficeFloor();

		// Ensure can send to URI
		this.assertHttpRequest("http://localhost:7878/test", 200, "submitted");
	}

	/**
	 * Ensure able to link {@link OfficeSectionOutput} to {@link HttpTemplate}.
	 */
	public void testLinkToHttpTemplate() throws Exception {

		// Add linking to HTTP template
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class, MockLinkHttpTemplate.class.getName());
		this.source.linkUri("test", section, "service");
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"template.ofp", MockTemplateLogic.class);
		this.source.linkToHttpTemplate(section, "http-template", template);
		this.source.openOfficeFloor();

		// Ensure link to the HTTP template
		this.assertHttpRequest("http://localhost:7878/test", 200,
				"LINK to /resource0.links/submit.task");
	}

	/**
	 * Ensure able to link to resource.
	 */
	public void testLinkToResource() throws Exception {

		// Add linking to resource
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class, MockLinkResource.class.getName());
		this.source.linkUri("test", section, "service");
		this.source.linkToResource(section, "resource", "resource.html");
		this.source.openOfficeFloor();

		// Ensure link to the HTTP template
		this.assertHttpRequest("http://localhost:7878/test", 200,
				"LINK to RESOURCE");
	}

	/**
	 * Ensure able to utilise the {@link HttpSession}.
	 */
	public void testHttpSession() throws Exception {

		// Add section that uses the HTTP Session
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class, MockTemplateLogic.class.getName());
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
	 * Ensure able to override the non-routed servicing.
	 */
	public void testOverrideNonHandledServicing() throws Exception {

		// Add section to override servicing
		AutoWireSection section = this.source
				.addSection("SECTION", ClassSectionSource.class,
						MockNonRoutedServicer.class.getName());
		this.source.setNonHandledServicer(section, "service");
		this.source.linkToSendResponse(section, "send");

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure override non-routed servicing
		this.assertHttpRequest("http://localhost:7878/unhandled", 200,
				"NON_ROUTED - /unhandled");
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
	 * Mock logic for the template.
	 */
	public static class MockTemplateLogic {

		/**
		 * Submit handler.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		@NextTask("doNothing")
		public void submit(ServerHttpConnection connection) throws IOException {
			HttpServerAutoWireOfficeFloorSourceTest.writeResponse("submitted",
					connection);
		}

		/**
		 * Do nothing after submit.
		 */
		public void doNothing() {
		}

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
	 * Provides mock functionality of non-routed servicing.
	 */
	public static class MockNonRoutedServicer {
		@NextTask("send")
		public void service(ServerHttpConnection connection) throws IOException {
			String uri = connection.getHttpRequest().getRequestURI();
			HttpServerAutoWireOfficeFloorSourceTest.writeResponse(
					"NON_ROUTED - " + uri, connection);
		}
	}

	/**
	 * Provides mock functionality to link to a HTTP template.
	 */
	public static class MockLinkHttpTemplate {
		@NextTask("http-template")
		public void service(ServerHttpConnection connection) throws IOException {
			HttpServerAutoWireOfficeFloorSourceTest.writeResponse("LINK to ",
					connection);
		}
	}

	/**
	 * Provides mock functionality to link to a resource.
	 */
	public static class MockLinkResource {
		@NextTask("resource")
		public void service(ServerHttpConnection connection) throws IOException {
			HttpServerAutoWireOfficeFloorSourceTest.writeResponse("LINK to ",
					connection);
		}
	}

}