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
package net.officefloor.plugin.web.http.security.scheme;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderUtil;
import net.officefloor.plugin.web.http.security.type.HttpSecurityTypeBuilder;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link BasicHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpAuthenticateContext}.
	 */
	private final MockHttpAuthenticateContext<HttpSecurity, Void, None> authenticationContext = new MockHttpAuthenticateContext<HttpSecurity, Void, None>(
			null, this);

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil
				.validateSpecification(MockHttpSecuritySource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil
				.createHttpSecurityTypeBuilder();
		type.setSecurityClass(HttpSecurity.class);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type,
				MockHttpSecuritySource.class);
	}

	/**
	 * Ensure can ratify from cached {@link HttpSecurity}.
	 */
	public void testRatifyFromSession() throws IOException {

		final MockHttpRatifyContext<HttpSecurity, Void> ratifyContext = new MockHttpRatifyContext<HttpSecurity, Void>(
				null, this);
		final HttpSecurity security = this.createMock(HttpSecurity.class);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(
				session,
				session.getAttribute("http.security.source.mock.http.security"),
				security);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		MockHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockHttpSecuritySource.class);

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached",
				source.ratify(ratifyContext));
		assertSame("Incorrect security", security,
				ratifyContext.getHttpSecurity());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can ratify if have authorization header.
	 */
	public void testRatifyWithAuthorizationHeader() throws IOException {

		final MockHttpRatifyContext<HttpSecurity, Void> ratifyContext = new MockHttpRatifyContext<HttpSecurity, Void>(
				null, this);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(
				session,
				session.getAttribute("http.security.source.mock.http.security"),
				null);
		ratifyContext.recordAuthorizationHeader("Basic ZGFuaWVsOmRhbmllbA");

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		MockHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockHttpSecuritySource.class);

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate",
				source.ratify(ratifyContext));
		assertNull("Should not yet have security",
				ratifyContext.getHttpSecurity());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure ratify indicates no authentication credentials.
	 */
	public void testRatifyNoAuthentication() throws IOException {

		final MockHttpRatifyContext<HttpSecurity, Void> ratifyContext = new MockHttpRatifyContext<HttpSecurity, Void>(
				null, this);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(
				session,
				session.getAttribute("http.security.source.mock.http.security"),
				null);
		ratifyContext.recordAuthorizationHeader(null);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		MockHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockHttpSecuritySource.class);

		// Undertake ratify
		assertFalse("Should not attempt authentication",
				source.ratify(ratifyContext));
		assertNull("Should not yet have security",
				ratifyContext.getHttpSecurity());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load challenge.
	 */
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<None, None> challengeContext = new MockHttpChallengeContext<None, None>(
				this);

		// Record the challenge
		challengeContext.recordAuthenticateChallenge("Basic realm=\"Test\"");

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		MockHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockHttpSecuritySource.class);

		// Undertake the challenge
		source.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure not authenticated with no authorization header.
	 */
	public void testNoAuthorizationHeader() throws Exception {

		// Record no authorization header
		this.authenticationContext.recordAuthorizationHeader(null);

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure handle incorrect authentication scheme.
	 */
	public void testIncorrectAuthenticationScheme() throws Exception {

		// Record authenticate
		this.authenticationContext
				.recordAuthorizationHeader("Incorrect ZGFuaWVsOmRhbmllbA");

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure handle invalid Base64 encoding.
	 */
	public void testInvalidAuthorizationHeader() throws Exception {

		// Record authenticate
		this.authenticationContext.recordAuthorizationHeader("Basic wrong");

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {

		// Record simple authenticate
		this.authenticationContext
				.recordAuthorizationHeader("Basic ZGFuaWVsOmRhbmllbA");
		this.authenticationContext
				.recordRegisterHttpSecurityWithHttpSession("http.security.source.mock.http.security");

		// Test
		this.doAuthenticate("daniel", "daniel");
	}

	/**
	 * Ensure can authenticate with multiple roles.
	 */
	public void testMultipleRoleAuthenticate() throws Exception {

		// Record simple authenticate
		this.authenticationContext
				.recordAuthorizationHeader("Basic ZGFuaWVsLCBmb3VuZGVyOmRhbmllbCwgZm91bmRlcg==");
		this.authenticationContext
				.recordRegisterHttpSecurityWithHttpSession("http.security.source.mock.http.security");

		// Test
		this.doAuthenticate("daniel, founder", "daniel", "founder");
	}

	/**
	 * Ensure can extra spacing.
	 */
	public void testExtraSpacing() throws Exception {

		// Record authenticate
		this.authenticationContext
				.recordAuthorizationHeader("  Basic    ZGFuaWVsOmRhbmllbA  ");
		this.authenticationContext
				.recordRegisterHttpSecurityWithHttpSession("http.security.source.mock.http.security");

		// Test
		this.doAuthenticate("daniel", "daniel");
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param userName
	 *            User name if authenticated. <code>null</code> if not
	 *            authenticated.
	 * @param roles
	 *            Expected roles.
	 */
	private void doAuthenticate(String userName, String... roles)
			throws IOException {

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the source
		MockHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockHttpSecuritySource.class);

		// Undertake the authenticate
		source.authenticate(this.authenticationContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Validate authentication
		HttpSecurity security = this.authenticationContext.getHttpSecurity();
		if (userName == null) {
			assertNull("Should not be authenticated", security);
			assertNull("Should not register HTTP Security with HTTP Session",
					this.authenticationContext
							.getRegisteredHttpSecurityWithHttpSession());

		} else {
			assertNotNull("Should be authenticated", security);
			assertEquals("Incorrect authentication scheme", "Mock",
					security.getAuthenticationScheme());
			assertEquals("Incorrect user name", userName,
					security.getRemoteUser());
			assertEquals("Incorrect principle", userName, security
					.getUserPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role,
						security.isUserInRole(role));
			}
			assertSame(
					"Same HTTP Security should be registered with HTTP Session",
					security, this.authenticationContext
							.getRegisteredHttpSecurityWithHttpSession());
		}
	}

}