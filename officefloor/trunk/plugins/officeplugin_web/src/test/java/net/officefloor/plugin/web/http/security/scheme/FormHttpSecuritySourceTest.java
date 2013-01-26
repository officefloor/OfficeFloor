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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.scheme.FormHttpSecuritySource.Dependencies;
import net.officefloor.plugin.web.http.security.scheme.FormHttpSecuritySource.Flows;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderUtil;
import net.officefloor.plugin.web.http.security.type.HttpSecurityTypeBuilder;

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
	 * {@link HttpCredentials}.
	 */
	private final HttpCredentials credentials = this
			.createMock(HttpCredentials.class);

	/**
	 * {@link HttpAuthenticateContext}.
	 */
	private MockHttpAuthenticateContext<HttpSecurity, HttpCredentials, Dependencies> authenticationContext = new MockHttpAuthenticateContext<HttpSecurity, HttpCredentials, Dependencies>(
			this.credentials, this);

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this
			.createMock(CredentialStore.class);

	/**
	 * {@link CredentialEntry}.
	 */
	private final CredentialEntry entry = this
			.createMock(CredentialEntry.class);

	@Override
	protected void setUp() throws Exception {
		this.authenticationContext.registerObject(
				Dependencies.CREDENTIAL_STORE, this.store);
	}

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(
				BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil
				.createHttpSecurityTypeBuilder();
		type.setSecurityClass(HttpSecurity.class);
		type.setCredentialsClass(HttpCredentials.class);
		type.addDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class, null);
		type.addFlow(Flows.FORM_LOGIN_PAGE, null, null, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type,
				BasicHttpSecuritySource.class,
				BasicHttpSecuritySource.PROPERTY_REALM, REALM);
	}

	/**
	 * Ensure can load challenge.
	 */
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<Dependencies, Flows> challengeContext = new MockHttpChallengeContext<Dependencies, Flows>(
				this);
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE,
				this.store);

		// Record the triggering flow for form login page
		challengeContext.recordDoFlow(Flows.FORM_LOGIN_PAGE);

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		FormHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(FormHttpSecuritySource.class,
						FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the challenge
		source.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure not authenticated if no credentials provided.
	 */
	public void testNullCredentials() throws Exception {

		// No credentials
		this.authenticationContext = new MockHttpAuthenticateContext<HttpSecurity, HttpCredentials, FormHttpSecuritySource.Dependencies>(
				null, this);

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure not authenticated if no credentials.
	 */
	public void testNoUserName() throws Exception {

		// Record no authorization header
		this.recordReturn(this.credentials, this.credentials.getUsername(),
				null);

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure not authenticated if no password.
	 */
	public void testNoPassword() throws Exception {

		// Record authenticate
		this.recordReturn(this.credentials, this.credentials.getPassword(),
				null);

		// Test
		this.doAuthenticate(null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {

		byte[] password = "open sesame"
				.getBytes(HttpRequestParserImpl.US_ASCII);

		// Record simple authenticate
		this.recordReturn(this.credentials, this.credentials.getUsername(),
				"Aladdin");
		this.recordReturn(this.credentials, this.credentials.getPassword(),
				password);
		this.recordReturn(this.store,
				this.store.retrieveCredentialEntry("Aladdin", REALM),
				this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(),
				password);
		this.recordReturn(this.store, this.store.getAlgorithm(), null);
		this.recordReturn(this.entry, this.entry.retrieveRoles(),
				new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("Aladdin", "prince");
	}

	/**
	 * Ensure can authenticate with algorithm applied to credentials.
	 */
	public void testAlgorithmAuthenticate() throws Exception {

		byte[] password = "open sesame"
				.getBytes(HttpRequestParserImpl.US_ASCII);

		// Determine credentials
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(password);
		byte[] credentials = digest.digest();

		// Record authentication with algorithm
		this.recordReturn(this.credentials, this.credentials.getUsername(),
				"Aladdin");
		this.recordReturn(this.credentials, this.credentials.getPassword(),
				password);
		this.recordReturn(this.store,
				this.store.retrieveCredentialEntry("Aladdin", REALM),
				this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(),
				credentials);
		this.recordReturn(this.store, this.store.getAlgorithm(), "MD5");
		this.recordReturn(this.entry, this.entry.retrieveRoles(),
				new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("Aladdin", "prince");
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
		FormHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(FormHttpSecuritySource.class,
						FormHttpSecuritySource.PROPERTY_REALM, REALM);

		// Undertake the authenticate
		source.authenticate(this.authenticationContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Validate authentication
		HttpSecurity security = this.authenticationContext.getHttpSecurity();
		if (userName == null) {
			assertNull("Should not be authenticated", security);

		} else {
			assertNotNull("Should be authenticated", security);
			assertEquals("Incorrect authentication scheme", "Basic",
					security.getAuthenticationScheme());
			assertEquals("Incorrect user name", userName,
					security.getRemoteUser());
			assertEquals("Incorrect principle", userName, security
					.getUserPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role,
						security.isUserInRole(role));
			}
		}
	}

}