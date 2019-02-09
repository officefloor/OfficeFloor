package net.officefloor.web.jwt.repository;

import java.security.Key;

/**
 * JWT refresh key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtRefreshKey extends JwtAuthorityKey {

	/**
	 * Obtains the init vector.
	 * 
	 * @return Init vector.
	 */
	String getInitVector();

	/**
	 * Obtains the start salt.
	 * 
	 * @return Start salt.
	 */
	String getStartSalt();

	/**
	 * Obtains the lace.
	 * 
	 * @return Lace.
	 */
	String getLace();

	/**
	 * Obtains the end salt.
	 * 
	 * @return End salt.
	 */
	String getEndSalt();

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to encrypt/decrypt the refresh token.
	 */
	Key getKey();

}