package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;

/**
 * Authority for JWT.
 * 
 * @param <I> Identity type.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthority<I> {

	/**
	 * Creates the refresh token for the identity.
	 * 
	 * @param identity Identity.
	 * @return Refresh token.
	 */
	String createRefreshToken(I identity);

	/**
	 * Decodes the refresh token for the identity.
	 * 
	 * @param refreshToken Refresh token.
	 * @return Identity within the refresh token.
	 */
	I decodeRefreshToken(String refreshToken);

	/**
	 * Create the access token for the claims.
	 * 
	 * @param        <C> Claims type.
	 * @param claims Claims.
	 * @return Access token.
	 * @throws AccessTokenException If fails to create the access token.
	 */
	<C> String createAccessToken(C claims) throws AccessTokenException;

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