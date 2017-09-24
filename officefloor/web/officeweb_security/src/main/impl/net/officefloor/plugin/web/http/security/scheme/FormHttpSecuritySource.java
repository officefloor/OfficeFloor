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

import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpLogoutContext;
import net.officefloor.plugin.web.http.security.HttpRatifyContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.CredentialStoreUtil;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Form based {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpSecuritySource
		extends
		AbstractHttpSecuritySource<HttpSecurity, HttpCredentials, FormHttpSecuritySource.Dependencies, FormHttpSecuritySource.Flows> {

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
	 * Name of property to retrieve the realm being secured.
	 */
	public static final String PROPERTY_REALM = "http.security.form.realm";

	/**
	 * Name of attribute to register the {@link HttpSecurity} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.source.form.http.security";

	/**
	 * Realm being secured.
	 */
	private String realm;

	/**
	 * <p>
	 * Undertakes the authentication.
	 * <p>
	 * This is separated out so that may be overridden to provide differing
	 * means for authentication.
	 * 
	 * @param userId
	 *            Identifier for the user.
	 * @param realm
	 *            Security realm.
	 * @param password
	 *            Password.
	 * @param store
	 *            {@link CredentialStore}.
	 * @return {@link HttpSecurity} or <code>null</code> if not authenticated.
	 * @throws IOException
	 *             If fails communication with the {@link CredentialStore}.
	 */
	protected HttpSecurity authenticate(String userId, String realm,
			byte[] password, CredentialStore store) throws IOException {
		return CredentialStoreUtil.authenticate(userId, realm, password,
				"Form", store);
	}

	/*
	 * ======================== HttpSecuritySource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_REALM, "Realm");
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<HttpSecurity, HttpCredentials, Dependencies, Flows> context)
			throws Exception {
		HttpSecuritySourceContext securityContext = context
				.getHttpSecuritySourceContext();

		// Obtain the realm
		this.realm = securityContext.getProperty(PROPERTY_REALM);

		// Provide meta-data
		context.setSecurityClass(HttpSecurity.class);
		context.setCredentialsClass(HttpCredentials.class);
		context.addDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
		context.addFlow(Flows.FORM_LOGIN_PAGE, null);
	}

	@Override
	public boolean ratify(
			HttpRatifyContext<HttpSecurity, HttpCredentials> context) {

		// Attempt to obtain from session
		HttpSecurity security = (HttpSecurity) context.getSession()
				.getAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		if (security != null) {
			// Load the security and no need to authenticate
			context.setHttpSecurity(security);
			return false;
		}

		// Determine if have credentials
		HttpCredentials credentials = context.getCredentials();
		if (credentials == null) {
			return false; // no credentials, so no authentication
		}

		// As here, then have credentials to authenticate
		return true;
	}

	@Override
	public void authenticate(
			HttpAuthenticateContext<HttpSecurity, HttpCredentials, Dependencies> context)
			throws IOException {

		// Obtain the credentials
		HttpCredentials credentials = context.getCredentials();
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
		CredentialStore store = (CredentialStore) context
				.getObject(Dependencies.CREDENTIAL_STORE);

		// Authenticate
		HttpSecurity security = this.authenticate(username, this.realm,
				password, store);
		if (security == null) {
			return; // not authenticated
		}

		// Remember HTTP Security for further requests
		context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY,
				security);

		// Return the HTTP Security
		context.setHttpSecurity(security);
	}

	@Override
	public void challenge(HttpChallengeContext<Dependencies, Flows> context)
			throws IOException {

		// Trigger flow for login page
		context.doFlow(Flows.FORM_LOGIN_PAGE);
	}

	@Override
	public void logout(HttpLogoutContext<Dependencies> context)
			throws IOException {

		// Forget HTTP Security for further requests (requires login again)
		context.getSession().removeAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
	}

}