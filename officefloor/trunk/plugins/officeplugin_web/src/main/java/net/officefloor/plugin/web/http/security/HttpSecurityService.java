/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * <p>
 * {@link HttpSecurity} service.
 * <p>
 * Provides facade functions for obtaining the {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityService {

	/**
	 * Creates the {@link HttpSecurity} for the {@link ServerHttpConnection}
	 * being processed.
	 * 
	 * @return {@link HttpSecurity} or <code>null</code> if not authenticated.
	 * @throws AuthenticationException
	 *             If failure in authentication.
	 */
	HttpSecurity authenticate() throws AuthenticationException;

	/**
	 * Loads the unauthorised information to the {@link HttpResponse}.
	 * 
	 * @throws AuthenticationException
	 *             If failure in loading the information.
	 */
	void loadUnauthorised() throws AuthenticationException;

}