/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security.scheme;

import net.officefloor.compile.properties.Property;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.store.CredentialStoreUtil;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Form based {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpSecuritySource extends
		AbstractHttpSecuritySource<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, FormHttpSecuritySource.Dependencies, FormHttpSecuritySource.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		CREDENTIAL_STORE
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		FORM_LOGIN_PAGE
	}

	/**
	 * Name of {@link Property} to retrieve the realm being secured.
	 */
	public static final String PROPERTY_REALM = "realm";

	/**
	 * Name of attribute to register the {@link HttpAccessControl} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.form";

	/**
	 * Realm being secured.
	 */
	private String realm;

	/**
	 * <p>
	 * Undertakes the authentication.
	 * <p>
	 * This is separated out so that may be overridden to provide differing means
	 * for authentication.
	 * 
	 * @param userId   Identifier for the user.
	 * @param realm    Security realm.
	 * @param password Password.
	 * @param store    {@link CredentialStore}.
	 * @return {@link HttpAccessControl} or <code>null</code> if not authenticated.
	 * @throws HttpException If fails communication with the
	 *                       {@link CredentialStore}.
	 */
	protected HttpAccessControl authenticate(String userId, String realm, byte[] password, CredentialStore store)
			throws HttpException {
		return CredentialStoreUtil.authenticate(userId, realm, password, "Form", store);
	}

	/*
	 * ======================== HttpSecuritySource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_REALM, "Realm");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(
			MetaDataContext<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> context)
			throws Exception {
		HttpSecuritySourceContext securityContext = context.getHttpSecuritySourceContext();

		// Obtain the realm
		this.realm = securityContext.getProperty(PROPERTY_REALM);

		// Provide meta-data
		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass(HttpAccessControl.class);
		context.setCredentialsClass(HttpCredentials.class);
		context.addDependency(Dependencies.CREDENTIAL_STORE, CredentialStore.class);
		context.addFlow(Flows.FORM_LOGIN_PAGE, null).setLabel("form");
	}

	@Override
	public HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {

		// Create and return the mock HTTP security
		return new FormHttpSecurity(this.realm);
	}

	/**
	 * Form {@link HttpSecurity}.
	 */
	private class FormHttpSecurity implements
			HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, Dependencies, Flows> {

		/**
		 * Realm.
		 */
		private final String realm;

		/**
		 * Instantiate.
		 * 
		 * @param realm Realm.
		 */
		private FormHttpSecurity(String realm) {
			this.realm = realm;
		}

		/*
		 * ====================== HttpSecurity =========================
		 */

		@Override
		public HttpAuthentication<HttpCredentials> createAuthentication(
				AuthenticationContext<HttpAccessControl, HttpCredentials> context) {
			HttpAuthenticationImpl<HttpCredentials> authentication = new HttpAuthenticationImpl<>(context,
					HttpCredentials.class);
			authentication.authenticate(null, null);
			return authentication;
		}

		@Override
		public boolean ratify(HttpCredentials credentials, RatifyContext<HttpAccessControl> context) {

			// Attempt to obtain from session
			HttpAccessControl accessControl = (HttpAccessControl) context.getSession()
					.getAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY));
			if (accessControl != null) {
				// Load the access control and no need to authenticate
				context.accessControlChange(accessControl, null);
				return false;
			}

			// Determine if have credentials
			if (credentials == null) {
				return false; // no credentials, so no authentication
			}

			// As here, then have credentials to authenticate
			return true;
		}

		@Override
		public void authenticate(HttpCredentials credentials,
				AuthenticateContext<HttpAccessControl, Dependencies, Flows> context) throws HttpException {

			// Obtain the session
			HttpSession session = context.getSession();

			// Obtain the credentials
			if (credentials == null) {
				return; // must have credentials
			}

			// Obtain the credential details
			String username = credentials.getUsername();
			if (username == null) {
				return; // must have user name
			}
			byte[] password = credentials.getPassword();
			if (password == null) {
				return; // must have password
			}

			// Obtain the credential entry for the connection
			CredentialStore store = (CredentialStore) context.getObject(Dependencies.CREDENTIAL_STORE);

			// Authenticate
			HttpAccessControl accessControl = FormHttpSecuritySource.this.authenticate(username, this.realm, password,
					store);
			if (accessControl == null) {
				return; // not authenticated
			}

			// Remember access control for further requests
			session.setAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY), accessControl);

			// Return the access control
			context.accessControlChange(accessControl, null);
		}

		@Override
		public void challenge(ChallengeContext<Dependencies, Flows> context) throws HttpException {

			// Trigger flow for login page
			context.doFlow(Flows.FORM_LOGIN_PAGE, null, null);
		}

		@Override
		public void logout(LogoutContext<Dependencies, Flows> context) throws HttpException {

			// Obtain the session
			HttpSession session = context.getSession();

			// Forget HTTP Security for further requests (requires login again)
			session.removeAttribute(context.getQualifiedAttributeName(SESSION_ATTRIBUTE_HTTP_SECURITY));
		}
	}

}
