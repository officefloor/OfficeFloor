package net.officefloor.web.jwt.spi;

import java.security.Key;
import java.security.PrivateKey;

/**
 * JWT key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtKey {

	/**
	 * Obtains the milliseconds since Epoch for expiry of this {@link JwtKey}.
	 * 
	 * @return Milliseconds since Epoch for expiry of this {@link JwtKey}.
	 */
	long expireTime();

	/**
	 * <p>
	 * Obtains the {@link Key}.
	 * <p>
	 * This is the {@link Key} used to validate the JWT.
	 * 
	 * @return {@link Key} to validate the JWT.
	 */
	Key getKey();

	/**
	 * <p>
	 * Obtains the {@link PrivateKey}.
	 * <p>
	 * Should the server only be validating JWT tokens, then should not return the
	 * {@link PrivateKey}.
	 * 
	 * @return {@link PrivateKey} or <code>null</code> if only authenticate.
	 */
	PrivateKey getPrivateKey();

}