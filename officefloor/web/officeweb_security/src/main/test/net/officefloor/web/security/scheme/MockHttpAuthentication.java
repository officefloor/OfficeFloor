/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
	 * Instantiate.
	 * 
	 * @param authentication
	 *            {@link MockAuthentication}.
	 */
	public MockHttpAuthentication(MockAuthentication authentication) {
		this.authentication = authentication;
	}

	/*
	 * ================== HttpAuthentication =========================
	 */

	@Override
	public boolean isAuthenticated() throws HttpException {
		return this.authentication.isAuthenticated();
	}

	@Override
	public void authenticate(C credentials, AuthenticateRequest authenticationRequest) {
		this.authentication.authenticate((failure) -> authenticationRequest.authenticateComplete(failure));
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
