package net.officefloor.web.jwt.spi.encode;

import java.security.Key;

import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;
import net.officefloor.web.jwt.spi.repository.JwtAuthorityRepository;

/**
 * JWT encode key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtEncodeKey {

	/**
	 * Obtains the seconds since Epoch for when this {@link JwtEncodeKey} becomes
	 * active.
	 * 
	 * @return Seconds since Epoch for when this {@link JwtEncodeKey} becomes
	 *         active.
	 */
	long startTime();

	/**
	 * Obtains the seconds since Epoch for expiry of this {@link JwtEncodeKey}.
	 * 
	 * @return Seconds since Epoch for expiry of this {@link JwtEncodeKey}.
	 */
	long expireTime();

	/**
	 * Obtains the private {@link Key}.
	 * 
	 * @return Private {@link Key} to sign the JWT.
	 */
	Key getPrivateKey();

	/**
	 * <p>
	 * Obtains the public {@link Key}.
	 * <p>
	 * While not used for encoding, is kept together to enable
	 * {@link JwtAuthorityRepository} to associate public/private {@link Key}
	 * instances for {@link JwtEncodeKey} to {@link JwtDecodeKey} relationships.
	 * 
	 * @return Public {@link Key} to validate the JWT.
	 */
	Key getPublicKey();

}