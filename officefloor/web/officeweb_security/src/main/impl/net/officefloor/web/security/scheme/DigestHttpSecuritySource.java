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
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.store.CredentialEntry;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.store.CredentialStoreUtil;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link HttpSecuritySource} for <code>Digest</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecuritySource extends
		AbstractHttpSecuritySource<HttpAuthentication<Void>, HttpAccessControl, Void, DigestHttpSecuritySource.Dependencies, None> {

	/**
	 * Authentication scheme Digest.
	 */
	public static final String AUTHENTICATION_SCHEME_DIGEST = "Digest";

	/**
	 * Name of property for the realm.
	 */
	public static final String PROPERTY_REALM = "realm";

	/**
	 * Name of property for the private key.
	 */
	public static final String PROPERTY_PRIVATE_KEY = "http.security.digest.private.key";

	/**
	 * Name of attribute to register the {@link HttpAccessControl} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.digest";

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
	 * @param text Text to parse out the parameters.
	 * @return Parameters parsed from text.
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadMetaData(
			MetaDataContext<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> context)
			throws Exception {
		HttpSecuritySourceContext securityContext = context.getHttpSecuritySourceContext();

		// Obtain the properties
		this.realm = securityContext.getProperty(PROPERTY_REALM);
		this.privateKey = securityContext.getProperty(PROPERTY_PRIVATE_KEY);

		// Provide meta-data
		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass(HttpAccessControl.class);
		context.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {

		// Create and return the digest security
		return new DigestHttpSecurity(this.realm);
	}

	/**
	 * Digest {@link HttpSecurity}.
	 */
	private class DigestHttpSecurity
			implements HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> {

		/**
		 * Realm.
		 */
		private final String realm;

		/**
		 * Instantiate.
		 * 
		 * @param realm Realm.
		 */
		private DigestHttpSecurity(String realm) {
			this.realm = realm;
		}

		/*
		 * ===================== HttpSecuirty ==============================
		 */

		@Override
		public HttpAuthentication<Void> createAuthentication(AuthenticationContext<HttpAccessControl, Void> context) {
			HttpAuthenticationImpl<Void> authentication = new HttpAuthenticationImpl<>(context, null);
			authentication.authenticate(null, null);
			return authentication;
		}

		@Override
		public boolean ratify(Void credentials, RatifyContext<HttpAccessControl> context) {

			// Attempt to obtain from session
			HttpAccessControl accessControl = (HttpAccessControl) context.getSession()
					.getAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY));
			if (accessControl != null) {
				// Load the access control and no need to authenticate
				context.accessControlChange(accessControl, null);
				return false;
			}

			// Determine if digest credentials on request
			HttpAuthenticationScheme scheme = HttpAuthenticationScheme
					.getHttpAuthenticationScheme(context.getConnection().getRequest());
			if ((scheme == null)
					|| (!(AUTHENTICATION_SCHEME_DIGEST.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
				return false; // no/incorrect authentication scheme
			}

			// As here, then have digest authentication details
			return true;
		}

		@Override
		public void authenticate(Void credentials, AuthenticateContext<HttpAccessControl, Dependencies, None> context)
				throws HttpException {

			// Obtain the connection and session
			ServerHttpConnection connection = context.getConnection();
			HttpSession session = context.getSession();

			// Obtain the dependencies
			HttpRequest request = connection.getRequest();
			CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

			// Obtain the authentication scheme
			HttpAuthenticationScheme scheme = HttpAuthenticationScheme.getHttpAuthenticationScheme(request);
			if ((scheme == null)
					|| (!(AUTHENTICATION_SCHEME_DIGEST.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
				return; // no/incorrect authentication scheme
			}

			// Authenticate Digest as per RFC2617

			// Obtain the nounce
			SecurityState securityState = (SecurityState) session
					.getAttribute(context.getQualifiedAttributeName(SECURITY_STATE_SESSION_KEY));
			if (securityState == null) {
				// No challenge (i.e. no nounce), so not authenticated
				return;
			}
			String nonce = securityState.nonce;

			// Obtain the parameter values
			Properties params = DigestHttpSecuritySource.this.parseParameters(scheme.getParameters());
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
					throw new HttpException(ex);
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
			String a1 = new String(usernameRealmPasswordCredentials, UTF_8);
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
			String httpMethod = request.getMethod().getName();

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
			String a2 = a2Digest.getDigest();

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
			String requiredResponse = digest.getDigest();

			// Ensure correct response
			if (!requiredResponse.equals(response)) {
				return; // not authenticated
			}

			// Authenticated, so obtain roles and create the access control
			Set<String> roles = entry.retrieveRoles();
			HttpAccessControl accessControl = new HttpAccessControlImpl(AUTHENTICATION_SCHEME_DIGEST, username, roles);

			// Remember access control for further requests
			session.setAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY), accessControl);

			// Return the access control
			context.accessControlChange(accessControl, null);
		}

		@Override
		public void challenge(ChallengeContext<Dependencies, None> context) throws HttpException {

			// Obtain the connection and session
			ServerHttpConnection connection = context.getConnection();
			HttpSession session = context.getSession();

			// Obtain the dependencies
			HttpRequest request = connection.getRequest();
			CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

			// Obtain the algorithm
			String algorithm = store.getAlgorithm();

			// Obtain the ETag header value (if available)
			String eTag = "";
			for (HttpHeader header : request.getHeaders()) {
				if ("ETag".equalsIgnoreCase(header.getName())) {
					eTag = header.getValue();
				}
			}

			// Obtain the current time stamp
			String timestamp = DigestHttpSecuritySource.this.getTimestamp();

			// Calculate the nonce
			Digest nonceDigest = new Digest(algorithm);
			nonceDigest.append(timestamp);
			nonceDigest.appendColon();
			nonceDigest.append(eTag);
			nonceDigest.appendColon();
			nonceDigest.append(DigestHttpSecuritySource.this.privateKey);
			String nonce = nonceDigest.getDigest();

			// Calculate the opaque
			Digest opaqueDigest = new Digest(algorithm);
			opaqueDigest.append(DigestHttpSecuritySource.this.getOpaqueSeed());
			String opaque = opaqueDigest.getDigest();

			// Construct the authentication challenge
			HttpChallenge challenge = context.setChallenge(AUTHENTICATION_SCHEME_DIGEST, this.realm);
			challenge.addParameter("qop", "\"auth,auth-int\"");
			challenge.addParameter("nonce", "\"" + nonce + "\"");
			challenge.addParameter("opaque", "\"" + opaque + "\"");
			challenge.addParameter("algorithm", "\"" + algorithm + "\"");

			// Record details for authentication
			session.setAttribute(context.getQualifiedAttributeName(SECURITY_STATE_SESSION_KEY),
					new SecurityState(nonce, opaque));
		}

		@Override
		public void logout(LogoutContext<Dependencies, None> context) throws HttpException {

			// Obtain the session
			HttpSession session = context.getSession();

			// Forget HTTP Security for further requests (requires login again)
			session.removeAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY));
		}
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
		protected static final SecurityState MOCK_SECURITY_STATE = new SecurityState(MOCK_NONCE, MOCK_OPAQUE);
	}

	/**
	 * State of security to be stored within the {@link HttpSession}.
	 */
	private static class SecurityState implements Serializable {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

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
		 * @param nonce  Nounce for authentication.
		 * @param opaque Opaque for authentication.
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
		private static final byte[] COLON = ":".getBytes(UTF_8);

		/**
		 * {@link MessageDigest}.
		 */
		private final MessageDigest digest;

		/**
		 * Initiate.
		 * 
		 * @param algorithm Algorithm.
		 * @throws HttpException If fails to initiate for algorithm.
		 */
		public Digest(String algorithm) throws HttpException {
			this.digest = CredentialStoreUtil.createDigest(algorithm);
			if (this.digest == null) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"Unable to create Digest for algorithm '" + algorithm + "'");
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
		 * @param text Text.
		 */
		public void append(String text) {

			// Ensure have text to append
			if (text == null) {
				return; // no text
			}

			// Append the text
			this.append(text.getBytes(UTF_8));
		}

		/**
		 * Appends the data.
		 * 
		 * @param data Data.
		 */
		public void append(byte[] data) {
			this.digest.update(data);
		}

		/**
		 * Appends all the data of the {@link InputStream}.
		 * 
		 * @param stream {@link InputStream} of data to append.
		 * @throws HttpException If fails to append the {@link InputStream} data.
		 */
		public void append(InputStream stream) throws HttpException {
			try {
				for (int value = stream.read(); value != -1; value = stream.read()) {
					this.digest.update((byte) value);
				}
			} catch (IOException ex) {
				throw new HttpException(ex);
			}
		}

		/**
		 * Obtains the digest data after algorithm applied.
		 * 
		 * @return Digest string.
		 */
		public String getDigest() {

			// Obtain the digest
			byte[] digest = this.digest.digest();

			// Obtain the Hex encoded digest
			return new String(Hex.encodeHex(digest, true));
		}
	}

}
