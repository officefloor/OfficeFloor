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
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.plugin.servlet.container.source.HttpServletTask;
import net.officefloor.plugin.servlet.container.source.RequestAttributesManagedObjectSource;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;
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

/**
 * <p>
 * Mock server for running {@link HttpServlet} functionality.
 * <p>
 * This is NOT be used in production and is only available to simplify testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class MockHttpServletServer extends MockHttpServer {

	/**
	 * Realm for authentication.
	 */
	public static final String REALM = "TestRealm";

	/**
	 * Builds the {@link HttpServlet} servicer.
	 * 
	 * @param httpName
	 *            Name of the {@link ServerHttpConnection}.
	 * @param servletContextName
	 *            Name of the {@link ServletContext}.
	 * @param requestAttributesName
	 *            Name of the request attributes.
	 * @param sessionName
	 *            Name of the {@link HttpSession}.
	 * @param securityName
	 *            Name of the {@link HttpSecurity}.
	 * @return {@link HttpServicerTask} identifying the {@link Task} to service
	 *         the {@link HttpRequest}.
	 */
	public abstract HttpServicerTask buildServlet(String servletContextName,
			String httpName, String requestAttributesName, String sessionName,
			String securityName);

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
	public static void assertHttpResponse(HttpResponse response,
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
	 * Flag indicating if {@link Team} is constructed for {@link HttpServlet}.
	 */
	private boolean isHttpServletTeamConstructed = false;

	/**
	 * Convenience method to construct {@link HttpServlet} {@link WorkSource}.
	 * 
	 * @param workName
	 *            Name of {@link Work} for the {@link HttpServlet}.
	 * @param servletContextName
	 *            Name of the {@link ServletContext}.
	 * @param httpName
	 *            Name of the {@link ServerHttpConnection}.
	 * @param requestAttributesName
	 *            Name of the request attributes.
	 * @param sessionName
	 *            Name of the {@link HttpSession}.
	 * @param securityName
	 *            Name of the {@link HttpSecurity}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param properties
	 *            Properties for the {@link WorkSource}.
	 * @return {@link HttpServicerTask} for the constructed {@link HttpServlet}
	 *         {@link Task}.
	 */
	@SuppressWarnings("unchecked")
	protected HttpServicerTask constructHttpServlet(String workName,
			String servletContextName, String httpName,
			String requestAttributesName, String sessionName,
			String securityName,
			Class<? extends WorkSource<HttpServletTask>> workSourceClass,
			String... properties) {

		// Construct the reference
		final HttpServicerTask reference = new HttpServicerTask(workName,
				"service");

		// Construct servicer (only once for test)
		final String SERVICER_NAME = "Servicer";
		if (!this.isHttpServletTeamConstructed) {
			this.constructTeam(SERVICER_NAME, OnePersonTeamSource.class);
			this.isHttpServletTeamConstructed = true;
		}

		// Constructs the HTTP Servlet
		WorkType<HttpServletTask> servlet = WorkLoaderUtil.loadWorkType(
				workSourceClass, properties);
		this.constructWork(reference.workName, servlet.getWorkFactory());
		TaskBuilder<HttpServletTask, DependencyKeys, None> service = (TaskBuilder<HttpServletTask, DependencyKeys, None>) this
				.constructTask(reference.taskName, servlet.getTaskTypes()[0]
						.getTaskFactory(), SERVICER_NAME);
		service
				.setDifferentiator(servlet.getTaskTypes()[0]
						.getDifferentiator());
		service.linkManagedObject(DependencyKeys.OFFICE_SERVLET_CONTEXT,
				servletContextName, OfficeServletContext.class);
		service.linkManagedObject(DependencyKeys.HTTP_CONNECTION, httpName,
				ServerHttpConnection.class);
		service.linkManagedObject(DependencyKeys.REQUEST_ATTRIBUTES,
				requestAttributesName, Map.class);
		service.linkManagedObject(DependencyKeys.HTTP_SESSION, sessionName,
				HttpSession.class);
		service.linkManagedObject(DependencyKeys.HTTP_SECURITY, securityName,
				HttpSecurity.class);

		// Return reference to Servlet
		return reference;
	}

	/*
	 * ================== MockHttpServer =======================
	 */

	@Override
	public HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {

		final long TIMEOUT = 100000; // 100 seconds (for debugging)

		// Obtain locations for testing
		final File passwordFile = this.findFile(this.getClass(),
				"password-file.txt");
		final File resourcePathRoot = passwordFile.getParentFile();

		// Obtain Office Name
		String officeName = this.getOfficeName();

		// ServletContext
		final String SERVLET_CONTEXT_NAME = "ServletContext";
		ManagedObjectBuilder<None> servletContext = this
				.constructManagedObject(SERVLET_CONTEXT_NAME,
						OfficeServletContextManagedObjectSource.class);
		servletContext.addProperty(
				OfficeServletContextManagedObjectSource.PROPERTY_SERVER_NAME,
				"localhost");
		servletContext
				.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						"ServletContext");
		servletContext.addProperty(
				OfficeServletContextManagedObjectSource.PROPERTY_CONTEXT_PATH,
				"/");
		servletContext
				.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_RESOURCE_PATH_ROOT,
						resourcePathRoot.getAbsolutePath());
		servletContext.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject(SERVLET_CONTEXT_NAME,
				SERVLET_CONTEXT_NAME);

		// Request Attributes
		final String REQUEST_ATTRIBUTES_NAME = "RequestAttributes";
		ManagedObjectBuilder<None> requestAttributes = this
				.constructManagedObject(REQUEST_ATTRIBUTES_NAME,
						RequestAttributesManagedObjectSource.class);
		requestAttributes.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject(
				REQUEST_ATTRIBUTES_NAME, REQUEST_ATTRIBUTES_NAME);

		// HTTP Session
		final String HTTP_SESSION_NAME = "HttpSession";
		ManagedObjectBuilder<Indexed> httpSession = this
				.constructManagedObject(HTTP_SESSION_NAME,
						HttpSessionManagedObjectSource.class);
		httpSession.setManagingOffice(officeName);
		httpSession.setTimeout(TIMEOUT);
		DependencyMappingBuilder httpSessionDependencies = this
				.getOfficeBuilder().addProcessManagedObject(HTTP_SESSION_NAME,
						HTTP_SESSION_NAME);
		httpSessionDependencies.mapDependency(0, managedObjectName);

		// Credential Store
		final String CREDENTIAL_STORE_NAME = "CredentialStore";
		ManagedObjectBuilder<?> credentialStore = this.constructManagedObject(
				CREDENTIAL_STORE_NAME, PasswordFileManagedObjectSource.class);
		credentialStore.addProperty(
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH,
				passwordFile.getAbsolutePath());
		credentialStore.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject(CREDENTIAL_STORE_NAME,
				CREDENTIAL_STORE_NAME);

		// HTTP Security Service
		final String HTTP_SECURITY_SERVICE_NAME = "HttpSecurityService";
		ManagedObjectBuilder<?> httpSecurityService = this
				.constructManagedObject(HTTP_SECURITY_SERVICE_NAME,
						HttpSecurityServiceManagedObjectSource.class);
		httpSecurityService
				.addProperty(
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						HttpSecurityServiceManagedObjectSource.BASIC_AUTHENTICATION_SCHEME);
		httpSecurityService.addProperty(BasicHttpSecuritySource.PROPERTY_REALM,
				REALM);
		httpSecurityService.setManagingOffice(officeName);
		DependencyMappingBuilder httpSecurityServiceDependencies = this
				.getOfficeBuilder().addProcessManagedObject(
						HTTP_SECURITY_SERVICE_NAME, HTTP_SECURITY_SERVICE_NAME);
		httpSecurityServiceDependencies.mapDependency(0, managedObjectName);
		httpSecurityServiceDependencies.mapDependency(1, HTTP_SESSION_NAME);
		httpSecurityServiceDependencies.mapDependency(2, CREDENTIAL_STORE_NAME);

		// HTTP Security
		final String HTTP_SECURITY_NAME = "HttpSecurity";
		ManagedObjectBuilder<FlowKeys> httpSecurity = this
				.constructManagedObject(HTTP_SECURITY_NAME,
						HttpSecurityManagedObjectSource.class);
		ManagingOfficeBuilder<FlowKeys> httpSecurityOffice = httpSecurity
				.setManagingOffice(officeName);
		httpSecurity.setTimeout(TIMEOUT);
		httpSecurityOffice
				.setInputManagedObjectName("InputHttpSecurity")
				.mapDependency(
						net.officefloor.plugin.socket.server.http.security.HttpSecurityManagedObjectSource.DependencyKeys.HTTP_SECURITY_SERVICE,
						HTTP_SECURITY_SERVICE_NAME);
		DependencyMappingBuilder httpSecurityDependencies = this
				.getOfficeBuilder().addProcessManagedObject(HTTP_SECURITY_NAME,
						HTTP_SECURITY_NAME);
		httpSecurityDependencies
				.mapDependency(
						net.officefloor.plugin.socket.server.http.security.HttpSecurityManagedObjectSource.DependencyKeys.HTTP_SECURITY_SERVICE,
						HTTP_SECURITY_SERVICE_NAME);

		// Service authentication
		this.constructTeam("of-HttpSecurity.AUTHENTICATOR",
				OnePersonTeamSource.class);

		// Construct and return the HTTP Servlet task
		HttpServicerTask task = this.buildServlet(SERVLET_CONTEXT_NAME,
				managedObjectName, REQUEST_ATTRIBUTES_NAME, HTTP_SESSION_NAME,
				HTTP_SECURITY_NAME);
		return task;
	}

}