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
