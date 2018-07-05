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