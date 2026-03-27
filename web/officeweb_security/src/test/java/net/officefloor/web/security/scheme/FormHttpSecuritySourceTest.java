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
import java.util.function.BiConsumer;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.scheme.FormHttpSecuritySource.Dependencies;
import net.officefloor.web.security.scheme.FormHttpSecuritySource.Flows;
import net.officefloor.web.security.store.CredentialEntry;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
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
		type.addFlow("form", null, 0, Flows.FORM_LOGIN_PAGE);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, FormHttpSecuritySource.class,
				FormHttpSecuritySource.PROPERTY_REALM, REALM);
	}

	/**
	 * Ensure can ratify from cached {@link HttpAccessControl}.
	 */
	public void testRatifyFromSession() throws IOException {

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>();
		final HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);

		// Provide access control in session
		ratifyContext.getSession().setAttribute(ratifyContext.getQualifiedAttributeName("http.security.form"),
				accessControl);

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
		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>();

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

		final MockHttpRatifyContext<HttpAccessControl> ratifyContext = new MockHttpRatifyContext<>();

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
	@SuppressWarnings("unchecked")
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<Dependencies, Flows> challengeContext = new MockHttpChallengeContext<>();
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Record the triggering flow for form login page
		BiConsumer<Object, FlowCallback> flow = this.createMock(BiConsumer.class);
		flow.accept(null, null);
		challengeContext.registerFlow(Flows.FORM_LOGIN_PAGE, flow);

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
		this.doAuthenticate(null, null);
	}

	/**
	 * Ensure not authenticated if no credentials.
	 */
	public void testNoUserName() throws Exception {

		// Record no user name
		HttpCredentials credentials = this.createMock(HttpCredentials.class);
		this.recordReturn(credentials, credentials.getUsername(), null);

		// Test
		this.doAuthenticate(credentials, null);
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
		this.doAuthenticate(credentials, null);
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
		this.doAuthenticate(credentials, "Aladdin", "prince");
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
		this.doAuthenticate(credentials, "Aladdin", "prince");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<Dependencies, Flows> logoutContext = new MockHttpLogoutContext<Dependencies, Flows>();

		// Record logging out
		logoutContext.getSession().setAttribute(logoutContext.getQualifiedAttributeName("http.security.form"),
				this.createMock(HttpAccessControl.class));

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(FormHttpSecuritySource.class, FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Logout
		security.logout(logoutContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Ensure the access control is cleared from session
		assertNull("Should clear session",
				logoutContext.getSession().getAttribute(logoutContext.getQualifiedAttributeName("http.security.form")));
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param credentials {@link HttpCredentials}.
	 * @param userName    User name if authenticated. <code>null</code> if not
	 *                    authenticated.
	 * @param roles       Expected roles.
	 */
	private void doAuthenticate(HttpCredentials credentials, String userName, String... roles) throws IOException {

		// Create the authentication context
		MockHttpAuthenticateContext<HttpAccessControl, Dependencies, Flows> authenticationContext = new MockHttpAuthenticateContext<>();
		authenticationContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

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
		HttpAccessControl sessionAccessControl = (HttpAccessControl) authenticationContext.getSession()
				.getAttribute(authenticationContext.getQualifiedAttributeName("http.security.form"));
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);
			assertNull("Should not load session", sessionAccessControl);

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", "Form", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect principle", userName, accessControl.getPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.inRole(role));
			}
			assertSame("Should load session", accessControl, sessionAccessControl);
		}
	}

}
