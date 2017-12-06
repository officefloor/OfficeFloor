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
import java.util.function.Consumer;

import org.apache.commons.codec.binary.Hex;
import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.AbstractHttpSecuritySource;
import net.officefloor.web.security.scheme.DigestHttpSecuritySource.Dependencies;
import net.officefloor.web.security.store.CredentialEntry;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link DigestHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Realm.
	 */
	private static final String REALM = "testrealm@host.com";

	/**
	 * Algorithm.
	 */
	private static final String ALGORITHM = "MD5";

	/**
	 * Private key.
	 */
	private static final String PRIVATE_KEY = "Private Key";

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this.createMock(CredentialStore.class);

	/**
	 * {@link CredentialEntry}.
	 */
	private final CredentialEntry entry = this.createMock(CredentialEntry.class);

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(DigestHttpSecuritySource.class,
				DigestHttpSecuritySource.PROPERTY_REALM, "Realm", DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY,
				"Private Key");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(HttpAccessControl.class);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);
		type.addDependency(Dependencies.SESSION, HttpSession.class, null);
		type.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, DigestHttpSecuritySource.class,
				DigestHttpSecuritySource.PROPERTY_REALM, REALM, DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY,
				PRIVATE_KEY);
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
		this.recordReturn(session, session.getAttribute("http.security.digest"), accessControl);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(DigestHttpSecuritySource.class, DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);

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
				this, "Digest credentials");

		// Record obtaining HTTP security from HTTP session
		HttpSession session = ratifyContext.getSession();
		this.recordReturn(session, session.getAttribute("http.security.digest"), null);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(DigestHttpSecuritySource.class, DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);

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
		this.recordReturn(session, session.getAttribute("http.security.digest"), null);

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(DigestHttpSecuritySource.class, DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);

		// Undertake ratify
		assertFalse("Should not attempt authentication", security.ratify(null, ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getAccessControl());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can send challenge.
	 */
	public void testChallenge() throws Exception {

		// Mock digest values
		final String eTag = "Test ETag";
		MockDigestHttpSecuritySource.timestamp = "Test Timestamp";
		MockDigestHttpSecuritySource.opaqueSeed = "Test Opaque";

		// Create the connection
		MockHttpRequestBuilder request = MockHttpServer.mockRequest().header("ETag", eTag);
		ServerHttpConnection connection = MockHttpServer.mockConnection(request);

		// Create challenge context
		final MockHttpChallengeContext<Dependencies, None> challengeContext = new MockHttpChallengeContext<Dependencies, None>(
				connection, this);
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Mock
		final HttpSession session = challengeContext.getSession();

		// Create the expected nonce
		byte[] nonceDigest = this.createDigest(ALGORITHM, MockDigestHttpSecuritySource.timestamp, eTag, PRIVATE_KEY);
		final String nonce = new String(nonceDigest, AbstractHttpSecuritySource.UTF_8);

		// Create the expected opaque
		byte[] opaqueDigest = this.createDigest(ALGORITHM, MockDigestHttpSecuritySource.opaqueSeed);
		final String opaque = new String(opaqueDigest, AbstractHttpSecuritySource.UTF_8);

		// Record
		this.recordReturn(this.store, this.store.getAlgorithm(), ALGORITHM);
		session.setAttribute("#" + DigestHttpSecuritySource.class.getName() + "#", "SecurityStore");
		this.control(session).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect key", expected[0], actual[0]);
				assertNotNull("Expecting security store", actual[1]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockDigestHttpSecuritySource.class, DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);

		// Undertake the challenge
		security.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Ensure correct challenge
		assertEquals(
				"Incorrect challenge", "Digest realm=\"" + REALM + "\", qop=\"auth,auth-int\"," + " nonce=\"" + nonce
						+ "\"," + " opaque=\"" + opaque + "\"," + " algorithm=\"" + ALGORITHM + "\"",
				challengeContext.getChallenge());
	}

	/**
	 * Ensure not authenticated if no authorization header.
	 */
	public void testNoAuthorizationHeader() throws Exception {
		this.doAuthenticate(null, false, null, null);
	}

	/**
	 * Ensure handle incorrect authentication scheme.
	 */
	public void testIncorrectAuthenticationScheme() throws Exception {
		this.doAuthenticate("Incorrect parameters=\"should no be used\"", false, null, null);
	}

	/**
	 * Ensure handle invalid Base64 encoding.
	 */
	public void testInvalidAuthorizationHeader() throws Exception {
		this.doAuthenticate("Basic wrong", false, null, null);
	}

	/**
	 * Ensure can do simple authentication.
	 */
	public void testSimpleAuthenticate() throws Exception {
		this.doAuthenticate("Digest username=\"Mufasa\", realm=\"" + REALM
				+ "\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," + " uri=\"/dir/index.html\","
				+ " qop=auth, nc=00000001, cnonce=\"0a4f113b\"," + " response=\"6629fae49393a05397450978507c4ef1\","
				+ " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"", true, "Mufasa", (context) -> {

					// Mock values
					final byte[] digest = this.createDigest(ALGORITHM, "Mufasa", REALM, "Circle Of Life");
					final HttpSession session = context.getSession();

					// Record authentication
					this.recordReturn(session,
							session.getAttribute(DigestHttpSecuritySource.SECURITY_STATE_SESSION_KEY),
							DigestHttpSecuritySource.Mock.MOCK_SECURITY_STATE);
					this.recordReturn(this.store, this.store.retrieveCredentialEntry("Mufasa", REALM), this.entry);
					this.recordReturn(this.entry, this.entry.retrieveCredentials(), digest);
					this.recordReturn(this.store, this.store.getAlgorithm(), ALGORITHM);
					this.recordReturn(this.entry, this.entry.retrieveRoles(),
							new HashSet<String>(Arrays.asList("prince")));
				}, "prince");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<Dependencies> logoutContext = new MockHttpLogoutContext<Dependencies>(this);

		// Record logging out
		HttpSession session = logoutContext.getSession();
		session.removeAttribute("http.security.digest");

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockDigestHttpSecuritySource.class, DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);

		// Logout
		security.logout(logoutContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Creates the digest.
	 * 
	 * @param algorithm
	 *            Algorithm.
	 * @param values
	 *            Values to load into the digest.
	 * @return Digest.
	 */
	private byte[] createDigest(String algorithm, String... values) {
		try {

			// Create the digest
			MessageDigest message = MessageDigest.getInstance(algorithm);
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				if (i > 0) {
					message.update(":".getBytes(AbstractHttpSecuritySource.UTF_8));
				}
				message.update(value.getBytes(AbstractHttpSecuritySource.UTF_8));
			}
			byte[] digest = message.digest();

			// Transform to text
			String text = new String(Hex.encodeHex(digest, true));

			// Return the digest
			return text.getBytes(AbstractHttpSecuritySource.UTF_8);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param authorizationHttpHeaderValue
	 *            <code>Authenticate</code> {@link HttpHeader} value.
	 * @param isLoadSession
	 *            Indicates if load to {@link HttpSession}.
	 * @param userName
	 *            User name if authenticated. <code>null</code> if not
	 *            authenticated.
	 * @param initialiser
	 *            Initialiser.
	 * @param roles
	 *            Expected roles.
	 */
	private void doAuthenticate(String authorizationHttpHeaderValue, boolean isLoadSession, String userName,
			Consumer<MockHttpAuthenticateContext<HttpAccessControl, Dependencies>> initialiser, String... roles)
			throws IOException {

		// Create the authenticate context
		MockHttpAuthenticateContext<HttpAccessControl, Dependencies> authenticationContext = new MockHttpAuthenticateContext<HttpAccessControl, Dependencies>(
				this, authorizationHttpHeaderValue);
		authenticationContext.registerObject(Dependencies.CREDENTIAL_STORE, this.store);

		// Determine if initialise
		if (initialiser != null) {
			initialiser.accept(authenticationContext);
		}

		// Determine if load session
		if (isLoadSession) {
			authenticationContext.recordRegisterAccessControlWithHttpSession("http.security.digest");
		}

		// Replay mock objects
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockDigestHttpSecuritySource.class, DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);

		// Undertake the authenticate
		security.authenticate(null, authenticationContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Validate authentication
		HttpAccessControl accessControl = authenticationContext.getAccessControl();
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", "Digest", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect principle", userName, accessControl.getPrincipal().getName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.inRole(role));
			}
		}
	}

	/**
	 * {@link DigestHttpSecuritySource} for testing to provide consistent
	 * values.
	 */
	public static class MockDigestHttpSecuritySource extends DigestHttpSecuritySource {

		/**
		 * Time stamp.
		 */
		public static String timestamp;

		/**
		 * Opaque seed.
		 */
		public static String opaqueSeed;

		/*
		 * =============== DigestHttpSecuritySource ================
		 */

		@Override
		protected String getTimestamp() {
			return timestamp;
		}

		@Override
		protected String getOpaqueSeed() {
			return opaqueSeed;
		}
	}

}