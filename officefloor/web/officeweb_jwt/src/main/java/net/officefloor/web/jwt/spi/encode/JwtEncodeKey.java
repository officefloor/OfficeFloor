package net.officefloor.web.jwt.spi.encode;

import java.security.Key;

/**
 * JWT encode key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtEncodeKey {

	/**
	 * Obtains the milliseconds since Epoch for when this {@link JwtEncodeKey}
	 * becomes active.
	 * 
	 * @return Milliseconds since Epoch for when this {@link JwtEncodeKey} becomes
	 *         active.
	 */
	long startTime();

	/**
	 * Obtains the milliseconds since Epoch for expiry of this {@link JwtEncodeKey}.
	 * 
	 * @return Milliseconds since Epoch for expiry of this {@link JwtEncodeKey}.
	 */
	long expireTime();

	/**
	 * Obtains the time in milliseconds the created JWT should be valid.
	 * 
	 * @return Time in millisconds the create JWT should be valid.
	 */
	long jwtActivePeriod();

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to sign the JWT.
	 */
	Key getKey();

}