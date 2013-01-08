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
package net.officefloor.plugin.web.http.security.scheme;

import java.security.Principal;
import java.util.Set;

import net.officefloor.plugin.web.http.security.HttpSecurity;

/**
 * {@link HttpSecurity} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityImpl implements HttpSecurity {

	/**
	 * Authentication scheme.
	 */
	private final String authenticationScheme;

	/**
	 * User identifier.
	 */
	private final String userId;

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
	 * @param authenticationScheme
	 *            Authentication scheme.
	 * @param userId
	 *            User Id.
	 * @param principal
	 *            {@link Principal}.
	 * @param roles
	 *            Roles for the user.
	 */
	public HttpSecurityImpl(String authenticationScheme, String userId,
			Principal principal, Set<String> roles) {
		this.authenticationScheme = authenticationScheme;
		this.userId = userId;
		this.principal = principal;
		this.roles = roles;
	}

	/**
	 * Initiate with simple {@link Principal}.
	 * 
	 * @param authenticationScheme
	 *            Authentication scheme.
	 * @param userId
	 *            User Id.
	 * @param roles
	 *            Roles for the user.
	 */
	public HttpSecurityImpl(String authenticationScheme, final String userId,
			Set<String> roles) {
		this.authenticationScheme = authenticationScheme;
		this.userId = userId;
		this.roles = roles;

		// Create simple principal
		this.principal = new Principal() {
			@Override
			public String getName() {
				return userId;
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
	public String getRemoteUser() {
		return this.userId;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.principal;
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.roles.contains(role);
	}

}