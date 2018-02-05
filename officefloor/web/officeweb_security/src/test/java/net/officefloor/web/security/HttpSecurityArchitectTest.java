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

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.build.HttpSecurerBuilder;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.security.scheme.MockAccessControl;
import net.officefloor.web.security.scheme.MockAuthentication;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.security.scheme.MockCredentials;
import net.officefloor.web.security.scheme.MockFlowHttpSecuritySource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpChallenge;
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
	 * Ensure can check not authenticated with custom authentication.
	 */
	public void testCustom_CheckNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Custom_CheckNotAuthenticatedServicer.class);

		// Ensure indicate not logged in
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST");
	}

	public static class Custom_CheckNotAuthenticatedServicer {
		public void service(MockAuthentication authentication, ServerHttpConnection connection) throws IOException {

			// Ensure indicate not logged in
			assertFalse("Should not be authenticated", authentication.isAuthenticated());
			try {
				assertNull("Should not have access control", authentication.getAccessControl());
				fail("Should not successfully obtain access control");
			} catch (AuthenticationRequiredException ex) {
			}

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
	 * Ensure can check not authenticated with {@link HttpAuthentication}.
	 */
	public void testStandard_CheckNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Standard_CheckNotAuthenticatedServicer.class);

		// Ensure indicate not logged in
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
	}

	public static class Standard_CheckNotAuthenticatedServicer {
		public void service(HttpAuthentication<HttpCredentials> authentication, ServerHttpConnection connection)
				throws IOException {

			// Ensure indicate not logged in
			assertFalse("Should not be authenticated", authentication.isAuthenticated());
			try {
				assertNull("Should not have access control", authentication.getAccessControl());
				fail("Should not successfully obtain access control");
			} catch (AuthenticationRequiredException ex) {
			}

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
	 * Ensure can check that authenticated with custom authentication.
	 */
	public void testCustom_CheckAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Custom_CheckAuthenticatedServicer.class);

		// Send request (with authentication)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class Custom_CheckAuthenticatedServicer {
		public void service(MockAuthentication authentication, ServerHttpConnection connection) throws IOException {

			// Ensure indicate logged in
			assertTrue("Should be authenticated", authentication.isAuthenticated());
			assertNotNull("Should have access control", authentication.getAccessControl());

			// Send response
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure can check that authenticated with {@link HttpAuthentication}.
	 */
	public void testStandard_CheckAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Standard_CheckAuthenticatedServicer.class);

		// Send request (with authentication)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class Standard_CheckAuthenticatedServicer {
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
	 * Ensure disallows access if not authenticated with custom authentication.
	 */
	public void testCustom_NoAccessAsNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Custom_NoAccessAsNotAuthenticatedServicer.class);

		// Ensure not allowed access
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be allowed access", 401, response.getStatus().getStatusCode());
		assertEquals("Should issue challenge", MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"),
				response.getHeader("www-authenticate").getValue());
	}

	public static class Custom_NoAccessAsNotAuthenticatedServicer {
		public void service(MockAccessControl accessControl) {
			fail("Should not gain access to method, as not authenticated");
		}
	}

	/**
	 * Ensure disallows access if not authenticated with
	 * {@link HttpAccessControl}.
	 */
	public void testStandard_NoAccessAsNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Standard_NoAccessAsNotAuthenticatedServicer.class);

		// Ensure not allowed access
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be allowed access", 401, response.getStatus().getStatusCode());
		assertEquals("Should issue challenge", MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"),
				response.getHeader("www-authenticate").getValue());
	}

	public static class Standard_NoAccessAsNotAuthenticatedServicer {
		public void service(HttpAccessControl accessControl) {
			fail("Should not gain access to method, as not authenticated");
		}
	}

	/**
	 * Ensure can check custom access control.
	 */
	public void testCustom_AccessControl() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Custom_AccessControlServicer.class);

		// Send request (with authentication)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class Custom_AccessControlServicer {
		public void service(MockAccessControl accessControl, ServerHttpConnection connection) throws IOException {

			// Ensure correct authentication
			assertEquals("Incorrect authentication scheme", "Mock", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect user", "test", accessControl.getUserName());

			// Ensure in role
			assertTrue("Should be in test role", accessControl.getRoles().contains("test"));
			assertFalse("Should only be in test role", accessControl.getRoles().contains("not"));

			// Send response
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure can check {@link HttpAccessControl}.
	 */
	public void testStandard_AccessControl() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Standard_AccessControlServicer.class);

		// Send request (with authentication)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	public static class Standard_AccessControlServicer {
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
	 * Ensure able to qualify which {@link HttpSecurity} to use.
	 */
	public void testQualifiedSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class).addProperty("realm", "api");

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class).addProperty("realm", "app");

			// Provide servicer that will use any security
			context.link(false, "/path", QualifiedHttpAccessServicer.class);
		});

		// Ensure only app challenge is on response
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include only the qualified security", response.getHeader("www-authenticate").getValue(),
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("app"));

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "QUALIFIED", response.getEntity(null));
	}

	public static class QualifiedHttpAccessServicer {
		@HttpAccess(withQualifier = "app", ifRole = { "test", "not" })
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("QUALIFIED");
		}
	}

	/**
	 * Ensure able to configure multiple challenge {@link HttpSecurity}
	 * instances.
	 */
	public void testMultipleChallengeSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class).addProperty("realm", "one");

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class).addProperty("realm", "two");

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure both security is on the challenge
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be authenticated: " + response.getEntity(null), 401,
				response.getStatus().getStatusCode());
		assertEquals("Should include both security challenges",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one") + ", "
						+ MockChallengeHttpSecuritySource.getHeaderChallengeValue("two"),
				response.getHeader("www-authenticate").getValue());

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "TEST", response.getEntity(null));
	}

	/**
	 * Ensure able to negotiate {@link HttpSecurity} based on
	 * <code>Accept-Type</code>.
	 */
	public void testNegotiateSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"))
					.addContentType("application/json");

			// Provide security for web content
			security.addHttpSecurity("app", new MockChallengeHttpSecuritySource("two")).addContentType("text/html");

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure can select security based on accept-type
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/path").header("accept", "application/json"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include the negotiated security challenge",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one"),
				response.getHeader("www-authenticate").getValue());
	}

	/**
	 * Ensure able to negotiate {@link HttpSecurity} with wildcards.
	 */
	public void testNegotiateWildcardSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one")).addContentType("application/*");

			// Provide security for web content
			security.addHttpSecurity("app", new MockChallengeHttpSecuritySource("two")).addContentType("text/*");

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure can select security based on accept-type
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/path").header("accept", "application/json"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include the negotiated security challenge",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one"),
				response.getHeader("www-authenticate").getValue());
	}

	/**
	 * Ensure can mix {@link HttpChallenge} {@link HttpSecurity} with appliation
	 * {@link HttpSecurity}.
	 */
	public void testMixChallengeWithApplicationSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"))
					.addContentType("application/json");

			// Provide security for web content
			HttpSecurityBuilder app = security.addHttpSecurity("app", new MockFlowHttpSecuritySource("two"));
			app.addContentType("text/html");

			// Provide servicer that will use any security
			OfficeSection section = context.addSection("service", MixServicer.class);
			context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));

			// Configure challenge
			context.getOfficeArchitect().link(app.getOutput("CHALLENGE"), section.getOfficeSectionInput("challenge"));
		});

		// Ensure for html request that application challenge
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").header("accept", "text/html"));
		assertEquals("Should provide successful form response", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect form response", "User name and password please", response.getEntity(null));

		// Ensure for json request that challenge
		response = this.server.send(MockHttpServer.mockRequest("/path").header("accept", "application/json"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include the negotiated security challenge",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one"),
				response.getHeader("www-authenticate").getValue());

	}

	public static class MixServicer {
		@HttpAccess(ifRole = "test")
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}

		public void challenge(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("User name and password please");
		}
	}

	/**
	 * Ensure can secure {@link OfficeManagedObject}.
	 */
	public void testHttpOfficeSecurerManagedObject() throws Exception {
		this.compile((context, security) -> {

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("api",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("service", HttpSecurerManagedObjectServicer.class);
			context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));

			// Provide object
			OfficeManagedObject mo = context.addManagedObject("MO", HttpSecurerObject.class, ManagedObjectScope.THREAD);

			// Secure the managed object
			builder.createHttpSecurer()
					.secure((secureContext) -> mo.addPreLoadAdministration(secureContext.getAdministration()));
		});

		// Ensure use of managed object is secured
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should not be authenticated", 401, response.getStatus().getStatusCode());
		assertEquals("Should include the negotiated security challenge",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("secure"),
				response.getHeader("www-authenticate").getValue());

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "SECURE", response.getEntity(null));
	}

	public static class HttpSecurerObject {
	}

	public static class HttpSecurerManagedObjectServicer {
		public void service(HttpSecurerObject object, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("SECURE");
		}
	}

	/**
	 * Ensure can secure {@link OfficeSectionFunction}.
	 */
	public void testHttpOfficeSecurerFunction() throws Exception {
		this.compile((context, security) -> {

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("api",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("servicer", HttpSecurerFunctionServicer.class);
			context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));

			// Secure the function
			OfficeSectionFunction function = section.getOfficeSectionFunction("service");
			HttpSecurerBuilder securer = builder.createHttpSecurer();
			securer.addRole("role");
			securer.secure((secureContext) -> function.addPreAdministration(secureContext.getAdministration()));
		});

		// Ensure not access if do not have role
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		assertEquals("Should not be authenticated", 403, response.getStatus().getStatusCode());

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "SECURE", response.getEntity(null));
	}

	public static class HttpSecurerFunctionServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("SECURE");
		}
	}

	/**
	 * Ensure can secure {@link Office} {@link Flow}.
	 */
	public void testHttpOfficeSecurerFlow() throws Exception {
		this.compile((context, security) -> {

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("app",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("servicer", HttpOfficeSecurerFlowServicer.class);
			context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));

			// Configure the secure decision
			HttpSecurerBuilder securer = builder.createHttpSecurer();
			securer.addRole("role");
			securer.secure((securerContext) -> securerContext.link(section.getOfficeSectionOutput("decision"),
					section.getOfficeSectionInput("secure"), section.getOfficeSectionInput("insecure")));
		});

		// Ensure insecure flow followed
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect insecure response", "INSECURE", response.getEntity(null));

		// Ensure insecure flow if not in role
		response = this.server.send(this.mockRequest("/path", "no_access", "no_access"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect insecure response", "INSECURE", response.getEntity(null));

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect secure response", "SECURE", response.getEntity(null));
	}

	public static class HttpOfficeSecurerFlowServicer extends HttpSectionSecurerFlowServicer {
		@NextFunction("decision")
		public void service() {
		}
	}

	public static class HttpSectionSecurerFlowServicer {
		public void secure(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("SECURE");
		}

		public void insecure(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("INSECURE");
		}
	}

	/**
	 * Ensure can secure {@link Flow} section.
	 */
	public void testHttpSectionSecurerFlow() throws Exception {
		this.compile((context, security) -> {

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("app",
					new MockChallengeHttpSecuritySource("secure"));

			// Configure securer
			HttpSecurerBuilder securer = builder.createHttpSecurer();
			securer.addRole("role");

			// Configure the section
			OfficeSection section = context.getOfficeArchitect().addOfficeSection("section",
					new HttpSectionSecurerFlowSection(securer.createFlowSecurer()), null);
			context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));
		});

		// Ensure insecure flow followed
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect insecure response", "INSECURE", response.getEntity(null));

		// Ensure insecure flow if not in role
		response = this.server.send(this.mockRequest("/path", "no_access", "no_access"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect insecure response", "INSECURE", response.getEntity(null));

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect secure response", "SECURE", response.getEntity(null));
	}

	public class HttpSectionSecurerFlowSection extends AbstractSectionSource {

		private final HttpFlowSecurer securer;

		private HttpSectionSecurerFlowSection(HttpFlowSecurer securer) {
			this.securer = securer;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Add the sub section
			SubSection subSection = designer.addSubSection("sub_section", ClassSectionSource.class.getName(),
					HttpSectionSecurerFlowServicer.class.getName());
			designer.link(subSection.getSubSectionObject(ServerHttpConnection.class.getName()),
					designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
							ServerHttpConnection.class.getName()));
			designer.link(subSection.getSubSectionOutput(IOException.class.getName()),
					designer.addSectionOutput(IOException.class.getSimpleName(), IOException.class.getName(), true));

			// Configure secure flow
			this.securer.link(designer, designer.addSectionInput("service", null),
					subSection.getSubSectionInput("secure"), subSection.getSubSectionInput("insecure"));
		}
	}

	/**
	 * Ensure can trigger logout.
	 */
	public void testCustom_Logout() throws Exception {
		this.compile((context, security) -> {
			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"));
			OfficeSection section = context.addSection("section", CustomLogout.class);
			WebArchitect web = context.getWebArchitect();
			web.link(false, "/login", section.getOfficeSectionInput("login"));
			web.link(false, "/logout", section.getOfficeSectionInput("logout"));
			web.link(false, "/check", section.getOfficeSectionInput("check"));
		});

		// Ensure login
		MockHttpResponse response = this.server.send(this.mockRequest("/login", "test", "test"));
		assertEquals("Should login", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect login", "login", response.getEntity(null));

		// Trigger logout
		response = this.server.send(MockHttpServer.mockRequest("/logout").cookies(response));
		assertEquals("Incorrect logout", "logout", response.getEntity(null));
		assertEquals("Should logout", 200, response.getStatus().getStatusCode());

		// Ensure logged out
		response = this.server.send(MockHttpServer.mockRequest("/check").cookies(response));
		assertEquals("Incorrect check", 200, response.getStatus().getStatusCode());
		assertEquals("Should be logged out", "guest", response.getEntity(null));
	}

	public static class CustomLogout {
		@HttpAccess(ifRole = "test")
		public void login(ServerHttpConnection connection, MockAuthentication authentication) throws IOException {
			connection.getResponse().getEntityWriter().write(authentication.isAuthenticated() ? "login" : "failed");
		}

		@HttpAccess(ifRole = "test")
		public void logout(MockAuthentication authentication, ServerHttpConnection connection) throws IOException {
			authentication.logout(null);
			connection.getResponse().getEntityWriter().write("logout");
		}

		public void check(ServerHttpConnection connection, MockAuthentication authentication) throws IOException {
			connection.getResponse().getEntityWriter()
					.write(authentication.isAuthenticated() ? "authenticated" : "guest");
		}
	}

	/**
	 * Ensure can trigger logout.
	 */
	public void testStandard_Logout() throws Exception {
		this.compile((context, security) -> {
			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"));
			OfficeSection section = context.addSection("section", CustomLogout.class);
			WebArchitect web = context.getWebArchitect();
			web.link(false, "/login", section.getOfficeSectionInput("login"));
			web.link(false, "/logout", section.getOfficeSectionInput("logout"));
			web.link(false, "/check", section.getOfficeSectionInput("check"));
		});

		// Ensure login
		MockHttpResponse response = this.server.send(this.mockRequest("/login", "test", "test"));
		assertEquals("Should login", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect login", "login", response.getEntity(null));

		// Trigger logout
		response = this.server.send(MockHttpServer.mockRequest("/logout").cookies(response));
		assertEquals("Incorrect logout", "logout", response.getEntity(null));
		assertEquals("Should logout", 200, response.getStatus().getStatusCode());

		// Ensure logged out
		response = this.server.send(MockHttpServer.mockRequest("/check").cookies(response));
		assertEquals("Incorrect check", 200, response.getStatus().getStatusCode());
		assertEquals("Should be logged out", "guest", response.getEntity(null));
	}

	public static class StandardLogout {
		@HttpAccess(ifRole = "test")
		public void login(ServerHttpConnection connection, HttpAuthentication<Void> authentication) throws IOException {
			connection.getResponse().getEntityWriter().write(authentication.isAuthenticated() ? "login" : "failed");
		}

		@HttpAccess(ifRole = "test")
		public void logout(HttpAuthentication<Void> authentication, ServerHttpConnection connection)
				throws IOException {
			authentication.logout(null);
			connection.getResponse().getEntityWriter().write("logout");
		}

		public void check(ServerHttpConnection connection, HttpAuthentication<Void> authentication) throws IOException {
			connection.getResponse().getEntityWriter()
					.write(authentication.isAuthenticated() ? "authenticated" : "guest");
		}
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
		return new MockCredentials(userName, password).loadHttpRequest(MockHttpServer.mockRequest(path));
	}

	/**
	 * Initialises with {@link MockChallengeHttpSecuritySource}.
	 */
	private void initialiseMockHttpSecurity(String path, String realm, Class<?> sectionClass) throws Exception {
		this.compile((context, security) -> {
			security.addHttpSecurity(realm, new MockChallengeHttpSecuritySource(realm));
			context.link(false, path, sectionClass);
		});
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