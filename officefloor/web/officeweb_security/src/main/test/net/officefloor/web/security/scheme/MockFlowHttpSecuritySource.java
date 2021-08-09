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
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Mock {@link HttpSecuritySource} that challenges with a HTML form.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockFlowHttpSecuritySource extends
		AbstractHttpSecuritySource<MockAuthentication, MockAccessControl, MockCredentials, None, MockFlowHttpSecuritySource.Flows>
		implements
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, MockFlowHttpSecuritySource.Flows> {

	/**
	 * {@link Flow} keys.
	 */
	public static enum Flows {
		CHALLENGE
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
	private static final String SESSION_ATTRIBUTE_HTTP_SECURITY = "http.security.mock.form";

	/**
	 * Realm.
	 */
	private String realm = null;

	/**
	 * Instantiate with the realm.
	 * 
	 * @param realm Realm.
	 */
	public MockFlowHttpSecuritySource(String realm) {
		this.realm = realm;
	}

	/**
	 * Default constructor.
	 */
	public MockFlowHttpSecuritySource() {
	}

	/*
	 * =================== HttpSecuritySource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		if (this.realm == null) {
			context.addProperty(PROPERTY_REALM, "Realm");
		}
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> context)
			throws Exception {

		// Ensure have the realm
		if (this.realm == null) {
			this.realm = context.getHttpSecuritySourceContext().getProperty(PROPERTY_REALM);
		}

		// Specify the access control
		context.setAuthenticationClass(MockAuthentication.class);
		context.setHttpAuthenticationFactory(
				(authentication) -> new MockHttpAuthentication<>(authentication, MockCredentials.class));
		context.setAccessControlClass(MockAccessControl.class);
		context.setHttpAccessControlFactory((accessControl) -> new MockHttpAccessControl(accessControl));
		context.setCredentialsClass(MockCredentials.class);
		context.addFlow(Flows.CHALLENGE, null);
	}

	@Override
	public HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {

		// Return the mock HTTP security
		return this;
	}

	/*
	 * =================== HttpSecurity ===========================
	 */

	@Override
	public MockAuthentication createAuthentication(AuthenticationContext<MockAccessControl, MockCredentials> context) {
		MockAuthentication authentication = new MockAuthentication(context);
		context.authenticate(null, null); // attempt authentication
		return authentication;
	}

	@Override
	public boolean ratify(MockCredentials credentials, RatifyContext<MockAccessControl> context) {

		// Attempt to obtain from session
		MockAccessControl accessControl = (MockAccessControl) context.getSession()
				.getAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
		if (accessControl != null) {
			// Load the access control and no need to authenticate
			context.accessControlChange(accessControl, null);
			return false;
		}

		// Determine if credentials
		if (credentials != null) {
			return true;
		}

		// As here, then not able to authenticate
		return false;
	}

	@Override
	public void authenticate(MockCredentials credentials, AuthenticateContext<MockAccessControl, None, Flows> context)
			throws HttpException {

		// Ensure have credentials (and they are valid)
		if ((credentials == null) || (credentials.getUserName() == null)
				|| (!(credentials.getUserName().equals(credentials.getPassword())))) {
			return;
		}

		// Create the access control
		MockAccessControl accessControl = new MockAccessControl(credentials);

		// Remember access control for further requests
		context.getSession().setAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY, accessControl);

		// Return the access control
		context.accessControlChange(accessControl, null);
	}

	@Override
	public void challenge(ChallengeContext<None, Flows> context) throws HttpException {

		// Trigger flow for challenge
		context.doFlow(Flows.CHALLENGE, null, null);
	}

	@Override
	public void logout(LogoutContext<None, Flows> context) throws HttpException {

		// Forget access control for further requests (requires login again)
		context.getSession().removeAttribute(SESSION_ATTRIBUTE_HTTP_SECURITY);
	}

}
