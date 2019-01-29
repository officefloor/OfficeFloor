package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;

/**
 * Authority for JWT.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthority<C> {

	/**
	 * Create the access token for the claims.
	 * 
	 * @param claims Claims.
	 * @return Access token.
	 */
	String createAccessToken(C claims);

	/**
	 * Creates the refresh token for the claims.
	 * 
	 * @param claims Claims.
	 * @return Refresh token.
	 */
	String createRefreshToken(C claims);

	/**
	 * <p>
	 * Obtains the current active {@link JwtDecodeKey} instances.
	 * <p>
	 * This allows publishing the {@link JwtDecodeKey} instances to
	 * {@link JwtHttpSecuritySource} implementations.
	 * 
	 * @return Current active {@link JwtDecodeKey} instances.
	 */
	JwtDecodeKey[] getActiveJwtDecodeKeys();

}