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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.BasicHttpSecuritySource.Dependencies;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderUtil;
import net.officefloor.plugin.web.http.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.session.HttpSession;

/**
 * Tests the {@link BasicHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Realm for testing.
	 */
	private static final String REALM = "WallyWorld";

	/**
	 * {@link HttpAuthenticateContext}.
	 */
	private final MockHttpAuthenticateContext<HttpSecurity, Void, Dependencies> authenticationContext = new MockHttpAuthenticateContext<HttpSecurity, Void, Dependencies>(
			null, this);

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this.createMock(CredentialStore.class);

	/**
	 * {@link CredentialEntry}.
	 */
	private final CredentialEntry entry = this.createMock(CredentialEntry.class);

	@Override
	protected void setUp() throws Exception {
		this.authenticationContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);
	}

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setSecurityClass(HttpSecurity.class);
		type.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);
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
		this.recordReturn(session, session.getAttribute("http.security.source.basic.http.security"), security);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		BasicHttpSecuritySource source = HttpSecurityLoaderUtil.loadHttpSecuritySource(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached", source.ratify(ratifyContext));
		assertSame("Incorrect security", security, ratifyContext.getHttpSecurity());

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
		this.recordReturn(session, session.getAttribute("http.security.source.basic.http.security"), null);
		ratifyContext.recordHttpRequestWithAuthorizationHeader("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		BasicHttpSecuritySource source = HttpSecurityLoaderUtil.loadHttpSecuritySource(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate", source.ratify(ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getHttpSecurity());

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
		this.recordReturn(session, session.getAttribute("http.security.source.basic.http.security"), null);
		ratifyContext.recordHttpRequestWithAuthorizationHeader(null);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		BasicHttpSecuritySource source = HttpSecurityLoaderUtil.loadHttpSecuritySource(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake ratify
		assertFalse("Should not attempt authentication", source.ratify(ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getHttpSecurity());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load challenge.
	 */
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<Dependencies, None> challengeContext = new MockHttpChallengeContext<Dependencies, None>(
				this);
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Record the challenge
		challengeContext.recordAuthenticateChallenge("Basic realm=\"" + REALM + "\"");

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		BasicHttpSecuritySource source = HttpSecurityLoaderUtil.loadHttpSecuritySource(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);

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
		this.authenticationContext.recordHttpRequestWithAuthorizationHeader(null);

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure handle incorrect authentication scheme.
	 */
	public void testIncorrectAuthenticationScheme() throws Exception {

		// Record authenticate
		this.authenticationContext.recordHttpRequestWithAuthorizationHeader("Incorrect QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure handle invalid Base64 encoding.
	 */
	public void testInvalidAuthorizationHeader() throws Exception {

		// Record authenticate
		this.authenticationContext.recordHttpRequestWithAuthorizationHeader("Basic wrong");

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {

		// Record simple authenticate
		this.authenticationContext.recordHttpRequestWithAuthorizationHeader("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(),
				"open sesame".getBytes(AbstractHttpSecuritySource.UTF_8));
		this.recordReturn(this.store, this.store.getAlgorithm(), null);
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));
		this.authenticationContext
				.recordRegisterHttpSecurityWithHttpSession("http.security.source.basic.http.security");

		// Test
		this.doAuthenticate("Aladdin", "prince");
	}

	/**
	 * Ensure can authenticate with algorithm applied to credentials.
	 */
	public void testAlgorithmAuthenticate() throws Exception {

		// Determine credentials
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update("open sesame".getBytes(AbstractHttpSecuritySource.UTF_8));
		byte[] credentials = digest.digest();

		// Record authentication with algorithm
		this.authenticationContext.recordHttpRequestWithAuthorizationHeader("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(), credentials);
		this.recordReturn(this.store, this.store.getAlgorithm(), "MD5");
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));
		this.authenticationContext
				.recordRegisterHttpSecurityWithHttpSession("http.security.source.basic.http.security");

		// Test
		this.doAuthenticate("Aladdin", "prince");
	}

	/**
	 * Ensure can extra spacing.
	 */
	public void testExtraSpacing() throws Exception {

		// Record authenticate
		this.authenticationContext.recordHttpRequestWithAuthorizationHeader("  Basic    QWxhZGRpbjpvcGVuIHNlc2FtZQ==  ");
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(),
				"open sesame".getBytes(AbstractHttpSecuritySource.UTF_8));
		this.recordReturn(this.store, this.store.getAlgorithm(), CredentialStore.NO_ALGORITHM);
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));
		this.authenticationContext
				.recordRegisterHttpSecurityWithHttpSession("http.security.source.basic.http.security");

		// Test
		this.doAuthenticate("Aladdin", "prince");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<Dependencies> logoutContext = new MockHttpLogoutContext<Dependencies>(this);

		// Record logging out
		HttpSession session = logoutContext.getSession();
		session.removeAttribute("http.security.source.basic.http.security");

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the source
		BasicHttpSecuritySource source = HttpSecurityLoaderUtil.loadHttpSecuritySource(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Logout
		source.logout(logoutContext);

		// Verify mock objects
		this.verifyMockObjects();
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
	private void doAuthenticate(String userName, String... roles) throws IOException {

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the source
		BasicHttpSecuritySource source = HttpSecurityLoaderUtil.loadHttpSecuritySource(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the authenticate
		source.authenticate(this.authenticationContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Validate authentication
		HttpSecurity security = this.authenticationContext.getHttpSecurity();
		if (userName == null) {
			assertNull("Should not be authenticated", security);
			assertNull("Should not register HTTP Security with HTTP Session",
					this.authenticationContext.getRegisteredHttpSecurityWithHttpSession());

		} else {
			assertNotNull("Should be authenticated", security);
			assertEquals("Incorrect authentication scheme", "Basic", security.getAuthenticationScheme());
			assertEquals("Incorrect user name", userName, security.getRemoteUser());
			assertEquals("Incorrect principle", userName, security.getUserPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, security.isUserInRole(role));
			}
			assertSame("Same HTTP Security should be registered with HTTP Session", security,
					this.authenticationContext.getRegisteredHttpSecurityWithHttpSession());
		}
	}

}