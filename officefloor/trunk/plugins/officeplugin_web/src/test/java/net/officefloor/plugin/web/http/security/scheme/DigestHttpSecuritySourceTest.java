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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource.Dependencies;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;

import org.apache.commons.codec.binary.Hex;
import org.easymock.AbstractMatcher;

/**
 * Tests the {@link DigestHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecuritySourceTest extends
		AbstractHttpSecuritySourceTestCase<Dependencies> {

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
	private final CredentialStore store = this
			.createMock(CredentialStore.class);

	/**
	 * {@link CredentialEntry}.
	 */
	private final CredentialEntry entry = this
			.createMock(CredentialEntry.class);

	/**
	 * Initiate.
	 */
	public DigestHttpSecuritySourceTest() {
		super(MockDigestHttpSecuritySource.class, "Digest");

		// Make properties available
		this.recordReturn(this.context, this.context
				.getProperty(DigestHttpSecuritySource.PROPERTY_REALM), REALM);
		this.recordReturn(this.context, this.context
				.getProperty(DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY),
				PRIVATE_KEY);

		// Always require credential store
		this.context.requireDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
	}

	@Override
	protected void loadDependencies(Map<Dependencies, Object> dependencies) {
		dependencies.put(Dependencies.CREDENTIAL_STORE, this.store);
	}

	/**
	 * Ensure can send challenge.
	 */
	public void testChallenge() throws Exception {

		// Mock
		final HttpRequest request = this.createMock(HttpRequest.class);
		final HttpHeader header = this.createMock(HttpHeader.class);
		MockDigestHttpSecuritySource.timestamp = "Test Timestamp";
		MockDigestHttpSecuritySource.opaqueSeed = "Test Opaque";
		final String eTag = "Test ETag";

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
		this.recordReturn(this.store, this.store.getAlgorithm(), ALGORITHM);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				request);
		this.recordReturn(request, request.getHeaders(), Arrays.asList(header));
		this.recordReturn(header, header.getName(), "ETag");
		this.recordReturn(header, header.getValue(), eTag);
		this.session.setAttribute("#"
				+ DigestHttpSecuritySource.class.getName() + "#",
				"SecurityStore");
		this.control(this.session).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect key", expected[0], actual[0]);
				assertNotNull("Expecting security store", actual[1]);
				return true;
			}
		});

		// Test
		this.doChallenge("realm=\"" + REALM + "\", qop=\"auth,auth-int\","
				+ " nonce=\"" + nonce + "\"," + " opaque=\"" + opaque + "\","
				+ " algorithm=\"" + ALGORITHM + "\"");
	}

	/**
	 * Ensure can do simple authentication.
	 */
	public void testSimpleAuthenticate() throws Exception {

		// Mock values
		final byte[] digest = this.createDigest(ALGORITHM, "Mufasa", REALM,
				"Circle Of Life");
		final HttpRequest request = this.createMock(HttpRequest.class);

		// Record authentication
		this
				.recordReturn(
						this.session,
						this.session
								.getAttribute(DigestHttpSecuritySource.SECURITY_STATE_SESSION_KEY),
						DigestHttpSecuritySource.MOCK_SECURITY_STATE);
		this.recordReturn(this.store, this.store.retrieveCredentialEntry(
				"Mufasa", REALM), this.entry);
		this.recordReturn(this.entry, this.entry.retrieveCredentials(), digest);
		this.recordReturn(this.store, this.store.getAlgorithm(), ALGORITHM);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				request);
		this.recordReturn(request, request.getMethod(), "GET");
		this.recordReturn(this.entry, this.entry.retrieveRoles(),
				new HashSet<String>(Arrays.asList("prince")));

		// Test
		this.doAuthenticate("username=\"Mufasa\", realm=\"" + REALM
				+ "\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","
				+ " uri=\"/dir/index.html\","
				+ " qop=auth, nc=00000001, cnonce=\"0a4f113b\","
				+ " response=\"6629fae49393a05397450978507c4ef1\","
				+ " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
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