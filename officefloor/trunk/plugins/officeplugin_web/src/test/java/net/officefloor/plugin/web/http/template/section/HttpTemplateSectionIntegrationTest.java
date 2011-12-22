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

package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;

import net.officefloor.autowire.AutoWire;
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
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.source.HttpSessionManagedObjectSource;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteWorkSource;

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
	private static final String RENDERED_TEMPLATE_XML = "<html><body>Template Test:<table>"
			+ "<tr><td>Name</td><td>Description</td></tr>"
			+ "<tr><td>row</td><td>test row</td></tr></table>"
			+ "<form action=\"/SECTION.links-nextTask.task\"><input type=\"submit\"/></form>"
			+ "<form action=\"/SECTION.links-submit.task\"><input type=\"submit\"/></form>"
			+ "</body></html>";

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
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("");
		assertXmlEquals("Incorrect rendering", RENDERED_TEMPLATE_XML, rendering);
	}

	/**
	 * Ensure can handle submit to a link that has {@link NextTask} annotation
	 * for handling.
	 */
	public void testSubmitWithNextTask() throws Exception {

		// Start the server
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "nextTask - finished(NextTask)";

		// Ensure correctly renders template on submit
		this.assertHttpRequest("/SECTION.links-nextTask.task", RESPONSE);
	}

	/**
	 * Ensure default behaviour of #{link} method without a {@link NextTask}
	 * annotation is to render the template.
	 */
	public void testSubmitWithoutNextTask() throws Exception {

		// Start the server
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "<submit />" + RENDERED_TEMPLATE_XML;

		// Ensure correctly renders template on submit not invoking flow
		String response = this.doHttpRequest("/SECTION.links-submit.task");
		assertXmlEquals("Incorrect rendering", RESPONSE, response);
	}

	/**
	 * Ensure with {@link NextTask} annotation that invoking a Flow takes
	 * precedence.
	 */
	public void testSubmitInvokingFlow() throws Exception {

		// Start the server
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "<submit /> - doInternalFlow[1] - finished(Parameter for External Flow)";

		// Ensure correctly renders template on submit when invoking flow
		this.assertHttpRequest("/SECTION.links-submit.task?doFlow=true",
				RESPONSE);
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
		this.assertHttpRequest("",
				"<a href='/SECTION.links-increment.task'>1</a>");
		this.assertHttpRequest("/SECTION.links-increment.task",
				"increment - finished(2)");
		this.assertHttpRequest("",
				"<a href='/SECTION.links-increment.task'>2</a>");
		this.assertHttpRequest("/SECTION.links-increment.task",
				"increment - finished(3)");
		this.assertHttpRequest("",
				"<a href='/SECTION.links-increment.task'>3</a>");
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
		this.assertHttpRequest("/SECTION.links-submit.task",
				"Submit-RenderByDefault-/SECTION.links-submit.task");
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

		// Reset the mock extension for testing
		MockHttpTemplateSectionExtension.reset();

		// Start the server (with extension)
		this.startHttpServer("ExtensionTemplate.ofp",
				MockExtensionTemplateLogic.class, "extension.1",
				MockHttpTemplateSectionExtension.class.getName(),
				"extension.1.name", "value", "extension.2",
				MockHttpTemplateSectionExtension.class.getName(),
				"extension.2.name", "value", "section.name", "section.value");

		// Ensure change with extension
		this.assertHttpRequest("", "Overridden template with overridden class");
	}

	/**
	 * Mock {@link HttpTemplateSectionExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements
			HttpTemplateSectionExtension {

		/**
		 * Indicates if first.
		 */
		private static int extensionIndex = 1;

		/**
		 * Resets for testing.
		 */
		public static void reset() {
			extensionIndex = 1;
		}

		/*
		 * ================== HttpTemplateSectionExtension ====================
		 */

		@Override
		public void extendTemplate(HttpTemplateSectionExtensionContext context)
				throws Exception {

			final String TEMPLATE_CONTENT = "Overridden template with ${property}";

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
			assertEquals("Incorrect number of properties", 1, names.length);
			assertEquals("Incorrect property name", "name", names[0]);
			assertEquals("Incorrect property value", "value",
					context.getProperty("name"));
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

			// Increment for next extension use
			extensionIndex++;
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
		HttpGet request = new HttpGet("http://localhost:" + this.port + uri);
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

		// Link service Task name prefix
		final String LINK_SERVICE_TASK_NAME_PREFIX = "LINK_";

		// Provide HTTP template router for testing
		AutoWireSection routeSection = source.addSection("ROUTE",
				WorkSectionSource.class.getName(),
				HttpTemplateRouteWorkSource.class.getName());
		routeSection.addProperty(
				HttpTemplateRouteWorkSource.PROPERTY_TASK_NAME_PREFIX,
				LINK_SERVICE_TASK_NAME_PREFIX);

		// Load the template section
		final String templateLocation = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/" + templateName;
		AutoWireSection templateSection = source.addSection("SECTION",
				HttpTemplateSectionSource.class.getName(), templateLocation);
		templateSection.addProperty(
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				logicClass.getName());
		templateSection.addProperty(
				HttpTemplateSectionSource.PROPERTY_LINK_TASK_NAME_PREFIX,
				LINK_SERVICE_TASK_NAME_PREFIX);
		for (int i = 0; i < templatePropertyPairs.length; i += 2) {
			String name = templatePropertyPairs[i];
			String value = templatePropertyPairs[i + 1];
			templateSection.addProperty(name, value);
		}

		// Load mock section for handling outputs
		AutoWireSection handleOutputSection = source
				.addSection("OUTPUT", ClassSectionSource.class.getName(),
						MockSection.class.getName());

		// Link flow outputs
		source.link(routeSection, "NON_MATCHED_REQUEST", templateSection,
				"renderTemplate");
		source.link(templateSection, "output", handleOutputSection, "finished");
		source.link(templateSection, "doExternalFlow", handleOutputSection,
				"finished");

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
		Writer writer = new OutputStreamWriter(connection.getHttpResponse()
				.getBody().getOutputStream());
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
				Writer writer = new OutputStreamWriter(connection
						.getHttpResponse().getBody().getOutputStream());
				writer.write(" - finished(");
				writer.write(parameter);
				writer.write(")");
				writer.flush();
			}
		}
	}

}