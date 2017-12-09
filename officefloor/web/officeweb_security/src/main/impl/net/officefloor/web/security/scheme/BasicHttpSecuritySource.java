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

import org.apache.commons.codec.binary.Base64;

import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.AbstractHttpSecuritySource;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.store.CredentialStoreUtil;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpAuthenticateContext;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpLogoutContext;
import net.officefloor.web.spi.security.HttpRatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;

/**
 * {@link HttpSecuritySource} for <code>Basic</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecuritySource extends
		AbstractHttpSecuritySource<HttpAuthentication<Void>, HttpAccessControl, Void, BasicHttpSecuritySource.Dependencies, None> {

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
		 * @param realm
		 *            Realm.
		 */
		private BasicHttpSecurity(String realm) {
			this.realm = realm;
		}

		/*
		 * ==================== HttpSecurity ==============================
		 */

		@Override
		public HttpAuthentication<Void> createAuthentication() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean ratify(Void credentials, HttpRatifyContext<HttpAccessControl> context) {

			// Attempt to obtain from session
			HttpAccessControl accessControl = (HttpAccessControl) context.getSession()
					.getAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
			if (accessControl != null) {
				// Load the security and no need to authenticate
				context.setAccessControl(accessControl);
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
		public void authenticate(Void credentials, HttpAuthenticateContext<HttpAccessControl, Dependencies> context)
				throws HttpException {

			// Obtain the connection and session
			ServerHttpConnection connection = context.getConnection();
			HttpSession session = context.getSession();

			// Obtain the authentication scheme
			HttpAuthenticationScheme scheme = HttpAuthenticationScheme
					.getHttpAuthenticationScheme(connection.getRequest());
			if ((scheme == null)
					|| (!(AUTHENTICATION_SCHEME_BASIC.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
				return; // no/incorrect authentication scheme
			}

			// Decode Base64 credentials into userId:password text
			byte[] userIdPasswordBytes = Base64.decodeBase64(scheme.getParameters());
			String userIdPassword = new String(userIdPasswordBytes, UTF_8);

			// Split out the userId and password
			int separatorIndex = userIdPassword.indexOf(':');
			if (separatorIndex < 0) {
				return; // no user/password to authenticate
			}
			String userId = userIdPassword.substring(0, separatorIndex);
			String password = userIdPassword.substring(separatorIndex + 1);

			// Obtain the credential entry for the connection
			CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

			// Authenticate
			HttpAccessControl security = CredentialStoreUtil.authenticate(userId, this.realm, password.getBytes(UTF_8),
					AUTHENTICATION_SCHEME_BASIC, store);
			if (security == null) {
				return; // not authenticated
			}

			// Remember HTTP Security for further requests
			session.setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY, security);

			// Return the HTTP Security
			context.setAccessControl(security);
		}

		@Override
		public void challenge(HttpChallengeContext<Dependencies, None> context) throws HttpException {

			// Provide challenge
			context.setChallenge(AUTHENTICATION_SCHEME_BASIC, this.realm);
		}

		@Override
		public void logout(HttpLogoutContext<Dependencies> context) throws HttpException {

			// Obtain the session
			HttpSession session = context.getSession();

			// Forget HTTP Security for further requests (requires login again)
			session.removeAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		}
	}

}