package net.officefloor.web.jwt.repository;

import java.security.Key;

import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * JWT key to encode access token.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAccessKey extends JwtAuthorityKey {

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
	 * instances for {@link JwtAccessKey} to {@link JwtValidateKey} relationships.
	 * 
	 * @return Public {@link Key} to validate the JWT.
	 */
	Key getPublicKey();

}