/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.security.parser;

import net.officefloor.plugin.socket.server.http.HttpHeader;

/**
 * Handler that receives the <code>Authorization</code> {@link HttpHeader}
 * tokens.
 * 
 * @author Daniel Sagenschneider
 */
public interface AuthorisationTokenHandler {

	/**
	 * Name of parameter specifying the <code>Basic</code> authentication
	 * scheme's user id.
	 */
	static final String BASIC_USER_ID = "userid";

	/**
	 * Name of parameter specifying the <code>Basic</code> authentication
	 * scheme's password.
	 */
	static final String BASIC_PASSWORD = "password";

	/**
	 * <p>
	 * Handles the authentication scheme.
	 * <p>
	 * Typically for HTTP this is either:
	 * <ol>
	 * <li>Basic</li>
	 * <li>Digest</li>
	 * </ol>
	 * 
	 * @param authenticationScheme
	 *            Authentication scheme.
	 * @throws AuthorisationTokeniseException
	 *             If fails to handle authentication scheme.
	 */
	void handleAuthenticationScheme(String authenticationScheme)
			throws AuthorisationTokeniseException;

	/**
	 * <p>
	 * Handles a parameter for authentication.
	 * <p>
	 * For <code>Basic</code> authentication the user id and password are
	 * providing via this method with parameter names as per the constants
	 * defined on this interface.
	 * 
	 * @param name
	 *            Name of parameter.
	 * @param value
	 *            Value of parameter.
	 * @throws AuthorisationTokeniseException
	 *             If fails to handle the parameter.
	 */
	void handleParameter(String name, String value)
			throws AuthorisationTokeniseException;

}