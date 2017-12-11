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

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.codec.binary.Base64;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpAuthenticateContext;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpLogoutContext;
import net.officefloor.web.spi.security.HttpRatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * <p>
 * Mock {@link HttpSecuritySource} to use for testing with challenges.
 * <p>
 * It provides a {@link HttpAccessControl} by the following <code>Basic</code>
 * authentication scheme, except that:
 * <ul>
 * <li>authentication is obtained by user name and password being the same</li>
 * <li>the {@link HttpAccessControl} is provided the user name as a role (allows
 * logging in with various roles for testing). Multiple roles can be specified
 * by the user name being a comma separate list.</li>
 * </ul>
 * 
 * @author Daniel Sagenschneider
 */
public class MockChallengeHttpSecuritySource
		extends AbstractHttpSecuritySource<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> {

	/**
	 * Obtains the <code>WWW-Authenticate</code> {@link HttpHeader} value.
	 * 
	 * @param realm
	 *            Realm.
	 * @return <code>WWW-Authenticate</code> {@link HttpHeader} value.
	 */
	public static String getHeaderChallengeValue(String realm) {
		return IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC + " realm=\"" + realm + "\"";
	}

	/**
	 * Name of {@link Property} to configure the realm.
	 */
	public static final String PROPERTY_REALM = "realm";

	/**
	 * Authentication scheme reported to the application via the
	 * {@link HttpAccessControl}.
	 */
	public static final String AUTHENTICATION_SCHEME_MOCK = "Mock";

	/**
	 * Authentication scheme used for implementation. This is the scheme re-used
	 * for implementing the mocking of authentication but should not be reported
	 * to the application as the trusted scheme of authentication.
	 */
	public static final String IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC = "Basic";

	/**
	 * Name of attribute to register the {@link HttpAccessControl} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.mock";

	/**
	 * Realm.
	 */
	private String realm = null;

	/**
	 * Instantiate with the realm.
	 * 
	 * @param realm
	 *            Realm.
	 */
	public MockChallengeHttpSecuritySource(String realm) {
		this.realm = realm;
	}

	/**
	 * Default constructor.
	 */
	public MockChallengeHttpSecuritySource() {
	}

	/*
	 * ==================== HttpSecuritySource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		if (this.realm == null) {
			context.addProperty(PROPERTY_REALM, "Realm");
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> context)
			throws Exception {

		// Ensure have the realm
		if (this.realm == null) {
			this.realm = context.getHttpSecuritySourceContext().getProperty(PROPERTY_REALM);
		}

		// Specify the access control
		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass(HttpAccessControl.class);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {

		// Create and return the mock HTTP security
		return new MockChallengeHttpSecurity(this.realm);
	}

	/**
	 * Mock {@link HttpSecurity}.
	 */
	private class MockChallengeHttpSecurity
			implements HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> {

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
		private MockChallengeHttpSecurity(String realm) {
			this.realm = realm;
		}

		/*
		 * =================== HttpSecurity ===========================
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
			if ((scheme == null) || (!(IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC
					.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
				return false; // must be basic authentication
			}

			// As here, then have basic authentication details
			return true;
		}

		@Override
		public void authenticate(Void credentials, HttpAuthenticateContext<HttpAccessControl, None> context)
				throws HttpException {

			// Obtain the authentication scheme
			HttpAuthenticationScheme scheme = HttpAuthenticationScheme
					.getHttpAuthenticationScheme(context.getConnection().getRequest());
			if ((scheme == null) || (!(IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC
					.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
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
			HttpAccessControl security = new HttpAccessControlImpl(AUTHENTICATION_SCHEME_MOCK, userId,
					new HashSet<String>(Arrays.asList(roles)));

			// Remember HTTP Security for further requests
			context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY, security);

			// Return the HTTP Security
			context.setAccessControl(security);
		}

		@Override
		public void challenge(HttpChallengeContext<None, None> context) throws HttpException {
			// Load the challenge
			context.setChallenge(IMPLEMENTING_AUTHENTICATION_SCHEME_BASIC, this.realm);
		}

		@Override
		public void logout(HttpLogoutContext<None> context) throws HttpException {

			// Forget HTTP Security for further requests (requires login again)
			context.getSession().removeAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		}
	}

}