/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.security.scheme;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.security.scheme.DigestHttpSecuritySource.Dependencies;

/**
 * Tests the {@link DigestHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecuritySourceTest extends
		AbstractHttpSecuritySourceTest<Dependencies> {

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this
			.createMock(CredentialStore.class);

	/**
	 * Initiate.
	 */
	public DigestHttpSecuritySourceTest() {
		super(DigestHttpSecuritySource.class, "Digest");

		// Always require credential store
		this.context.requireDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
	}

	@Override
	protected void loadDependencies(Map<Dependencies, Object> dependencies) {
		dependencies.put(Dependencies.CREDENTIAL_STORE, this.store);
	}

	/**
	 * Ensure can do simple authentication.
	 */
	public void testSimpleAuthenticate() throws Exception {
		// Mock values
		final byte[] digest = this.createDigest("MD5", "Mufasa",
				"testrealm@host.com", "Circle Of Life");
		final HttpRequest request = this.createMock(HttpRequest.class);

		// Record authentication
		this
				.recordReturn(
						this.session,
						this.session
								.getAttribute(DigestHttpSecuritySource.SECURITY_STATE_SESSION_KEY),
						DigestHttpSecuritySource.MOCK_SECURITY_STATE);
		this.recordReturn(this.store, this.store.retrieveCredentials("Mufasa",
				"testrealm@host.com"), digest);
		this.recordReturn(this.store, this.store.getAlgorithm(), "MD5");
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				request);
		this.recordReturn(request, request.getMethod(), "GET");
		this.recordReturn(this.store, this.store.retrieveRoles("Mufasa",
				"testrealm@host.com"), new HashSet<String>(Arrays
				.asList("prince")));

		// Test
		this
				.doTest("username=\"Mufasa\", realm=\"testrealm@host.com\","
						+ " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\","
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

}