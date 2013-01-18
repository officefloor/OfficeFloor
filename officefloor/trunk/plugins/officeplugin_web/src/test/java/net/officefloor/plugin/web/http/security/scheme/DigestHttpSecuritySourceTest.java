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
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource.Dependencies;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderUtil;
import net.officefloor.plugin.web.http.security.type.HttpSecurityTypeBuilder;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.commons.codec.binary.Hex;
import org.easymock.AbstractMatcher;

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
	 * {@link HttpAuthenticateContext}.
	 */
	private final MockHttpAuthenticateContext<HttpSecurity, Void, Dependencies> authenticationContext = new MockHttpAuthenticateContext<HttpSecurity, Void, Dependencies>(
			null, this);

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
	 * Validates the specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(
				DigestHttpSecuritySource.class,
				DigestHttpSecuritySource.PROPERTY_REALM, "Realm",
				DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, "Private Key");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil
				.createHttpSecurityTypeBuilder();
		type.setSecurityClass(HttpSecurity.class);
		type.addDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type,
				DigestHttpSecuritySource.class,
				DigestHttpSecuritySource.PROPERTY_REALM, REALM,
				DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, PRIVATE_KEY);
	}

	/**
	 * Ensure can send challenge.
	 */
	public void testChallenge() throws Exception {

		final MockHttpChallengeContext<Dependencies, None> challengeContext = new MockHttpChallengeContext<Dependencies, None>(
				this);
		challengeContext.registerObject(Dependencies.CREDENTIAL_STORE,
				this.store);

		// Mock
		final HttpHeader header = this.createMock(HttpHeader.class);
		MockDigestHttpSecuritySource.timestamp = "Test Timestamp";
		MockDigestHttpSecuritySource.opaqueSeed = "Test Opaque";
		final String eTag = "Test ETag";
		final HttpSession session = challengeContext.getSession();

		// Create the expected nonce
		byte[] nonceDigest = this.createDigest(ALGORITHM,
				MockDigestHttpSecuritySource.timestamp, eTag, PRIVATE_KEY);
		final String nonce = new String(nonceDigest,
				HttpRequestParserImpl.US_ASCII);

		// Create the expected opaque
		byte[] opaqueDigest = this.createDigest(ALGORITHM,
				MockDigestHttpSecuritySource.opaqueSeed);
		final String opaque = new String(opaqueDigest,
				HttpRequestParserImpl.US_ASCII);

		// Record
		HttpRequest request = challengeContext.recordGetHttpRequest();
		this.recordReturn(this.store, this.store.getAlgorithm(), ALGORITHM);
		this.recordReturn(request, request.getHeaders(), Arrays.asList(header));
		this.recordReturn(header, header.getName(), "ETag");
		this.recordReturn(header, header.getValue(), eTag);
		session.setAttribute("#" + DigestHttpSecuritySource.class.getName()
				+ "#", "SecurityStore");
		this.control(session).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect key", expected[0], actual[0]);
				assertNotNull("Expecting security store", actual[1]);
				return true;
			}
		});
		challengeContext.recordAuthenticateChallenge("Digest realm=\"" + REALM
				+ "\", qop=\"auth,auth-int\"," + " nonce=\"" + nonce + "\","
				+ " opaque=\"" + opaque + "\"," + " algorithm=\"" + ALGORITHM
				+ "\"");

		// Test
		this.replayMockObjects();

		// Create and initialise the source
		DigestHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockDigestHttpSecuritySource.class,
						DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY,
						PRIVATE_KEY);

		// Undertake the challenge
		source.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure not authenticated if no authorization header.
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
				.recordAuthorizationHeader("Incorrect parameters=\"should no be used\"");

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
	 * Ensure can do simple authentication.
	 */
	public void testSimpleAuthenticate() throws Exception {

		// Mock values
		final byte[] digest = this.createDigest(ALGORITHM, "Mufasa", REALM,
				"Circle Of Life");
		final HttpSession session = this.authenticationContext.getSession();

		// Record authentication
		HttpRequest request = this.authenticationContext
				.recordAuthorizationHeader("Digest username=\"Mufasa\", realm=\""
						+ REALM
						+ "\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","
						+ " uri=\"/dir/index.html\","
						+ " qop=auth, nc=00000001, cnonce=\"0a4f113b\","
						+ " response=\"6629fae49393a05397450978507c4ef1\","
						+ " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
		this.recordReturn(
				session,
				session.getAttribute(DigestHttpSecuritySource.SECURITY_STATE_SESSION_KEY),
				DigestHttpSecuritySource.Mock.MOCK_SECURITY_STATE);
		this.recordReturn(this.store,
				this.store.retrieveCredentialEntry("Mufasa", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(), digest);
		this.recordReturn(this.store, this.store.getAlgorithm(), ALGORITHM);
		this.recordReturn(request, request.getMethod(), "GET");
		this.recordReturn(this.entry, this.entry.retrieveRoles(),
				new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("Mufasa", "prince");
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

			// Obtain the charset
			Charset usAscii = HttpRequestParserImpl.US_ASCII;

			// Create the digest
			MessageDigest message = MessageDigest.getInstance(algorithm);
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				if (i > 0) {
					message.update(":".getBytes(usAscii));
				}
				message.update(value.getBytes(usAscii));
			}
			byte[] digest = message.digest();

			// Transform to text
			String text = new String(Hex.encodeHex(digest, true));

			// Return the digest
			return text.getBytes(usAscii);

		} catch (Exception ex) {
			throw fail(ex);
		}
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
		DigestHttpSecuritySource source = HttpSecurityLoaderUtil
				.loadHttpSecuritySource(MockDigestHttpSecuritySource.class,
						DigestHttpSecuritySource.PROPERTY_REALM, REALM,
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY,
						PRIVATE_KEY);

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
			assertEquals("Incorrect authentication scheme", "Digest",
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

	/**
	 * {@link DigestHttpSecuritySource} for testing to provide consistent
	 * values.
	 */
	public static class MockDigestHttpSecuritySource extends
			DigestHttpSecuritySource {

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