/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
	private final AuthenticationContext<HttpAccessControl, C> authenticationContext;

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
	 * @param authenticationContext
	 *            {@link AuthenticationContext}.
	 * @param credentialsType
	 *            Type of credentials.
	 */
	public HttpAuthenticationImpl(AuthenticationContext<HttpAccessControl, C> authenticationContext,
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