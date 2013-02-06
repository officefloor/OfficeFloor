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
import java.util.Set;

import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpRatifyContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.CredentialStoreUtil;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.commons.codec.binary.Base64;

/**
 * {@link HttpSecuritySource} for <code>Basic</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecuritySource
		extends
		AbstractHttpSecuritySource<HttpSecurity, Void, BasicHttpSecuritySource.Dependencies, None> {

	/**
	 * Authentication scheme Basic.
	 */
	public static final String AUTHENTICATION_SCHEME_BASIC = "Basic";

	/**
	 * Name of property to retrieve the realm being secured.
	 */
	public static final String PROPERTY_REALM = "http.security.basic.realm";

	/**
	 * {@link Charset} for {@link HttpRequest} headers.
	 */
	private static final Charset US_ASCII = HttpRequestParserImpl.US_ASCII;

	/**
	 * Name of attribute to register the {@link HttpSecurity} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.source.basic.http.security";

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
	protected void loadMetaData(
			MetaDataContext<HttpSecurity, Void, Dependencies, None> context)
			throws Exception {
		HttpSecuritySourceContext securityContext = context
				.getHttpSecuritySourceContext();

		// Obtain the realm
		this.realm = securityContext.getProperty(PROPERTY_REALM);

		// Provide meta-data
		context.setSecurityClass(HttpSecurity.class);
		context.addDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
	}

	@Override
	public boolean ratify(HttpRatifyContext<HttpSecurity, Void> context) {

		// Attempt to obtain from session
		HttpSecurity security = (HttpSecurity) context.getSession()
				.getAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		if (security != null) {
			// Load the security and no need to authenticate
			context.setHttpSecurity(security);
			return false;
		}

		// Determine if basic credentials on request
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection()
						.getHttpRequest());
		if ((scheme == null)
				|| (!(AUTHENTICATION_SCHEME_BASIC.equalsIgnoreCase(scheme
						.getAuthentiationScheme())))) {
			return false; // must be basic authentication
		}

		// As here, then have basic authentication details
		return true;
	}

	@Override
	public void authenticate(
			HttpAuthenticateContext<HttpSecurity, Void, Dependencies> context)
			throws IOException {

		// Obtain the connection
		ServerHttpConnection connection = context.getConnection();

		// Obtain the authentication scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(connection.getHttpRequest());
		if ((scheme == null)
				|| (!(AUTHENTICATION_SCHEME_BASIC.equalsIgnoreCase(scheme
						.getAuthentiationScheme())))) {
			return; // no/incorrect authentication scheme
		}

		// Decode Base64 credentials into userId:password text
		byte[] userIdPasswordBytes = Base64
				.decodeBase64(scheme.getParameters());
		String userIdPassword = new String(userIdPasswordBytes, US_ASCII);

		// Split out the userId and password
		int separatorIndex = userIdPassword.indexOf(':');
		if (separatorIndex < 0) {
			return; // no user/password to authenticate
		}
		String userId = userIdPassword.substring(0, separatorIndex);
		String password = userIdPassword.substring(separatorIndex + 1);

		// Obtain the credential entry for the connection
		CredentialStore store = (CredentialStore) context
				.getObject(Dependencies.CREDENTIAL_STORE);
		CredentialEntry entry = store.retrieveCredentialEntry(userId,
				this.realm);
		if (entry == null) {
			return; // unknown user
		}

		// Obtain the required credentials for the connection
		byte[] requiredCredentials = entry.retrieveCredentials();

		// Translate password as per algorithm
		byte[] inputCredentials = password.getBytes(US_ASCII);
		String algorithm = store.getAlgorithm();
		MessageDigest digest = CredentialStoreUtil.createDigest(algorithm);
		if (digest != null) {
			// Translate credentials as per algorithm
			digest.update(inputCredentials);
			inputCredentials = digest.digest();
		}

		// Ensure match for authentication
		if (requiredCredentials.length != inputCredentials.length) {
			return; // not authenticated
		} else {
			for (int i = 0; i < requiredCredentials.length; i++) {
				if (requiredCredentials[i] != inputCredentials[i]) {
					return; // not authenticated
				}
			}
		}

		// Authenticated, so obtain roles and create the HTTP Security
		Set<String> roles = entry.retrieveRoles();
		HttpSecurity security = new HttpSecurityImpl(
				AUTHENTICATION_SCHEME_BASIC, userId, roles);

		// Remember HTTP Security for further requests
		context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY,
				security);

		// Return the HTTP Security
		context.setHttpSecurity(security);
	}

	@Override
	public void challenge(HttpChallengeContext<Dependencies, None> context)
			throws IOException {

		// Load the challenge
		HttpResponse response = context.getConnection().getHttpResponse();
		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		response.addHeader("WWW-Authenticate", AUTHENTICATION_SCHEME_BASIC
				+ " realm=\"" + this.realm + "\"");
	}

}