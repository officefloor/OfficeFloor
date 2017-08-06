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
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpLogoutContext;
import net.officefloor.plugin.web.http.security.HttpRatifyContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.CredentialStoreUtil;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.parse.impl.HttpRequestParserImpl;

/**
 * {@link HttpSecuritySource} for <code>Digest</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecuritySource
		extends AbstractHttpSecuritySource<HttpSecurity, Void, DigestHttpSecuritySource.Dependencies, None> {

	/**
	 * Authentication scheme Digest.
	 */
	public static final String AUTHENTICATION_SCHEME_DIGEST = "Digest";

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
	 * Name of attribute to register the {@link HttpSecurity} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.source.digest.http.security";

	/**
	 * Key into the {@link HttpSession} for the {@link SecurityState}.
	 */
	protected static final String SECURITY_STATE_SESSION_KEY = "#" + DigestHttpSecuritySource.class.getName() + "#";

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
	 * @throws IOException
	 *             If fails to parse parameters.
	 */
	private Properties parseParameters(String text) {

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
			NEXT_CHARACTER: switch (character) {

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
					break NEXT_CHARACTER;

				default:
					// Include comma
					break NEXT_CHARACTER;
				}

			case '=':
				// Handle assignment
				switch (state) {
				case NAME:
					// Obtain the name
					end = i;
					name = text.substring(start, end);
					name = name.trim(); // ignore spacing
					state = ParameterState.NAME_VALUE_SEPARATION;
					break NEXT_CHARACTER;

				default:
					// Include equals
					break NEXT_CHARACTER;
				}

			case '"':
				// Handle quote
				switch (state) {
				case NAME_VALUE_SEPARATION:
					// Quoted value
					start = i + 1; // ignore quote
					state = ParameterState.QUOTED_VALUE;
					break NEXT_CHARACTER;

				case QUOTED_VALUE:
					// End of quoted value
					end = i;
					String value = text.substring(start, end);

					// Load parameter
					parameters.setProperty(name.toLowerCase(), value);

					// Reset for next parameter
					state = ParameterState.INIT;
					break NEXT_CHARACTER;

				default:
					// Include quote
					break NEXT_CHARACTER;
				}

			case ' ':
				// Handle space
				switch (state) {
				case INIT:
					// Ignore leading space
					break NEXT_CHARACTER;

				default:
					// Include space
					break NEXT_CHARACTER;
				}

			default:
				// Handle value
				switch (state) {
				case INIT:
					// Start processing the name
					start = i;
					state = ParameterState.NAME;
					break NEXT_CHARACTER;

				case NAME:
					// Letter of name
					break NEXT_CHARACTER;

				case NAME_VALUE_SEPARATION:
					// Non quoted value
					start = i;
					state = ParameterState.VALUE;
					break NEXT_CHARACTER;

				default:
					// Include other characters
					break NEXT_CHARACTER;
				}
			}
		}

		// Provide final value
		if (ParameterState.VALUE.equals(state)) {
			// Remaining content is value
			String value = text.substring(start);
			value = value.trim(); // ignore spacing

			// Load parameter
			parameters.setProperty(name.toLowerCase(), value);
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
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_REALM, "Realm");
		context.addProperty(PROPERTY_PRIVATE_KEY, "Private Key");
	}

	@Override
	protected void loadMetaData(MetaDataContext<HttpSecurity, Void, Dependencies, None> context) throws Exception {
		HttpSecuritySourceContext securityContext = context.getHttpSecuritySourceContext();

		// Obtain the properties
		this.realm = securityContext.getProperty(PROPERTY_REALM);
		this.privateKey = securityContext.getProperty(PROPERTY_PRIVATE_KEY);

		// Provide meta-data
		context.setSecurityClass(HttpSecurity.class);
		context.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class);
	}

	@Override
	public boolean ratify(HttpRatifyContext<HttpSecurity, Void> context) {

		// Attempt to obtain from session
		HttpSecurity security = (HttpSecurity) context.getSession().getAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		if (security != null) {
			// Load the security and no need to authenticate
			context.setHttpSecurity(security);
			return false;
		}

		// Determine if digest credentials on request
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection().getHttpRequest());
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME_DIGEST.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			return false; // no/incorrect authentication scheme
		}

		// As here, then have digest authentication details
		return true;
	}

	@Override
	public void authenticate(HttpAuthenticateContext<HttpSecurity, Void, Dependencies> context) throws IOException {

		// Obtain the dependencies
		ServerHttpConnection connection = context.getConnection();
		HttpRequest request = connection.getHttpRequest();
		HttpSession session = context.getSession();
		CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

		// Obtain the authentication scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme.getHttpAuthenticationScheme(request);
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME_DIGEST.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			return; // no/incorrect authentication scheme
		}

		// Authenticate Digest as per RFC2617

		// Obtain the nounce
		SecurityState securityState = (SecurityState) session.getAttribute(SECURITY_STATE_SESSION_KEY);
		if (securityState == null) {
			// No challenge (i.e. no nounce), so not authenticated
			return;
		}
		String nonce = securityState.nonce;

		// Obtain the parameter values
		Properties params = this.parseParameters(scheme.getParameters());
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
			return; // not authenticated
		}

		// Ensure correct nonce count
		if (nonceCount != null) {
			try {
				byte[] decodedNonceCount = Hex.decodeHex(nonceCount.toCharArray());
				long nonceCountValue = 0;
				for (byte decodedNonceCountByte : decodedNonceCount) {
					nonceCountValue <<= 8; // shift to right by byte
					nonceCountValue += decodedNonceCountByte; // add byte
				}
				if (securityState.nonceCount != nonceCountValue) {
					return; // likely replay attack so do not authenticate
				}

				// Increment for next authentication attempt
				securityState.nonceCount++;

			} catch (Exception ex) {
				throw new IOException(ex);
			}
		}

		// Obtain the credentials entry
		CredentialEntry entry = store.retrieveCredentialEntry(username, realm);
		if (entry == null) {
			return; // unknown user in realm
		}

		// Obtain the required credentials
		byte[] usernameRealmPasswordCredentials = entry.retrieveCredentials();

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
		String httpMethod = request.getHttpMethod().getName();

		// Calculate A2 value
		Digest a2Digest = new Digest(algorithm);
		a2Digest.append(httpMethod);
		a2Digest.appendColon();
		a2Digest.append(digestUri);
		if ("auth-int".equalsIgnoreCase(qop)) {
			// Calculate body digest
			Digest bodyDigest = new Digest(algorithm);
			InputStream body = request.getEntity().createBrowseInputStream();
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
		if (("auth".equalsIgnoreCase(qop)) || ("auth-int".equalsIgnoreCase(qop))) {
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
			return; // not authenticated
		}

		// Authenticated, so obtain roles and create the HTTP Security
		Set<String> roles = entry.retrieveRoles();
		HttpSecurity security = new HttpSecurityImpl(AUTHENTICATION_SCHEME_DIGEST, username, roles);

		// Remember HTTP Security for further requests
		context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY, security);

		// Return the HTTP Security
		context.setHttpSecurity(security);
	}

	@Override
	public void challenge(HttpChallengeContext<Dependencies, None> context) throws IOException {

		// Obtain the dependencies
		ServerHttpConnection connection = context.getConnection();
		HttpRequest request = connection.getHttpRequest();
		HttpSession session = context.getSession();
		CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

		// Obtain the algorithm
		String algorithm = store.getAlgorithm();

		// Obtain the ETag header value (if available)
		String eTag = "";
		for (HttpHeader header : request.getHttpHeaders()) {
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
		String challenge = AUTHENTICATION_SCHEME_DIGEST + " realm=\"" + this.realm
				+ "\", qop=\"auth,auth-int\", nonce=\"" + nonce + "\", opaque=\"" + opaque + "\", algorithm=\""
				+ algorithm + "\"";

		// Specify unauthorised
		HttpResponse response = connection.getHttpResponse();
		response.setHttpStatus(HttpStatus.UNAUTHORIZED);
		response.addHeader("WWW-Authenticate", challenge);

		// Record details for authentication
		session.setAttribute(SECURITY_STATE_SESSION_KEY, new SecurityState(nonce, opaque));
	}

	@Override
	public void logout(HttpLogoutContext<Dependencies> context) throws IOException {

		// Forget HTTP Security for further requests (requires login again)
		context.getSession().removeAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
	}

	/**
	 * Allows mocking {@link SecurityState} for testing.
	 */
	public static class Mock {

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
		protected static final Object MOCK_SECURITY_STATE = new SecurityState(MOCK_NONCE, MOCK_OPAQUE);
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
		 * @throws IOException
		 *             If fails to initiate for algorithm.
		 */
		public Digest(String algorithm) throws IOException {
			this.digest = CredentialStoreUtil.createDigest(algorithm);
			if (this.digest == null) {
				throw new IOException("Unable to create Digest for algorithm '" + algorithm + "'");
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
		 * @throws IOException
		 *             If fails to append the {@link InputStream} data.
		 */
		public void append(InputStream stream) throws IOException {
			for (int value = stream.read(); value != -1; value = stream.read()) {
				this.digest.update((byte) value);
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