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