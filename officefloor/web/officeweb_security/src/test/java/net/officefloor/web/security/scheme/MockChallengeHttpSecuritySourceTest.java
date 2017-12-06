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
package net.officefloor.web.security.scheme;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link BasicHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockChallengeHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(MockChallengeHttpSecuritySource.class, "realm", "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(HttpAccessControl.class);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, MockChallengeHttpSecuritySource.class, "realm", "test");
	}

	/**
	 * Ensure can ratify from cached {@link HttpAccessControl}.
	 */
	public void testRatifyFromSession() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>(
				this, null);
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(session, session.getAttribute("http.security.mock"), accessControl);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached", security.ratify(null, ratifyContext));
		assertSame("Incorrect access control", accessControl, ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can ratify if have authorization header.
	 */
	public void testRatifyWithAuthorizationHeader() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>(
				this, "Basic ZGFuaWVsOmRhbmllbA");

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(session, session.getAttribute("http.security.mock"), null);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate", security.ratify(null, ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure ratify indicates no authentication credentials.
	 */
	public void testRatifyNoAuthentication() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>(
				this, null);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(session, session.getAttribute("http.security.mock"), null);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake ratify
		assertFalse("Should not attempt authentication", security.ratify(null, ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load challenge.
	 */
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<None, None> challengeContext = new MockHttpChallengeContext<None, None>(this);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "Test");

		// Undertake the challenge
		security.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Ensure correct challenge
		assertEquals("Incorrect challenge", "Basic realm=\"Test\"", challengeContext.getChallenge());
	}

	/**
	 * Ensure not authenticated with no authorization header.
	 */
	public void testNoAuthorizationHeader() throws Exception {
		this.doAuthenticate(null, false, null);
	}

	/**
	 * Ensure handle incorrect authentication scheme.
	 */
	public void testIncorrectAuthenticationScheme() throws Exception {
		this.doAuthenticate("Incorrect ZGFuaWVsOmRhbmllbA", false, null);
	}

	/**
	 * Ensure handle invalid Base64 encoding.
	 */
	public void testInvalidAuthorizationHeader() throws Exception {
		this.doAuthenticate("Basic wrong", false, null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {
		this.doAuthenticate("Basic ZGFuaWVsOmRhbmllbA", true, "daniel", "daniel");
	}

	/**
	 * Ensure can authenticate with multiple roles.
	 */
	public void testMultipleRoleAuthenticate() throws Exception {
		this.doAuthenticate("Basic ZGFuaWVsLCBmb3VuZGVyOmRhbmllbCwgZm91bmRlcg==", true, "daniel, founder", "daniel",
				"founder");
	}

	/**
	 * Ensure can extra spacing.
	 */
	public void testExtraSpacing() throws Exception {
		this.doAuthenticate("  Basic    ZGFuaWVsOmRhbmllbA  ", true, "daniel", "daniel");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<None> logoutContext = new MockHttpLogoutContext<None>(this);

		// Record logging out
		HttpSession session = logoutContext.getSession();
		session.removeAttribute("http.security.mock");

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Logout
		security.logout(logoutContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param authoriseHttpHeaderValue
	 *            <code>Authorize</code> {@link HttpHeader} value.
	 * @param isLoadSession
	 *            Indicates if load {@link HttpSession}.
	 * @param userName
	 *            User name if authenticated. <code>null</code> if not
	 *            authenticated.
	 * @param roles
	 *            Expected roles.
	 */
	private void doAuthenticate(String authoriseHttpHeaderValue, boolean isLoadSession, String userName,
			String... roles) throws IOException {

		// Create the authentication context
		MockHttpAuthenticateContext<HttpAccessControl, None> authenticationContext = new MockHttpAuthenticateContext<HttpAccessControl, None>(
				this, authoriseHttpHeaderValue);

		// Load to session
		if (isLoadSession) {
			authenticationContext.recordRegisterAccessControlWithHttpSession("http.security.mock");
		}

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake the authenticate
		security.authenticate(null, authenticationContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Validate authentication
		HttpAccessControl accessControl = authenticationContext.getAccessControl();
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);
			assertNull("Should not register HTTP Security with HTTP Session",
					authenticationContext.getRegisteredAccessControlWithHttpSession());

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", "Mock", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect principle", userName, accessControl.getPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.inRole(role));
			}
			assertSame("Same access control should be registered with HTTP Session", accessControl,
					authenticationContext.getRegisteredAccessControlWithHttpSession());
		}
	}

}