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
package net.officefloor.plugin.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpApplicationStateful;
import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateful;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Ensure able to integrate to provide objects for JSP rendering by Servlet
 * container.
 * 
 * @author Daniel Sagenschneider
 */
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
		this.context = new WebAppContext();
		this.context.setBaseResource(Resource.newClassPathResource(this
				.getClass().getPackage().getName().replace('.', '/')
				+ "/integrate"));
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
		this.context.setClassLoader(Thread.currentThread()
				.getContextClassLoader());

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
		this.assertHttpRequest("/template-submit.integrate",
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
		public String getTemplateUriSuffix() {
			return ".integrate";
		}

		@Override
		public boolean configure(WebAutoWireApplication application,
				ServletContext servletContext) throws Exception {

			// Should obtain template content from ServletContext
			final String templatePath = "JspTemplate.ofp";

			HttpTemplateAutoWireSection template = application.addHttpTemplate(
					"template", templatePath, MockTemplateLogic.class);
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
	public static class MockSessionObject implements Serializable {

		public String text;

		public String getText() {
			return this.text;
		}
	}

	/**
	 * {@link HttpRequestState} object.
	 */
	@HttpRequestStateful("RequestBean")
	public static class MockRequestObject implements Serializable {

		public String text;

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to retrieve HTTP template content.
	 */
	public void testTemplateIntegration() throws Exception {

		// Add the servlet for handling requests
		MockTemplateServlet.templateUri = "/template";
		this.context.addEventListener(new MockTemplateServlet());

		// Start the server
		this.server.start();

		// Ensure can obtain template content
		this.assertHttpRequest("/template.resource", "OfficeFloor Template");
	}

	/**
	 * Ensure able to retrieve resource from the {@link ServletContext}.
	 */
	public void testResourceIntegration_SameSuffix() throws Exception {

		// Add the servlet for handling requests
		MockTemplateServlet.templateUri = "/template";
		this.context.addEventListener(new MockTemplateServlet());

		// Start the server
		this.server.start();

		// Ensure can obtain resource from ServletContext
		this.assertHttpRequest("/resource.resource",
				"Servlet Container Resource");
	}

	/**
	 * Ensure able to retrieve HTTP template content from the
	 * {@link ServletContext}.
	 */
	public void testResourceIntegration_NonRootTemplate() throws Exception {

		// Add the servlet with template servicing root
		MockTemplateServlet.templateUri = "/non-root";
		this.context.addEventListener(new MockTemplateServlet());

		// Start the server
		this.server.start();

		// Ensure can obtain Servlet resource
		this.assertHttpRequest("/resource.html",
				"<html><body>Servlet Resource</body></html>");
	}

	/**
	 * Ensure able to retrieve HTTP template content from the
	 * {@link ServletContext}.
	 */
	public void testResourceIntegration_RootTemplate() throws Exception {

		// Add the servlet with template servicing root
		MockTemplateServlet.templateUri = "/";
		this.context.addEventListener(new MockTemplateServlet());

		// Start the server
		this.server.start();

		// Ensure can obtain Servlet resource
		this.assertHttpRequest("/resource.html",
				"<html><body>Servlet Resource</body></html>");
	}

	/**
	 * {@link OfficeFloorServlet} for testing integration of a
	 * {@link HttpTemplate}.
	 */
	public static class MockTemplateServlet extends OfficeFloorServlet {

		/**
		 * Template URI path.
		 */
		private static String templateUri;

		@Override
		public String getServletName() {
			return "MockTemplateIntegrate";
		}

		@Override
		public String getTemplateUriSuffix() {
			return "resource"; // '.' should be provided
		}

		@Override
		public boolean configure(WebAutoWireApplication application,
				ServletContext servletContext) throws Exception {

			// Should obtain template content from ServletContext
			final String templatePath = "ServletTemplate.ofp";

			// Add the template
			application.addHttpTemplate(templateUri, templatePath,
					MockTemplate.class);

			// Configure
			return true;
		}
	}

	/**
	 * Template for testing obtaining template content from the
	 * {@link ServletContext}.
	 */
	public static class MockTemplate {

		public MockTemplate getTemplate() {
			return this;
		}

		public String getText() {
			return "OfficeFloor Template";
		}
	}

	/**
	 * Ensure able to undertake POST/Redirect/GET pattern.
	 */
	public void testPostRedirectGetPattern() throws Exception {

		// Add the servlet for handling requests
		this.context.addEventListener(new PostRedirectGetServlet());

		// Start the server
		this.server.start();

		// Ensure can obtain template content from ServletContext
		HttpResponse response = this.client.execute(new HttpPost(
				"http://localhost:" + this.port
						+ "/template-post.redirect?parameter=TEST"));
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Validate content
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String body = buffer.toString();
		assertEquals(
				"Incorrect response",
				"POST - parameter=TEST, state=AVAILABLE, link=/template-post.redirect",
				body.trim());
	}

	/**
	 * {@link OfficeFloorServlet} to test POST/Redirect/GET pattern.
	 */
	public static class PostRedirectGetServlet extends OfficeFloorServlet {

		@Override
		public String getServletName() {
			return "PostRedirectGet";
		}

		@Override
		public String getTemplateUriSuffix() {
			return "redirect";
		}

		@Override
		public boolean configure(WebAutoWireApplication application,
				ServletContext servletContext) throws Exception {

			// Add the template
			application.addHttpTemplate("template", "PostRedirectGet.ofp",
					PostRedirectGetLogic.class);

			// Run
			return true;
		}
	}

	/**
	 * Logic for the POST/Redirect/Get pattern.
	 */
	public static class PostRedirectGetLogic {

		public void post(PostRedirectGetParameters parameters,
				ServerHttpConnection connection) throws IOException {

			// Provide initial content that should be saved across redirect
			connection.getHttpResponse().getEntityWriter().write("POST - ");

			// Provide value to parameters to be saved across redirect
			parameters.state = "AVAILABLE";
		}

		public PostRedirectGetParameters getTemplateData(
				PostRedirectGetParameters parameters) {
			return parameters;
		}
	}

	/**
	 * POST/Redirect/GET parameters.
	 */
	@HttpParameters
	public static class PostRedirectGetParameters implements Serializable {

		private String parameter;

		public void setParameter(String parameter) {
			this.parameter = parameter;
		}

		public String getParameter() {
			return this.parameter;
		}

		private String state;

		public String getState() {
			return this.state;
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
		int statusCode = response.getStatusLine().getStatusCode();

		// Ensure appropriately integrated state for JSP
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String body = buffer.toString();
		assertEquals("Incorrect response (response status " + statusCode
				+ ")", expectedResponse, body.trim());

		// Ensure also successful
		assertEquals("Should be successful", 200, statusCode);
	}

}