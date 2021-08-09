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

import java.io.IOException;
import java.util.function.BiConsumer;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.scheme.MockFlowHttpSecuritySource.Flows;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link MockFlowHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFlowHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(MockFlowHttpSecuritySource.class, "realm", "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(MockAuthentication.class);
		type.setAccessControlClass(MockAccessControl.class);
		type.setCredentialsClass(MockCredentials.class);
		type.setInput(true);
		type.addFlow(Flows.CHALLENGE, null);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, MockFlowHttpSecuritySource.class, "realm", "REALM");
	}

	/**
	 * Ensure can ratify from cached {@link MockAccessControl}.
	 */
	public void testRatifyFromSession() throws IOException {

		final MockHttpRatifyContext<MockAccessControl> ratifyContext = new MockHttpRatifyContext<>();
		MockAccessControl accessControl = new MockAccessControl("mock", "user", null);

		// Load access to session
		ratifyContext.getSession().setAttribute("http.security.mock.form", accessControl);

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockFlowHttpSecuritySource.class, "realm", "REALM");

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached", security.ratify(null, ratifyContext));
		assertSame("Incorrect access control", accessControl, ratifyContext.getAccessControl());
	}

	/**
	 * Ensure can ratify if have {@link HttpCredentials}.
	 */
	public void testRatifyWithCredentials() throws IOException {

		final MockCredentials credentials = new MockCredentials("user", "password");
		final MockHttpRatifyContext<MockAccessControl> ratifyContext = new MockHttpRatifyContext<>();

		// Create and initialise the source
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockFlowHttpSecuritySource.class, "realm", "REALM");

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate", security.ratify(credentials, ratifyContext));
		assertNull("Should not yet have access control", ratifyContext.getAccessControl());
	}

	/**
	 * Ensure ratify indicates no authentication credentials.
	 */
	public void testRatifyNoCredentials() throws IOException {

		final MockHttpRatifyContext<MockAccessControl> ratifyContext = new MockHttpRatifyContext<>();

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockFlowHttpSecuritySource.class, "realm", "REALM");

		// Undertake ratify
		assertFalse("Should not attempt authentication", security.ratify(null, ratifyContext));
		assertNull("Should not yet have access control", ratifyContext.getAccessControl());
	}

	/**
	 * Ensure can load challenge.
	 */
	@SuppressWarnings("unchecked")
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<None, Flows> challengeContext = new MockHttpChallengeContext<>();

		// Record the triggering flow for challenge
		BiConsumer<Object, FlowCallback> flow = this.createMock(BiConsumer.class);
		flow.accept(null, null);
		challengeContext.registerFlow(Flows.CHALLENGE, flow);

		// Test
		this.replayMockObjects();

		// Create and initialise the access control
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockFlowHttpSecuritySource.class, "realm", "REALM");

		// Undertake the challenge
		security.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure not authenticated if no credentials provided.
	 */
	public void testNullCredentials() throws Exception {
		this.doAuthenticate(null, null);
	}

	/**
	 * Ensure not authenticated if no credentials.
	 */
	public void testNoUserName() throws Exception {
		this.doAuthenticate(new MockCredentials(null, null), null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testAuthenticate() throws Exception {
		this.doAuthenticate(new MockCredentials("test", "test"), "test", "test");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<None, Flows> logoutContext = new MockHttpLogoutContext<>();

		// Provide state in session (that should be cleared)
		logoutContext.getSession().setAttribute("http.security.mock.form", new MockAccessControl("mock", "user", null));

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockFlowHttpSecuritySource.class, "realm", "REALM");

		// Logout
		security.logout(logoutContext);

		// Ensure session cleared
		assertNull("Should clear session of access control",
				logoutContext.getSession().getAttribute("http.security.mock.form"));
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param credentials {@link MockCredentials}.
	 * @param userName    User name if authenticated. <code>null</code> if not
	 *                    authenticated.
	 * @param roles       Expected roles.
	 */
	private void doAuthenticate(MockCredentials credentials, String userName, String... roles) throws IOException {

		// Create the authentication context
		MockHttpAuthenticateContext<MockAccessControl, None, Flows> authenticationContext = new MockHttpAuthenticateContext<>();

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, MockCredentials, None, Flows> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockFlowHttpSecuritySource.class, "realm", "REALM");

		// Undertake the authenticate
		security.authenticate(credentials, authenticationContext);

		// Validate authentication
		MockAccessControl accessControl = authenticationContext.getAccessControl();
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);
			assertNull("Session should not have access control",
					authenticationContext.getSession().getAttribute("http.security.mock.form"));

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", MockFlowHttpSecuritySource.AUTHENTICATION_SCHEME,
					accessControl.getAuthenticationScheme());
			assertEquals("Incorrect user", userName, accessControl.getUserName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.getRoles().contains(role));
			}
			assertEquals("Should load session with access control", userName,
					((MockAccessControl) authenticationContext.getSession().getAttribute("http.security.mock.form"))
							.getUserName());
		}
	}

}
