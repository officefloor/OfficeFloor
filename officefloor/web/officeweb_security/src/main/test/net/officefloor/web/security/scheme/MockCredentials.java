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