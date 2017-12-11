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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.FormHttpSecuritySource.Dependencies;
import net.officefloor.web.security.scheme.FormHttpSecuritySource.Flows;
import net.officefloor.web.security.store.CredentialEntry;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpCredentials;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Tests the {@link FormHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Realm for testing.
	 */
	private static final String REALM = "WallyWorld";

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this.createMock(CredentialStore.class);

	/**
	 * {@link CredentialEntry}.
	 */
	private final CredentialEntry entry = this.createMock(CredentialEntry.class);

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(FormHttpSecuritySource.class,
				FormHttpSecuritySource.PROPERTY_REALM, "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(HttpAccessControl.class);
		type.setCredentialsClass(HttpCredentials.class);
		type.setInput(true);
		type.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class, null);
		type.addFlow(Flows.FORM_LOGIN_PAGE, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, FormHttpSecuritySource.class,
				FormHttpSecuritySource.PROPERTY_REALM, REALM);
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
		this.recordReturn(session, session.getAttribute("http.security.form"), accessControl);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached", security.ratify(null, ratifyContext));
		assertSame("Incorrect security", accessControl, ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can ratify if have {@link HttpCredentials}.
	 */
	public void testRatifyWithCredentials() throws IOException {

		final HttpCredentials credentials = this.createMock(HttpCredentials.class);
		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>(
				this, null);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(session, session.getAttribute("http.security.form"), null);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate", security.ratify(credentials, ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure ratify indicates no authentication credentials.
	 */
	public void testRatifyNoCredentials() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>(
				this, null);

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(session, session.getAttribute("http.security.form"), null);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

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

		final MockHttpChallengeContext<Dependencies, Flows> challengeContext = new MockHttpChallengeContext<Dependencies, Flows>(
				this);
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Record the triggering flow for form login page
		challengeContext.recordDoFlow(Flows.FORM_LOGIN_PAGE);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the challenge
		security.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure not authenticated if no credentials provided.
	 */
	public void testNullCredentials() throws Exception {
		this.doAuthenticate(null, false, null);
	}

	/**
	 * Ensure not authenticated if no credentials.
	 */
	public void testNoUserName() throws Exception {

		// Record no user name
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		this.recordReturn(credentials, credentials.getUsername(), null);

		// Test
		this.doAuthenticate(credentials, false, null);
	}

	/**
	 * Ensure not authenticated if no password.
	 */
	public void testNoPassword() throws Exception {

		// Record authenticate
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		this.recordReturn(credentials, credentials.getUsername(), "Aladdin");
		this.recordReturn(credentials, credentials.getPassword(), null);

		// Test
		this.doAuthenticate(credentials, false, null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {

		byte[] password = "open sesame".getBytes(AbstractHttpSecuritySource.UTF_8);

		// Record simple authenticate
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		this.recordReturn(credentials, credentials.getUsername(), "Aladdin");
		this.recordReturn(credentials, credentials.getPassword(), password);
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(), password);
		this.recordReturn(this.store, this.store.getAlgorithm(), null);
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate(credentials, true, "Aladdin", "prince");
	}

	/**
	 * Ensure can authenticate with algorithm applied to credentials.
	 */
	public void testAlgorithmAuthenticate() throws Exception {

		byte[] password = "open sesame".getBytes(AbstractHttpSecuritySource.UTF_8);

		// Determine credentials
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(password);
		byte[] credentialsDigest = digest.digest();

		// Record authentication with algorithm
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		this.recordReturn(credentials, credentials.getUsername(), "Aladdin");
		this.recordReturn(credentials, credentials.getPassword(), password);
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(), credentialsDigest);
		this.recordReturn(this.store, this.store.getAlgorithm(), "MD5");
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate(credentials, true, "Aladdin", "prince");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<Dependencies> logoutContext = new MockHttpLogoutContext<Dependencies>(this);

		// Record logging out
		HttpSession session = logoutContext.getSession();
		session.removeAttribute("http.security.form");

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Logout
		security.logout(logoutContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param credentials
	 *            {@link HttpCredentials}.
	 * @param isLoadSession
	 *            Indicate if load {@link HttpSession}.
	 * @param userName
	 *            User name if authenticated. <code>null</code> if not
	 *            authenticated.
	 * @param roles
	 *            Expected roles.
	 */
	private void doAuthenticate(HttpCredentials credentials, boolean isLoadSession, String userName, String... roles)
			throws IOException {

		// Create the authentication context
		MockHttpAuthenticateContext<HttpAccessControl, Dependencies> authenticationContext = new MockHttpAuthenticateContext<HttpAccessControl, Dependencies>(
				this, null);
		authenticationContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Determine if should be loading to session
		if (isLoadSession) {
			authenticationContext.recordRegisterAccessControlWithHttpSession("http.security.form");
		}

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the authenticate
		security.authenticate(credentials, authenticationContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Validate authentication
		HttpAccessControl accessControl = authenticationContext.getAccessControl();
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", "Form", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect principle", userName, accessControl.getPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.inRole(role));
			}
		}
	}

}