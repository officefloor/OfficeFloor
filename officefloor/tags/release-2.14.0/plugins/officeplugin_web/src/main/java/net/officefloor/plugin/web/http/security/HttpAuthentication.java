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
package net.officefloor.plugin.web.http.security;

import java.io.IOException;

/**
 * Dependency interface allowing the application to check if the HTTP client is
 * authenticated.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthentication<S, C> {

	/**
	 * Undertakes authentication.
	 * 
	 * @param authenticationRequest
	 *            {@link HttpAuthenticateRequest}.
	 */
	void authenticate(HttpAuthenticateRequest<C> authenticationRequest);

	/**
	 * Obtains the HTTP security.
	 * 
	 * @return HTTP security or <code>null</code> if not authenticated.
	 * @throws IOException
	 *             If authentication has been attempted but there were failures
	 *             communicating to necessary security services.
	 */
	S getHttpSecurity() throws IOException;

	/**
	 * Undertakes logging out.
	 * 
	 * @param logoutRequest
	 *            {@link HttpLogoutRequest}.
	 */
	void logout(HttpLogoutRequest logoutRequest);

}