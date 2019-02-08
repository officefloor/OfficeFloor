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
	long getStartTime();

	/**
	 * Obtains the seconds since Epoch for expiry of this {@link JwtRefreshKey}.
	 * 
	 * @return Seconds since Epoch for expiry of this {@link JwtRefreshKey}.
	 */
	long getExpireTime();

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to encrypt/decrypt the refresh token.
	 */
	Key getKey();

}