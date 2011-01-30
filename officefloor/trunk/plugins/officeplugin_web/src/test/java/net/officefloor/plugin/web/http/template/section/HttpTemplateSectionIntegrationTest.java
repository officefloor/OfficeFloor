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

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.autowire.AutoWireSection;
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
			+ "<form action=\"/SECTION.links/nextTask.task\"><input type=\"submit\"/></form>"
			+ "<form action=\"/SECTION.links/submit.task\"><input type=\"submit\"/></form>"
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
		this.assertHttpRequest("/SECTION.links/nextTask.task", RESPONSE);
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
		String response = this.doHttpRequest("/SECTION.links/submit.task");
		assertXmlEquals("Incorrect rendering", RESPONSE, response);
	}

	/**
	 * Ensure with {@link NextTask} annotation that invoking a {@link Flow}
	 * takes precedence.
	 */
	public void testSubmitInvokingFlow() throws Exception {

		// Start the server
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "<submit /> - doInternalFlow[1] - finished(Parameter for External Flow)";

		// Ensure correctly renders template on submit when invoking flow
		this.assertHttpRequest("/SECTION.links/submit.task?doFlow=true",
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
	 * Ensure stateful across {@link HttpRequest}.
	 */
	public void testStatefulTemplate() throws Exception {

		// Start the server
		this.startHttpServer("StatefulTemplate.ofp",
				StatefulTemplateLogic.class);

		// Ensure retains state across HTTP requests (by incrementing counter)
		this.assertHttpRequest("",
				"<a href='/SECTION.links/increment.task'>1</a>");
		this.assertHttpRequest("/SECTION.links/increment.task",
				"increment - finished(2)");
		this.assertHttpRequest("",
				"<a href='/SECTION.links/increment.task'>2</a>");
		this.assertHttpRequest("/SECTION.links/increment.task",
				"increment - finished(3)");
		this.assertHttpRequest("",
				"<a href='/SECTION.links/increment.task'>3</a>");
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
	 */
	protected void startHttpServer(String templateName, Class<?> logicClass)
			throws Exception {

		// Auto-wire for testing
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();

		// Add the HTTP server socket listener
		this.port = MockHttpServer.getAvailablePort();
		HttpServerSocketManagedObjectSource.autoWire(source, this.port,
				"ROUTE", "route");

		// Add dependencies
		source.addObject(this.connection, Connection.class);
		source.addManagedObject(HttpSessionManagedObjectSource.class, null,
				HttpSession.class).setTimeout(10 * 1000);

		// Provide HTTP template router for testing
		AutoWireSection routeSection = source.addSection("ROUTE",
				WorkSectionSource.class,
				HttpTemplateRouteWorkSource.class.getName());

		// Load the template section
		final String templateLocation = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/" + templateName;
		AutoWireSection templateSection = source.addSection("SECTION",
				HttpTemplateSectionSource.class, templateLocation);
		templateSection.addProperty(
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				logicClass.getName());

		// Load mock section for handling outputs
		AutoWireSection handleOutputSection = source.addSection("OUTPUT",
				ClassSectionSource.class, MockSection.class.getName());

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