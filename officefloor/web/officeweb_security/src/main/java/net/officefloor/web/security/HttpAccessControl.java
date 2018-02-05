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

	/**
	 * Indicates if have access.
	 * 
	 * @param anyRoles
	 *            Listing of roles that must have access to at least one. May be
	 *            <code>null</code>.
	 * @param allRoles
	 *            Listing of roles that must have access to all. May be
	 *            <code>null</code>.
	 * @return <code>true</code> if have access.
	 */
	default boolean isAccess(String[] anyRoles, String[] allRoles) {

		// Ensure in all roles
		if ((allRoles != null) && (allRoles.length > 0)) {
			for (int i = 0; i < allRoles.length; i++) {
				String role = allRoles[i];
				boolean isInRole = this.inRole(role);
				if (!isInRole) {
					return false; // must be in all roles
				}
			}
		}

		// Ensure in any roles
		if ((anyRoles != null) && (anyRoles.length > 0)) {
			for (int i = 0; i < anyRoles.length; i++) {
				String role = anyRoles[i];
				boolean isInRole = this.inRole(role);
				if (isInRole) {
					return true; // allow access
				}
			}

			// As here, not in any role
			return false;
		}

		// As here, have access
		return true;
	}

}