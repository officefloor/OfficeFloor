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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

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
		httpSession.setTimeout(1000);
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
	 * Ensure can {@link HttpServlet} can service {@link HttpRequest}.
	 */
	public void testService() throws Exception {
		// Obtain the Http Client
		HttpClient client = this.createHttpClient();

		// Send request
		HttpGet request = new HttpGet(this.getServerUrl());
		HttpResponse response = client.execute(request);

		// Validate the response
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());
		String body = getEntityBody(response);
		assertEquals("Incorrect response body", "Hello World", body);
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	public static class MockHttpServlet extends HttpServlet {

		/*
		 * ================== HttpServlet =========================
		 */

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			PrintWriter writer = resp.getWriter();
			writer.write("Hello World");
			writer.flush();
		}
	}

}