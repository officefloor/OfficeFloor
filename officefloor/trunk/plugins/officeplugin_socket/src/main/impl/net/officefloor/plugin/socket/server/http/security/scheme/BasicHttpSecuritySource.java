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

import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.security.store.CredentialStore;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

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

		// Decode Base64 credentials
		byte[] credentials = Base64.decodeBase64(parameters);

		// Obtain the string value
		String userIdPassword = new String(credentials,
				HttpRequestParserImpl.US_ASCII);

		// Obtain location of user Id to password separator
		int separatorIndex = userIdPassword.indexOf(':');
		if (separatorIndex < 0) {
			return null; // no user/password to authenticate
		}

		// Have user Id and password, so parse out
		String userId = userIdPassword.substring(0, separatorIndex);
		String password = userIdPassword.substring(separatorIndex + 1);

		// Obtain the credentials store
		CredentialStore store = (CredentialStore) dependencies
				.get(Dependencies.CREDENTIAL_STORE);

		// Obtain the password for the user
		byte[] usAsciiPassword = store.retrieveCredentials(userId, this.realm);
		String requiredPassword = new String(usAsciiPassword,
				HttpRequestParserImpl.US_ASCII);

		// Ensure match for authentication
		if (!requiredPassword.equals(password)) {
			return null; // not authenticated
		}

		// Obtain the roles for the user
		Set<String> roles = store.retrieveRoles(userId, this.realm);

		// Return the Http Security
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