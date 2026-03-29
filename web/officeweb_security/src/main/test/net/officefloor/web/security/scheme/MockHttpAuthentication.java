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

import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.LogoutRequest;

/**
 * Mock {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAuthentication<C> implements HttpAuthentication<C> {

	/**
	 * {@link MockAuthentication}.
	 */
	private final MockAuthentication authentication;

	/**
	 * Type of credentials.
	 */
	private final Class<C> credentialsType;

	/**
	 * Instantiate.
	 * 
	 * @param authentication
	 *            {@link MockAuthentication}.
	 * @param credentialsType
	 *            Credentials type.
	 */
	public MockHttpAuthentication(MockAuthentication authentication, Class<C> credentialsType) {
		this.authentication = authentication;
		this.credentialsType = credentialsType;
	}

	/*
	 * ================== HttpAuthentication =========================
	 */

	@Override
	public boolean isAuthenticated() throws HttpException {
		return this.authentication.isAuthenticated();
	}

	@Override
	public Class<C> getCredentialsType() {
		return this.credentialsType;
	}

	@Override
	public void authenticate(C credentials, AuthenticateRequest authenticationRequest) {
		this.authentication.authenticate((MockCredentials) credentials,
				(failure) -> authenticationRequest.authenticateComplete(failure));
	}

	@Override
	public HttpAccessControl getAccessControl() throws AuthenticationRequiredException, HttpException {
		MockAccessControl accessControl = this.authentication.getAccessControl();
		if (accessControl == null) {
			throw new AuthenticationRequiredException();
		}
		return new MockHttpAccessControl(accessControl);
	}

	@Override
	public void logout(LogoutRequest logoutRequest) {
		this.authentication.logout((failure) -> logoutRequest.logoutComplete(failure));
	}

}
