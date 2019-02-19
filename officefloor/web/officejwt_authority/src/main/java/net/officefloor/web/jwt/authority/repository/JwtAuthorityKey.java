package net.officefloor.web.jwt.authority.repository;

/**
 * Common attributes for authority key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthorityKey {

	/**
	 * Obtains the seconds since Epoch for when this {@link JwtAuthorityKey} becomes
	 * active.
	 * 
	 * @return Seconds since Epoch for when this {@link JwtAuthorityKey} becomes
	 *         active.
	 */
	long getStartTime();

	/**
	 * Obtains the seconds since Epoch for expiry of this {@link JwtAuthorityKey}.
	 * 
	 * @return Seconds since Epoch for expiry of this {@link JwtAuthorityKey}.
	 */
	long getExpireTime();

}