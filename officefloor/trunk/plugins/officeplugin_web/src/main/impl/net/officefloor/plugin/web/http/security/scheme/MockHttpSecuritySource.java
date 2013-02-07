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
import java.util.Arrays;
import java.util.HashSet;

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
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>
 * Mock {@link HttpSecuritySource} to use for testing.
 * <p>
 * It provides a {@link HttpSecurity} by the following <code>Basic</code>
 * authentication scheme, except that:
 * <ul>
 * <li>authentication is obtained by username and password being the same</li>
 * <li>the {@link HttpSecurity} is provided the username as a role (allows
 * logging in with various roles for testing). Multiple roles can be specified
 * by the username being a comma separate list.</li>
 * </ul>
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpSecuritySource extends
		AbstractHttpSecuritySource<HttpSecurity, Void, None, None> {

	/**
	 * Authentication scheme reported to the application via the
	 * {@link HttpSecurity}.
	 */
	public static final String AUTHENTICATION_SCHEME_MOCK = "Mock";

	/**
	 * Authentication scheme used for implementation. This is the scheme re-used
	 * for implementing the mocking of authentication but should not be reported
	 * to the application as the trusted scheme of authentication.
	 */
	private static final String IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC = "Basic";

	/**
	 * {@link Charset} for {@link HttpRequest} headers.
	 */
	private static final Charset US_ASCII = HttpRequestParserImpl.US_ASCII;

	/**
	 * Always the test realm when mocking.
	 */
	private static final String REALM = "Test";

	/**
	 * Name of attribute to register the {@link HttpSecurity} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.source.mock.http.security";

	/*
	 * ==================== HttpSecuritySource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<HttpSecurity, Void, None, None> context)
			throws Exception {
		context.setSecurityClass(HttpSecurity.class);
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
				|| (!(IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC
						.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			return false; // must be basic authentication
		}

		// As here, then have basic authentication details
		return true;
	}

	@Override
	public void authenticate(
			HttpAuthenticateContext<HttpSecurity, Void, None> context)
			throws IOException {

		// Obtain the connection
		ServerHttpConnection connection = context.getConnection();

		// Obtain the authentication scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(connection.getHttpRequest());
		if ((scheme == null)
				|| (!(IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC
						.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
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

		// Authenticated if user Id and password match
		if (!(userId.equalsIgnoreCase(password))) {
			return; // not match, so not authenticated
		}

		// Split the user Id for the potential multiple roles
		String[] roles = userId.split(",");
		for (int i = 0; i < roles.length; i++) {
			roles[i] = roles[i].trim();
		}

		// Create the HTTP Security
		HttpSecurity security = new HttpSecurityImpl(
				AUTHENTICATION_SCHEME_MOCK, userId, new HashSet<String>(
						Arrays.asList(roles)));

		// Remember HTTP Security for further requests
		context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY,
				security);

		// Return the HTTP Security
		context.setHttpSecurity(security);
	}

	@Override
	public void challenge(HttpChallengeContext<None, None> context)
			throws IOException {

		// Load the challenge
		HttpResponse response = context.getConnection().getHttpResponse();
		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		response.addHeader("WWW-Authenticate",
				IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC + " realm=\"" + REALM
						+ "\"");
	}

}