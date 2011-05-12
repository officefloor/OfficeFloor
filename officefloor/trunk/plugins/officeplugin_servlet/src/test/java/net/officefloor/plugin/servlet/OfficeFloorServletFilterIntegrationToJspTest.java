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
package net.officefloor.plugin.servlet;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpApplicationStateful;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateful;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Ensure able to integrate to provide objects for JSP rendering by Servlet
 * container.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorServletFilterIntegrationToJspTest extends
		OfficeFrameTestCase {

	/**
	 * Port.
	 */
	private int port;

	/**
	 * {@link Server}.
	 */
	private Server server;

	/**
	 * {@link HttpClient}.
	 */
	private HttpClient client;

	/**
	 * Ensure able to render JSP from HTTP state objects.
	 */
	public void testJspStateIntegration() throws Exception {

		// Obtain the port for the application
		this.port = MockHttpServer.getAvailablePort();

		// Start servlet container with filter
		this.server = new Server(this.port);
		ServletContextHandler context = new ServletContextHandler();
		context.setBaseResource(Resource.newClassPathResource(this.getClass()
				.getPackage().getName().replace('.', '/')
				+ "/jsp"));
		context.setContextPath("/");
		context.setSessionHandler(new SessionHandler());
		this.server.setHandler(context);

		// Add the filter for handling requests
		context.addFilter(new FilterHolder(MockOfficeFloorServletFilter.class),
				"/*", FilterMapping.REQUEST);

		// Add the JSP
		context.addServlet(new ServletHolder(JspServlet.class), "*.jsp");

		// Add the servlet for testing (should initialise application object)
		ServletHolder servlet = new ServletHolder(MockHttpServlet.class);
		servlet.setInitOrder(1);
		context.addServlet(servlet, "/");

		// Start the server
		this.server.start();

		// Create client
		this.client = new DefaultHttpClient();

		// Ensure can invoke JSP directly (without initialising)
		this.assertHttpRequest("/Template.jsp", "INIT null null");

		// Invoke to use template submit to create state for JSP
		this.assertHttpRequest("/template.links-submit.task",
				"application session request");
	}

	@Override
	protected void tearDown() throws Exception {
		// Ensure stop client
		if (this.client != null) {
			this.client.getConnectionManager().shutdown();
		}

		// Ensure stop server
		if (this.server != null) {
			this.server.stop();
		}
	}

	/**
	 * Asserts the HTTP request.
	 * 
	 * @param uri
	 *            URI.
	 * @param expectedResponse
	 *            Expected response.
	 */
	private void assertHttpRequest(String uri, String expectedResponse)
			throws Exception {

		// Send request
		HttpResponse response = this.client.execute(new HttpGet(
				"http://localhost:" + this.port + uri));
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure appropriately integrated state for JSP
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String body = buffer.toString();
		assertEquals("Incorrect response", expectedResponse, body.trim());

	}

	/**
	 * {@link OfficeFloorServletFilter} for testing.
	 */
	public static class MockOfficeFloorServletFilter extends
			OfficeFloorServletFilter {
		@Override
		protected void configure() throws Exception {
			String templatePath = this.getClass().getPackage().getName()
					.replace('.', '/')
					+ "/jsp/SubmitTemplate.ofp";
			HttpTemplateAutoWireSection template = this.addHttpTemplate(
					templatePath, MockTemplateLogic.class, "template");
			this.linkToResource(template, "jsp", "Template.jsp");
		}
	}

	/**
	 * Template Logic for testing.
	 */
	public static class MockTemplateLogic {
		@NextTask("jsp")
		public void submit(MockApplicationObject application,
				MockSessionObject session, MockRequestObject request) {
			// Ensure pick up state from Servlet Container
			assertEquals(
					"Application object should be initialised from Servlet",
					"INIT", application.getText());

			// Specify state for JSP
			application.text = "application";
			session.text = "session";
			request.text = "request";
		}
	}

	/**
	 * {@link HttpApplicationState} object.
	 */
	@HttpApplicationStateful("ApplicationBean")
	public static class MockApplicationObject {
		public String text;

		public String getText() {
			return this.text;
		}
	}

	/**
	 * {@link HttpSession} object.
	 */
	@HttpSessionStateful("SessionBean")
	public static class MockSessionObject {
		public String text;

		public String getText() {
			return this.text;
		}
	}

	/**
	 * {@link HttpRequestState} object.
	 */
	@HttpRequestStateful("RequestBean")
	public static class MockRequestObject {
		public String text;

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Mock {@link HttpServlet} to initiate an application object.
	 */
	public static class MockHttpServlet extends HttpServlet {
		@Override
		public void init() throws ServletException {
			MockApplicationObject object = new MockApplicationObject();
			object.text = "INIT";
			this.getServletContext().setAttribute("ApplicationBean", object);
		}
	}

}