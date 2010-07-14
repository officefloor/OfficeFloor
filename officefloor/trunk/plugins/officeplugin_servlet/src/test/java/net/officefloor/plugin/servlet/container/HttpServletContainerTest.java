/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * Tests the {@link HttpServletContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * Tests the unsupported functions.
	 */
	public void testUnsupportedFunctions() {
		this.doTest(new MockHttpServlet() {
			@Override
			protected void test(HttpServletRequest req, HttpServletResponse resp)
					throws ServletException, IOException {
				assertEquals("getAuthType()", null, req.getAuthType());
				assertEquals("getContextPath()", "/", req.getContextPath());
			}
		});
	}

	/**
	 * Ensure correct method.
	 */
	public void test_getMethod() {
		this.recordReturn(this.request, this.request.getMethod(), "POST");
		this.doTest(new MockHttpServlet() {
			@Override
			protected void test(HttpServletRequest req, HttpServletResponse resp)
					throws ServletException, IOException {
				assertEquals("getMethod()", "POST", req.getMethod());
			}
		});
	}

	/**
	 * Ensure able to obtain cookies.
	 */
	public void test_getCookies() {
		this.recordReturn(this.request, this.request.getHeaders(), this
				.createHttpHeaders("cookie", "name=\"value\""));
		this.doTest(new MockHttpServlet() {
			@Override
			protected void test(HttpServletRequest req, HttpServletResponse resp)
					throws ServletException, IOException {
				Cookie[] cookies = req.getCookies();
				assertEquals("Incorrect number of cookies", 1, cookies.length);
				Cookie cookie = cookies[0];
				assertEquals("Incorrect cookie name", "name", cookie.getName());
				assertEquals("Incorrect cookie value", "value", cookie
						.getValue());
			}
		});
	}

	/**
	 * Ensure able to obtain header values.
	 */
	public void test_getHeaders() {
		// Should cache headers (only single call)
		this.recordReturn(this.request, this.request.getHeaders(), this
				.createHttpHeaders("name", "value"));

		// Validate able to obtain header values
		this.doTest(new MockHttpServlet() {
			@Override
			protected void test(HttpServletRequest req, HttpServletResponse resp)
					throws ServletException, IOException {
				assertNull("getHeader(missing)", req.getHeader("missing"));
				assertEquals("getHeader(name)", "value", req.getHeader("name"));
			}
		});
	}

	/**
	 * Creates the {@link HttpHeader} listing.
	 * 
	 * @param httpHeaderNameValues
	 *            {@link HttpHeader} name value pairs.
	 * @return Listing of {@link HttpHeader} instances.
	 */
	private List<HttpHeader> createHttpHeaders(String... httpHeaderNameValues) {
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		for (int i = 0; i < httpHeaderNameValues.length; i += 2) {
			String name = httpHeaderNameValues[i];
			String value = httpHeaderNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}
		return headers;
	}

	/**
	 * Does the test.
	 * 
	 * @param servlet
	 *            {@link HttpServlet} containing the functionality for testing.
	 */
	private void doTest(HttpServlet servlet) {
		try {

			// Record obtaining the request and responses
			this.recordReturn(this.connection,
					this.connection.getHttpRequest(), this.request);
			this.recordReturn(this.connection, this.connection
					.getHttpResponse(), this.response);

			// Replay
			this.replayMockObjects();

			// Create the HTTP Servlet container
			HttpServletContainer container = new HttpServletContainerImpl(
					servlet);

			// Process a request
			container.service(this.connection, this.session);

			// Verify functionality
			this.verifyMockObjects();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	private static abstract class MockHttpServlet extends HttpServlet {

		/**
		 * Implement to provide testing of the {@link HttpServlet}.
		 * 
		 * @param req
		 *            {@link HttpServletRequest}.
		 * @param resp
		 *            {@link HttpServletResponse}.
		 * @throws ServletException
		 *             As per API.
		 * @throws IOException
		 *             As per API.
		 */
		protected abstract void test(HttpServletRequest req,
				HttpServletResponse resp) throws ServletException, IOException;

		/*
		 * ====================== HttpServlet ==========================
		 */

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			this.test(req, resp);
		}
	}

}