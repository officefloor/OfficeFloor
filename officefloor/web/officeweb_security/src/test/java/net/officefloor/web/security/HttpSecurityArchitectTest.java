/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security;

import java.io.IOException;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.build.AbstractHttpSecurable;
import net.officefloor.web.security.build.HttpSecurer;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.security.scheme.FormHttpSecuritySource;
import net.officefloor.web.security.scheme.MockAccessControl;
import net.officefloor.web.security.scheme.MockAuthentication;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.security.scheme.MockCredentials;
import net.officefloor.web.security.scheme.MockFlowHttpSecuritySource;
import net.officefloor.web.security.store.MockCredentialStoreManagedObjectSource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Tests the {@link HttpSecurityArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityArchitectTest extends OfficeFrameTestCase {

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	protected final WebCompileOfficeFloor compile = new WebCompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	protected MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Configure mock server
		this.compile.officeFloor((context) -> {
			this.server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
		});
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Ensure able to employ {@link HttpSecurityArchitect} without configuring
	 * {@link HttpSecurity}.
	 */
	public void testNoSecurity() throws Exception {
		this.compile((context, security) -> {
			// No security
			context.link(false, "/path", GuestLogic.class);
		});

		// Ensure service request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST");
	}

	/**
	 * Ensure can make request as guest.
	 */
	public void testGuest() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", GuestLogic.class);

		// Ensure service request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST");
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
		response.assertResponse(200, "TEST");
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
		response.assertResponse(200, "TEST");
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
		response.assertResponse(200, "TEST");
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
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"));
	}

	public static class Custom_NoAccessAsNotAuthenticatedServicer {
		public void service(MockAccessControl accessControl) {
			fail("Should not gain access to method, as not authenticated");
		}
	}

	/**
	 * Ensure disallows access if not authenticated with {@link HttpAccessControl}.
	 */
	public void testStandard_NoAccessAsNotAuthenticated() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", Standard_NoAccessAsNotAuthenticatedServicer.class);

		// Ensure not allowed access
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"));
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
		response.assertResponse(200, "TEST");
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
		response.assertResponse(200, "TEST");
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
	 * Ensure allows access with {@link HttpAccess} if authenticated and in role.
	 */
	public void testHttpAccess() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure able to access
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		response.assertResponse(200, "TEST");
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
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"));
	}

	/**
	 * Ensure disallow as not in role.
	 */
	public void testHttpAccessNotInRole() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure not allowed access (with a challenge to authenticate)
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "not", "not"));
		response.assertResponse(403, "");
		assertEquals("Already authenticated, so should be no challenge", 0, response.getHeaders().size());
	}

	/**
	 * Ensure authentication/access to be cached in {@link HttpSession}.
	 */
	public void testHttpAccessViaSession() throws Exception {
		this.initialiseMockHttpSecurity("/path", "REALM", HttpAccessServicer.class);

		// Ensure able to access
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		response.assertResponse(200, "TEST");

		// Ensure able to access via cached security in session
		response = this.server.send(MockHttpServer.mockRequest("/path").cookies(response));
		response.assertResponse(200, "TEST");
	}

	/**
	 * Ensure able to qualify which {@link HttpSecurity} to use.
	 */
	public void testQualifiedSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class.getName()).addProperty("realm",
					"api");

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class.getName()).addProperty("realm",
					"app");

			// Provide servicer that will use any security
			context.link(false, "/path", QualifiedHttpAccessServicer.class);
		});

		// Ensure only app challenge is on response
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("app"));

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		response.assertResponse(200, "QUALIFIED");
	}

	public static class QualifiedHttpAccessServicer {
		@HttpAccess(withHttpSecurity = "app", ifRole = { "test", "not" })
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("QUALIFIED");
		}
	}

	/**
	 * Ensure able to configure multiple challenge {@link HttpSecurity} instances.
	 */
	public void testMultipleChallengeSecurity() throws Exception {
		this.compile((context, security) -> {
			// Provide security for REST API calls
			security.addHttpSecurity("api", MockChallengeHttpSecuritySource.class.getName()).addProperty("realm",
					"one");

			// Provide security for web content
			security.addHttpSecurity("app", MockChallengeHttpSecuritySource.class.getName()).addProperty("realm",
					"two");

			// Provide servicer that will use any security
			context.link(false, "/path", HttpAccessServicer.class);
		});

		// Ensure both security is on the challenge
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one") + ", "
						+ MockChallengeHttpSecuritySource.getHeaderChallengeValue("two"));

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		response.assertResponse(200, "TEST");
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
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one"));
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
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one"));
	}

	/**
	 * Ensure can mix {@link HttpChallenge} {@link HttpSecurity} with appliation
	 * {@link HttpSecurity}.
	 */
	public void testMixChallengeWithApplicationSecurity() throws Exception {
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security for REST API calls
			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"))
					.addContentType("application/json");

			// Provide security for web content
			HttpSecurityBuilder app = security.addHttpSecurity("app", new MockFlowHttpSecuritySource("two"));
			app.addContentType("text/html");

			// Provide servicer that will use any security
			OfficeSection section = context.addSection("service", MixServicer.class);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));

			// Configure challenge
			office.link(app.getOutput("CHALLENGE"), section.getOfficeSectionInput("challenge"));
		});

		// Ensure for html request that application challenge
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").header("accept", "text/html"));
		response.assertResponse(200, "User name and password please");

		// Ensure for json request that challenge
		response = this.server.send(MockHttpServer.mockRequest("/path").header("accept", "application/json"));
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("one"));
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
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("api",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("service", HttpSecurerManagedObjectServicer.class);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));

			// Provide object
			OfficeManagedObject mo = context.addManagedObject("MO", HttpSecurerObject.class, ManagedObjectScope.THREAD);

			// Secure the managed object (with authentication only)
			builder.createHttpSecurer(null)
					.secure((secureContext) -> mo.addPreLoadAdministration(secureContext.getAdministration()));
		});

		// Ensure use of managed object is secured
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("secure"));

		// Ensure can access once providing credentials
		response = this.server.send(this.mockRequest("/path", "test", "test"));
		response.assertResponse(200, "SECURE");
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
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("api",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("servicer", HttpSecurerFunctionServicer.class);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));

			// Secure the function
			OfficeSectionFunction function = section.getOfficeSectionFunction("service");
			AbstractHttpSecurable securable = new AbstractHttpSecurable() {
			};
			securable.addRole("role");
			HttpSecurer securer = builder.createHttpSecurer(securable);
			securer.secure((secureContext) -> function.addPreAdministration(secureContext.getAdministration()));
		});

		// Ensure no access if do not have role
		MockHttpResponse response = this.server.send(this.mockRequest("/path", "test", "test"));
		response.assertResponse(403, "");

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		response.assertResponse(200, "SECURE");
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
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("app",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("servicer", HttpSectionSecurerFlowServicer.class);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));

			// Configure the secure decision
			AbstractHttpSecurable securable = new AbstractHttpSecurable() {
			};
			securable.addRole("role");
			HttpSecurer securer = builder.createHttpSecurer(securable);
			securer.secure((securerContext) -> {
				OfficeFlowSinkNode secureFlow = securerContext.secureFlow(null, section.getOfficeSectionInput("secure"),
						section.getOfficeSectionInput("insecure"));
				office.link(section.getOfficeSectionOutput("decision"), secureFlow);
			});
		});

		// Ensure insecure flow followed
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "INSECURE");

		// Ensure insecure flow if not in role
		response = this.server.send(this.mockRequest("/path", "no_access", "no_access"));
		response.assertResponse(200, "INSECURE");

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		response.assertResponse(200, "SECURE");
	}

	public static class HttpSectionSecurerFlowServicer {
		@NextFunction("decision")
		public void service() {
		}

		public void secure(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("SECURE");
		}

		public void insecure(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("INSECURE");
		}
	}

	/**
	 * Ensure can secure {@link Office} {@link Flow} passing through an argument.
	 */
	public void testHttpOfficeSecurerFlowWithArgument() throws Exception {
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("app",
					new MockChallengeHttpSecuritySource("secure"));

			// Provide servicer
			OfficeSection section = context.addSection("servicer", HttpSectionSecurerFlowServicerWithArgument.class);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));

			// Configure the secure decision
			AbstractHttpSecurable securable = new AbstractHttpSecurable() {
			};
			securable.addRole("role");
			HttpSecurer securer = builder.createHttpSecurer(securable);
			securer.secure((securerContext) -> {
				OfficeFlowSinkNode secureFlow = securerContext.secureFlow(String.class,
						section.getOfficeSectionInput("secure"), section.getOfficeSectionInput("insecure"));
				office.link(section.getOfficeSectionOutput("decision"), secureFlow);
			});
		});

		// Ensure insecure flow followed
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "INSECURE - argument");

		// Ensure insecure flow if not in role
		response = this.server.send(this.mockRequest("/path", "no_access", "no_access"));
		response.assertResponse(200, "INSECURE - argument");

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		response.assertResponse(200, "SECURE - argument");
	}

	public static class HttpSectionSecurerFlowServicerWithArgument {
		@NextFunction("decision")
		public String service() {
			return "argument";
		}

		public void secure(ServerHttpConnection connection, @Parameter String parameter) throws IOException {
			connection.getResponse().getEntityWriter().write("SECURE - " + parameter);
		}

		public void insecure(ServerHttpConnection connection, @Parameter String parameter) throws IOException {
			connection.getResponse().getEntityWriter().write("INSECURE - " + parameter);
		}
	}

	/**
	 * Ensure can secure {@link Flow} section.
	 */
	public void testHttpSectionSecurerFlow() throws Exception {
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("app",
					new MockChallengeHttpSecuritySource("secure"));

			// Configure securer
			AbstractHttpSecurable securable = new AbstractHttpSecurable() {
			};
			securable.addRole("role");
			HttpSecurer securer = builder.createHttpSecurer(securable);

			// Configure the section
			OfficeSection section = context.getOfficeArchitect().addOfficeSection("section",
					new HttpSectionSecurerFlowSection(securer.createFlowSecurer(), HttpSectionSecurerFlowServicer.class,
							null),
					null);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));
		});

		// Ensure insecure flow followed
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "INSECURE");

		// Ensure insecure flow if not in role
		response = this.server.send(this.mockRequest("/path", "no_access", "no_access"));
		response.assertResponse(200, "INSECURE");

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		response.assertResponse(200, "SECURE");
	}

	public class HttpSectionSecurerFlowSection extends AbstractSectionSource {

		private final HttpFlowSecurer securer;

		private final Class<?> sectionLogicClass;

		private final Class<?> argumentType;

		private HttpSectionSecurerFlowSection(HttpFlowSecurer securer, Class<?> sectionLogicClass,
				Class<?> argumentType) {
			this.securer = securer;
			this.sectionLogicClass = sectionLogicClass;
			this.argumentType = argumentType;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Add the sub section
			SubSection subSection = designer.addSubSection("sub_section", ClassSectionSource.class.getName(),
					this.sectionLogicClass.getName());
			designer.link(subSection.getSubSectionObject(ServerHttpConnection.class.getName()),
					designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
							ServerHttpConnection.class.getName()));
			designer.link(subSection.getSubSectionOutput(IOException.class.getName()),
					designer.addSectionOutput(IOException.class.getSimpleName(), IOException.class.getName(), true));

			// Configure secure flow
			SectionFlowSinkNode secureFlow = this.securer.secureFlow(designer, this.argumentType,
					subSection.getSubSectionInput("secure"), subSection.getSubSectionInput("insecure"));
			designer.link(subSection.getSubSectionOutput("decision"), secureFlow);
			designer.link(designer.addSectionInput("service", null), subSection.getSubSectionInput("service"));
		}
	}

	/**
	 * Ensure can secure {@link Flow} section with argument.
	 */
	public void testHttpSectionSecurerFlowWithArgument() throws Exception {
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide security
			HttpSecurityBuilder builder = security.addHttpSecurity("app",
					new MockChallengeHttpSecuritySource("secure"));

			// Configure securer
			AbstractHttpSecurable securable = new AbstractHttpSecurable() {
			};
			securable.addRole("role");
			HttpSecurer securer = builder.createHttpSecurer(securable);

			// Configure the section
			OfficeSection section = context.getOfficeArchitect().addOfficeSection("section",
					new HttpSectionSecurerFlowSection(securer.createFlowSecurer(),
							HttpSectionSecurerFlowServicerWithArgument.class, String.class),
					null);
			office.link(web.getHttpInput(false, "/path").getInput(), section.getOfficeSectionInput("service"));
		});

		// Ensure insecure flow followed
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "INSECURE - argument");

		// Ensure insecure flow if not in role
		response = this.server.send(this.mockRequest("/path", "no_access", "no_access"));
		response.assertResponse(200, "INSECURE - argument");

		// May access if have role
		response = this.server.send(this.mockRequest("/path", "role", "role"));
		response.assertResponse(200, "SECURE - argument");
	}

	/**
	 * Ensure can trigger logout.
	 */
	public void testCustom_Logout() throws Exception {
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"));

			OfficeSection section = context.addSection("section", CustomLogout.class);
			office.link(web.getHttpInput(false, "/login").getInput(), section.getOfficeSectionInput("login"));
			office.link(web.getHttpInput(false, "/logout").getInput(), section.getOfficeSectionInput("logout"));
			office.link(web.getHttpInput(false, "/check").getInput(), section.getOfficeSectionInput("check"));
		});

		// Ensure login
		MockHttpResponse response = this.server.send(this.mockRequest("/login", "test", "test"));
		response.assertResponse(200, "login");

		// Trigger logout
		response = this.server.send(MockHttpServer.mockRequest("/logout").cookies(response));
		response.assertResponse(200, "logout");

		// Ensure logged out
		response = this.server.send(MockHttpServer.mockRequest("/check").cookies(response));
		response.assertResponse(200, "guest");
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
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			security.addHttpSecurity("api", new MockChallengeHttpSecuritySource("one"));

			OfficeSection section = context.addSection("section", CustomLogout.class);
			office.link(web.getHttpInput(false, "/login").getInput(), section.getOfficeSectionInput("login"));
			office.link(web.getHttpInput(false, "/logout").getInput(), section.getOfficeSectionInput("logout"));
			office.link(web.getHttpInput(false, "/check").getInput(), section.getOfficeSectionInput("check"));
		});

		// Ensure login
		MockHttpResponse response = this.server.send(this.mockRequest("/login", "test", "test"));
		response.assertResponse(200, "login");

		// Trigger logout
		response = this.server.send(MockHttpServer.mockRequest("/logout").cookies(response));
		response.assertResponse(200, "logout");

		// Ensure logged out
		response = this.server.send(MockHttpServer.mockRequest("/check").cookies(response));
		response.assertResponse(200, "guest");
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
	 * Ensure able to invoke start up {@link ProcessState} and stop
	 * {@link HttpSecuritySource}.
	 */
	public void testExecuteContext() throws Exception {

		// Initiate state to test
		MockExecutionHttpSecuritySource.isStarted = false;
		MockExecutionHttpSecuritySource.isStartupComplete = false;
		MockExecutionHttpSecuritySource.isStopped = false;
		MockExecutionStartupHandler.startupParameter = null;

		// Compile and open
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Configure the security (for startup testing)
			HttpSecurityBuilder httpSecurity = security.addHttpSecurity("execute",
					new MockExecutionHttpSecuritySource());
			httpSecurity.addProperty(MockExecutionHttpSecuritySource.PROPERTY_REALM, "test");

			// Mock Credential Store
			office.addOfficeManagedObjectSource("CREDENTIAL_STORE",
					MockCredentialStoreManagedObjectSource.class.getName())
					.addOfficeManagedObject("CREDENTIAL_STORE", ManagedObjectScope.PROCESS);

			// Link in startup functions
			OfficeSection section = context.addSection("section", MockExecutionStartupHandler.class);
			office.link(httpSecurity.getOutput("form"), section.getOfficeSectionInput("startup"));
		});

		// Should be started (but not stopped)
		assertTrue("Should be started", MockExecutionHttpSecuritySource.isStarted);
		assertTrue("Should have completed startup (single threaded)",
				MockExecutionHttpSecuritySource.isStartupComplete);
		assertFalse("Should not yet be stopped", MockExecutionHttpSecuritySource.isStopped);

		// Close the OfficeFloor (to stop HttpSecurity)
		this.officeFloor.close();
		assertTrue("Should have stopped", MockExecutionHttpSecuritySource.isStopped);
	}

	@TestSource
	public static class MockExecutionHttpSecuritySource extends FormHttpSecuritySource {

		private static boolean isStarted = false;

		private static boolean isStartupComplete = false;

		private static boolean isStopped = false;

		@Override
		public void start(HttpSecurityExecuteContext<Flows> context) throws Exception {
			isStarted = true;
			final String parameter = "PARAMETER";
			context.registerStartupProcess(Flows.FORM_LOGIN_PAGE, parameter, (error) -> {
				if (error != null) {
					throw error;
				}
				assertSame("Incorrect startup parameter", parameter, MockExecutionStartupHandler.startupParameter);
				isStartupComplete = true;
			});
		}

		@Override
		public void stop() {
			isStopped = true;
		}
	}

	public static class MockExecutionStartupHandler {

		private static Object startupParameter;

		public void startup(@Parameter Object parameter) {
			startupParameter = parameter;
		}
	}

	/**
	 * Creates a {@link MockHttpRequestBuilder} with credentials.
	 * 
	 * @param path     Path.
	 * @param userName User name.
	 * @param password Password.
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
		 * @param context  {@link CompileWebContext}.
		 * @param security {@link HttpSecurityArchitect}.
		 */
		void initialise(CompileWebContext context, HttpSecurityArchitect security);
	}

	/**
	 * Compiles with the {@link Initialiser}.
	 * 
	 * @param initialiser {@link Initialiser}.
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