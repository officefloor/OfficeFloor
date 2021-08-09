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

package net.officefloor.web.security.scheme;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource.Dependencies;
import net.officefloor.web.security.store.CredentialEntry;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

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
		HttpSecurityLoaderUtil.validateSpecification(BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(HttpAccessControl.class);
		type.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);
	}

	/**
	 * Ensure can ratify from cached {@link HttpAccessControl}.
	 */
	public void testRatifyFromSession() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>();
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);

		// Load access control to session
		ratifyContext.getSession().setAttribute(ratifyContext.getQualifiedAttributeName("http.security.basic"),
				accessControl);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached", security.ratify(null, ratifyContext));
		assertSame("Incorrect security", accessControl, ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can ratify if have authorization header.
	 */
	public void testRatifyWithAuthorizationHeader() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<HttpAccessControl>(
				"Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, REALM);

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

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>();

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, REALM);

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

		final MockHttpChallengeContext<Dependencies, None> challengeContext = new MockHttpChallengeContext<>();
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the challenge
		security.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Ensure correct challenge
		assertEquals("Incorrect challenge", "Basic realm=\"" + REALM + "\"", challengeContext.getChallenge());
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
		this.doAuthenticate("Incorrect QWxhZGRpbjpvcGVuIHNlc2FtZQ==", false, null);
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

		// Record simple authenticate
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(),
				"open sesame".getBytes(AbstractHttpSecuritySource.UTF_8));
		this.recordReturn(this.store, this.store.getAlgorithm(), null);
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", true, "Aladdin", "prince");
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
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(), credentials);
		this.recordReturn(this.store, this.store.getAlgorithm(), "MD5");
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", true, "Aladdin", "prince");
	}

	/**
	 * Ensure can extra spacing.
	 */
	public void testExtraSpacing() throws Exception {

		// Record authenticate
		this.recordReturn(this.store, this.store.retrieveCredentialEntry("Aladdin", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(),
				"open sesame".getBytes(AbstractHttpSecuritySource.UTF_8));
		this.recordReturn(this.store, this.store.getAlgorithm(), CredentialStore.NO_ALGORITHM);
		this.recordReturn(this.entry, this.entry.retrieveRoles(), new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("  Basic    QWxhZGRpbjpvcGVuIHNlc2FtZQ==  ", true, "Aladdin", "prince");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<Dependencies, None> logoutContext = new MockHttpLogoutContext<>();
		logoutContext.getSession().setAttribute(logoutContext.getQualifiedAttributeName("http.security.basic"),
				this.createMock(HttpAccessControl.class));

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Logout
		security.logout(logoutContext);

		// Ensure access control removed from session
		assertNull("Should clear access control from session",
				logoutContext.getSession().getAttribute("http.security.basic"));
	}

	/**
	 * Ensure convenience method for creating {@link HttpHeader} value works and
	 * available for testing.
	 */
	public void testConvenienceCreateHttpHeaderValue() {
		String headerValue = BasicHttpSecuritySource.createAuthorizationHttpHeaderValue("Aladdin", "open sesame");
		assertEquals("Incorrect HTTP Header value", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", headerValue);
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param authorizationHttpHeaderValue <code>authorize</code> {@link HttpHeader}
	 *                                     value.
	 * @param isLoadSession                Indicates if load {@link HttpSession}
	 *                                     with access control.
	 * @param userName                     User name if authenticated.
	 *                                     <code>null</code> if not authenticated.
	 * @param roles                        Expected roles.
	 */
	private void doAuthenticate(String authorizationHttpHeaderValue, boolean isLoadSession, String userName,
			String... roles) {

		// Create the mock authenticate context
		MockHttpAuthenticateContext<HttpAccessControl, Dependencies, None> authenticationContext = new MockHttpAuthenticateContext<>(
				authorizationHttpHeaderValue);
		authenticationContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Replacy
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the authenticate
		security.authenticate(null, authenticationContext);

		// Verify
		this.verifyMockObjects();

		// Validate authentication
		HttpAccessControl accessControl = authenticationContext.getAccessControl();
		HttpAccessControl sessionAccessControl = (HttpAccessControl) authenticationContext.getSession()
				.getAttribute(authenticationContext.getQualifiedAttributeName("http.security.basic"));
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);
			assertNull("Should not register HTTP Security with HTTP Session", sessionAccessControl);

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", "Basic", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect principle", userName, accessControl.getPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.inRole(role));
			}
			assertSame("Same access control should be registered with HTTP Session", accessControl,
					sessionAccessControl);
		}
	}

}
