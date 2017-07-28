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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.server.HttpServicerFunction;

/**
 * Integrate test of {@link RequestDispatcher}.
 * 
 * @author Daniel Sagenschneider
 */
public class DispatchIntegrateTest extends MockHttpServletServer {

	@Override
	public HttpServicerFunction buildServlet(String servletContextName,
			String httpName, String requestAttributesName, String sessionName,
			String securityName) {

		// Construct HTTP Servlet to handle request
		HttpServicerFunction reference = this.constructHttpServlet("DISPATCH",
				servletContextName, httpName, requestAttributesName,
				sessionName, securityName, HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "Dispatch",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				DispatchHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
				"/servlet/dispatch/*");

		// Construct HTTP Servlet to dispatch.
		// Include matching by extensions for the tests.
		this.constructHttpServlet("HANDLER", servletContextName, httpName,
				requestAttributesName, sessionName, securityName,
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "Handler",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				HandlerHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
				"/servlet/handler/*, *.frwd, *.inc");

		// Return the reference to handling HTTP Servlet
		return reference;
	}

	/**
	 * Ensure can forward by path.
	 */
	public void testRequestForward() throws Exception {
		this.doTest("request", "/servlet/handler/path/info", "forward",
				"/servlet/handler", "/path/info");
	}

	/**
	 * Ensure can include by path.
	 */
	public void testRequestInclude() throws Exception {
		this.doTest("request", "/servlet/handler/path/info", "include",
				"/servlet/handler", "/path/info");
	}

	/**
	 * Ensure can forward by name.
	 */
	public void testNamedForward() {
		this.doTest("name", "Handler", "forward", "/", "");
	}

	/**
	 * Ensure can include by name.
	 */
	public void testNamedInclude() {
		this.doTest("name", "Handler", "include", "/", "");
	}

	/**
	 * Ensure can include by extension.
	 */
	public void testExtensionForward() {
		this.doTest("request", "/extension.frwd", "forward", "/extension.frwd",
				"");
	}

	/**
	 * Ensure can include by extension.
	 */
	public void testExtensionInclude() {
		this.doTest("request", "/extension.inc", "include", "/extension.inc",
				"");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param type
	 *            Either <code>request</code> or <code>name</code>.
	 * @param value
	 *            Value to lookup the {@link RequestDispatcher}.
	 * @param dispatch
	 *            Either <code>forward</code> or <code>include</code>.
	 * @param expectedServletPath
	 *            Expected Servlet path in handler.
	 * @param expectedPathInfo
	 *            Expected Path Info in handler.
	 */
	private void doTest(String type, String value, String dispatch,
			String expectedServletPath, String expectedPathInfo) {
		try {

			// Trigger request dispatch
			HttpClient client = this.createHttpClient();
			HttpResponse response = client.execute(new HttpGet(this
					.getServerUrl()
					+ "?type="
					+ type
					+ "&value="
					+ value
					+ "&dispatch="
					+ dispatch));

			// Ensure correctly dispatched
			assertHttpResponse(response, 200, "Dispatched", new String[] {
					"ServletPath", expectedServletPath, "PathInfo",
					expectedPathInfo });

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * {@link HttpServlet} to dispatch {@link HttpRequest}.
	 */
	public static class DispatchHttpServlet extends HttpServlet {
		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			// Obtain details for dispatch
			String type = req.getParameter("type");
			String value = req.getParameter("value");
			String dispatch = req.getParameter("dispatch");

			// Obtain the dispatcher
			RequestDispatcher dispatcher = null;
			if ("request".equals(type)) {
				dispatcher = req.getRequestDispatcher(value);
			} else if ("name".equals(type)) {
				dispatcher = this.getServletContext().getNamedDispatcher(value);
			} else {
				// Invalid type parameter
				resp.getWriter().write("Invalid 'type' parameter");
				return;
			}

			// Ensure have dispatcher
			if (dispatcher == null) {
				// No dispatcher
				resp.getWriter().write("No dispatcher");
				return;
			}

			// Dispatch
			if ("forward".equals(dispatch)) {
				dispatcher.forward(req, resp);
			} else if ("include".equals(dispatch)) {
				dispatcher.include(req, resp);
			} else {
				// Invalid dispatch parameter
				resp.getWriter().write("Invalid 'dispatch' parameter");
				return;
			}
		}
	}

	/**
	 * {@link HttpServlet} to handle dispatch.
	 */
	public static class HandlerHttpServlet extends HttpServlet {
		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			// Provide details for response
			resp.setHeader("ServletPath", req.getServletPath());
			resp.setHeader("PathInfo", req.getPathInfo());

			// Send response
			resp.getWriter().write("Dispatched");
		}
	}

}