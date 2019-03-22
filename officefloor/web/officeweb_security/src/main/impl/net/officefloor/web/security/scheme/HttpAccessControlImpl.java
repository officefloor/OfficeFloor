/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.scheme;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

import net.officefloor.web.security.HttpAccessControl;

/**
 * {@link HttpAccessControl} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAccessControlImpl implements HttpAccessControl, Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Authentication scheme.
	 */
	private final String authenticationScheme;

	/**
	 * {@link Principal}.
	 */
	private final Principal principal;

	/**
	 * Roles for the user.
	 */
	private final Set<String> roles;

	/**
	 * Initiate.
	 * 
	 * @param authenticationScheme Authentication scheme.
	 * @param principal            {@link Principal}.
	 * @param roles                Roles for the user.
	 */
	public HttpAccessControlImpl(String authenticationScheme, Principal principal, Set<String> roles) {
		this.authenticationScheme = authenticationScheme;
		this.principal = principal;
		this.roles = roles;
	}

	/**
	 * Initiate with simple {@link Principal}.
	 * 
	 * @param authenticationScheme Authentication scheme.
	 * @param principalName        {@link Principal} name.
	 * @param roles                Roles for the user.
	 */
	public HttpAccessControlImpl(String authenticationScheme, final String principalName, Set<String> roles) {
		this.authenticationScheme = authenticationScheme;
		this.roles = roles;

		// Create simple principal
		this.principal = new Principal() {
			@Override
			public String getName() {
				return principalName;
			}
		};
	}

	/*
	 * ======================== HttpSecurity =========================
	 */

	@Override
	public String getAuthenticationScheme() {
		return this.authenticationScheme;
	}

	@Override
	public Principal getPrincipal() {
		return this.principal;
	}

	@Override
	public boolean inRole(String role) {
		return this.roles.contains(role);
	}

}