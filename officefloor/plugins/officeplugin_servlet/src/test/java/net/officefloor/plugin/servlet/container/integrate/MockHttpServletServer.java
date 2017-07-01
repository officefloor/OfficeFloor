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
package net.officefloor.plugin.servlet.container.integrate;

import java.io.File;
import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.plugin.servlet.container.source.HttpServletTask;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.host.ServletServerManagedObjectSource;
import net.officefloor.plugin.servlet.host.ServletServerManagedObjectSource.Dependencies;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.security.HttpServletSecurity;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.HttpServicerFunction;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateManagedObjectSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

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
	 * Name of the {@link ServletServer} {@link ManagedObject}.
	 */
	protected final String SERVLET_SERVER_NAME = "ServletServer";

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
	 * @return {@link HttpServicerFunction} identifying the {@link ManagedFunction} to service
	 *         the {@link HttpRequest}.
	 */
	public abstract HttpServicerFunction buildServlet(String servletContextName,
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
			String body = HttpTestUtil.getEntityBody(response);
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
	 * Resource path root.
	 */
	protected File resourcePathRoot;

	/**
	 * Flag indicating if {@link Team} is constructed for {@link HttpServlet}.
	 */
	private boolean isHttpServletTeamConstructed = false;

	/**
	 * Convenience method to construct {@link HttpServlet} {@link ManagedFunctionSource}.
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
	 *            {@link ManagedFunctionSource} class.
	 * @param properties
	 *            Properties for the {@link ManagedFunctionSource}.
	 * @return {@link HttpServicerFunction} for the constructed {@link HttpServlet}
	 *         {@link ManagedFunction}.
	 */
	@SuppressWarnings("unchecked")
	protected HttpServicerFunction constructHttpServlet(String workName,
			String servletContextName, String httpName,
			String requestAttributesName, String sessionName,
			String securityName,
			Class<? extends ManagedFunctionSource<HttpServletTask>> workSourceClass,
			String... properties) {

		// Construct the reference
		final HttpServicerFunction reference = new HttpServicerFunction(workName,
				"service");

		// Construct servicer (only once for test)
		final String SERVICER_NAME = "Servicer";
		if (!this.isHttpServletTeamConstructed) {
			this.constructTeam(SERVICER_NAME, OnePersonTeamSource.class);
			this.isHttpServletTeamConstructed = true;
		}

		// Constructs the HTTP Servlet
		FunctionNamespaceType<HttpServletTask> servlet = WorkLoaderUtil.loadWorkType(
				workSourceClass, properties);
		this.constructWork(reference.workName, servlet.getWorkFactory());
		ManagedFunctionBuilder<HttpServletTask, DependencyKeys, None> service = (ManagedFunctionBuilder<HttpServletTask, DependencyKeys, None>) this
				.constructTask(reference.functionName,
						servlet.getManagedFunctionTypes()[0].getManagedFunctionFactory(),
						SERVICER_NAME);
		service.setDifferentiator(servlet.getManagedFunctionTypes()[0].getDifferentiator());
		service.linkParameter(DependencyKeys.SERVICER_MAPPING,
				ServicerMapping.class);
		service.linkManagedObject(DependencyKeys.OFFICE_SERVLET_CONTEXT,
				servletContextName, OfficeServletContext.class);
		service.linkManagedObject(DependencyKeys.HTTP_CONNECTION, httpName,
				ServerHttpConnection.class);
		service.linkManagedObject(DependencyKeys.REQUEST_ATTRIBUTES,
				requestAttributesName, HttpRequestState.class);
		service.linkManagedObject(DependencyKeys.HTTP_SESSION, sessionName,
				HttpSession.class);
		service.linkManagedObject(DependencyKeys.HTTP_SECURITY, securityName,
				HttpServletSecurity.class);

		// Return reference to Servlet
		return reference;
	}

	/*
	 * ================== MockHttpServer =======================
	 */

	@Override
	public HttpServicerFunction buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {

		final long TIMEOUT = 100000; // 100 seconds (for debugging)

		// Obtain locations for testing
		final File passwordFile = this.findFile(this.getClass(), "Simple.jsp");
		this.resourcePathRoot = passwordFile.getParentFile();

		// Obtain Office Name
		String officeName = this.getOfficeName();

		// HttpApplicationLocation
		ManagedObjectBuilder<None> httpApplicationLocation = this
				.constructManagedObject("HttpApplicationLocation",
						HttpApplicationLocationManagedObjectSource.class);
		httpApplicationLocation.setManagingOffice(officeName);
		DependencyMappingBuilder httpApplicationLocationDepedendencies = this
				.getOfficeBuilder().addProcessManagedObject(
						"HttpApplicationLocation", "HttpApplicationLocation");
		httpApplicationLocationDepedendencies
				.mapDependency(
						net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource.Dependencies.SERVER_HTTP_CONNECTION,
						managedObjectName);

		// ServletServer
		ManagedObjectBuilder<None> servletServer = this.constructManagedObject(
				SERVLET_SERVER_NAME, ServletServerManagedObjectSource.class);
		servletServer.addProperty(
				ServletServerManagedObjectSource.PROPERTY_CLASS_PATH_PREFIX,
				this.getClass().getPackage().getName());
		servletServer.setManagingOffice(officeName);
		DependencyMappingBuilder servletServerDepedendencies = this
				.getOfficeBuilder().addProcessManagedObject(
						SERVLET_SERVER_NAME, SERVLET_SERVER_NAME);
		servletServerDepedendencies.mapDependency(
				Dependencies.HTTP_APPLICATION_LOCATION,
				"HttpApplicationLocation");

		// OfficeServletContext
		final String SERVLET_CONTEXT_NAME = "ServletContext";
		ManagedObjectBuilder<None> servletContext = this
				.constructManagedObject(SERVLET_CONTEXT_NAME,
						OfficeServletContextManagedObjectSource.class);
		servletContext
				.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						"ServletContext");
		servletContext.setManagingOffice(officeName);
		DependencyMappingBuilder servletContextDependencies = this
				.getOfficeBuilder().addProcessManagedObject(
						SERVLET_CONTEXT_NAME, SERVLET_CONTEXT_NAME);
		servletContextDependencies
				.mapDependency(
						net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource.DependencyKeys.SERVLET_SERVER,
						SERVLET_SERVER_NAME);

		// Request Attributes
		final String REQUEST_ATTRIBUTES_NAME = "RequestAttributes";
		ManagedObjectBuilder<None> requestAttributes = this
				.constructManagedObject(REQUEST_ATTRIBUTES_NAME,
						HttpRequestStateManagedObjectSource.class);
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

		// Provide mock HTTP Security
		final String HTTP_SECURITY_NAME = "HttpSecurity";
		this.constructManagedObject(new HttpServletSecurity() {
			@Override
			public String getAuthenticationScheme() {
				return "Mock";
			}

			@Override
			public Principal getUserPrincipal() {
				return null;
			}

			@Override
			public String getRemoteUser() {
				return "Daniel";
			}

			@Override
			public boolean isUserInRole(String role) {
				return ("test".equals(role));
			}
		}, HTTP_SECURITY_NAME, officeName);
		this.getOfficeBuilder().addProcessManagedObject(HTTP_SECURITY_NAME,
				HTTP_SECURITY_NAME);

		// Service authentication
		final String TEAM_NAME = "of-HttpSecurity.AUTHENTICATOR";
		this.constructTeam(TEAM_NAME, OnePersonTeamSource.class);

		// Construct the HTTP Servlet task
		HttpServicerFunction task = this.buildServlet(SERVLET_CONTEXT_NAME,
				managedObjectName, REQUEST_ATTRIBUTES_NAME, HTTP_SESSION_NAME,
				HTTP_SECURITY_NAME);

		// Construct the invoker (due to parameter/argument difference)
		HttpServicerFunction invoker = new HttpServicerFunction("INVOKER", "invoke");
		this.constructWork(new Invoker(), invoker.workName, null)
				.buildTask(invoker.functionName, TEAM_NAME)
				.buildFlow(task.workName, task.functionName,
						FlowInstigationStrategyEnum.SEQUENTIAL, null);

		// Return Invoker to invoke the HTTP Servlet
		return invoker;
	}

	/**
	 * Invoker.
	 */
	public static class Invoker {

		/**
		 * Invokes the flow.
		 * 
		 * @param flow
		 *            Flow.
		 */
		public void invoke(ReflectiveFlow flow) {
			flow.doFlow(null);
		}
	}

}