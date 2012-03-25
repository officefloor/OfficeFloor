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

package net.officefloor.plugin.web.http.application;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.sql.SQLException;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileSenderWorkSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.source.HttpSessionManagedObjectSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link WebApplicationAutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebApplicationAutoWireOfficeFloorSourceTest extends
		OfficeFrameTestCase {

	/**
	 * {@link WebApplicationAutoWireOfficeFloorSource} to be tested.
	 */
	private final WebApplicationAutoWireOfficeFloorSource source = new WebApplicationAutoWireOfficeFloorSource();

	/**
	 * Default not found file path.
	 */
	private final String DEFAULT_NOT_FOUND_PATH = ClasspathHttpFileSenderWorkSource.DEFAULT_NOT_FOUND_FILE_PATH;

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * Port on which the web application is to run.
	 */
	private int port;

	@Override
	protected void setUp() throws Exception {

		// Configure the HTTP Server Socket
		this.port = MockHttpServer.getAvailablePort();
		HttpServerSocketManagedObjectSource.autoWire(this.source, this.port,
				WebApplicationAutoWireOfficeFloorSource.HANDLER_SECTION_NAME,
				WebApplicationAutoWireOfficeFloorSource.HANDLER_INPUT_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		// Ensure close
		AutoWireManagement.closeAllOfficeFloors();

		// Stop the client
		this.client.getConnectionManager().shutdown();
	}

	/**
	 * Ensure able to add HTTP template that is available via URI.
	 */
	public void testTemplateWithUri() throws Exception {

		final String SUBMIT_URI = "/uri.ofp.links-submit.task";

		final String templatePath = this.getClassPath("template.ofp");

		// Add HTTP template (with URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				templatePath, MockTemplateLogic.class, "uri.ofp");
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "uri.ofp",
				section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class.getName(),
				section.getSectionSourceClassName());
		assertEquals("Incorrect section location",
				this.getClassPath("template.ofp"), templatePath);
		assertEquals("Incorrect template path", templatePath,
				section.getTemplatePath());
		assertEquals("Incorrect template URI", "uri.ofp",
				section.getTemplateUri());

		// Ensure template available
		this.assertHttpRequest("/uri.ofp", 200, SUBMIT_URI);
	}

	/**
	 * Ensure able to add HTTP template that is available for default root.
	 */
	public void testRootTemplate() throws Exception {

		final String SUBMIT_URI = "/.links-submit.task";

		final String templatePath = this.getClassPath("template.ofp");

		// Add HTTP template (with URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				templatePath, MockTemplateLogic.class, "/");
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "/", section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class.getName(),
				section.getSectionSourceClassName());
		assertEquals("Incorrect section location",
				this.getClassPath("template.ofp"), templatePath);
		assertEquals("Incorrect template path", templatePath,
				section.getTemplatePath());
		assertEquals("Incorrect template URI", "/", section.getTemplateUri());

		// Ensure template available at default root
		this.assertHttpRequest("", 200, SUBMIT_URI);

		// Ensure root link works
		this.assertHttpRequest(SUBMIT_URI, 200, "submitted" + SUBMIT_URI);
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
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"submitted", connection);
		}

		/**
		 * Do nothing after submit.
		 */
		public void doNothing() {
		}
	}

	/**
	 * Ensure issue if attempt to add more than one HTTP template for a URI.
	 */
	public void testMultipleTemplatesWithSameUri() throws Exception {

		final String TEMPLATE_URI = "template.ofp";

		// Add HTTP template
		this.source.addHttpTemplate(this.getClassPath("template.ofp"),
				MockTemplateLogic.class, TEMPLATE_URI);

		// Ensure indicates template already registered for URI
		try {
			this.source.addHttpTemplate(this.getClassPath("template.ofp"),
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

		final String templatePath = this.getClassPath("template.ofp");

		// Add HTTP template (without URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				templatePath, MockTemplateLogic.class);
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "resource0",
				section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class.getName(),
				section.getSectionSourceClassName());
		assertEquals("Incorrect section location", templatePath,
				section.getSectionLocation());
		assertEquals("Incorrect template path", templatePath,
				section.getTemplatePath());
		assertNull("Should not have a template URI", section.getTemplateUri());

		// Ensure template NOT available
		this.assertHttpRequest("/template.ofp", 404, fileNotFound);
	}

	/**
	 * Ensure able to request the template link on public template.
	 */
	public void testTemplateLinkWithUri() throws Exception {

		final String SUBMIT_URI = "/uri.ofp.links-submit.task";

		// Add HTTP template
		this.source.addHttpTemplate(this.getClassPath("template.ofp"),
				MockTemplateLogic.class, "uri.ofp");
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		this.assertHttpRequest(SUBMIT_URI, 200, "submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure able to request the template link on private template.
	 */
	public void testTemplateLinkWithoutUri() throws Exception {

		final String SUBMIT_URI = "/resource0.links-submit.task";

		// Add HTTP template
		this.source.addHttpTemplate(this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		this.assertHttpRequest(SUBMIT_URI, 200, "submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure able to provide {@link HttpTemplateSectionExtension}.
	 */
	public void testTemplateExtension() throws Exception {

		// Add HTTP template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				this.getClassPath("Extension.ofp"),
				MockExtensionTemplateLogic.class, "template");

		// Add template extension
		HttpTemplateAutoWireSectionExtension extension = template
				.addTemplateExtension(MockHttpTemplateSectionExtension.class);
		extension.addProperty("name", "value");

		// Open
		this.source.openOfficeFloor();

		// Ensure extend the template
		this.assertHttpRequest("/template", 200, "extended");
	}

	/**
	 * Mock {@link HttpTemplateSectionExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements
			HttpTemplateSectionExtension {
		@Override
		public void extendTemplate(HttpTemplateSectionExtensionContext context)
				throws Exception {
			context.setTemplateContent("${extend}");
		}
	}

	/**
	 * Template logic for the extension test.
	 */
	public static class MockExtensionTemplateLogic {

		public MockExtensionTemplateLogic getTemplate() {
			return this;
		}

		public String getExtend() {
			return "extended";
		}
	}

	/**
	 * Ensure able to link URI to {@link OfficeSectionInput} for processing.
	 */
	public void testLinkUriToSectionInput() throws Exception {

		// Add section for handling request
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockTemplateLogic.class.getName());
		this.source.linkUri("test", section, "submit");
		this.source.openOfficeFloor();

		// Ensure can send to URI
		this.assertHttpRequest("/test", 200, "submitted");
	}

	/**
	 * Ensure able to link {@link OfficeSectionOutput} to {@link HttpTemplate}.
	 */
	public void testLinkToHttpTemplate() throws Exception {

		// Add linking to HTTP template
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockLinkHttpTemplate.class.getName());
		this.source.linkUri("test", section, "service");
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				this.getClassPath("template.ofp"), MockTemplateLogic.class);
		this.source.linkToHttpTemplate(section, "http-template", template);
		this.source.openOfficeFloor();

		// Ensure link to the HTTP template
		this.assertHttpRequest("/test", 200,
				"LINK to /resource0.links-submit.task");
	}

	/**
	 * Provides mock functionality to link to a HTTP template.
	 */
	public static class MockLinkHttpTemplate {
		@NextTask("http-template")
		public void service(ServerHttpConnection connection) throws IOException {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"LINK to ", connection);
		}
	}

	/**
	 * Ensure able to link to resource.
	 */
	public void testLinkToResource() throws Exception {

		// Add linking to resource
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockLinkResource.class.getName());
		this.source.linkUri("test", section, "service");
		this.source.linkToResource(section, "resource", "resource.html");
		this.source.openOfficeFloor();

		// Ensure link to the HTTP template
		this.assertHttpRequest("/test", 200, "LINK to RESOURCE");
	}

	/**
	 * Provides mock functionality to link to a resource.
	 */
	public static class MockLinkResource {
		@NextTask("resource")
		public void service(ServerHttpConnection connection) throws IOException {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"LINK to ", connection);
		}
	}

	/**
	 * Ensure able to link {@link Escalation} to
	 * {@link HttpTemplateAutoWireSection}.
	 */
	public void testLinkEscalationToTemplate() throws Exception {

		// Add escalation to template
		AutoWireSection failingSection = this.source.addSection("FAILING",
				ClassSectionSource.class.getName(),
				FailingSection.class.getName());
		this.source.linkUri("test", failingSection, "task");
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				this.getClassPath("template.ofp"), MockTemplateLogic.class,
				"handler");
		this.source.linkEscalation(SQLException.class, template);
		this.source.openOfficeFloor();

		// Ensure link escalation to template
		this.assertHttpRequest("/test", 200,
				"Escalated to /handler.links-submit.task");
	}

	/**
	 * Section class that fails and provides an {@link Escalation}.
	 */
	public static class FailingSection {
		public void task(ServerHttpConnection connection) throws Exception {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"Escalated to ", connection);
			throw new SQLException("Test failure");
		}
	}

	/**
	 * Ensure able to link {@link Escalation} to resource.
	 */
	public void testLinkEscalationToResource() throws Exception {

		// Add escalation to resource
		AutoWireSection failingSection = this.source.addSection("FAILING",
				ClassSectionSource.class.getName(),
				FailingSection.class.getName());
		this.source.linkUri("test", failingSection, "task");
		this.source.linkEscalation(SQLException.class, "resource.html");
		this.source.openOfficeFloor();

		// Ensure link escalation to resource
		this.assertHttpRequest("/test", 200, "Escalated to RESOURCE");
	}

	/**
	 * Ensure able to utilise the HTTP Parameters Object.
	 */
	public void testHttpParametersObject() throws Exception {

		// Add the template to use parameters object
		this.source.addHttpTemplate(this.getClassPath("ParametersObject.ofp"),
				MockHttpParametersObjectTemplate.class, "template");

		// Add the HTTP Parameters Object
		this.source.addHttpParametersObject(MockHttpParametersObject.class);

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure provide HTTP parameters
		this.assertHttpRequest("/template?text=VALUE", 200, "VALUE");
	}

	/**
	 * Provides mock template logic for the HTTP Parameters Object.
	 */
	public static class MockHttpParametersObjectTemplate {
		public MockHttpParametersObject getTemplate(
				MockHttpParametersObject object) {
			return object;
		}
	}

	/**
	 * Mock HTTP Parameters Object.
	 */
	public static class MockHttpParametersObject {
		private String text;

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure {@link HttpParameters} is honoured for templates.
	 */
	public void testAnnotatedHttpParameters() throws Exception {

		// Add the template to use parameters object
		this.source.addHttpTemplate(this.getClassPath("ParametersObject.ofp"),
				MockAnnotatedHttpParametersTemplate.class, "template");

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure provide HTTP parameters
		this.assertHttpRequest("/template?text=VALUE", 200, "VALUE");
	}

	/**
	 * Provides mock template logic for the HTTP Parameters Object.
	 */
	public static class MockAnnotatedHttpParametersTemplate {
		public MockAnnotatedHttpParameters getTemplate(
				MockAnnotatedHttpParameters object) {
			return object;
		}
	}

	/**
	 * Mock HTTP Parameters Object.
	 */
	@HttpParameters
	public static class MockAnnotatedHttpParameters {
		private String text;

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to utilise the Http Session object.
	 */
	public void testHttpSessionObject() throws Exception {

		// Provide HTTP Session
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);

		// Add two templates to ensure object available to both
		this.source.addHttpTemplate(this.getClassPath("StatefulObject.ofp"),
				MockHttpSessionObjectTemplate.class, "one");
		this.source.addHttpTemplate(this.getClassPath("StatefulObject.ofp"),
				MockHttpSessionObjectTemplate.class, "two");

		// Add the HTTP Session object
		this.source.addHttpSessionObject(MockHttpSessionObject.class);

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure state maintained across requests
		this.assertHttpRequest("/one", 200, "1");
		this.assertHttpRequest("/one", 200, "2");

		// Ensure state maintained on another template
		this.assertHttpRequest("/two", 200, "3");

		// Ensure reflected on original template
		this.assertHttpRequest("/one", 200, "4");
	}

	/**
	 * Provides mock template logic for Http Session Object.
	 */
	public static class MockHttpSessionObjectTemplate {
		public MockHttpSessionObject getTemplate(MockHttpSessionObject object) {
			object.count++; // increment count to indicate maintaining state
			return object;
		}
	}

	/**
	 * Mock Http Session Object.
	 */
	public static class MockHttpSessionObject implements Serializable {
		public int count = 0;

		public int getCount() {
			return count;
		}
	}

	/**
	 * Ensure {@link HttpSessionStateful} annotation is honoured for templates.
	 */
	public void testHttpSessionStatefulAnnotation() throws Exception {

		// Provide HTTP Session
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);

		// Add two templates with annotations for HttpSessionStateful
		this.source.addHttpTemplate(this.getClassPath("StatefulObject.ofp"),
				MockAnnotatedHttpSessionStatefulTemplate.class, "one");
		this.source.addHttpTemplate(this.getClassPath("StatefulObject.ofp"),
				MockAnnotatedHttpSessionStatefulTemplate.class, "two");

		// No Http Session Object to be added as should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure state maintained across requests
		this.assertHttpRequest("/one", 200, "1");
		this.assertHttpRequest("/one", 200, "2");

		// Ensure state maintained on another template
		this.assertHttpRequest("/two", 200, "3");

		// Ensure reflected on original template
		this.assertHttpRequest("/one", 200, "4");
	}

	/**
	 * Provides mock template logic for using the {@link HttpSessionStateful}
	 * annotation.
	 */
	public static class MockAnnotatedHttpSessionStatefulTemplate {
		public MockAnnotatedHttpSessionStatefulObject getTemplate(
				MockAnnotatedHttpSessionStatefulObject object) {
			object.count++; // increment count to indicate maintaining state
			return object;
		}
	}

	/**
	 * Mock Http Session Object as annotated.
	 */
	@HttpSessionStateful
	public static class MockAnnotatedHttpSessionStatefulObject implements
			Serializable {
		public int count = 0;

		public int getCount() {
			return count;
		}
	}

	/**
	 * Ensure can override the binding name to the {@link HttpSession}.
	 */
	public void testHttpSessionStatefulAnnotationOverridingBoundName()
			throws Exception {

		// Provide HTTP Session
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);

		// Add the template
		this.source
				.addHttpTemplate(
						this.getClassPath("StatefulObject.ofp"),
						MockAnnotatedOverriddenBindNameHttpSessionStatefulTemplate.class,
						"template");

		// No HTTP Session object as should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure same object (test within template logic)
		this.assertHttpRequest("/template", 200, "1");
	}

	/**
	 * Provides mock template logic for validating the overriding binding name
	 * for the {@link HttpSession} object.
	 */
	public static class MockAnnotatedOverriddenBindNameHttpSessionStatefulTemplate {
		public MockAnnotatedOverriddenBindNameHttpSessionStatefulObject getTemplate(
				MockAnnotatedOverriddenBindNameHttpSessionStatefulObject object,
				HttpSession session) {

			// Ensure object bound under annotated name within the session
			Object sessionObject = session.getAttribute("BIND");
			assertEquals("Should be same object", object, sessionObject);

			// Return for rendering
			return object;
		}
	}

	/**
	 * Mock Http Session Object with overridden binding name.
	 */
	@HttpSessionStateful("BIND")
	public static class MockAnnotatedOverriddenBindNameHttpSessionStatefulObject {
		public int getCount() {
			return 1;
		}
	}

	/**
	 * Ensure able to utilise the {@link HttpRequestState} object.
	 */
	public void testHttpRequestObject() throws Exception {

		final String URI = "/template.links-submit.task";

		// Provide HTTP Request State
		this.source.addManagedObject(
				HttpRequestStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpRequestState.class));

		// Add the template
		this.source.addHttpTemplate(this.getClassPath("HttpStateObject.ofp"),
				MockHttpRequestStateTemplate.class, "template");

		// No HTTP request state object as should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure same object (test within template logic)
		this.assertHttpRequest(URI, 200, "maintained state-" + URI);
	}

	/**
	 * Provides mock template logic for validating the {@link HttpRequestState}.
	 */
	public static class MockHttpRequestStateTemplate {

		public void submit(MockHttpRequestStateObject object,
				HttpRequestState state) {

			// Ensure object bound under annotated name within the request state
			Object requestObject = state.getAttribute("BIND");
			assertEquals("Should be same object", object, requestObject);

			// Specify value as should maintain state through request
			object.text = "maintained state";
		}

		public MockHttpRequestStateObject getTemplate(
				MockHttpRequestStateObject object) {
			// Value should be specified in submit
			return object;
		}
	}

	/**
	 * Mock Http Request State Object with overridden binding name.
	 */
	@HttpRequestStateful("BIND")
	public static class MockHttpRequestStateObject {

		public String text = "not specified";

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to utilise the HTTP Application object.
	 */
	public void testHttpApplicationObject() throws Exception {

		final String URI = "/template.links-submit.task";

		// Provide HTTP Application State
		this.source.addManagedObject(
				HttpApplicationStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpApplicationState.class));

		// Add the template
		this.source.addHttpTemplate(this.getClassPath("HttpStateObject.ofp"),
				MockHttpApplicationStateTemplate.class, "template");

		// No HTTP application state object as should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure same object (test within template logic)
		this.assertHttpRequest(URI, 200, "maintained state-" + URI);
	}

	/**
	 * Provides mock template logic for validating the
	 * {@link HttpApplicationState}.
	 */
	public static class MockHttpApplicationStateTemplate {

		public void submit(MockHttpApplicationStateObject object,
				HttpApplicationState state) {

			// Ensure object bound under annotated name within application state
			Object applicationObject = state.getAttribute("BIND");
			assertEquals("Should be same object", object, applicationObject);

			// Specify value as should maintain state through application
			object.text = "maintained state";
		}

		public MockHttpApplicationStateObject getTemplate(
				MockHttpApplicationStateObject object) {
			// Value should be specified in submit
			return object;
		}
	}

	/**
	 * Mock Http Application State Object with overridden binding name.
	 */
	@HttpApplicationStateful("BIND")
	public static class MockHttpApplicationStateObject {

		public String text = "not specified";

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to override the non-routed servicing.
	 */
	public void testOverrideNonHandledServicing() throws Exception {

		// Add section to override servicing
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockNonRoutedServicer.class.getName());
		this.source.setNonHandledServicer(section, "service");
		this.source.linkToSendResponse(section, "send");

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure override non-routed servicing
		this.assertHttpRequest("/unhandled", 200, "NON_ROUTED - /unhandled");
	}

	/**
	 * Provides mock functionality of non-routed servicing.
	 */
	public static class MockNonRoutedServicer {
		@NextTask("send")
		public void service(ServerHttpConnection connection) throws IOException {
			String uri = connection.getHttpRequest().getRequestURI();
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"NON_ROUTED - " + uri, connection);
		}
	}

	/**
	 * Asserts the HTTP request returns expected result.
	 * 
	 * @param uri
	 *            URI to send the HTTP request.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseBody
	 *            Expected response body.
	 */
	private void assertHttpRequest(String uri, int expectedResponseStatus,
			String expectedResponseBody) {
		try {

			// Create the URL
			String url = "http://localhost:" + this.port + uri;

			// Send the request
			HttpGet request = new HttpGet(url);
			HttpResponse response = this.client.execute(request);

			// Ensure obtained as expected
			String actualResponseBody = MockHttpServer.getEntityBody(response);
			assertEquals("Incorrect response for URL '" + url + "'",
					expectedResponseBody, actualResponseBody);

			// Ensure correct response status
			assertEquals("Should be successful", expectedResponseStatus,
					response.getStatusLine().getStatusCode());

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Obtains the class path to the file.
	 * 
	 * @param fileName
	 *            Name of the file.
	 * @return Class path to the file.
	 */
	private String getClassPath(String fileName) {
		return this.getFileLocation(this.getClass(), fileName);
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

}