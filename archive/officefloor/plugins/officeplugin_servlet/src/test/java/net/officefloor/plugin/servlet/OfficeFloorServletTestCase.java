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
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import javax.ejb.EJB;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;

/**
 * Tests for the {@link OfficeFloorServlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorServletTestCase extends OfficeFrameTestCase {

	/**
	 * Port.
	 */
	private int port;

	/**
	 * {@link Server}.
	 */
	private Server server;

	/**
	 * Context path.
	 */
	private String contextPath = "/";

	/**
	 * {@link CloseableHttpClient}.
	 */
	private CloseableHttpClient client;

	/**
	 * {@link OfficeFloorServlet} as {@link ServletContextListener}.
	 */
	private MockOfficeFloorServlet listener;

	/**
	 * Directory containing the templates.
	 */
	private static String templateDirectory;

	/**
	 * {@link MockEjb} to inject.
	 */
	private static MockEjb ejb;

	/**
	 * {@link OfficeFloorServlet} as instance servicing
	 * {@link HttpServletRequest}.
	 */
	private static MockOfficeFloorServlet servlet;

	@Override
	protected void setUp() throws Exception {

		// Obtain location of template path
		templateDirectory = this.getClass().getPackage().getName().replace('.', '/');

		// Create the listener to configure the Servlet
		this.listener = new MockOfficeFloorServlet();

		// Configure the EJB to inject
		ejb = new MockEjb();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.stop();
		}
		if (this.client != null) {
			this.client.close();
		}
	}

	/**
	 * Ensure pass onto {@link Servlet} if not handled.
	 */
	public void testNotHandle() throws Exception {
		assertEquals("Should pass onto servlet", "UNHANDLED", this.doGetEntity("/unhandled"));
	}

	/**
	 * Ensure more specific link URI is handled by configured {@link Servlet}.
	 */
	public void testNonHandledTask() throws Exception {
		assertEquals("Should pass onto servlet", "UNHANDLED_LINK", this.doGetEntity("/unhandled-link.woof"));
	}

	/**
	 * Ensure can service from public template.
	 */
	public void testPublicTemplate() throws Exception {
		assertEquals("Should be handled by template", "TEMPLATE", this.doGetEntity("/test.suffix"));
	}

	/**
	 * Ensures the context path is handled for the request URI.
	 */
	public void testServiceWithinContext() throws Exception {
		this.contextPath = "/path";
		assertEquals("Should be handled by template", "TEMPLATE", this.doGetEntity("/path/test.suffix"));
	}

	/**
	 * Ensure remembers state within the {@link HttpSession}.
	 */
	public void testSessionTemplate() throws Exception {
		this.doGetEntity("/session.suffix?name=value");
		String value = this.doGetEntity("/session.suffix");
		assertEquals("Should maintain state between requests", "value", value);
	}

	/**
	 * Ensure renders link.
	 */
	public void testLinkRendering() throws Exception {
		assertEquals("Incorrect rendered link", "/link-link.suffix", this.doGetEntity("/link.suffix"));
	}

	/**
	 * Ensure renders link with context path.
	 */
	public void testLinkRenderingWithinContext() throws Exception {
		this.contextPath = "/path";
		assertEquals("Incorrect rendered link with context path", "/path/link-link.suffix",
				this.doGetEntity("/path/link.suffix"));
	}

	/**
	 * Ensure can invoke a link.
	 */
	public void testlink() throws Exception {
		assertEquals("Should handle link", "LINK - /link-link.suffix", this.doGetEntity("/link-link.suffix"));
	}

	/**
	 * Ensure can invoke link with a {@link ServletContext} path.
	 */
	public void testLinkWithinContext() throws Exception {
		this.contextPath = "/path";
		String entity = this.doGetEntity("/path/link-link.suffix");
		assertEquals("Should handle link with context path", "LINK - /path/link-link.suffix", entity);
	}

	/**
	 * Ensure can link to section.
	 */
	public void testLinkUriToSection() throws Exception {
		assertEquals("Should be handled by section", "SECTION", this.doGetEntity("/section"));
	}

	/**
	 * Ensure can link to section within a {@link ServletContext} path.
	 */
	public void testLinkUriToSectionWithinContext() throws Exception {
		this.contextPath = "/path";
		assertEquals("Should be handled by section", "SECTION", this.doGetEntity("/path/section"));
	}

	/**
	 * Ensure handle {@link Escalation} with {@link Servlet} resource.
	 */
	public void testResourceHandlingEscalation() throws Exception {
		assertEquals("Should escalate and be handled by resource", "SERVLET_RESOURCE", this.doGetEntity("/fail"));
	}

	/**
	 * Ensure can link to {@link Servlet} resource.
	 */
	public void testLinkToResource() throws Exception {
		assertEquals("Should provide servlet resource", "SERVLET_RESOURCE", this.doGetEntity("/servlet-resource"));
	}

	/**
	 * Ensure can link to {@link Servlet} resource within {@link ServletContext}
	 * path.
	 */
	public void testLinkToResourceWithinContext() throws Exception {
		this.contextPath = "/path";
		assertEquals("Should provide servlet resource", "SERVLET_RESOURCE", this.doGetEntity("/path/servlet-resource"));
	}

	/**
	 * Ensure can utilise the object (such as EJB).
	 */
	public void testObject() throws Exception {
		ejb.value = "TEST";
		assertEquals("Should obtain EJB value", "TEST", this.doGetEntity("/ejb"));
	}

	/**
	 * Executes the {@link HttpGet} for the URI returning the text content of
	 * the entity.
	 * 
	 * @param uri
	 *            URI.
	 * @return Text content of the entity.
	 */
	private String doGetEntity(String uri) throws Exception {
		HttpResponse response = this.doGet(uri);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		return buffer.toString();
	}

	/**
	 * Executes the {@link HttpGet} for the URI.
	 * 
	 * @param uri
	 *            URI.
	 * @return {@link HttpResponse}.
	 */
	private HttpResponse doGet(String uri) throws Exception {
		HttpClient client = this.getHttpClient();
		HttpGet request = new HttpGet("http://localhost:" + this.port + uri);
		HttpResponse response = client.execute(request);
		assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());
		return response;
	}

	/**
	 * Obtains the {@link HttpClient}.
	 * 
	 * @return {@link HttpClient}.
	 */
	private HttpClient getHttpClient() throws Exception {

		// Ensure server started
		if (this.server == null) {
			this.startSever();
		}

		// Lazy create the client
		if (this.client == null) {
			this.client = HttpTestUtil.createHttpClient();
		}
		return this.client;
	}

	/**
	 * Starts the {@link Server}.
	 */
	protected void startSever() throws Exception {

		// Clear the Servlet instance
		servlet = null;

		// Obtain the port for the application
		this.port = HttpTestUtil.getAvailablePort();

		// Start servlet container with servlet
		this.server = new Server(this.port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath(this.contextPath);
		context.setSessionHandler(new SessionHandler());
		this.server.setHandler(context);

		// Add the linked Servlet Resource
		context.addServlet(new ServletHolder(new MockHttpServlet("SERVLET_RESOURCE")), "/Template.jsp");

		// Add the servlets for testing
		context.addServlet(new ServletHolder(new MockHttpServlet("UNHANDLED")), "/unhandled");
		context.addServlet(new ServletHolder(new MockHttpServlet("UNHANDLED_LINK")), "/unhandled-link.woof");

		// Configure the Servlet
		context.addEventListener(this.listener);

		// Start the server
		this.server.start();

		// Ensure different Servlet instance
		assertNotNull("Should have initiated Servlet instance", servlet);
		assertNotSame("Servlet instance should be different to listener instance", this.listener, servlet);

		// Ensure EJB injected (for valid tests)
		assertNotNull("EJB should be injected", servlet.ejb);
	}

	/**
	 * Mock EJB.
	 */
	public static class MockEjb {
		public String value;
	}

	/**
	 * Mock {@link OfficeFloorServlet} for testing.
	 */
	public static class MockOfficeFloorServlet extends OfficeFloorServlet {

		/**
		 * Dependency injected EJB.
		 */
		@EJB
		private MockEjb ejb;

		/*
		 * ================= OfficeFloorServlet =====================
		 */

		@Override
		public String getServletName() {
			return "Mock";
		}

		@Override
		public String getTemplateUriSuffix() {
			return "suffix";
		}

		@Override
		public boolean configure(WebArchitect application, ServletContext servletContext) throws Exception {

			// Obtain the template directory
			String templateDirectory = OfficeFloorServletTestCase.templateDirectory;

			// HTTP template
			application.addHttpTemplate("test", templateDirectory + "/Template.ofp", MockTemplate.class);

			// Session stateful template
			application.addHttpTemplate("session", templateDirectory + "/SessionTemplate.ofp",
					MockSessionTemplate.class);

			// Link template
			application.addHttpTemplate("link", templateDirectory + "/LinkTemplate.ofp", MockLinkTemplate.class);

			// Link to section
			AutoWireSection section = application.addSection("SECTION", ClassSectionSource.class.getName(),
					MockSection.class.getName());
			application.linkUri("section", section, "doSection");

			// Link to Servlet resource
			application.linkUri("servlet-resource", section, "doServletResource");
			application.linkToResource(section, "resource", "Template.jsp");

			// Enable access to EJB of servlet
			AutoWireSection ejb = application.addSection("EJB", ClassSectionSource.class.getName(),
					MockEjbSection.class.getName());
			application.linkUri("ejb", ejb, "doEjb");

			// Enable escalation handling to resource
			AutoWireSection failSection = application.addSection("FAILURE", ClassSectionSource.class.getName(),
					MockFailureSection.class.getName());
			application.linkUri("fail", failSection, "task");
			application.linkEscalation(IOException.class, "Template.jsp");

			// Configure
			return true;
		}

		@Override
		public void init() throws ServletException {

			// Initialise
			super.init();

			// Flag the instance being used
			OfficeFloorServletTestCase.servlet = this;

			// Mimick EJB injection (will be done by JEE container)
			this.ejb = OfficeFloorServletTestCase.ejb;
		}
	}

	/**
	 * Mock template.
	 */
	public static class MockTemplate {
		public void doTask() {
		}
	}

	/**
	 * Mock {@link HttpSessionStateful} template.
	 */
	@HttpSessionStateful
	public static class MockSessionTemplate implements Serializable {

		private String value = "";

		public MockSessionTemplate getTemplate(ServerHttpConnection connection) throws IOException {

			// Overwrite the cached value (if have value)
			String inputValue = HttpRequestTokeniserImpl.extractParameters(connection.getHttpRequest()).get("name");
			this.value = (inputValue == null ? this.value : inputValue);

			// Return this to obtain value
			return this;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * Mock link template.
	 */
	public static class MockLinkTemplate {
		public void link(ServerHttpConnection connection) throws IOException {
			Writer writer = new OutputStreamWriter(connection.getHttpResponse().getEntity());
			writer.write("LINK - ");
			writer.flush();
		}
	}

	/**
	 * Mock section class.
	 */
	public static class MockSection {
		public void doSection(ServerHttpConnection connection) throws IOException {
			Writer writer = connection.getHttpResponse().getEntityWriter();
			writer.write("SECTION");
			writer.flush();
		}

		@NextTask("resource")
		public void doServletResource() {
		}
	}

	/**
	 * Mock EJB section.
	 */
	public static class MockEjbSection {
		public void doEjb(MockEjb ejb, ServerHttpConnection connection) throws IOException {
			Writer writer = connection.getHttpResponse().getEntityWriter();
			writer.write(ejb.value);
			writer.flush();
		}
	}

	/**
	 * Mock failure section.
	 */
	public static class MockFailureSection {
		public void task(ServerHttpConnection connection) throws Exception {

			// Content should not appear as reset on resource dispatch
			connection.getHttpResponse().getEntityWriter().write("ESCALTION - ");

			// Fail
			throw new IOException("Test failure");
		}
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	private class MockHttpServlet extends HttpServlet {

		/**
		 * Response content.
		 */
		private final String response;

		/**
		 * Initiate.
		 * 
		 * @param response
		 *            Response content.
		 */
		public MockHttpServlet(String response) {
			this.response = response;
		}

		/*
		 * ================ HttpServlet =====================
		 */

		@Override
		protected void service(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			response.getWriter().write(this.response);
		}
	}

}