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
	 * Obtains the type of credentials.
	 * 
	 * @return Type of credentials.
	 */
	Class<C> getCredentialsType();

	/**
	 * Triggers to undertake authentication.
	 *
	 * @param credentials
	 *            Credentials. May be <code>null</code> if no credentials are
	 *            required, or they are pulled from the {@link HttpRequest}.
	 * @param authenticationRequest
	 *            {@link AuthenticateRequest}.
	 */
	void authenticate(C credentials, AuthenticateRequest authenticationRequest);

	/**
	 * Obtains the {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControl}.
	 * @throws AuthenticationRequiredException
	 *             If not authenticated.
	 * @throws HttpException
	 *             If failure occurred in authentication.
	 */
	HttpAccessControl getAccessControl() throws AuthenticationRequiredException, HttpException;

	/**
	 * Undertakes logging out.
	 * 
	 * @param logoutRequest
	 *            {@link LogoutRequest}.
	 */
	void logout(LogoutRequest logoutRequest);

}