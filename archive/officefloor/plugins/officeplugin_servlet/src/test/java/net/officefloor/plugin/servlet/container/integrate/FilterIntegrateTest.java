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
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.servlet.filter.configuration.FilterInstance;
import net.officefloor.plugin.servlet.filter.configuration.FilterMappings;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.server.HttpServicerFunction;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Integrate test of {@link RequestDispatcher}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterIntegrateTest extends MockHttpServletServer {

	@Override
	public HttpServicerFunction buildServlet(String servletContextName,
			String httpName, String requestAttributesName, String sessionName,
			String securityName) {

		// Obtain the office name
		String officeName = this.getOfficeName();

		final String DISPATCH_SERVLET_NAME = "DISPATCH";
		final String HANDLER_SERVELT_NAME = "HANDLER";

		// Create the properties for the Office Servlet Context
		PropertyList properties = OfficeFloorCompiler.newPropertyList();

		// Load the Office Servlet Context properties
		properties
				.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME)
				.setValue("FilterServletContext");

		// Create the filter mappings
		FilterMappings mappings = new FilterMappings();

		// Add Request Filter on Servlet Name
		new FilterInstance("s:R", IntegrateFilter.class.getName())
				.outputProperties(properties);
		mappings.addFilterMapping("s:R", null, DISPATCH_SERVLET_NAME,
				MappingType.REQUEST);

		// Add Request Filter on Path
		new FilterInstance("f:R", IntegrateFilter.class.getName())
				.outputProperties(properties);
		mappings
				.addFilterMapping("f:R", "/filter/*", null, MappingType.REQUEST);

		// Add Forward Filter on Servlet Name
		new FilterInstance("s:F", IntegrateFilter.class.getName())
				.outputProperties(properties);
		mappings.addFilterMapping("s:F", null, HANDLER_SERVELT_NAME,
				MappingType.FORWARD);

		// Add Forward Filter on Path
		new FilterInstance("f:F", IntegrateFilter.class.getName())
				.outputProperties(properties);
		mappings
				.addFilterMapping("f:F", "/filter/*", null, MappingType.FORWARD);

		// Add Include Filter on Servlet Name
		new FilterInstance("s:I", IntegrateFilter.class.getName())
				.outputProperties(properties);
		mappings.addFilterMapping("s:I", null, HANDLER_SERVELT_NAME,
				MappingType.INCLUDE);

		// Add Forward Filter on Path
		new FilterInstance("f:I", IntegrateFilter.class.getName())
				.outputProperties(properties);
		mappings
				.addFilterMapping("f:I", "/filter/*", null, MappingType.INCLUDE);

		// Output filter mapping properties
		mappings.outputProperties(properties);

		// Construct another Office Servlet Context with filtering
		final String SERVLET_CONTEXT_NAME = "FilteringServletContext";
		ManagedObjectBuilder<None> servletContext = this
				.constructManagedObject(SERVLET_CONTEXT_NAME,
						OfficeServletContextManagedObjectSource.class);
		for (Property property : properties) {
			servletContext.addProperty(property.getName(), property.getValue());
		}
		servletContext.setManagingOffice(officeName);
		DependencyMappingBuilder servletContextDependencies = this
				.getOfficeBuilder().addProcessManagedObject(
						SERVLET_CONTEXT_NAME, SERVLET_CONTEXT_NAME);
		servletContextDependencies.mapDependency(DependencyKeys.SERVLET_SERVER,
				SERVLET_SERVER_NAME);

		// Construct HTTP Servlet to handle request
		HttpServicerFunction reference = this.constructHttpServlet(
				DISPATCH_SERVLET_NAME, SERVLET_CONTEXT_NAME, httpName,
				requestAttributesName, sessionName, securityName,
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME,
				DISPATCH_SERVLET_NAME,
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				DispatchHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
				"/servlet/dispatch/*");

		// Construct HTTP Servlet to dispatch.
		// Include matching by extensions for the tests.
		this.constructHttpServlet(HANDLER_SERVELT_NAME, SERVLET_CONTEXT_NAME,
				httpName, requestAttributesName, sessionName, securityName,
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME,
				HANDLER_SERVELT_NAME,
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				HandlerHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
				"/forward/*, /include/*, /filter/*");

		// Return the reference to handling HTTP Servlet
		return reference;
	}

	/**
	 * Ensure service request with filtering based on {@link Servlet} name.
	 */
	public void test_request_ServletFiltering() throws Exception {
		this.doTest("/servlet-only", "none", null, "s:R> D>  <D <s:R");
	}

	/**
	 * Ensure service request with filtering based on path.
	 */
	public void test_request_PathFiltering() throws Exception {
		this.doTest("/filter", "none", null, "f:R> s:R> D>  <D <s:R <f:R");
	}

	/**
	 * Ensure can forward with filtering based on {@link Servlet} name.
	 */
	public void test_forward_ServletFiltering() {
		this.doTest("/servlet-only", "/forward", "forward",
				"s:R> D>  <D <s:Rs:F> H <s:F");
	}

	/**
	 * Ensure can forward with filtering based on path.
	 */
	public void test_forward_PathFiltering() {
		this.doTest("/servlet-only", "/filter", "forward",
				"s:R> D>  <D <s:Rf:F> s:F> H <s:F <f:F");
	}

	/**
	 * Ensure can include with filtering based on {@link Servlet} name.
	 */
	public void test_include_ServletFiltering() {
		this.doTest("/servlet-only", "/include", "include",
				"s:R> D> s:I> H <s:I <D <s:R");
	}

	/**
	 * Ensure can include with filtering based on path.
	 */
	public void test_include_PathFiltering() {
		this.doTest("/servlet-only", "/filter", "include",
				"s:R> D> f:I> s:I> H <s:I <f:I <D <s:R");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param path
	 *            Path sent on {@link HttpRequest}.
	 * @param dispatch
	 *            Path to dispatch on. <code>none</code> does not dispatch.
	 * @param type
	 *            Either <code>forward</code> or <code>include</code>.
	 * @param expectedResponse
	 *            Expected response body.
	 */
	private void doTest(String path, String dispatch, String type,
			String expectedResponse) {
		try {

			// Trigger request dispatch
			HttpClient client = this.createHttpClient();
			HttpResponse response = client.execute(new HttpGet(this
					.getServerUrl()
					+ path + "?dispatch=" + dispatch + "&type=" + type));

			// Ensure correctly dispatched
			assertHttpResponse(response, 200, expectedResponse);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * {@link Filter} to test integration.
	 */
	public static class IntegrateFilter implements Filter {

		/**
		 * {@link Filter} name.
		 */
		private String filterName;

		/*
		 * =================== Filter =====================;==
		 */

		@Override
		public void init(FilterConfig config) throws ServletException {
			this.filterName = config.getFilterName();
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			PrintWriter writer = response.getWriter();
			writer.write(this.filterName + "> ");
			chain.doFilter(request, response);
			writer.write(" <" + this.filterName);
		}

		@Override
		public void destroy() {
			fail("Should not be destroyed");
		}
	}

	/**
	 * {@link HttpServlet} to dispatch {@link HttpRequest}.
	 */
	public static class DispatchHttpServlet extends HttpServlet {
		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			// Indicate location in response
			PrintWriter writer = resp.getWriter();
			writer.write("D> ");

			// Obtain details for dispatch
			String dispatch = req.getParameter("dispatch");
			String type = req.getParameter("type");

			// Obtain the dispatcher
			RequestDispatcher dispatcher = null;
			if ("none".equals(dispatch)) {
				// No dispatching
				writer.write(" <D");
				return;
			} else {
				// Obtain dispatcher for path
				dispatcher = req.getRequestDispatcher(dispatch);
			}

			// Ensure have dispatcher
			if (dispatcher == null) {
				// No dispatcher
				resp.getWriter().write("No dispatcher");
				return;
			}

			// Dispatch
			if ("forward".equals(type)) {
				dispatcher.forward(req, resp);
			} else if ("include".equals(type)) {
				dispatcher.include(req, resp);
			} else {
				// Invalid dispatch parameter
				resp.getWriter().write("Invalid 'dispatch' parameter");
				return;
			}

			// Note exit from servlet in response
			writer.write(" <D");
		}
	}

	/**
	 * {@link HttpServlet} to handle dispatch.
	 */
	public static class HandlerHttpServlet extends HttpServlet {
		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.getWriter().write("H");
		}
	}

}