package net.officefloor.web.security.scheme;

import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link HttpCredentials} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCredentialsImpl implements HttpCredentials {

	/**
	 * Username.
	 */
	private final String username;

	/**
	 * Password.
	 */
	private final byte[] password;

	/**
	 * Initiate.
	 * 
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 */
	public HttpCredentialsImpl(String username, byte[] password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Convenience constructor for providing a {@link String} password.
	 * 
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 */
	public HttpCredentialsImpl(String username, String password) {
		this(username, (password == null ? null : password.getBytes(AbstractHttpSecuritySource.UTF_8)));
	}

	/*
	 * ===================== HttpCredentials =======================
	 */

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public byte[] getPassword() {
		return this.password;
	}

}