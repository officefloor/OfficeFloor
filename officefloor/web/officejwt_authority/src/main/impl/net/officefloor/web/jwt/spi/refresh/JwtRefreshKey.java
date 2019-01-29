package net.officefloor.web.jwt.spi.refresh;

import java.security.Key;

/**
 * JWT refresh key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtRefreshKey {

	/**
	 * Obtains the seconds since Epoch for when this {@link JwtRefreshKey} becomes
	 * active.
	 * 
	 * @return Seconds since Epoch for when this {@link JwtRefreshKey} becomes
	 *         active.
	 */
	long startTime();

	/**
	 * Obtains the seconds since Epoch for expiry of this {@link JwtRefreshKey}.
	 * 
	 * @return Seconds since Epoch for expiry of this {@link JwtRefreshKey}.
	 */
	long expireTime();

	/**
	 * Obtains the time in seconds the created refresh token should be valid.
	 * 
	 * @return Time in seconds the created refresh token should be valid.
	 */
	long activePeriod();

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to encrypt/decrypt the refresh token.
	 */
	Key getKey();

}