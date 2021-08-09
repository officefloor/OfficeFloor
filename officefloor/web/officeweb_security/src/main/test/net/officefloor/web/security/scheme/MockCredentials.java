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

import net.officefloor.server.http.mock.MockHttpRequestBuilder;

/**
 * Mock credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class MockCredentials {

	/**
	 * User name.
	 */
	private final String userName;

	/**
	 * Password.
	 */
	private final String password;

	/**
	 * Roles.
	 */
	private final String[] roles;

	/**
	 * Instantiate.
	 * 
	 * @param userName
	 *            User name.
	 * @param password
	 *            Password.
	 * @param roles
	 *            Optional listing of roles.
	 */
	public MockCredentials(String userName, String password, String... roles) {
		this.userName = userName;
		this.password = password;
		this.roles = roles;
	}

	/**
	 * Obtains the user name.
	 * 
	 * @return User name.
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Obtains the password.
	 * 
	 * @return Password.
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Obtain the roles.
	 * 
	 * @return Roles.
	 */
	public String[] getRoles() {
		return this.roles;
	}

	/**
	 * Loads the {@link MockHttpRequestBuilder} with the credentials.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @return Input {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder loadHttpRequest(MockHttpRequestBuilder request) {
		return request.header("authorization", MockChallengeHttpSecuritySource.AUTHENTICATION_SCHEME + " "
				+ this.userName + "," + this.password + "," + String.join(",", this.roles));
	}

}
