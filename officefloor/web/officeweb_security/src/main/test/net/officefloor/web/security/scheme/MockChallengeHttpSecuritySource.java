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

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * <p>
 * Mock {@link HttpSecuritySource} to use for testing with challenges.
 * <p>
 * It provides a {@link MockAuthentication} and {@link MockAccessControl} by the
 * following <code>Basic</code> authentication scheme, except that:
 * <ul>
 * <li>authentication is obtained by user name and password being the same</li>
 * <li>the {@link MockAccessControl} is provided the user name as a role (allows
 * logging in with various roles for testing). Multiple roles can be specified
 * by the user name being a comma separate list.</li>
 * </ul>
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockChallengeHttpSecuritySource
		extends AbstractHttpSecuritySource<MockAuthentication, MockAccessControl, Void, None, None>
		implements HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> {

	/**
	 * Obtains the <code>WWW-Authenticate</code> {@link HttpHeader} value.
	 * 
	 * @param realm Realm.
	 * @return <code>WWW-Authenticate</code> {@link HttpHeader} value.
	 */
	public static String getHeaderChallengeValue(String realm) {
		return AUTHENTICATION_SCHEME + " realm=\"" + realm + "\"";
	}

	/**
	 * Name of {@link Property} to configure the realm.
	 */
	public static final String PROPERTY_REALM = "realm";

	/**
	 * Authentication scheme reported to the application via the
	 * {@link HttpAccessControl}.
	 */
	public static final String AUTHENTICATION_SCHEME = "Mock";

	/**
	 * Name of attribute to register the {@link HttpAccessControl} within the
	 * {@link HttpSession}.
	 */
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.mock.challenge";

	/**
	 * Realm.
	 */
	private String realm = null;

	/**
	 * Instantiate with the realm.
	 * 
	 * @param realm Realm.
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
	protected void loadMetaData(MetaDataContext<MockAuthentication, MockAccessControl, Void, None, None> context)
			throws Exception {

		// Ensure have the realm
		if (this.realm == null) {
			this.realm = context.getHttpSecuritySourceContext().getProperty(PROPERTY_REALM);
		}

		// Specify the access control
		context.setAuthenticationClass(MockAuthentication.class);
		context.setHttpAuthenticationFactory((authentication) -> new MockHttpAuthentication<>(authentication, null));
		context.setAccessControlClass(MockAccessControl.class);
		context.setHttpAccessControlFactory((accessControl) -> new MockHttpAccessControl(accessControl));
	}

	@Override
	public HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {

		// Return the mock HTTP security
		return this;
	}

	/*
	 * =================== HttpSecurity ===========================
	 */

	@Override
	public MockAuthentication createAuthentication(AuthenticationContext<MockAccessControl, Void> context) {
		MockAuthentication authentication = new MockAuthentication(context);
		context.authenticate(null, null); // attempt authentication
		return authentication;
	}

	@Override
	public boolean ratify(Void credentials, RatifyContext<MockAccessControl> context) {

		// Attempt to obtain from session
		MockAccessControl accessControl = (MockAccessControl) context.getSession()
				.getAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		if (accessControl != null) {
			// Load the security and no need to authenticate
			context.accessControlChange(accessControl, null);
			return false;
		}

		// Determine if basic credentials on request
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection().getRequest());
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			return false; // must be mock authentication
		}

		// As here, then have mock authentication details
		return true;
	}

	@Override
	public void authenticate(Void credentials, AuthenticateContext<MockAccessControl, None, None> context)
			throws HttpException {

		// Obtain the authentication scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection().getRequest());
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			return; // no/incorrect authentication scheme
		}

		// Parse out user and roles
		String[] parameters = scheme.getParameters().split(",");
		String userName = parameters[0].trim();
		String password = parameters.length > 1 ? parameters[1].trim() : null;
		if (!(userName.equals(password))) {
			return; // must match to authenticate
		}
		String[] roles = new String[parameters.length - 1];
		for (int i = 1; i < parameters.length; i++) {
			roles[i - 1] = parameters[i].trim();
		}

		// Create the access control
		MockAccessControl accessControl = new MockAccessControl(userName, roles);

		// Remember access control for further requests
		context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY, accessControl);

		// Return the access control
		context.accessControlChange(accessControl, null);
	}

	@Override
	public void challenge(ChallengeContext<None, None> context) throws HttpException {
		// Load the challenge
		context.setChallenge(AUTHENTICATION_SCHEME, this.realm);
	}

	@Override
	public void logout(LogoutContext<None, None> context) throws HttpException {

		// Forget access control for further requests (requires login again)
		context.getSession().removeAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
	}

}
