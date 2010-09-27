/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container.integrate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.servlet.container.source.HttpServletTask;
import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.servlet.container.source.RequestAttributesManagedObjectSource;
import net.officefloor.plugin.servlet.container.source.ServletContextManagedObjectSource;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityManagedObjectSource;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityServiceManagedObjectSource;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityManagedObjectSource.FlowKeys;
import net.officefloor.plugin.socket.server.http.security.scheme.BasicHttpSecuritySource;
import net.officefloor.plugin.socket.server.http.security.store.PasswordFileManagedObjectSource;
import net.officefloor.plugin.socket.server.http.server.HttpServicerTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.source.HttpSessionManagedObjectSource;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests integration of {@link HttpServletWorkSource} with dependencies to
 * service a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletIntegrateTest extends MockHttpServer {

	@Override
	@SuppressWarnings("unchecked")
	public HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {

		final long TIMEOUT = 10000; // 100 seconds (for debugging)

		// Obtain Office Name
		String officeName = this.getOfficeName();

		// HttpServlet
		WorkType<HttpServletTask> servlet = WorkLoaderUtil.loadWorkType(
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "Servlet",
				HttpServletWorkSource.PROPERTY_SERVLET_PATH, "/servlet/path",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				MockHttpServlet.class.getName());
		this.constructWork("Servlet", servlet.getWorkFactory());
		TaskBuilder<HttpServletTask, DependencyKeys, None> service = (TaskBuilder<HttpServletTask, DependencyKeys, None>) this
				.constructTask("service", servlet.getTaskTypes()[0]
						.getTaskFactory(), "Servicer");
		service.linkManagedObject(DependencyKeys.SERVLET_CONTEXT,
				"ServletContext", ServletContext.class);
		service.linkManagedObject(DependencyKeys.HTTP_CONNECTION,
				managedObjectName, ServerHttpConnection.class);
		service.linkManagedObject(DependencyKeys.REQUEST_ATTRIBUTES,
				"RequestAttributes", Map.class);
		service.linkManagedObject(DependencyKeys.HTTP_SESSION, "HttpSession",
				HttpSession.class);
		service.linkManagedObject(DependencyKeys.HTTP_SECURITY, "HttpSecurity",
				HttpSecurity.class);

		// ServletContext
		ManagedObjectBuilder<None> servletContext = this
				.constructManagedObject("ServletContext",
						ServletContextManagedObjectSource.class);
		servletContext.addProperty(
				ServletContextManagedObjectSource.PROPERTY_SERVER_NAME,
				"localhost");
		servletContext
				.addProperty(
						ServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						"ServletContext");
		servletContext.addProperty(
				ServletContextManagedObjectSource.PROPERTY_CONTEXT_PATH,
				"/context/path");
		servletContext.addProperty(
				ServletContextManagedObjectSource.PROPERTY_RESOURCE_PATH_ROOT,
				new File(".").getAbsolutePath());
		servletContext.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject("ServletContext",
				"ServletContext");

		// Request Attributes
		ManagedObjectBuilder<None> requestAttributes = this
				.constructManagedObject("RequestAttributes",
						RequestAttributesManagedObjectSource.class);
		requestAttributes.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject("RequestAttributes",
				"RequestAttributes");

		// HTTP Session
		ManagedObjectBuilder<Indexed> httpSession = this
				.constructManagedObject("HttpSession",
						HttpSessionManagedObjectSource.class);
		httpSession.setManagingOffice(officeName);
		httpSession.setTimeout(TIMEOUT);
		DependencyMappingBuilder httpSessionDependencies = this
				.getOfficeBuilder().addProcessManagedObject("HttpSession",
						"HttpSession");
		httpSessionDependencies.mapDependency(0, managedObjectName);

		// HTTP Security
		ManagedObjectBuilder<FlowKeys> httpSecurity = this
				.constructManagedObject("HttpSecurity",
						HttpSecurityManagedObjectSource.class);
		ManagingOfficeBuilder<FlowKeys> httpSecurityOffice = httpSecurity
				.setManagingOffice(officeName);
		httpSecurity.setTimeout(TIMEOUT);
		httpSecurityOffice
				.setInputManagedObjectName("InputHttpSecurity")
				.mapDependency(
						net.officefloor.plugin.socket.server.http.security.HttpSecurityManagedObjectSource.DependencyKeys.HTTP_SECURITY_SERVICE,
						"HttpSecurityService");
		DependencyMappingBuilder httpSecurityDependencies = this
				.getOfficeBuilder().addProcessManagedObject("HttpSecurity",
						"HttpSecurity");
		httpSecurityDependencies
				.mapDependency(
						net.officefloor.plugin.socket.server.http.security.HttpSecurityManagedObjectSource.DependencyKeys.HTTP_SECURITY_SERVICE,
						"HttpSecurityService");

		// HTTP Security Service
		ManagedObjectBuilder<?> httpSecurityService = this
				.constructManagedObject("HttpSecurityService",
						HttpSecurityServiceManagedObjectSource.class);
		httpSecurityService
				.addProperty(
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						HttpSecurityServiceManagedObjectSource.BASIC_AUTHENTICATION_SCHEME);
		httpSecurityService.addProperty(BasicHttpSecuritySource.PROPERTY_REALM,
				"TestRealm");
		httpSecurityService.setManagingOffice(officeName);
		DependencyMappingBuilder httpSecurityServiceDependencies = this
				.getOfficeBuilder().addProcessManagedObject(
						"HttpSecurityService", "HttpSecurityService");
		httpSecurityServiceDependencies.mapDependency(0, managedObjectName);
		httpSecurityServiceDependencies.mapDependency(1, "HttpSession");
		httpSecurityServiceDependencies.mapDependency(2, "CredentialStore");

		// Credential Store
		File passwordFile = this.findFile(this.getClass(), "password-file.txt");
		ManagedObjectBuilder<?> credentialStore = this.constructManagedObject(
				"CredentialStore", PasswordFileManagedObjectSource.class);
		credentialStore.addProperty(
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH,
				passwordFile.getAbsolutePath());
		credentialStore.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject("CredentialStore",
				"CredentialStore");

		// Servicing team
		this.constructTeam("Servicer", OnePersonTeamSource.class);
		this.constructTeam("of-HttpSecurity.AUTHENTICATOR",
				OnePersonTeamSource.class);

		// Return the servicer
		return new HttpServicerTask("Servlet", "service");
	}

	/**
	 * Ensure can {@link HttpServlet} can service a simple {@link HttpRequest}.
	 */
	public void testSimpleRequest() throws Exception {

		// Specify servicing
		setServicing(new Servicer() {
			@Override
			public String service(HttpServlet servlet, HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {
				resp.addHeader("test", "value");
				return "Hello World";
			}
		});

		// Send request
		HttpClient client = this.createHttpClient();
		HttpGet request = new HttpGet(this.getServerUrl());
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello World", "test", "value");
	}

	/**
	 * Ensure can remember state between {@link HttpRequest} instances via the
	 * {@link HttpSession}.
	 */
	public void testSession() throws Exception {

		final String KEY = "test";

		// Specify servicing
		setServicing(new Servicer() {
			@Override
			public String service(HttpServlet servlet, HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {

				// Obtain response state from session
				String body = (String) req.getSession().getAttribute(KEY);

				// Load state to session for next request
				String value = req.getHeader(KEY);
				req.getSession().setAttribute(KEY, value);

				// Return the body
				return body;
			}
		});

		// Create the client
		HttpClient client = this.createHttpClient();

		final String VALUE = "state";

		// Send first request with details (expect no body returned)
		HttpGet requestOne = new HttpGet(this.getServerUrl());
		requestOne.setHeader(KEY, VALUE);
		HttpResponse responseOne = client.execute(requestOne);
		assertHttpResponse(responseOne, 204, null);

		// Send another request and validate obtained session state
		HttpGet requestTwo = new HttpGet(this.getServerUrl());
		HttpResponse responseTwo = client.execute(requestTwo);
		assertHttpResponse(responseTwo, 200, VALUE);
	}

	/**
	 * Ensure can handle authenticated {@link HttpRequest}.
	 */
	public void testAuthenticatedRequest() throws Exception {

		// Specify servicing
		setServicing(new Servicer() {
			@Override
			public String service(HttpServlet servlet, HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {

				// Determine if authenticated
				String remoteUser = req.getRemoteUser();
				if (remoteUser == null) {
					// Challenge for authentication
					resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
					resp.setHeader("WWW-Authenticate",
							"Basic realm=\"TestRealm\"");
					return "Challenge"; // challenge constructed
				}

				// Send response to user
				return "Hello " + req.getRemoteUser();
			}
		});

		// Provide preemptive authentication
		DefaultHttpClient client = (DefaultHttpClient) this.createHttpClient();
		client.getCredentialsProvider().setCredentials(
				new AuthScope(null, -1, "TestRealm"),
				new UsernamePasswordCredentials("Daniel", "password"));

		// Send request
		HttpGet request = new HttpGet(this.getServerUrl());
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello Daniel");
	}

	/**
	 * Asserts the correctness of the {@link HttpResponse}.
	 * 
	 * @param response
	 *            {@link HttpResponse} to validate.
	 * @param expectedStatus
	 *            Expected status.
	 * @param expectedBody
	 *            Expected body content.
	 * @param expectedHeaderNameValuePairs
	 *            Expected {@link Header} instances on the {@link HttpResponse}.
	 */
	private static void assertHttpResponse(HttpResponse response,
			int expectedStatus, String expectedBody,
			String... expectedHeaderNameValuePairs) {
		try {
			// Validate the status
			assertEquals("Request should be successful", expectedStatus,
					response.getStatusLine().getStatusCode());

			// Validate the body
			String body = getEntityBody(response);
			assertEquals("Incorrect response body", expectedBody, body);

			// Validate the headers
			for (int i = 0; i < expectedHeaderNameValuePairs.length; i += 2) {
				String name = expectedHeaderNameValuePairs[i];
				String value = expectedHeaderNameValuePairs[i + 1];
				Header header = response.getFirstHeader(name);
				assertEquals("Incorrect header " + name, value,
						(header == null ? null : header.getValue()));
			}

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Specifies the {@link Servicer} for servcing the {@link HttpRequest}.
	 * 
	 * @param servicer
	 *            {@link Servicer}.
	 */
	private static void setServicing(Servicer servicer) {
		MockHttpServlet.servicer = servicer;
	}

	/**
	 * Interface for servicing the {@link HttpRequest} via the
	 * {@link HttpServlet}.
	 */
	private static interface Servicer {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param servlet
		 *            {@link HttpServlet}.
		 * @param req
		 *            {@link HttpServletRequest}.
		 * @param resp
		 *            {@link HttpServletResponse}.
		 * @return Body content for {@link HttpResponse}.
		 * @throws ServletException
		 *             As per {@link HttpServlet}.
		 * @throws IOException
		 *             As per {@link HttpServlet}.
		 */
		String service(HttpServlet servlet, HttpServletRequest req,
				HttpServletResponse resp) throws ServletException, IOException;
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	public static class MockHttpServlet extends HttpServlet {

		/**
		 * {@link Servicer} to service the request.
		 */
		public static volatile Servicer servicer = null;

		/*
		 * ================== HttpServlet =========================
		 */

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			// Service the request
			String responseBody = servicer.service(this, req, resp);

			// Provide body response
			PrintWriter writer = resp.getWriter();
			writer.write(responseBody == null ? "" : responseBody);
			writer.flush();
		}
	}

}