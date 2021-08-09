/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
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
import net.officefloor.web.security.scheme.AbstractMockHttpSecuritySource;
import net.officefloor.web.security.scheme.MockAccessControl;
import net.officefloor.web.security.scheme.MockAuthentication;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.security.scheme.MockCredentials;
import net.officefloor.web.security.scheme.MockFlowHttpSecuritySource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityActionContext;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;

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
		this.compile.mockHttpServer((server) -> this.server = server);
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
		@Next("decision")
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
		@Next("decision")
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

			// Link in startup functions
			OfficeSection section = context.addSection("section", MockExecutionStartupHandler.class);
			office.link(httpSecurity.getOutput(MockExecutionHttpSecuritySource.Flows.CHALLENGE.name()),
					section.getOfficeSectionInput("startup"));
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
	public static class MockExecutionHttpSecuritySource extends MockFlowHttpSecuritySource {

		private static boolean isStarted = false;

		private static boolean isStartupComplete = false;

		private static boolean isStopped = false;

		@Override
		public void start(HttpSecurityExecuteContext<Flows> context) throws Exception {
			isStarted = true;
			final String parameter = "PARAMETER";
			context.registerStartupProcess(Flows.CHALLENGE, parameter, (error) -> {
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
	 * Ensure can use supporting {@link ManagedObject} instances.
	 */
	public void testSupportingManagedObject() throws Throwable {

		// Compile and open
		this.compile((context, security) -> {

			// Configure the security (with supporting object)
			HttpSecurityBuilder httpSecurity = security.addHttpSecurity("execute",
					new MockSupportedHttpSecuritySource(""));
			httpSecurity.addProperty(MockSupportedHttpSecuritySource.PROPERTY_REALM, "test");

			// Add the section
			context.addSection("section", MockSingleSupportingObjectSection.class);
		});

		// Ensure the supporting object is available
		MockSingleSupportingObjectSection.supportingObject = null;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "section.service", null);
		assertNotNull("Should make supporting object available", MockSingleSupportingObjectSection.supportingObject);
	}

	public static class MockSingleSupportingObjectSection {

		private static MockSupportingObject supportingObject;

		public void service(MockSupportingObject object) {
			supportingObject = object;
		}
	}

	@TestSource
	public static class MockSupportedHttpSecuritySource extends MockChallengeHttpSecuritySource {

		private final String identifier;

		private MockSupportedHttpSecuritySource(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public HttpSecuritySourceMetaData<MockAuthentication, MockAccessControl, Void, None, None> init(
				HttpSecuritySourceContext context) throws Exception {

			// Load the supporting object (validating properties)
			HttpSecuritySupportingManagedObject<Indexed> support = context.addSupportingManagedObject("support",
					new ClassManagedObjectSource(), ManagedObjectScope.THREAD);
			support.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					MockSupportingObject.class.getName());

			// Load the identifier
			context.addSupportingManagedObject("identified",
					new Singleton(new MockIdentifiedSupportingObject(this.identifier)), ManagedObjectScope.PROCESS);

			// Load default meta-data
			return super.init(context);
		}
	}

	public static class MockSupportingObject {
	}

	public static class MockIdentifiedSupportingObject {

		private String identifier;

		private MockIdentifiedSupportingObject(String identifier) {
			this.identifier = identifier;
		}
	}

	/**
	 * Ensure can use supporting {@link ManagedObject} instances are qualified when
	 * multiple by the same name.
	 */
	public void testQualifiedSupportingManagedObject() throws Throwable {

		// Compile and open
		this.compile((context, security) -> {

			// Configure security with clashing names (so requires qualifying)
			security.addHttpSecurity("one", new MockSupportedHttpSecuritySource("one"))
					.addProperty(MockSupportedHttpSecuritySource.PROPERTY_REALM, "test");
			security.addHttpSecurity("two", new MockSupportedHttpSecuritySource("two"))
					.addProperty(MockSupportedHttpSecuritySource.PROPERTY_REALM, "test");

			// Add the section
			context.addSection("section", MockQualifiedSupportingObjectSection.class);
		});

		// Ensure the supporting object is available via qualification
		MockQualifiedSupportingObjectSection.supportingOne = null;
		MockQualifiedSupportingObjectSection.supportingTwo = null;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "section.service", null);
		BiConsumer<MockIdentifiedSupportingObject, String> assertQualifiedSupportingObject = (object, identifier) -> {
			assertNotNull("Should have qualified supporting object for " + identifier, object);
			assertEquals("Incorrect supporting object", identifier, object.identifier);
		};
		assertQualifiedSupportingObject.accept(MockQualifiedSupportingObjectSection.supportingOne, "one");
		assertQualifiedSupportingObject.accept(MockQualifiedSupportingObjectSection.supportingTwo, "two");
	}

	public static class MockQualifiedSupportingObjectSection {

		private static MockIdentifiedSupportingObject supportingOne;

		private static MockIdentifiedSupportingObject supportingTwo;

		public void service(@Qualified("one") MockIdentifiedSupportingObject one,
				@Qualified("two") MockIdentifiedSupportingObject two) {
			supportingOne = one;
			supportingTwo = two;
		}
	}

	/**
	 * Ensure uniquely typed {@link HttpSecuritySupportingManagedObject}
	 * dependencies are registered unqualified.
	 */
	public void testUniqueSupportingManagedObjectNotQualifiedOnMultipleSecurities() throws Throwable {

		// Compile and open
		this.compile((context, security) -> {

			// Configure security with clashing names (so requires qualifying)
			security.addHttpSecurity("one", new MockSupportedHttpSecuritySource("one"))
					.addProperty(MockSupportedHttpSecuritySource.PROPERTY_REALM, "test");
			security.addHttpSecurity("two", new MockChallengeHttpSecuritySource())
					.addProperty(MockChallengeHttpSecuritySource.PROPERTY_REALM, "test");

			// Add the section
			context.addSection("section", UniqueSupportingManagedObjectNotQualifiedOnMultipleSecuritiesSection.class);
		});

		// Ensure the supporting object is available
		UniqueSupportingManagedObjectNotQualifiedOnMultipleSecuritiesSection.supporting = null;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "section.service", null);
		assertNotNull("Should have supporting object",
				UniqueSupportingManagedObjectNotQualifiedOnMultipleSecuritiesSection.supporting);
		assertEquals("Incorrect supporting object", "one",
				UniqueSupportingManagedObjectNotQualifiedOnMultipleSecuritiesSection.supporting.identifier);
	}

	public static class UniqueSupportingManagedObjectNotQualifiedOnMultipleSecuritiesSection {

		private static MockIdentifiedSupportingObject supporting;

		public void service(MockIdentifiedSupportingObject object) {
			supporting = object;
		}
	}

	/**
	 * Ensure can link {@link HttpSecuritySupportingManagedObject} dependencies.
	 */
	public void testSupportingManagedObjectDependencies() throws Throwable {

		// Create the supporting objects
		MockSupportingObject dependency = new MockSupportingObject();
		MockDependencyManagedObjectSource supporting = new MockDependencyManagedObjectSource();

		// Compile and open
		this.compile((context, security) -> {

			// Configure the security (with supporting object with dependencies)
			security.addHttpSecurity("dependencies",
					new MockDependencySupportedHttpSecuritySource("TEST", dependency, supporting));

			// Add the section
			context.link(false, "/dependencies", MockDependencySupportingObjectSection.class);
		});

		// Ensure the dependencies are available
		MockDependencySupportingObjectSection.supportingObject = null;
		this.server.send(MockHttpServer.mockRequest("/dependencies").header("Authorization", "Mock daniel,daniel"))
				.assertResponse(200, "Loaded");
		assertNotNull("Should make supporting object available",
				MockDependencySupportingObjectSection.supportingObject);
		MockDependencyManagedObjectSource object = MockDependencySupportingObjectSection.supportingObject;
		assertNotNull("Should have authentication", object.authentication);
		assertNotNull("Should have HTTP authentication", object.httpAuthentication);
		assertNotNull("Should have access control", object.accessControl);
		assertNotNull("Should have HTTP access control", object.httpAccessControl);
		assertSame("Should have supporting dependency", dependency, object.otherSupporting);
	}

	public static class MockDependencySupportingObjectSection {

		private static MockDependencyManagedObjectSource supportingObject;

		public void service(MockDependencyManagedObjectSource object, ServerHttpConnection connection)
				throws IOException {
			supportingObject = object;
			connection.getResponse().getEntityWriter().write("Loaded");
		}
	}

	@TestSource
	public static class MockDependencySupportedHttpSecuritySource extends MockChallengeHttpSecuritySource {

		private final MockSupportingObject dependency;

		private final MockDependencyManagedObjectSource supportingObject;

		public MockDependencySupportedHttpSecuritySource(String realm, MockSupportingObject dependency,
				MockDependencyManagedObjectSource supportingObject) {
			super(realm);
			this.dependency = dependency;
			this.supportingObject = supportingObject;
		}

		@Override
		public HttpSecuritySourceMetaData<MockAuthentication, MockAccessControl, Void, None, None> init(
				HttpSecuritySourceContext context) throws Exception {

			// Load the dependency supporting object
			HttpSecuritySupportingManagedObject<?> dependency = context.addSupportingManagedObject("DEPENDENCY",
					new Singleton(this.dependency), ManagedObjectScope.PROCESS);

			// Load the supporting object with dependencies
			HttpSecuritySupportingManagedObject<Dependencies> supporting = context
					.addSupportingManagedObject("SUPPORTING", this.supportingObject, ManagedObjectScope.THREAD);
			supporting.linkAuthentication(Dependencies.AUTHENTICATION);
			supporting.linkHttpAuthentication(Dependencies.HTTP_AUTHENTICATION);
			supporting.linkAccessControl(Dependencies.ACCESS_CONTROL);
			supporting.linkHttpAccessControl(Dependencies.HTTP_ACCESS_CONTROL);
			supporting.linkSupportingManagedObject(Dependencies.OTHER_SUPPORTING, dependency);

			// Load default meta-data
			return super.init(context);
		}
	}

	public static enum Dependencies {
		AUTHENTICATION, HTTP_AUTHENTICATION, ACCESS_CONTROL, HTTP_ACCESS_CONTROL, OTHER_SUPPORTING
	}

	@TestSource
	public static class MockDependencyManagedObjectSource extends AbstractManagedObjectSource<Dependencies, None>
			implements CoordinatingManagedObject<Dependencies> {

		private MockAuthentication authentication;
		private HttpAuthentication<Void> httpAuthentication;
		private MockAccessControl accessControl;
		private HttpAccessControl httpAccessControl;
		private MockSupportingObject otherSupporting;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.setManagedObjectClass(this.getClass());
			context.addDependency(Dependencies.AUTHENTICATION, MockAuthentication.class);
			context.addDependency(Dependencies.HTTP_AUTHENTICATION, HttpAuthentication.class);
			context.addDependency(Dependencies.ACCESS_CONTROL, MockAccessControl.class);
			context.addDependency(Dependencies.HTTP_ACCESS_CONTROL, HttpAccessControl.class);
			context.addDependency(Dependencies.OTHER_SUPPORTING, MockSupportingObject.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {
			this.authentication = (MockAuthentication) registry.getObject(Dependencies.AUTHENTICATION);
			this.httpAuthentication = (HttpAuthentication<Void>) registry.getObject(Dependencies.HTTP_AUTHENTICATION);
			this.accessControl = (MockAccessControl) registry.getObject(Dependencies.ACCESS_CONTROL);
			this.httpAccessControl = (HttpAccessControl) registry.getObject(Dependencies.HTTP_ACCESS_CONTROL);
			this.otherSupporting = (MockSupportingObject) registry.getObject(Dependencies.OTHER_SUPPORTING);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * Ensure can provide authentication state.
	 */
	public void testHttpRequestState() throws Throwable {

		// Compile and open
		MockAuthentiationStateHttpSecuritySource source = new MockAuthentiationStateHttpSecuritySource();
		this.compile((context, security) -> {
			security.addHttpSecurity("mock", source)
					.addProperty(MockAuthentiationStateHttpSecuritySource.PROPERTY_REALM, "test");
			context.link(false, "/login", AuthenticateStateLogin.class);
			context.link(false, "/logout", AuthenticateStateLogout.class);
		});

		// Ensure initial state
		Consumer<String> setup = (previousChallengeInnvocation) -> {
			AuthenticateStateLogin.isLoggedIn = false;
			source.setup(previousChallengeInnvocation);
		};

		// Provides assertion of state
		BiConsumer<String, String[]> assertState = (message, expectedInvocations) -> {
			Set<String> invocations = new HashSet<>(Arrays.asList(expectedInvocations));
			assertEquals(message + ": ratify", invocations.remove("RATIFY"), source.isRatifyInvoked);
			assertEquals(message + ": authenticate", invocations.remove("AUTHENTICATE"), source.isAuthenticateInvoked);
			assertEquals(message + ": challenge", invocations.remove("CHALLENGE"), source.isChallengeInvoked);
			assertEquals(message + ": login", invocations.remove("LOGIN"), AuthenticateStateLogin.isLoggedIn);
			assertEquals(message + ": logout", invocations.remove("LOGOUT"), source.isLogoutInvoked);
			assertEquals(message + ": invalid state", 0, invocations.size());
		};

		// Attempt login without credentials
		setup.accept("RATIFY");
		this.server.send(MockHttpServer.mockRequest("/login")).assertResponse(HttpStatus.UNAUTHORIZED.getStatusCode(),
				"");
		assertState.accept("No credentials", new String[] { "RATIFY", "CHALLENGE" });

		// Attempt login with invalid credentials
		setup.accept("AUTHENTICATE");
		this.server.send(MockHttpServer.mockRequest("/login").header("Authorization", "Mock daniel:invalid"))
				.assertResponse(HttpStatus.UNAUTHORIZED.getStatusCode(), "");
		assertState.accept("Invalid credentials", new String[] { "RATIFY", "AUTHENTICATE", "CHALLENGE" });

		// Attempt login
		setup.accept("AUTHENTICATE");
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/login").header("Authorization", "Mock daniel,daniel"));
		response.assertResponse(200, "Logged In");
		assertState.accept("Valid credentials", new String[] { "RATIFY", "AUTHENTICATE", "LOGIN" });

		// Attempt logout
		setup.accept("RATIFY");
		MockHttpRequestBuilder request = MockHttpServer.mockRequest("/logout");
		request.cookies(response);
		this.server.send(request).assertResponse(200, "Logged Out");
		assertState.accept("Logout", new String[] { "RATIFY", "LOGOUT" });
	}

	public static class AuthenticateStateLogin {
		private static boolean isLoggedIn = false;

		public void service(HttpAccessControl accessControl, ServerHttpConnection connection) throws IOException {
			isLoggedIn = true;
			connection.getResponse().getEntityWriter().write("Logged In");
		}
	}

	public static class AuthenticateStateLogout {
		public void service(HttpAuthentication<?> authentication, ServerHttpConnection connection) throws IOException {
			authentication.logout((context) -> {
			});
			connection.getResponse().getEntityWriter().write("Logged Out");
		}
	}

	@TestSource
	public static class MockAuthentiationStateHttpSecuritySource extends MockChallengeHttpSecuritySource {

		private static final String ATTRIBUTE_NAME = "state";

		private static final String QUALIFIED_ATTRIBUTE_NAME = HttpSecurity.class.getName() + ".mock.state";

		private boolean isRatifyInvoked = false;

		private boolean isAuthenticateInvoked = false;

		private String previousChallengeInnvocation = null;

		private boolean isChallengeInvoked = false;

		private boolean isLogoutInvoked = false;

		private void setup(String previousChallengeInnvocation) {
			this.isRatifyInvoked = false;
			this.isAuthenticateInvoked = false;
			this.previousChallengeInnvocation = previousChallengeInnvocation;
			this.isChallengeInvoked = false;
			this.isLogoutInvoked = false;
		}

		private String getStateAttributeName(HttpSecurityActionContext context) {
			String stateName = context.getQualifiedAttributeName(ATTRIBUTE_NAME);
			assertEquals("Incorrect qualified name", QUALIFIED_ATTRIBUTE_NAME, stateName);
			return stateName;
		}

		@Override
		public boolean ratify(Void credentials, RatifyContext<MockAccessControl> context) {
			String stateName = this.getStateAttributeName(context);
			assertNull("Should not have state for ratify", context.getRequestState().getAttribute(stateName));
			context.getRequestState().setAttribute(stateName, "RATIFY");
			this.isRatifyInvoked = true;
			return super.ratify(credentials, context);
		}

		@Override
		public void authenticate(Void credentials, AuthenticateContext<MockAccessControl, None, None> context)
				throws HttpException {
			String stateName = this.getStateAttributeName(context);
			assertEquals("Incorrect state for authenticate", "RATIFY",
					context.getRequestState().getAttribute(stateName));
			context.getRequestState().setAttribute(stateName, "AUTHENTICATE");
			this.isAuthenticateInvoked = true;
			super.authenticate(credentials, context);
		}

		@Override
		public void challenge(ChallengeContext<None, None> context) throws HttpException {
			String stateName = this.getStateAttributeName(context);
			assertEquals("Incorrect state for challenge", this.previousChallengeInnvocation,
					context.getRequestState().getAttribute(stateName));
			this.isChallengeInvoked = true;
			super.challenge(context);
		}

		@Override
		public void logout(LogoutContext<None, None> context) throws HttpException {
			String stateName = this.getStateAttributeName(context);
			assertEquals("Incorrect state for logout", "RATIFY", context.getRequestState().getAttribute(stateName));
			this.isLogoutInvoked = true;
			super.logout(context);
		}
	}

	/**
	 * Ensure able to invoke {@link Flow} from {@link AuthenticateContext} and
	 * {@link LogoutContext}.
	 */
	public void testAuthenticateLogoutFlows() throws Throwable {

		// Compile and open
		MockAuthenticateLogoutFlowsHttpSecuritySource source = new MockAuthenticateLogoutFlowsHttpSecuritySource();
		this.compile((context, security) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Add HTTP Security
			HttpSecurityBuilder httpSecurity = security.addHttpSecurity("mock", source);

			// Configure flow handling
			OfficeSection section = context.addSection("section", AuthenticateLogoutSection.class);
			office.link(httpSecurity.getOutput(AuthenticateLogoutFlows.AUTHENTICATE.name()),
					section.getOfficeSectionInput("authenticate"));
			office.link(httpSecurity.getOutput(AuthenticateLogoutFlows.LOGOUT.name()),
					section.getOfficeSectionInput("logout"));

			// Configure handling
			office.link(web.getHttpInput(false, "/flows").getInput(), section.getOfficeSectionInput("service"));
		});

		// Reset state
		AuthenticateLogoutSection.authenticatedArgument = null;
		AuthenticateLogoutSection.logoutArgument = null;
		source.isLogoutComplete = false;

		// Undertake request
		this.server.send(MockHttpServer.mockRequest("/flows")).assertResponse(HttpStatus.OK.getStatusCode(),
				"serviced");

		// Ensure authenticate flows invoked
		assertEquals("Should trigger authenticate flow", "AUTHENTICATE",
				AuthenticateLogoutSection.authenticatedArgument);

		// Ensure logout flows invoked
		assertEquals("Should trigger logout flow", "LOGOUT", AuthenticateLogoutSection.logoutArgument);
		assertTrue("Should be logged out", source.isLogoutComplete);
	}

	public static class AuthenticateLogoutSection {

		private static String authenticatedArgument = null;

		private static String logoutArgument = null;

		public void authenticate(@Parameter String value) {
			authenticatedArgument = value;
		}

		public void logout(@Parameter String value) {
			logoutArgument = value;
		}

		@HttpAccess(ifAllRoles = "test")
		public void service(MockAuthentication authentication, ServerHttpConnection connection) throws IOException {
			assertTrue("Should have access", authentication.getAccessControl().getRoles().contains("test"));
			authentication.logout(null);
			connection.getResponse().getEntityWriter().write("serviced");
		}
	}

	public static enum AuthenticateLogoutFlows {
		AUTHENTICATE, LOGOUT
	}

	@TestSource
	public static class MockAuthenticateLogoutFlowsHttpSecuritySource
			extends AbstractMockHttpSecuritySource<Void, None, AuthenticateLogoutFlows> {

		private boolean isLogoutComplete = false;

		@Override
		protected void loadMetaData(
				MetaDataContext<MockAuthentication, MockAccessControl, Void, None, AuthenticateLogoutFlows> context)
				throws Exception {
			super.loadMetaData(context);
			context.addFlow(AuthenticateLogoutFlows.AUTHENTICATE, String.class);
			context.addFlow(AuthenticateLogoutFlows.LOGOUT, String.class);
		}

		@Override
		public void authenticate(Void credentials,
				AuthenticateContext<MockAccessControl, None, AuthenticateLogoutFlows> context) throws HttpException {
			context.doFlow(AuthenticateLogoutFlows.AUTHENTICATE, "AUTHENTICATE", (error) -> {
				if (error != null) {
					throw error;
				}
				context.accessControlChange(new MockAccessControl("flow", "test"), null);
			});
		}

		@Override
		public void logout(LogoutContext<None, AuthenticateLogoutFlows> context) throws HttpException {
			context.doFlow(AuthenticateLogoutFlows.LOGOUT, "LOGOUT", (error) -> {
				if (error != null) {
					throw error;
				}
				this.isLogoutComplete = true;
			});
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
