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

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.route.ServletRouteTask;
import net.officefloor.plugin.servlet.route.ServletRouteTask.DependencyKeys;
import net.officefloor.plugin.servlet.route.ServletRouteTask.FlowKeys;
import net.officefloor.plugin.servlet.route.source.ServletRouteWorkSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.HttpServicerFunction;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Ensure can route appropriately to a {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class RouteIntegrateTest extends MockHttpServletServer {

	@Override
	@SuppressWarnings("unchecked")
	public HttpServicerFunction buildServlet(String servletContextName,
			String httpName, String requestAttributesName, String sessionName,
			String securityName) {

		// Construct the servlets
		this.constructMockHttpServlet("PathServlet", "/path/*",
				servletContextName, httpName, requestAttributesName,
				sessionName, securityName);
		this.constructMockHttpServlet("LargerPathServlet", "/path/larger/*",
				servletContextName, httpName, requestAttributesName,
				sessionName, securityName);
		this.constructMockHttpServlet("DefaultServlet", "/",
				servletContextName, httpName, requestAttributesName,
				sessionName, securityName);
		this.constructMockHttpServlet("ExtensionServlet", "*.extension",
				servletContextName, httpName, requestAttributesName,
				sessionName, securityName);

		// Team to route
		this.constructTeam("TEAM", OnePersonTeamSource.class);

		// Construct the router
		HttpServicerFunction routerRef = new HttpServicerFunction("ROUTE", "Route");
		FunctionNamespaceType<ServletRouteTask> type = WorkLoaderUtil
				.loadWorkType(ServletRouteWorkSource.class);
		ManagedFunctionBuilder<ServletRouteTask, DependencyKeys, FlowKeys> task = (ManagedFunctionBuilder<ServletRouteTask, DependencyKeys, FlowKeys>) this
				.constructWork(routerRef.workName, type.getWorkFactory())
				.addManagedFunction(routerRef.functionName,
						type.getManagedFunctionTypes()[0].getManagedFunctionFactory());
		task.setTeam("TEAM");
		task.linkManagedObject(DependencyKeys.HTTP_CONNECTION, httpName,
				ServerHttpConnection.class);
		task.linkManagedObject(DependencyKeys.OFFICE_SERVLET_CONTEXT,
				servletContextName, OfficeServletContext.class);
		task.linkFlow(FlowKeys.UNHANDLED, "Unhandled", "service",
				FlowInstigationStrategyEnum.SEQUENTIAL, null);

		// Construct unhandled request servicer
		this.constructWork(new UnhandledWork(), "Unhandled", null)
				.buildTask("service", "TEAM").buildObject(httpName);

		// Return the reference to the route
		return routerRef;
	}

	/**
	 * Constructs a {@link MockHttpServlet} for testing.
	 * 
	 * @param servletName
	 *            {@link Servlet} name (also the {@link Work} name).
	 * @param servletMappings
	 *            {@link Servlet} mappings.
	 * @param servletContextName
	 *            {@link OfficeServletContext} name.
	 * @param httpName
	 *            {@link ServerHttpConnection} name.
	 * @param requestAttributesName
	 *            Request attributes name.
	 * @param sessionName
	 *            {@link HttpSession} name.
	 * @param sercurityName
	 *            {@link HttpSecurity} name.
	 */
	private void constructMockHttpServlet(String servletName,
			String servletMappings, String servletContextName, String httpName,
			String requestAttributesName, String sessionName,
			String securityName) {
		this.constructHttpServlet(servletName, servletContextName, httpName,
				requestAttributesName, sessionName, securityName,
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, servletName,
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				MockHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
				servletMappings);
	}

	/**
	 * Ensure can route by path to {@link Servlet}.
	 */
	public void testPath() {
		this.doTest("/path", "PathServlet");
	}

	/**
	 * Ensure routes to largest matching path {@link Servlet}.
	 */
	public void testLargerPath() {
		this.doTest("/path/larger", "LargerPathServlet");
	}

	/**
	 * Ensure can route to default {@link Servlet}.
	 */
	public void testDefault() {
		this.doTest("", "DefaultServlet");
	}

	/**
	 * Ensure can route by extension to {@link Servlet}.
	 */
	public void testExtension() {
		this.doTest("/resource.extension", "ExtensionServlet");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param path
	 *            Path sent on {@link HttpRequest}.
	 * @param expectedResponse
	 *            Expected response body.
	 */
	private void doTest(String path, String expectedResponse) {
		try {

			// Trigger request dispatch
			HttpClient client = this.createHttpClient();
			HttpResponse response = client.execute(new HttpGet(this
					.getServerUrl() + path));

			// Ensure correctly routed
			assertHttpResponse(response, 200, expectedResponse);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	public static class MockHttpServlet extends HttpServlet {
		@Override
		protected void service(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			String servletName = this.getServletConfig().getServletName();
			response.getWriter().write(servletName);
		}
	}

	/**
	 * Handles unhandled {@link Servlet} request.
	 */
	public static class UnhandledWork {
		public void service(ServerHttpConnection connection) throws IOException {
			OutputStreamWriter writer = new OutputStreamWriter(connection
					.getHttpResponse().getEntity());
			writer.write("UNHANDLED");
			writer.flush();
		}
	}

}