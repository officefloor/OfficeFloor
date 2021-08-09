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
