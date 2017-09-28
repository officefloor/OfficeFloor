/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Tests the {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebArchitectTest extends OfficeFrameTestCase {

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compile = new WebCompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		this.compile.officeFloor((context) -> {
			this.server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure able to GET root.
	 */
	public void testGetRoot() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.GET, "/", MockSection.class,
				MockHttpServer.mockRequest("/"));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getHttpEntity(null));
	}

	/**
	 * Ensure appropriately indicates {@link HttpMethod} not supported.
	 */
	public void testPostUnsupported() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.GET, "/", MockSection.class,
				MockHttpServer.mockRequest("/").method(HttpMethod.POST));
		assertEquals("Incorrect response status", 405, response.getHttpStatus().getStatusCode());
		assertEquals("Must indicate allowed methods", "GET", response.getFirstHeader("Allow").getValue());
	}

	/**
	 * Ensure able to POST root.
	 */
	public void testPostRoot() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.POST, "/", MockSection.class,
				MockHttpServer.mockRequest("/").method(HttpMethod.POST));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getHttpEntity(null));
	}

	/**
	 * Ensure able to obtain resource at path.
	 */
	public void testPath() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.GET, "/path/to/resource", MockSection.class,
				MockHttpServer.mockRequest("/path/to/resource"));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getHttpEntity(null));
	}

	/**
	 * Ensure able to provide parameter via query string.
	 */
	public void testQueryStringParameter() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.GET, "/path", MockParameter.class,
				MockHttpServer.mockRequest("/path?param=value"));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));
	}

	/**
	 * Ensure able to provide parameter via path.
	 */
	public void testPathParameter() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.GET, "/path/{param}", MockParameter.class,
				MockHttpServer.mockRequest("/path/value"));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));
	}

	/**
	 * Ensure able to provide form parameter.
	 */
	public void testFormParameter() throws Exception {
		MockHttpResponse response = this.service(HttpMethod.POST, "/path", MockParameter.class,
				MockHttpServer.mockRequest("/path").method(HttpMethod.POST)
						.header("Content-Type", "application/x-www-form-urlencoded").entity("param=value"));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));
	}

	/**
	 * Ensure can store state within application.
	 */
	public void testApplicationState() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeSectionInput servicer = context.addSection("SECTION", MockApplication.class)
					.getOfficeSectionInput("service");
			WebArchitect web = context.getWebArchitect();
			web.link(false, HttpMethod.POST, "/path", servicer);
			web.link(false, "/path", servicer);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request to store state in application state
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/path?param=value").method(HttpMethod.POST));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));

		// Obtain value from application state
		response = this.server.send(
				MockHttpServer.mockRequest("/path").header("cookie", response.getFirstHeader("set-cookie").getValue()));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));
	}

	/**
	 * Ensure can store state within {@link HttpSession}.
	 */
	public void testSession() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeSectionInput servicer = context.addSection("SECTION", MockSession.class)
					.getOfficeSectionInput("service");
			WebArchitect web = context.getWebArchitect();
			web.link(false, HttpMethod.POST, "/path", servicer);
			web.link(false, "/path", servicer);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request to store state in session
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/path?param=value").method(HttpMethod.POST));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));

		// Obtain value from session
		response = this.server.send(
				MockHttpServer.mockRequest("/path").header("cookie", response.getFirstHeader("set-cookie").getValue()));
		assertEquals("Incorrect status", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Incorrect response", "Parameter=value", response.getHttpEntity(null));
	}

	/**
	 * Ensure can redirect (remembering original request).
	 */
	public void testRedirect() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			// Provide continuation
			HttpUrlContinuation continuation = context.linkUrl("/path", false, MockParameter.class);

			// Configure to redirect to continuation
			WebArchitect web = context.getWebArchitect();
			OfficeSection section = context.addSection("REDIRECT", MockRedirect.class);
			web.link(false, HttpMethod.POST, "/redirect", section.getOfficeSectionInput("service"));
			web.link(section.getOfficeSectionOutput("redirect"), continuation);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request that provides redirect
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/redirect?param=value").method(HttpMethod.POST));
		assertEquals("Incorrect status", 307, response.getHttpStatus().getStatusCode());
		String cookie = response.getFirstHeader("set-cookie").getValue();
		assertEquals("Should have redirect cookie", "ofc=/path", cookie);

		// Ensure can redirect
		response = this.server.send(MockHttpServer.mockRequest("/path").header("cookie", cookie));
		assertEquals("Should service redirected", 200, response.getHttpStatus().getStatusCode());
		assertEquals("Should re-instate request", "Parameter=value", response.getHttpEntity(null));
	}

	/**
	 * Services the {@link MockHttpRequestBuilder}.
	 * 
	 * @param httpMethod
	 *            {@link HttpMethod}.
	 * @param applicationPath
	 *            Application path.
	 * @param servicer
	 *            {@link Class} of the servicer.
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse service(HttpMethod httpMethod, String applicationPath, Class<?> servicer,
			MockHttpRequestBuilder request) throws Exception {
		this.compile.web((context) -> context.linkUrl(httpMethod, applicationPath, false, servicer));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
		return this.server.send(MockHttpServer.mockRequest("/").method(HttpMethod.POST));
	}

	public static class MockSection {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getHttpResponse().getEntityWriter().write("TEST");
		}
	}

	@HttpParameters
	public static class Parameter {
		private String param;

		public void setParam(String param) {
			this.param = param;
		}
	}

	public static class MockParameter {
		public void service(Parameter param, ServerHttpConnection connection) throws IOException {
			connection.getHttpResponse().getEntityWriter().write("Parameter=" + param.param);
		}
	}

	@HttpApplicationStateful
	public static class ApplicationObject {
		private String value = null;
	}

	public static class MockApplication {
		public void service(Parameter parameter, ServerHttpConnection connection, ApplicationObject object)
				throws IOException {
			if (connection.getHttpRequest().getHttpMethod() == HttpMethod.POST) {
				object.value = parameter.param;
			}
			connection.getHttpResponse().getEntityWriter().write("Application=" + object.value);
		}
	}

	@HttpSessionStateful
	public static class SessionObject implements Serializable {
		private String value = null;
	}

	public static class MockSession {
		public void service(ServerHttpConnection connection, SessionObject object, Parameter parameter)
				throws IOException {
			if (connection.getHttpRequest().getHttpMethod() == HttpMethod.POST) {
				object.value = parameter.param;
			}
			connection.getHttpResponse().getEntityWriter().write("Session=" + object.value);
		}
	}

	public static class MockRedirect {
		@NextFunction("redirect")
		public void service() {
		}
	}

}