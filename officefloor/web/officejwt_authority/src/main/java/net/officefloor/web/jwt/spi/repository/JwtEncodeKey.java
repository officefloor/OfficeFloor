package net.officefloor.web.jwt.spi.repository;

import java.security.Key;

import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;

/**
 * JWT encode key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtEncodeKey extends JwtAuthorityKey {

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