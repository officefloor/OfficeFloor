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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.template.HttpSessionStateful;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Tests the {@link OfficeFloorServletFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorServletFilterTest extends OfficeFrameTestCase {

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
	 * {@link HttpClient}.
	 */
	private HttpClient client;

	/**
	 * {@link OfficeFloorServletFilter} to be tested.
	 */
	private MockOfficeFloorServletFilter filter;

	@Override
	protected void setUp() throws Exception {

		// Obtain location of template path
		final String templateDirectory = this.getClass().getPackage().getName()
				.replace('.', '/');

		// Create the Filter to test
		this.filter = new MockOfficeFloorServletFilter(templateDirectory);

		// Inject the EJB
		this.filter.ejb = new MockEjb();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.stop();
		}
		if (this.client != null) {
			this.client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Ensure can call {@link Filter} and {@link Servlet}.
	 */
	public void testCallServlet() throws Exception {

		// Start mock filter to ensure invoking container is valid
		this.startServer(new Filter() {
			@Override
			public void init(FilterConfig arg0) throws ServletException {
			}

			@Override
			public void doFilter(ServletRequest request,
					ServletResponse response, FilterChain chain)
					throws IOException, ServletException {
				// Determine if filter request
				if ("filter".equals(request.getParameter("type"))) {
					response.getWriter().write("FILTER");
					return;
				}

				// Handle by servlet
				chain.doFilter(request, response);
			}

			@Override
			public void destroy() {
			}
		});

		// Send request for filter
		assertEquals("Incorrect filter handling", "FILTER",
				this.doGetBody("?type=filter"));

		// Send request for servlet
		assertEquals("Incorrect servlet handling", "SERVLET",
				this.doGetBody(""));
	}

	/**
	 * Ensure pass onto {@link Servlet} if not handled.
	 */
	public void testNotHandle() throws Exception {
		assertEquals("Should pass onto servlet", "SERVLET",
				this.doGetBody("/unhandled"));
	}

	/**
	 * Ensure pass onto {@link Servlet} for unhandled task.
	 */
	public void testNonHandledTask() throws Exception {
		assertEquals("Should pass onto servlet", "SERVLET",
				this.doGetBody("/unhandled.links/unhandled.task"));
	}

	/**
	 * Ensure can service from public template.
	 */
	public void testPublicTemplate() throws Exception {
		assertEquals("Should be handled by template", "TEMPLATE",
				this.doGetBody("/test"));
	}

	/**
	 * Ensure remembers state within the {@link HttpSession}.
	 */
	public void testSessionTemplate() throws Exception {
		this.doGetBody("/session?name=value");
		String value = this.doGetBody("/session");
		assertEquals("Should maintain state between requests", "value", value);
	}

	/**
	 * Ensure can invoke a link.
	 */
	public void testlink() throws Exception {
		assertEquals("Should handle link", "LINK - /link.links/link.task",
				this.doGetBody("/link.links/link.task"));
	}

	/**
	 * Ensure can link to section.
	 */
	public void testLinkUriToSection() throws Exception {
		assertEquals("Should be handled by section", "SECTION",
				this.doGetBody("/section"));
	}

	/**
	 * Ensure can link to {@link Servlet} resource.
	 */
	public void testLinkToServletResource() throws Exception {
		assertEquals("Should provide servlet resource", "SERVLET_RESOURCE",
				this.doGetBody("/servlet-resource"));
	}

	/**
	 * Ensure can utilise the object (such as EJB).
	 */
	public void testObject() throws Exception {
		this.filter.ejb.value = "TEST";
		assertEquals("Should obtain EJB value", "TEST", this.doGetBody("/ejb"));
	}

	/**
	 * Ensures the context path is stripped off the request URI.
	 */
	public void testStripOffContextPath() throws Exception {
		this.contextPath = "/path";
		assertEquals("Should be handled by template", "TEMPLATE",
				this.doGetBody("/path/test"));
	}

	/**
	 * Executes the {@link HttpGet} for the URI returning the text content of
	 * the body.
	 * 
	 * @param uri
	 *            URI.
	 * @return Text content of the body.
	 */
	private String doGetBody(String uri) throws Exception {
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
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());
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
			this.client = new DefaultHttpClient();
		}
		return this.client;
	}

	/**
	 * Starts the {@link Server}.
	 */
	protected void startSever() throws Exception {
		this.startServer(this.filter);
	}

	/**
	 * Starts the {@link Server} for the {@link Filter}.
	 * 
	 * @param filter
	 *            {@link Filter}.
	 */
	protected void startServer(Filter filter) throws Exception {

		// Obtain the port for the application
		this.port = MockHttpServer.getAvailablePort();

		// Start servlet container with filter
		this.server = new Server(this.port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath(this.contextPath);
		context.setSessionHandler(new SessionHandler());
		this.server.setHandler(context);

		// Add the filter for handling requests
		context.addFilter(new FilterHolder(filter), "/*", FilterMapping.REQUEST);

		// Add the linked Servlet Resource
		context.addServlet(new ServletHolder(new MockHttpServlet(
				"SERVLET_RESOURCE")), "/Template.jsp");

		// Add the servlet for testing
		context.addServlet(new ServletHolder(new MockHttpServlet("SERVLET")),
				"/");

		// Start the server
		this.server.start();
	}

	/**
	 * Mock EJB.
	 */
	public static class MockEjb {
		public String value;
	}

	/**
	 * Mock {@link OfficeFloorServletFilter} for testing.
	 */
	public static class MockOfficeFloorServletFilter extends
			OfficeFloorServletFilter {

		/**
		 * Template directory.
		 */
		private final String templateDirectory;

		/**
		 * Dependency injected EJB.
		 */
		@EJB
		private MockEjb ejb;

		/**
		 * Initiate.
		 * 
		 * @param templateDirectory
		 *            Template directory.
		 */
		public MockOfficeFloorServletFilter(String templateDirectory) {
			this.templateDirectory = templateDirectory;
		}

		/*
		 * ================= OfficeFloorServletFilter =====================
		 */

		@Override
		protected void configure() {

			// HTTP template
			this.addHttpTemplate(this.templateDirectory + "/Template.ofp",
					MockTemplate.class, "test");

			// Session stateful template
			this.addHttpTemplate(this.templateDirectory
					+ "/SessionTemplate.ofp", MockSessionTemplate.class,
					"session");

			// Link template
			this.addHttpTemplate(this.templateDirectory + "/LinkTemplate.ofp",
					MockLinkTemplate.class, "link");

			// Link to section
			AutoWireSection section = this.addSection("SECTION",
					ClassSectionSource.class, MockSection.class.getName());
			this.linkUri("section", section, "doSection");

			// Link to Servlet resource
			this.linkUri("servlet-resource", section, "doServletResource");
			this.linkToServletResource(section, "resource", "Template.jsp");

			// Enable access to EJB of filter
			AutoWireSection ejb = this.addSection("EJB",
					ClassSectionSource.class, MockEjbSection.class.getName());
			this.linkUri("ejb", ejb, "doEjb");
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

		public MockSessionTemplate getTemplate(ServerHttpConnection connection)
				throws IOException {

			// Overwrite the cached value (if have value)
			String inputValue = HttpRequestTokeniserImpl.extractParameters(
					connection.getHttpRequest()).get("name");
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
			Writer writer = new OutputStreamWriter(connection.getHttpResponse()
					.getBody().getOutputStream());
			writer.write("LINK - ");
			writer.flush();
		}
	}

	/**
	 * Mock section class.
	 */
	public static class MockSection {
		public void doSection(ServerHttpConnection connection)
				throws IOException {
			Writer writer = new OutputStreamWriter(connection.getHttpResponse()
					.getBody().getOutputStream());
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
		public void doEjb(MockEjb ejb, ServerHttpConnection connection)
				throws IOException {
			Writer writer = new OutputStreamWriter(connection.getHttpResponse()
					.getBody().getOutputStream());
			writer.write(ejb.value);
			writer.flush();
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
		protected void service(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			response.getWriter().write(this.response);
		}
	}

}