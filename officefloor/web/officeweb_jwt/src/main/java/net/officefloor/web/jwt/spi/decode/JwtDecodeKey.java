package net.officefloor.web.jwt.spi.decode;

import java.security.Key;

/**
 * JWT decode key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtDecodeKey {

	/**
	 * Obtains the milliseconds since Epoch for when this {@link JwtDecodeKey}
	 * becomes active.
	 * 
	 * @return Milliseconds since Epoch for when this {@link JwtDecodeKey} becomes
	 *         active.
	 */
	long startTime();

	/**
	 * Obtains the milliseconds since Epoch for expiry of this {@link JwtDecodeKey}.
	 * 
	 * @return Milliseconds since Epoch for expiry of this {@link JwtDecodeKey}.
	 */
	long expireTime();

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to validate the JWT.
	 */
	Key getKey();

}