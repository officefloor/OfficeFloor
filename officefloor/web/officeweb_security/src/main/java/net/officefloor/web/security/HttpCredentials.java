package net.officefloor.web.security;

import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Portable interface for {@link HttpSecuritySource} credentials.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpCredentials {

	/**
	 * Obtains the username.
	 * 
	 * @return Username.
	 */
	String getUsername();

	/**
	 * Obtains the password.
	 * 
	 * @return Password.
	 */
	byte[] getPassword();

}