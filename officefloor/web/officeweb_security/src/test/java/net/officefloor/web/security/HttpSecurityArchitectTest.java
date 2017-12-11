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
package net.officefloor.web.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Encoder;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpCredentials;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link HttpSecurityArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityArchitectTest extends OfficeFrameTestCase {

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
	 * Ensure can make request as guest.
	 */
	public void testGuest() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", GuestLogic.class);

		// Ensure service request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class GuestLogic {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure can check not authenticated.
	 */
	public void testCheckNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", CheckNotAuthenticatedServicer.class);

		// Ensure indicate not logged in
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
	}

	public static class CheckNotAuthenticatedServicer {
		public void service(HttpAuthentication<HttpCredentials> authentication, ServerHttpConnection connection)
				throws IOException {

			// Ensure indicate not logged in
			assertFalse("Should not be authenticated", authentication.isAuthenticated());
			assertNull("Should not have access control", authentication.getAccessControl());

			// Logout should be no operation
			Closure<Boolean> isLogout = new Closure<>(false);
			authentication.logout((failure) -> {
				assertNull("Should not be failure in no operation logout", failure);
				isLogout.value = true;
			});
			assertTrue("Logout callback should be called immediately", isLogout.value);

			// Send response
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure can check that authenticated.
	 */
	public void testCheckAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", CheckAuthenticatedServicer.class);

		// Send request (with authentication)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class CheckAuthenticatedServicer {
		public void service(HttpAuthentication<HttpCredentials> authentication, ServerHttpConnection connection)
				throws IOException {

			// Ensure indicate logged in
			assertTrue("Should be authenticated", authentication.isAuthenticated());
			assertNotNull("Should have access control", authentication.getAccessControl());

			// Send response
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure disallows access if not authenticated.
	 */
	public void testNoAccessAsNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", NoAccessAsNotAuthenticatedServicer.class);

		// Ensure not allowed access
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be allowed access", 401, response.getStatus().getStatusCode());
		assertEquals("Should issue challenge", MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"),
				response.getHeader("www-authenticate").getValue());
	}

	public static class NoAccessAsNotAuthenticatedServicer {
		public void service(HttpAccessControl accessControl) {
			fail("Should not gain access to method, as not authenticated");
		}
	}

	/**
	 * Ensure can check {@link HttpAccessControl}.
	 */
	public void testAccessControl() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", AccessControlServicer.class);

		// Send request (with authentication)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class AccessControlServicer {
		public void service(HttpAccessControl accessControl, ServerHttpConnection connection) throws IOException {

			// Ensure correct authentication scheme
			assertEquals("Incorrect authentication scheme", "Mock", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect principal", "test", accessControl.getPrincipal().getName());

			// Ensure in role
			assertTrue("Should be in test role", accessControl.inRole("test"));
			assertFalse("Should only be in test role", accessControl.inRole("not"));

			// Send response
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure allows access with {@link HttpAccess} if authenticated and in
	 * role.
	 */
	public void testHttpAccess() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure able to access
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be sucessful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class HttpAccessServicer {
		@HttpAccess(ifAllRoles = "test")
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure disallow request as no authentication.
	 */
	public void testHttpAccessNoAuthentication() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure not allowed access (with a challenge to authenticate)
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be allowed access", 401, response.getStatus().getStatusCode());
		assertEquals("Should issue challenge", MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"),
				response.getHeader("www-authenticate").getValue());
	}

	/**
	 * Ensure disallow as not in role.
	 */
	public void testHttpAccessNotInRole() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure not allowed access (with a challenge to authenticate)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "not", "not"));
		assertEquals("Should not be allowed access", 403, response.getStatus().getStatusCode());
		assertEquals("Already authenticated, so should be no challenge", 0, response.getHeaders().size());
	}

	/**
	 * Ensure authentication/access to be cached in {@link HttpSession}.
	 */
	public void testHttpAccessViaSession() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure able to access
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be sucessful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));

		// Ensure able to access via cached security in session
		response = this.server.send(MockHttpServer.mockRequest("/path").cookies(response));
		assertEquals("Should be sucessful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	/**
	 * Ensure able to configure multiple {@link HttpSecurity} instances.
	 */
	public void testMultipleSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class);

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class);

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure both security is on the challenge
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include both security challenges", response.getHeader("www-authenticate").getValue(),
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("test") + ", "
						+ MockChallengeHttpSecuritySource.getHeaderChallengeValue("test"));

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	/**
	 * Ensure able to qualify which {@link HttpSecurity} to use.
	 */
	public void testQualifiedSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class);

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class);

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure only app challenge is on response
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include both security challenges", response.getHeader("www-authenticate").getValue(),
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("test"));

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class QualifiedHttpAccessServicer {
		@HttpAccess(withQualifier = "app", ifRole = { "test", "not" })
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("QUALIFIED");
		}
	}

	/**
	 * Ensure able to negotiate {@link HttpSecurity} based on
	 * <code>Accept-Type</code>.
	 */
	public void testNegotiateSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class).addContentType("application/json");

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class).addContentType("text/html");

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure can select security based on accept-type
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/path").header("accept-type", "application/json"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
	}

	/**
	 * Ensure can trigger logout.
	 */
	public void testLogout() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Creates a {@link MockHttpRequestBuilder} with credentials.
	 * 
	 * @param path
	 *            Path.
	 * @param userName
	 *            User name.
	 * @param password
	 *            Password.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder mockRequest(String path, String userName, String password) {
		Encoder encoder = Base64.getEncoder();
		return MockHttpServer.mockRequest("/path").header("authorization", "Basic "
				+ encoder.encodeToString((userName + ":" + password).getBytes(ServerHttpConnection.HTTP_CHARSET)));
	}

	/**
	 * Initialises with {@link MockChallengeHttpSecuritySource}.
	 */
	private void initialiseMockHttpSecurity(String path, String realm, Class<?> sectionClass) throws Exception {
		this.compile((context, security) -> {
			HttpSecurityBuilder builder = security.addHttpSecurity(realm, new MockChallengeHttpSecuritySource(realm));
			context.link(false, path, sectionClass);

			OfficeSection handler = context.addSection("HANDLE", HandleSection.class);
			context.getOfficeArchitect().link(builder.getOutput("Failure"), handler.getOfficeSectionInput("handle"));
		});
	}

	public static class HandleSection {
		public void handle(@Parameter Throwable exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Initialises the {@link HttpSecurityArchitect}.
	 */
	private static interface Initialiser {

		/**
		 * Initialises the {@link HttpSecurityArchitect}.
		 * 
		 * @param context
		 *            {@link CompileWebContext}.
		 * @param security
		 *            {@link HttpSecurityArchitect}.
		 */
		void initialise(CompileWebContext context, HttpSecurityArchitect security);
	}

	/**
	 * Compiles with the {@link Initialiser}.
	 * 
	 * @param initialiser
	 *            {@link Initialiser}.
	 */
	private void compile(Initialiser initialiser) throws Exception {
		this.compile.web((context) -> {
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(
					context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
			initialiser.initialise(context, security);
			security.informWebArchitect();
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
	}

}