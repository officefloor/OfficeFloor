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

import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * <p>
 * Source for obtaining HTTP security.
 * <p>
 * As security is specific to applications, both the security object and
 * credentials are specified by the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySource<S, C, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Retrieves the cached security details.
	 * 
	 * @param session
	 *            {@link HttpSession}.
	 * @return Security object. Value of <code>null</code> indicates the
	 *         security object was not cached.
	 */
	S retrieveCached(HttpSession session);

	/**
	 * Undertakes authentication.
	 * 
	 * @param context
	 *            {@link HttpAuthenticateContext}.
	 */
	void authenticate(HttpAuthenticateContext<S, C, D, F> context);

}