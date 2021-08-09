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

import org.apache.commons.codec.binary.Base64;

import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.store.CredentialStoreUtil;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link HttpSecuritySource} for <code>Basic</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecuritySource extends
		AbstractHttpSecuritySource<HttpAuthentication<Void>, HttpAccessControl, Void, BasicHttpSecuritySource.Dependencies, None> {

	/**
	 * Creates the <code>Authorization</code> {@link HttpHeader} value.
	 * 
	 * @param username Username.
	 * @param password Password.
	 * @return <code>Authorization</code> {@link HttpHeader} value.
	 */
	public static String createAuthorizationHttpHeaderValue(String username, String password) {

		// Encode the username/password
		String credentials = username + ":" + password;
		byte[] credentialBytes = credentials.getBytes(UTF_8);
		String encoded = Base64.encodeBase64String(credentialBytes);

		// Return the HTTP header
		return AUTHENTICATION_SCHEME_BASIC + " " + encoded;
	}

	/**
	 * Decoded credentials.
	 */
	public static class BasicCredentials {

		/**
		 * User id.
		 */
		public final String userId;

		/**
		 * Password.
		 */
		public final String password;

		/**
		 * Instantiate.
		 * 
		 * @param userId   User id.
		 * @param password Password.
		 */
		private BasicCredentials(String userId, String password) {
			this.userId = userId;
			this.password = password;
		}
	}

	/**
	 * Obtains the {@link BasicCredentials} from the {@link HttpRequest}.
	 * 
	 * @param request {@link HttpRequest}.
	 * @return {@link BasicCredentials} or <code>null</code> if not available.
	 */
	public static BasicCredentials getBasicCredentials(HttpRequest request) {

		// Obtain the authentication scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme.getHttpAuthenticationScheme(request);
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME_BASIC.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			return null; // no/incorrect authentication scheme
		}

		// Return the basic credentials
		return getBasicCredentials(scheme.getParameters());
	}

	/**
	 * Obtains the {@link BasicCredentials} from the <code>basic</code> parameters.
	 * 
	 * @param basicParameters <code>basic</code> parameters.
	 * @return {@link BasicCredentials}.
	 */
	public static BasicCredentials getBasicCredentials(String basicParameters) {

		// Decode Base64 credentials into userId:password text
		byte[] userIdPasswordBytes = Base64.decodeBase64(basicParameters);
		String userIdPassword = new String(userIdPasswordBytes, UTF_8);

		// Split out the userId and password
		int separatorIndex = userIdPassword.indexOf(':');
		if (separatorIndex < 0) {
			return null; // no user/password to authenticate
		}
		String userId = userIdPassword.substring(0, separatorIndex);
		String password = userIdPassword.substring(separatorIndex + 1);

		// Return the basic credentials
		return new BasicCredentials(userId, password);
	}

	/**
	 * Authentication scheme Basic.
	 */
	public static final String AUTHENTICATION_SCHEME_BASIC = "Basic";

	/**
	 * Name of property to retrieve the realm being secured.
	 */
	public static final String PROPERTY_REALM = "realm";

	/**
	 * Name of attribute to register the {@link HttpAccessControl} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.basic";

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		CREDENTIAL_STORE
	}

	/**
	 * Realm being secured.
	 */
	private String realm;

	/*
	 * ====================== HttpSecuritySource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_REALM, "Realm");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(
			MetaDataContext<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> context)
			throws Exception {
		HttpSecuritySourceContext securityContext = context.getHttpSecuritySourceContext();

		// Obtain the realm
		this.realm = securityContext.getProperty(PROPERTY_REALM);

		// Provide meta-data
		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass(HttpAccessControl.class);
		context.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, Dependencies, None> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {

		// Create and return the basic security
		return new BasicHttpSecurity(this.realm);
	}

	/**
	 * Basic {@link HttpSecurity}.
	 */
	private class BasicHttpSecurity
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
		private BasicHttpSecurity(String realm) {
			this.realm = realm;
		}

		/*
		 * ==================== HttpSecurity ==============================
		 */

		@Override
		public HttpAuthentication<Void> createAuthentication(AuthenticationContext<HttpAccessControl, Void> context) {
			HttpAuthenticationImpl<Void> authentication = new HttpAuthenticationImpl<>(context, null);
			context.authenticate(null, null);
			return authentication;
		}

		@Override
		public boolean ratify(Void credentials, RatifyContext<HttpAccessControl> context) {

			// Attempt to obtain from session
			HttpAccessControl accessControl = (HttpAccessControl) context.getSession()
					.getAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY));
			if (accessControl != null) {
				// Load the security and no need to authenticate
				context.accessControlChange(accessControl, null);
				return false;
			}

			// Determine if basic credentials on request
			HttpAuthenticationScheme scheme = HttpAuthenticationScheme
					.getHttpAuthenticationScheme(context.getConnection().getRequest());
			if ((scheme == null)
					|| (!(AUTHENTICATION_SCHEME_BASIC.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
				return false; // must be basic authentication
			}

			// As here, then have basic authentication details
			return true;
		}

		@Override
		public void authenticate(Void credentials, AuthenticateContext<HttpAccessControl, Dependencies, None> context)
				throws HttpException {

			// Obtain the connection and session
			ServerHttpConnection connection = context.getConnection();
			HttpSession session = context.getSession();

			// Obtain the basic credentials
			BasicCredentials basicCredentials = getBasicCredentials(connection.getRequest());
			if (basicCredentials == null) {
				return; // no credentials
			}

			// Obtain the credential entry for the connection
			CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

			// Authenticate
			HttpAccessControl accessControl = CredentialStoreUtil.authenticate(basicCredentials.userId, this.realm,
					basicCredentials.password.getBytes(UTF_8), AUTHENTICATION_SCHEME_BASIC, store);
			if (accessControl == null) {
				return; // not authenticated
			}

			// Remember access control for further requests
			session.setAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY), accessControl);

			// Return the access control
			context.accessControlChange(accessControl, null);
		}

		@Override
		public void challenge(ChallengeContext<Dependencies, None> context) throws HttpException {

			// Provide challenge
			context.setChallenge(AUTHENTICATION_SCHEME_BASIC, this.realm);
		}

		@Override
		public void logout(LogoutContext<Dependencies, None> context) throws HttpException {

			// Obtain the session
			HttpSession session = context.getSession();

			// Forget HTTP Security for further requests (requires login again)
			session.removeAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY));
		}
	}

}
