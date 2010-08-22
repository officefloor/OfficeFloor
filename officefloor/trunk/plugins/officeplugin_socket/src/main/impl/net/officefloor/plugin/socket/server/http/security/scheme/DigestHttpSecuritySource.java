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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

import org.apache.commons.codec.binary.Hex;

/**
 * {@link HttpSecuritySource} for <code>Digest</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecuritySource implements
		HttpSecuritySource<DigestHttpSecuritySource.Dependencies> {

	/**
	 * Name of property for the realm.
	 */
	public static final String PROPERTY_REALM = "http.security.digest.realm";

	/**
	 * Name of property for the private key.
	 */
	public static final String PROPERTY_PRIVATE_KEY = "http.security.digest.private.key";

	/**
	 * US ASCII {@link Charset}.
	 */
	private static final Charset US_ASCII = HttpRequestParserImpl.US_ASCII;

	/**
	 * Key into the {@link HttpSession} for the {@link SecurityState}.
	 */
	protected static final String SECURITY_STATE_SESSION_KEY = "#"
			+ DigestHttpSecuritySource.class.getName() + "#";

	/**
	 * Process access to the mock nonce for testing.
	 */
	protected static final String MOCK_NONCE = "dcd98b7102dd2f0e8b11d0f600bfb0c093";

	/**
	 * Process access to the mock opaque for testing.
	 */
	protected static final String MOCK_OPAQUE = "5ccc069c403ebaf9f0171e9517f40e41";

	/**
	 * Provides access to a mock {@link SecurityState} for testing.
	 */
	protected static final Object MOCK_SECURITY_STATE = new SecurityState(
			MOCK_NONCE, MOCK_OPAQUE);

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		CREDENTIAL_STORE
	}

	/**
	 * State of parsing parameters.
	 */
	private static enum ParameterState {
		INIT, NAME, NAME_VALUE_SEPARATION, VALUE, QUOTED_VALUE
	}

	/**
	 * Realm.
	 */
	private String realm;

	/**
	 * Private key.
	 */
	private String privateKey;

	/**
	 * Parses out the parameters.
	 * 
	 * @param text
	 *            Text to parse out the parameters.
	 * @return Parameters parsed from text.
	 * @throws AuthenticationException
	 *             If fails to parse parameters.
	 */
	private Properties parseParameters(String text)
			throws AuthenticationException {

		// Initiate parsing
		ParameterState state = ParameterState.INIT;
		int start = -1;
		int end = -1;
		String name = null;

		// Create the parameters to loader
		Properties parameters = new Properties();

		// Parse the name and value parameters
		for (int i = 0; i < text.length(); i++) {

			// Handle next character
			char character = text.charAt(i);
			switch (character) {

			case ',':
				// Handle parameter separator
				switch (state) {
				case VALUE:
					// End of value, so obtain the value
					end = i;
					String value = text.substring(start, end);
					value = value.trim(); // ignore spacing

					// Load parameter
					parameters.setProperty(name.toLowerCase(), value);

					// Reset for next parameter
					state = ParameterState.INIT;
					break;
				}
				break;

			case '=':
				// Handle assignment
				switch (state) {
				case NAME:
					// Obtain the name
					end = i;
					name = text.substring(start, end);
					name = name.trim(); // ignore spacing
					state = ParameterState.NAME_VALUE_SEPARATION;
					break;
				}
				break;

			case '"':
				// Handle quote
				switch (state) {
				case NAME_VALUE_SEPARATION:
					// Quoted value
					start = i + 1; // ignore quote
					state = ParameterState.QUOTED_VALUE;
					break;

				case QUOTED_VALUE:
					// End of quoted value
					end = i;
					String value = text.substring(start, end);

					// Load parameter
					parameters.setProperty(name.toLowerCase(), value);

					// Reset for next parameter
					state = ParameterState.INIT;
					break;
				}
				break;

			case ' ':
				// Handle space
				switch (state) {
				case INIT:
					break; // ignore leading space
				}
				break;

			default:
				// Handle value
				switch (state) {
				case INIT:
					// Start processing the name
					start = i;
					state = ParameterState.NAME;
					break;

				case NAME:
					break; // Letter of name

				case NAME_VALUE_SEPARATION:
					// Non quoted value
					start = i;
					state = ParameterState.VALUE;
					break;
				}
				break;
			}
		}

		// Provide final value
		switch (state) {
		case VALUE:
			// Remaining content is value
			String value = text.substring(start);
			value = value.trim(); // ignore spacing

			// Load parameter
			parameters.setProperty(name.toLowerCase(), value);
			break;
		}

		// Return the parameters
		return parameters;
	}

	/**
	 * Obtains the time stamp.
	 * 
	 * @return Time stamp.
	 */
	protected String getTimestamp() {
		return String.valueOf(System.currentTimeMillis());
	}

	/**
	 * Obtains the <code>opaque</code> seed for the challenge.
	 * 
	 * @return <code>opaque</code> seed.
	 */
	protected String getOpaqueSeed() {
		return UUID.randomUUID().toString();
	}

	/*
	 * ======================= HttpSecuritySource ========================
	 */

	@Override
	public void init(HttpSecuritySourceContext<Dependencies> context)
			throws Exception {

		// Specify the properties
		this.realm = context.getProperty(PROPERTY_REALM);
		this.privateKey = context.getProperty(PROPERTY_PRIVATE_KEY);

		// Require credential store
		context.requireDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
	}

	@Override
	public String getAuthenticationScheme() {
		return "Digest";
	}

	@Override
	public HttpSecurity authenticate(String parameters,
			ServerHttpConnection connection, HttpSession session,
			Map<Dependencies, Object> dependencies)
			throws AuthenticationException {

		// Authenticate Digest as per RFC2617

		// Obtain the nounce
		SecurityState securityState = (SecurityState) session
				.getAttribute(SECURITY_STATE_SESSION_KEY);
		if (securityState == null) {
			// No challenge (i.e. no nounce), so not authenticated
			return null;
		}
		String nonce = securityState.nonce;

		// Obtain the parameter values
		Properties params = this.parseParameters(parameters);
		String username = params.getProperty("username");
		String realm = params.getProperty("realm");
		String response = params.getProperty("response");
		String opaque = params.getProperty("opaque");
		String digestUri = params.getProperty("uri");
		String qop = params.getProperty("qop");
		String cnonce = params.getProperty("cnonce");
		String nonceCount = params.getProperty("nc");

		// Ensure correct opaque
		if (!securityState.opaque.equals(opaque)) {
			return null; // not authenticated
		}

		// Ensure correct nonce count
		if (nonceCount != null) {
			try {
				byte[] decodedNonceCount = Hex.decodeHex(nonceCount
						.toCharArray());
				long nonceCountValue = 0;
				for (byte decodedNonceCountByte : decodedNonceCount) {
					nonceCountValue <<= 8; // shift to right by byte
					nonceCountValue += decodedNonceCountByte; // add byte
				}
				if (securityState.nonceCount != nonceCountValue) {
					return null; // likely replay attack so do not authenticate
				}

				// Increment for next authentication attempt
				securityState.nonceCount++;

			} catch (Exception ex) {
				throw new AuthenticationException(ex);
			}
		}

		// Obtain the required credentials
		CredentialStore store = (CredentialStore) dependencies
				.get(Dependencies.CREDENTIAL_STORE);
		byte[] usernameRealmPasswordCredentials = store.retrieveCredentials(
				username, realm);

		// Obtain the algorithm (stripping suffix)
		String algorithm = store.getAlgorithm();
		String algorithmSuffix = "";
		int algorithmSuffixIndex = algorithm.indexOf(':');
		if (algorithmSuffixIndex > 0) {
			// Obtain suffix first before changing algorithm value
			algorithmSuffix = algorithm.substring(algorithmSuffixIndex);
			algorithm = algorithm.substring(0, algorithmSuffixIndex);
		}

		// Calculate A1 value
		byte[] a1 = usernameRealmPasswordCredentials;
		if (algorithmSuffix.equalsIgnoreCase("sess")) {
			// Append the nounces for session integrity
			Digest a1Digest = new Digest(algorithm);
			a1Digest.append(a1);
			a1Digest.appendColon();
			a1Digest.append(nonce);
			a1Digest.appendColon();
			a1Digest.append(cnonce);
			a1 = a1Digest.getDigest();
		}

		// Obtain the method
		HttpRequest request = connection.getHttpRequest();
		String httpMethod = request.getMethod();

		// Calculate A2 value
		Digest a2Digest = new Digest(algorithm);
		a2Digest.append(httpMethod);
		a2Digest.appendColon();
		a2Digest.append(digestUri);
		if ("auth-int".equalsIgnoreCase(qop)) {
			// Calculate body digest
			Digest bodyDigest = new Digest(algorithm);
			InputStream body = request.getBody().getBrowseStream();
			bodyDigest.append(body);

			// Append the body digest
			a2Digest.appendColon();
			a2Digest.append(bodyDigest.getDigest());
		}
		byte[] a2 = a2Digest.getDigest();

		// Create the message digest for the required response
		Digest digest = new Digest(algorithm);

		// Add the A1 value
		digest.append(a1);

		// Add the nounce (both this and RFC2069 compatibility)
		digest.appendColon();
		digest.append(nonce);

		// Provide "auth", "auth-int" details
		if (("auth".equalsIgnoreCase(qop))
				|| ("auth-int".equalsIgnoreCase(qop))) {
			digest.appendColon();
			digest.append(nonceCount);
			digest.appendColon();
			digest.append(cnonce);
			digest.appendColon();
			digest.append(qop);
		}

		// Add the A2 value
		digest.appendColon();
		digest.append(a2);

		// Obtain required response
		byte[] requiredDigest = digest.getDigest();
		String requiredResponse = new String(requiredDigest, US_ASCII);

		// Ensure correct response
		if (!requiredResponse.equals(response)) {
			return null; // not authenticated
		}

		// Obtain the roles
		Set<String> roles = store.retrieveRoles(username, realm);

		// Return the HTTP security
		return new HttpSecurityImpl(this.getAuthenticationScheme(), username,
				roles);
	}

	@Override
	public void loadUnauthorised(ServerHttpConnection connection,
			HttpSession session, Map<Dependencies, Object> dependencies)
			throws AuthenticationException {

		// Obtain the algorithm
		CredentialStore store = (CredentialStore) dependencies
				.get(Dependencies.CREDENTIAL_STORE);
		String algorithm = store.getAlgorithm();

		// Obtain the ETag header value (if available)
		HttpRequest request = connection.getHttpRequest();
		String eTag = "";
		for (HttpHeader header : request.getHeaders()) {
			if ("ETag".equalsIgnoreCase(header.getName())) {
				eTag = header.getValue();
			}
		}

		// Obtain the current time stamp
		String timestamp = this.getTimestamp();

		// Calculate the nonce
		Digest nonceDigest = new Digest(algorithm);
		nonceDigest.append(timestamp);
		nonceDigest.appendColon();
		nonceDigest.append(eTag);
		nonceDigest.appendColon();
		nonceDigest.append(this.privateKey);
		byte[] nonceData = nonceDigest.getDigest();
		String nonce = new String(nonceData, US_ASCII);

		// Calculate the opaque
		Digest opaqueDigest = new Digest(algorithm);
		opaqueDigest.append(this.getOpaqueSeed());
		byte[] opaqueData = opaqueDigest.getDigest();
		String opaque = new String(opaqueData, US_ASCII);

		// Construct the authentication challenge
		String challenge = this.getAuthenticationScheme() + " realm=\""
				+ this.realm + "\", qop=\"auth,auth-int\", nonce=\"" + nonce
				+ "\", opaque=\"" + opaque + "\", algorithm=\"" + algorithm
				+ "\"";

		// Specify unauthorised
		HttpResponse response = connection.getHttpResponse();
		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		response.addHeader("WWW-Authenticate", challenge);

		// Record details for authentication
		session.setAttribute(SECURITY_STATE_SESSION_KEY, new SecurityState(
				nonce, opaque));
	}

	/**
	 * State of security to be stored within the {@link HttpSession}.
	 */
	private static class SecurityState implements Serializable {

		/**
		 * Nonce for the authentication.
		 */
		public final String nonce;

		/**
		 * Opaque for authentication.
		 */
		public final String opaque;

		/**
		 * Nonce count for current request.
		 */
		public int nonceCount = 1;

		/**
		 * Initiate.
		 * 
		 * @param nonce
		 *            Nounce for authentication.
		 * @param opaque
		 *            Opaque for authentication.
		 */
		private SecurityState(String nonce, String opaque) {
			this.nonce = nonce;
			this.opaque = opaque;
		}
	}

	/**
	 * Digest.
	 */
	private static class Digest {

		/**
		 * Colon for digest.
		 */
		private static final byte[] COLON = ":".getBytes(US_ASCII);

		/**
		 * {@link MessageDigest}.
		 */
		private final MessageDigest digest;

		/**
		 * Initiate.
		 * 
		 * @param algorithm
		 *            Algorithm.
		 * @throws AuthenticationException
		 *             If fails to initiate for algorithm.
		 */
		public Digest(String algorithm) throws AuthenticationException {
			try {
				this.digest = MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException ex) {
				throw new AuthenticationException(ex);
			}
		}

		/**
		 * Appends &apos;:&apos; to this digest.
		 */
		public void appendColon() {
			this.digest.update(COLON);
		}

		/**
		 * Appends the text.
		 * 
		 * @param text
		 *            Text.
		 */
		public void append(String text) {

			// Ensure have text to append
			if (text == null) {
				return; // no text
			}

			// Append the text
			this.append(text.getBytes(US_ASCII));
		}

		/**
		 * Appends the data.
		 * 
		 * @param data
		 *            Data.
		 */
		public void append(byte[] data) {
			this.digest.update(data);
		}

		/**
		 * Appends all the data of the {@link InputStream}.
		 * 
		 * @param stream
		 *            {@link InputStream} of data to append.
		 * @throws AuthenticationException
		 *             If fails to append the {@link InputStream} data.
		 */
		public void append(InputStream stream) throws AuthenticationException {
			try {
				for (int value = stream.read(); value != -1; value = stream
						.read()) {
					this.digest.update((byte) value);
				}
			} catch (IOException ex) {
				throw new AuthenticationException(ex);
			}
		}

		/**
		 * Obtains the digest data after algorithm applied.
		 * 
		 * @return Digest data.
		 */
		public byte[] getDigest() {

			// Obtain the digest
			byte[] digest = this.digest.digest();

			// Obtain the Hex encoded digest
			String digestText = new String(Hex.encodeHex(digest, true));
			byte[] textDigest = digestText.getBytes(US_ASCII);

			// Return the text digest
			return textDigest;
		}
	}

}