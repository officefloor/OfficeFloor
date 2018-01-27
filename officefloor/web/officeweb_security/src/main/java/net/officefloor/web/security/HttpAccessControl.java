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

import java.io.Serializable;
import java.security.Principal;

import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Adapting interface to provide standard access control by the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAccessControl extends Serializable {

	/**
	 * Obtains the authentication scheme used.
	 * 
	 * @return Authentication scheme.
	 */
	String getAuthenticationScheme();

	/**
	 * Obtains the {@link Principal}.
	 * 
	 * @return {@link Principal}.
	 */
	Principal getPrincipal();

	/**
	 * Indicates if within role.
	 * 
	 * @param role
	 *            Role to check if have access.
	 * @return <code>true</code> if supports the role.
	 */
	boolean inRole(String role);

}