/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security.scheme;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Mock access control.
 * 
 * @author Daniel Sagenschneider
 */
public class MockAccessControl implements Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * User identifier.
	 */
	private final String userName;

	/**
	 * Roles.
	 */
	private final Set<String> roles;

	/**
	 * Instantiate.
	 * 
	 * @param userName User name.
	 * @param roles    Roles.
	 */
	public MockAccessControl(String userName, String... roles) {
		this.userName = userName;
		this.roles = new HashSet<>();
		for (String role : roles) {
			this.roles.add(role);
		}
	}

	/**
	 * Instantiate from {@link MockCredentials}.
	 * 
	 * @param credentials {@link MockCredentials}.
	 */
	public MockAccessControl(MockCredentials credentials) {
		this(credentials.getUserName(), credentials.getRoles());
		this.roles.add(credentials.getUserName());
	}

	/**
	 * Obtains the authentication scheme.
	 * 
	 * @return Authentication scheme.
	 */
	public String getAuthenticationScheme() {
		return MockChallengeHttpSecuritySource.AUTHENTICATION_SCHEME;
	}

	/**
	 * Obtains the user name.
	 * 
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Obtains the roles.
	 * 
	 * @return Roles.
	 */
	public Set<String> getRoles() {
		return roles;
	}

}
