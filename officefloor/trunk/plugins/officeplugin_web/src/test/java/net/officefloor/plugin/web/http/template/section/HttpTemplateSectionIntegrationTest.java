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

package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.work.WorkSectionSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.route.HttpRouteWorkSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.source.HttpSessionManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the integration of the {@link HttpTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Rendered template XML.
	 */
	private static final String RENDERED_TEMPLATE_XML = "<html><body><p>Template Test</p>"
			+ "<p>&lt;img src=&quot;Test.png&quot; /&gt; <img src=\"Test.png\" /></p>"
			+ "<p>Bean with property bean-property  0 1 2 3 4 5 6 7 8 9</p><table>"
			+ "<tr><td>Name</td><td>Description</td></tr>"
			+ "<tr><td>row</td><td>test row</td></tr></table>"
			+ "<form action=\"${LINK_nextTask_QUALIFICATION}/uri-nextTask\"><input type=\"submit\"/></form>"
			+ "<form action=\"${LINK_submit_QUALIFICATION}/uri-submit\"><input type=\"submit\"/></form>"
			+ "<a href=\"${LINK_nonMethodLink_QUALIFICATION}/uri-nonMethodLink\">Non-method link</a></body></html>";

	/**
	 * Host name.
	 */
	private static final String HOST_NAME = HttpApplicationLocationManagedObjectSource
			.getDefaultHostName();

	/**
	 * Mock {@link Connection}.
	 */
	private final Connection connection = this
			.createSynchronizedMock(Connection.class);

	/**
	 * Port for running on.
	 */
	private int port;

	/**
	 * Indicates if non-method link is provided.
	 */
	private boolean isNonMethodLink = false;

	/**
	 * Indicates if service-method link is provided.
	 */
	private boolean isServiceMethodLink = false;

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	@Override
	protected void tearDown() throws Exception {

		// Disconnect client
		this.client.getConnectionManager().shutdown();

		// Close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can render the template.
	 */
	public void testRenderTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("");
		assertRenderedResponse("", false, false, false, rendering);
	}

	/**
	 * Ensure can render template with a particular secure link.
	 */
	public void testRenderTemplateWithSecureLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "submit",
				String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("");
		assertRenderedResponse("", false, true, false, rendering);
	}

	/**
	 * Ensure can render the template with secure links.
	 */
	public void testRenderSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("");
		assertRenderedResponse("", true, true, true, rendering);
	}

	/**
	 * Ensure can render a secure template with a non-secure link.
	 */
	public void testRenderSecureTemplateWithNonSecureLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				String.valueOf(true),
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX
						+ "nonMethodLink", String.valueOf(false));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("");
		assertRenderedResponse("", true, true, false, rendering);
	}

	/**
	 * Ensure can handle submit to a link that has {@link NextTask} annotation
	 * for handling.
	 */
	public void testSubmitWithNextTask() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "nextTask - finished(NextTask)";

		// Ensure correctly renders template on submit
		this.assertHttpRequest("/uri-nextTask", RESPONSE);
	}

	/**
	 * Ensure default behaviour of #{link} method without a {@link NextTask}
	 * annotation is to render the template.
	 */
	public void testSubmitWithoutNextTask() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure correctly renders template on submit not invoking flow
		String response = this.doHttpRequest("/uri-submit");
		assertRenderedResponse("<submit />", false, false, false, response);
	}

	/**
	 * Ensure with {@link NextTask} annotation that invoking a Flow takes
	 * precedence.
	 */
	public void testSubmitInvokingFlow() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "<submit /> - doInternalFlow[1] - finished(Parameter for External Flow)";

		// Ensure correctly renders template on submit when invoking flow
		this.assertHttpRequest("/uri-submit?doFlow=true", RESPONSE);
	}

	/**
	 * Ensure link straight to template output.
	 */
	public void testNonMethodLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "LINKED";

		// Ensure links out from template
		this.assertHttpRequest("/uri-nonMethodLink", RESPONSE);
	}

	/**
	 * Ensure can render page with section methods have Data suffix.
	 */
	public void testDataSuffix() throws Exception {

		// Start the server
		this.startHttpServer("TemplateData.ofp", TemplateDataLogic.class);

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("");
		assertXmlEquals(
				"Incorrect rendering",
				"<html><body><p>hello world</p><p>section data</p></body></html>",
				rendering);
	}

	/**
	 * Ensure can render template without logic class and have links.
	 */
	public void testNoLogicClass() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("NoLogicTemplate.ofp", null);

		// Ensure template is correct
		this.assertHttpRequest("", " /uri-nonMethodLink /uri-doExternalFlow");

		// Ensure links out from template
		this.assertHttpRequest("/uri-nonMethodLink", "LINKED");
	}

	/**
	 * Ensure add context path to link.
	 */
	public void testContextPathForLink() throws Exception {

		// Start the server (with context path)
		this.isNonMethodLink = true;
		this.startHttpServer(
				"NoLogicTemplate.ofp",
				null,
				HttpApplicationLocationManagedObjectSource.PROPERTY_CONTEXT_PATH,
				"context");

		// Ensure template has context path for links
		this.assertHttpRequest("/context",
				" /context/uri-nonMethodLink /context/uri-doExternalFlow");
	}

	/**
	 * Ensure able to invoke flows by template logic to alter template
	 * rendering.
	 */
	public void testFlowControl() throws Exception {

		// Start the server
		this.startHttpServer("FlowTemplate.ofp", FlowTemplateLogic.class);

		// Ensure get full template
		this.assertHttpRequest("", "TemplateOne1TwoEnd");

		// Ensure skip template one rendering
		this.assertHttpRequest("?getOne=getTwo", "TemplateTwoEnd");

		// Ensure can skip to end
		this.assertHttpRequest("?getTemplate=end", "End");

		// Ensure can loop back
		this.assertHttpRequest("?getEnd=getTemplate",
				"TemplateOne1TwoTemplateOne1TwoEnd");
	}

	/**
	 * Ensure template stateful across {@link HttpRequest}.
	 */
	public void testStatefulTemplate() throws Exception {

		// Start the server
		this.startHttpServer("StatefulTemplate.ofp",
				StatefulTemplateLogic.class);

		// Ensure retains state across HTTP requests (by incrementing counter)
		this.assertHttpRequest("", "<a href='/uri-increment'>1</a>");
		this.assertHttpRequest("/uri-increment", "increment - finished(2)");
		this.assertHttpRequest("", "<a href='/uri-increment'>2</a>");
		this.assertHttpRequest("/uri-increment", "increment - finished(3)");
		this.assertHttpRequest("", "<a href='/uri-increment'>3</a>");
	}

	/**
	 * Ensure on submit link that has next {@link Task} instances that if last
	 * {@link Task} in Flow does not indicate {@link NextTask} that the template
	 * is rendered.
	 */
	public void testRenderByDefault() throws Exception {

		// Start the server
		this.startHttpServer("SubmitTemplate.ofp",
				RenderByDefaultTemplateLogic.class);

		// Ensure render template again by default on link submit
		this.assertHttpRequest("/uri-submit",
				"Submit-RenderByDefault-/uri-submit");
	}

	/**
	 * Template logic for testing.
	 */
	public static class RenderByDefaultTemplateLogic {

		@NextTask("renderByDefault")
		public void submit(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "Submit");
		}

		// Next is to render the template
		public void renderByDefault(ServerHttpConnection connection)
				throws IOException {
			writeMessage(connection, "-RenderByDefault-");
		}

		// Required for test configuration
		@NextTask("doExternalFlow")
		public void required() {
		}
	}

	/**
	 * Ensure able to use a {@link HttpTemplateSectionExtension}.
	 */
	public void testTemplateSectionExtension() throws Exception {

		// Flag to provide service method link
		this.isServiceMethodLink = true;

		// Start the server (with extension)
		this.startHttpServer("ExtensionTemplate.ofp",
				MockExtensionTemplateLogic.class, "extension.1",
				MockHttpTemplateSectionExtension.class.getName(),
				"extension.1.name", "value",
				"extension.1.mock.extension.index", "1", "extension.2",
				MockHttpTemplateSectionExtension.class.getName(),
				"extension.2.name", "value",
				"extension.2.mock.extension.index", "2", "section.name",
				"section.value");

		// Ensure change with extension
		this.assertHttpRequest("",
				"Overridden template with overridden class and /uri-serviceLink");

		// Ensure service method not render template
		this.assertHttpRequest("/uri-serviceLink", "SERVICE_METHOD");
	}

	/**
	 * Mock {@link HttpTemplateSectionExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements
			HttpTemplateSectionExtension {

		/*
		 * ================== HttpTemplateSectionExtension ====================
		 */

		@Override
		public void extendTemplate(HttpTemplateSectionExtensionContext context)
				throws Exception {

			final String TEMPLATE_CONTENT = "Overridden template with ${property} and #{serviceLink}";

			// Obtain the particular extension index
			int extensionIndex = Integer.parseInt(context
					.getProperty("mock.extension.index"));

			// Validate overriding details
			switch (extensionIndex) {
			case 1:
				// Ensure original template content
				assertEquals("Incorrect original template content",
						"extension", context.getTemplateContent());
				break;
			case 2:
				// Ensure overridden template content
				assertEquals("Template content should be overridden",
						TEMPLATE_CONTENT, context.getTemplateContent());
				break;
			default:
				fail("Should only be two extensions");
			}

			// Validate extension configuration
			String[] names = context.getPropertyNames();
			assertEquals("Incorrect number of properties", 2, names.length);
			assertEquals("Incorrect property name", "name", names[0]);
			assertEquals("Incorrect property value", "value",
					context.getProperty("name"));
			assertEquals("Incorrect property mock.extension.index",
					"mock.extension.index", names[1]);
			assertEquals("Not defaulting property", "default",
					context.getProperty("unknown", "default"));
			try {
				// Ensure failure on unknown property
				context.getProperty("unknown");
				fail("Should not successfully obtain unknown property");
			} catch (UnknownPropertyError ex) {
				String unknownPropertyName = "extension." + extensionIndex
						+ ".unknown";
				assertEquals("Incorrect unknown property", unknownPropertyName,
						ex.getUnknownPropertyName());
				assertEquals("Incurrect unknown property message",
						"Unknown property '" + unknownPropertyName + "'",
						ex.getMessage());
			}

			// Validate section details
			assertEquals("Incorrect section context", "section.value", context
					.getSectionSourceContext().getProperty("section.name"));
			assertNotNull("Assuming correct designer",
					context.getSectionDesigner());

			// Extend the template (via overriding)
			context.setTemplateContent(TEMPLATE_CONTENT);

			// Flag a service method
			context.flagAsNonRenderTemplateMethod("serviceMethod");
		}
	}

	/**
	 * Template logic for the extension test.
	 */
	public static class MockExtensionTemplateLogic {

		/**
		 * Necessary as using this for overriding template logic class.
		 * 
		 * @return This.
		 */
		public MockExtensionTemplateLogic getTemplate() {
			return this;
		}

		/**
		 * Property to flag that overriding.
		 * 
		 * @return Value of property on template.
		 */
		public String getProperty() {
			return "overridden class";
		}

		/**
		 * Necessary as using this for overriding template logic class.
		 */
		@NextTask("doExternalFlow")
		public void submit() {
		}

		/**
		 * Service method that should not render template on completion.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public void serviceMethod(ServerHttpConnection connection)
				throws IOException {
			Writer entity = connection.getHttpResponse().getEntityWriter();
			entity.write("SERVICE_METHOD");
			entity.flush();
		}
	}

	/**
	 * Sends the {@link HttpRequest}.
	 * 
	 * @param uri
	 *            URI.
	 * @return Content of the {@link HttpResponse}.
	 */
	private String doHttpRequest(String uri) throws Exception {

		// Send the request to obtain results of rending template
		HttpGet request = new HttpGet("http://" + HOST_NAME + ":" + this.port
				+ uri);
		HttpResponse response = this.client.execute(request);

		// Ensure successful
		assertEquals("Ensure successful", 200, response.getStatusLine()
				.getStatusCode());

		// Obtain and return the response content
		String content = MockHttpServer.getEntityBody(response);
		return content;
	}

	/**
	 * Asserts the {@link HttpRequest}.
	 * 
	 * @param uri
	 *            URI for the {@link HttpRequest}.
	 * @param expectedResponse
	 *            Expected content of the {@link HttpResponse}.
	 */
	private void assertHttpRequest(String uri, String expectedResponse)
			throws Exception {

		// Obtain the rendering
		String rendering = this.doHttpRequest(uri);

		// Ensure correct rendering of template
		assertEquals("Incorrect rendering", expectedResponse, rendering);
	}

	/**
	 * Asserts the rendered response.
	 * 
	 * @param expectedResponsePrefix
	 *            Prefix on the expected response. Typically for testing
	 *            pre-processing before response.
	 * @param isNextTaskQualified
	 *            <code>true</code> if <code>nextTask</code> link is qualified.
	 * @param isSubmitQualified
	 *            <code>true</code> if <code>submit</code> link is qualified.
	 * @param isNonMethodLinkQualified
	 *            <code>true</code> if <code>nonMethodLink</code> link is
	 *            qualified.
	 * @param actualResponse
	 *            Actual rendered response
	 */
	private static void assertRenderedResponse(String expectedResponsePrefix,
			boolean isNextTaskQualified, boolean isSubmitQualified,
			boolean isNonMethodLinkQualified, String actualResponse) {

		final String LINK_QUALIFICATION = "https://" + HOST_NAME + ":7979";

		// Transform expected response for link qualifications
		String expectedResponse = expectedResponsePrefix
				+ RENDERED_TEMPLATE_XML;
		expectedResponse = expectedResponse.replace(
				"${LINK_nextTask_QUALIFICATION}",
				isNextTaskQualified ? LINK_QUALIFICATION : "");
		expectedResponse = expectedResponse.replace(
				"${LINK_submit_QUALIFICATION}",
				isSubmitQualified ? LINK_QUALIFICATION : "");
		expectedResponse = expectedResponse.replace(
				"${LINK_nonMethodLink_QUALIFICATION}",
				isNonMethodLinkQualified ? LINK_QUALIFICATION : "");

		// Validate the rendered response
		assertXmlEquals("Incorrect rendering", expectedResponse, actualResponse);
	}

	/**
	 * Starts the HTTP server for testing.
	 * 
	 * @param templateName
	 *            Name of the template file.
	 * @param logicClass
	 *            Template logic class.
	 * @param templateProperties
	 *            Template name/value property pairs.
	 */
	protected void startHttpServer(String templateName, Class<?> logicClass,
			String... templatePropertyPairs) throws Exception {

		// Auto-wire for testing
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();

		// Add the HTTP server socket listener
		this.port = MockHttpServer.getAvailablePort();
		HttpServerSocketManagedObjectSource.autoWire(source, this.port,
				"ROUTE", "route");

		// Add dependencies
		source.addObject(this.connection, new AutoWire(Connection.class));
		source.addManagedObject(HttpSessionManagedObjectSource.class.getName(),
				null, new AutoWire(HttpSession.class)).setTimeout(10 * 1000);
		AutoWireObject location = source.addManagedObject(
				HttpApplicationLocationManagedObjectSource.class.getName(),
				null, new AutoWire(HttpApplicationLocation.class));

		// Provide HTTP router for testing
		AutoWireSection templateRouteSection = source.addSection("ROUTE",
				WorkSectionSource.class.getName(),
				HttpRouteWorkSource.class.getName());

		// Load the template section
		final String templateLocation = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/" + templateName;
		AutoWireSection templateSection = source.addSection("SECTION",
				HttpTemplateSectionSource.class.getName(), templateLocation);
		if (logicClass != null) {
			templateSection.addProperty(
					HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
					logicClass.getName());
		}
		templateSection.addProperty(
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");

		// Load the additional properties
		for (int i = 0; i < templatePropertyPairs.length; i += 2) {
			String name = templatePropertyPairs[i];
			String value = templatePropertyPairs[i + 1];
			templateSection.addProperty(name, value);
			location.addProperty(name, value);
		}

		// Load mock section for handling outputs
		AutoWireSection handleOutputSection = source
				.addSection("OUTPUT", ClassSectionSource.class.getName(),
						MockSection.class.getName());

		// Link flow outputs
		source.link(templateRouteSection, "NOT_HANDLED", templateSection,
				"renderTemplate");
		source.link(templateSection, "output", handleOutputSection, "finished");
		source.link(templateSection, "doExternalFlow", handleOutputSection,
				"finished");

		// Link non-method link
		if (this.isNonMethodLink) {
			AutoWireSection handleOutputLink = source.addSection("LINK",
					ClassSectionSource.class.getName(),
					MockLink.class.getName());
			source.link(templateSection, "nonMethodLink", handleOutputLink,
					"linked");
		}

		// Link service method link
		if (this.isServiceMethodLink) {
			source.link(templateSection, "serviceLink", templateSection,
					"serviceMethod");
		}

		// Open the OfficeFloor
		this.officeFloor = source.openOfficeFloor();
	}

	/**
	 * Writes the message.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param message
	 *            Message.
	 * @throws IOException
	 *             If fails to write the message.
	 */
	private static void writeMessage(ServerHttpConnection connection,
			String message) throws IOException {
		Writer writer = connection.getHttpResponse().getEntityWriter();
		writer.write(message);
		writer.flush();
	}

	/**
	 * Mock section for output tasks of the template.
	 */
	public static class MockSection {
		public void finished(@Parameter String parameter,
				ServerHttpConnection connection) throws IOException {
			if ((parameter != null) && (parameter.length() > 0)) {
				Writer writer = connection.getHttpResponse().getEntityWriter();
				writer.write(" - finished(");
				writer.write(parameter);
				writer.write(")");
				writer.flush();
			}
		}
	}

	/**
	 * Mock section for non-method link from the template.
	 */
	public static class MockLink {
		public void linked(ServerHttpConnection connection) throws IOException {
			Writer writer = connection.getHttpResponse().getEntityWriter();
			writer.write("LINKED");
			writer.flush();
		}
	}

}