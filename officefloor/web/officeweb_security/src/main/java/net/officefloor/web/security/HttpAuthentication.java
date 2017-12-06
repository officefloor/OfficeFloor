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
package net.officefloor.web.security;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.spi.security.HttpAuthenticateCallback;
import net.officefloor.web.spi.security.HttpLogoutRequest;

/**
 * Dependency interface allowing the application to check if the HTTP client is
 * authenticated.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthentication<C> {

	/**
	 * Indicates if authenticated.
	 * 
	 * @return <code>true</code> if authenticated.
	 * @throws HttpException
	 *             If authentication has been attempted but there were failures
	 *             in undertaking authentication.
	 */
	boolean isAuthenticated() throws HttpException;

	/**
	 * Triggers to undertake authentication.
	 *
	 * @param credentials
	 *            Credentials. May be <code>null</code> if no credentials are
	 *            required, or they are pulled from the {@link HttpRequest}.
	 * @param authenticationCallback
	 *            {@link HttpAuthenticateCallback}.
	 */
	void authenticate(C credentials, HttpAuthenticateCallback authenticationCallback);

	/**
	 * Obtains the {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControl} or <code>null</code> if not
	 *         authenticated.
	 */
	HttpAccessControl getAccessControl();

	/**
	 * Undertakes logging out.
	 * 
	 * @param logoutRequest
	 *            {@link HttpLogoutRequest}.
	 */
	void logout(HttpLogoutRequest logoutRequest);

}