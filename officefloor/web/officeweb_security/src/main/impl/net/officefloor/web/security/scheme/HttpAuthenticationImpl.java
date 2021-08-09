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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.LogoutRequest;
import net.officefloor.web.spi.security.AccessControlListener;
import net.officefloor.web.spi.security.AuthenticationContext;

/**
 * {@link HttpAuthentication} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationImpl<C> implements HttpAuthentication<C>, AccessControlListener<HttpAccessControl> {

	/**
	 * {@link AuthenticationContext}.
	 */
	private final AuthenticationContext<? extends HttpAccessControl, C> authenticationContext;

	/**
	 * Type of credentials.
	 */
	private final Class<C> credentialsType;

	/**
	 * {@link HttpAccessControl}.
	 */
	private HttpAccessControl accessControl = null;

	/**
	 * {@link Escalation}.
	 */
	private Throwable escalation = null;

	/**
	 * Instantiate.
	 * 
	 * @param authenticationContext {@link AuthenticationContext}.
	 * @param credentialsType       Type of credentials.
	 */
	public HttpAuthenticationImpl(AuthenticationContext<? extends HttpAccessControl, C> authenticationContext,
			Class<C> credentialsType) {
		this.authenticationContext = authenticationContext;
		this.credentialsType = credentialsType;

		// Register to load access control
		this.authenticationContext.register(this);
	}

	/*
	 * =================== HttpAccessControl ======================
	 */

	@Override
	public void accessControlChange(HttpAccessControl accessControl, Throwable escalation) {
		this.accessControl = accessControl;
		this.escalation = escalation;
	}

	/*
	 * ==================== HttpAuthentication ====================
	 */

	@Override
	public boolean isAuthenticated() throws HttpException {
		return this.authenticationContext.run(() -> this.accessControl != null);
	}

	@Override
	public Class<C> getCredentialsType() {
		return this.credentialsType;
	}

	@Override
	public void authenticate(C credentials, AuthenticateRequest authenticateRequest) {
		this.authenticationContext.authenticate(credentials, authenticateRequest);
	}

	@Override
	public HttpAccessControl getAccessControl() throws AuthenticationRequiredException, HttpException {
		return this.authenticationContext.run(() -> {

			// Propagate potential failure
			if (this.escalation != null) {
				if (this.escalation instanceof HttpException) {
					throw (HttpException) this.escalation;
				} else {
					throw new HttpException(this.escalation);
				}
			}

			// Ensure have access control
			if (this.accessControl == null) {
				throw new AuthenticationRequiredException(this.authenticationContext.getQualifier());
			}

			// Return the access control
			return this.accessControl;
		});
	}

	@Override
	public void logout(LogoutRequest logoutRequest) {
		this.authenticationContext.logout(logoutRequest);
	}

}
