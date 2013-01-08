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
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.CredentialStoreUtil;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link HttpSecuritySource} for <code>Basic</code> HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecuritySource implements
		HttpSecuritySource<BasicHttpSecuritySource.Dependencies> {

	/**
	 * Name of property to retrieve the realm being secured.
	 */
	public static final String PROPERTY_REALM = "http.security.basic.realm";

	/**
	 * {@link Charset} for {@link HttpRequest} headers.
	 */
	private static final Charset US_ASCII = HttpRequestParserImpl.US_ASCII;

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
	public void init(HttpSecuritySourceContext<Dependencies> context)
			throws Exception {

		// Obtain the realm
		this.realm = context.getProperty(PROPERTY_REALM);

		// Require credential store
		context.requireDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
	}

	@Override
	public String getAuthenticationScheme() {
		return "Basic";
	}

	@Override
	public HttpSecurity authenticate(String parameters,
			ServerHttpConnection connection, HttpSession session,
			Map<Dependencies, Object> dependencies)
			throws AuthenticationException {

		// Decode Base64 credentials into userId:password text
		byte[] userIdPasswordBytes = Base64.decodeBase64(parameters);
		String userIdPassword = new String(userIdPasswordBytes, US_ASCII);

		// Split out the userId and password
		int separatorIndex = userIdPassword.indexOf(':');
		if (separatorIndex < 0) {
			return null; // no user/password to authenticate
		}
		String userId = userIdPassword.substring(0, separatorIndex);
		String password = userIdPassword.substring(separatorIndex + 1);

		// Obtain the credential entry for the connection
		CredentialStore store = (CredentialStore) dependencies
				.get(Dependencies.CREDENTIAL_STORE);
		CredentialEntry entry = store.retrieveCredentialEntry(userId,
				this.realm);
		if (entry == null) {
			return null; // unknown user
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
			return null; // not authenticated
		} else {
			for (int i = 0; i < requiredCredentials.length; i++) {
				if (requiredCredentials[i] != inputCredentials[i]) {
					return null; // not authenticated
				}
			}
		}

		// Authenticated, so obtain roles and return the Http Security
		Set<String> roles = entry.retrieveRoles();
		return new HttpSecurityImpl(this.getAuthenticationScheme(), userId,
				roles);
	}

	@Override
	public void loadUnauthorised(ServerHttpConnection connection,
			HttpSession session, Map<Dependencies, Object> depedendencies)
			throws AuthenticationException {

		// Obtain the response to load the challenge
		HttpResponse response = connection.getHttpResponse();

		// Load the challenge
		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		response.addHeader("WWW-Authenticate", this.getAuthenticationScheme()
				+ " realm=\"" + this.realm + "\"");
	}

}