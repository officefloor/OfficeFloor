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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.server.HttpServicerTask;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Tests integration of {@link HttpServletWorkSource} with dependencies to
 * service a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletIntegrateTest extends MockHttpServletServer {

	@Override
	public HttpServicerTask buildServlet(String servletContextName,
			String httpName, String requestAttributesName, String sessionName,
			String securityName) {
		// Construct and return reference to HTTP Servlet
		return this.constructHttpServlet("HttpServlet", servletContextName,
				httpName, requestAttributesName, sessionName, securityName,
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "Servlet",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				MockHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
				"/servlet/path/*");
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
		HttpClientBuilder builder = HttpClientBuilder.create();
		HttpTestUtil.configureCredentials(builder, "TestRealm", null, "Daniel",
				"password");
		try (CloseableHttpClient client = builder.build()) {

			// Send request
			HttpGet request = new HttpGet(this.getServerUrl());
			HttpResponse response = client.execute(request);

			// Validate the response
			assertHttpResponse(response, 200, "Hello Daniel");
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