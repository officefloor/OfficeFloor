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

package net.officefloor.plugin.servlet;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpApplicationStateful;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateful;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.Ignore;

/**
 * Ensure able to integrate to provide objects for JSP rendering by Servlet
 * container.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO provide link suffix for HttpTemplateSectionSource to allow Servlet mapping")
public class OfficeFloorServletIntegrationToContainerTest extends
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
	 * {@link ServletContextHandler}.
	 */
	private ServletContextHandler context;

	/**
	 * {@link HttpClient}.
	 */
	private HttpClient client;

	@Override
	protected void setUp() throws Exception {

		// Obtain the port for the application
		this.port = MockHttpServer.getAvailablePort();

		// Start servlet container with servlet
		this.server = new Server(this.port);
		this.context = new ServletContextHandler();
		this.context.setBaseResource(Resource.newClassPathResource(this
				.getClass().getPackage().getName().replace('.', '/')
				+ "/jsp"));
		this.context.setContextPath("/");
		this.context.setSessionHandler(new SessionHandler());
		this.server.setHandler(this.context);

		// Create client
		this.client = new DefaultHttpClient();
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
	 * Ensure able to render JSP from HTTP state objects.
	 */
	public void testJspStateIntegration() throws Exception {

		// Add the servlet for handling requests
		this.context.addEventListener(new MockJspIntergateServlet());

		// Add the JSP
		this.context.addServlet(new ServletHolder(JspServlet.class), "*.jsp");

		// Add listener to initialise application object
		this.context.addEventListener(new ServletContextListener() {
			@Override
			public void contextInitialized(ServletContextEvent event) {
				// Load the application object
				MockApplicationObject object = new MockApplicationObject();
				object.text = "INIT";
				event.getServletContext().setAttribute("ApplicationBean",
						object);
			}

			@Override
			public void contextDestroyed(ServletContextEvent event) {
				// Do nothing
			}
		});

		// Start the server
		this.server.start();

		// Ensure can invoke JSP directly (without initialising)
		this.assertHttpRequest("/Template.jsp", "INIT null null");

		// Invoke to use template submit to create state for JSP
		this.assertHttpRequest("/template.links-submit.task",
				"application session request");
	}

	/**
	 * {@link OfficeFloorServlet} for testing JSP HTTP state integration.
	 */
	public static class MockJspIntergateServlet extends OfficeFloorServlet {

		@Override
		public String getServletName() {
			return "MockJspIntegrate";
		}

		@Override
		public boolean configure(WebAutoWireApplication application,
				ServletContext servletContext) throws Exception {
			String templatePath = this.getClass().getPackage().getName()
					.replace('.', '/')
					+ "/jsp/SubmitTemplate.ofp";
			HttpTemplateAutoWireSection template = application.addHttpTemplate(
					templatePath, MockTemplateLogic.class, "template");
			application.linkToResource(template, "jsp", "Template.jsp");
			return true;
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
	 * Ensure able to retrieve HTTP template content from the
	 * {@link ServletContext}.
	 */
	public void testServletContextResourceIntegration() throws Exception {

		// Add the servlet for handling requests
		this.context.addEventListener(new MockServletContextResourceServlet());

		// Start the server
		this.server.start();

		// Ensure can obtain template content from ServletContext
		this.assertHttpRequest("/template", "ServletContext Resource");
	}

	/**
	 * {@link OfficeFloorServlet} for testing {@link ServletContext} resource
	 * for template content.
	 */
	public static class MockServletContextResourceServlet extends
			OfficeFloorServlet {

		@Override
		public String getServletName() {
			return "MockResourceIntegrate";
		}

		@Override
		public boolean configure(WebAutoWireApplication application,
				ServletContext servletContext) throws Exception {

			// Should obtain template content from ServletContext
			final String templatePath = "ServletContextResourceTemplate.ofp";

			// Add the template
			application.addHttpTemplate(templatePath,
					MockServletContextResourceTemplate.class, "template");

			// Configure
			return true;
		}
	}

	/**
	 * Template for testing obtaining template content from the
	 * {@link ServletContext}.
	 */
	public static class MockServletContextResourceTemplate {

		public MockServletContextResourceTemplate getTemplate() {
			return this;
		}

		public String getText() {
			return "ServletContext Resource";
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

}