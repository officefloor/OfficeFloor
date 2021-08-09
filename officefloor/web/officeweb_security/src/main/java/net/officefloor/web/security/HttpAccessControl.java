/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
