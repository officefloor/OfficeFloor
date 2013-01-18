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

/**
 * <p>
 * Dependency interface allowing the application to check if the HTTP client is
 * authenticated.
 * <p>
 * Unlike {@link HttpSecurity} (or its application specific equivalent), this
 * does not trigger authentication and allows the application to check whether
 * authenticated.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthentication<S> {

	/**
	 * <p>
	 * Obtains the HTTP security.
	 * <p>
	 * Should the client not be authenticated, this will return
	 * <code>null</code>.
	 * 
	 * @return HTTP security or <code>null</code> if not authenticated.
	 */
	S getHttpSecurity();

}