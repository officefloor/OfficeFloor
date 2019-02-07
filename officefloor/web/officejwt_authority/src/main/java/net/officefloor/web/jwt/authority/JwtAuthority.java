package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;
import net.officefloor.web.jwt.spi.encode.JwtEncodeKey;
import net.officefloor.web.jwt.spi.refresh.JwtRefreshKey;
import net.officefloor.web.jwt.spi.repository.JwtAuthorityRepository;

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
	 * <p>
	 * Allows manually triggering a reload of the {@link JwtRefreshKey} instances.
	 * <p>
	 * This is useful for manual intervention in the active {@link JwtRefreshKey}
	 * instances. For example, a compromised {@link JwtRefreshKey} can be removed
	 * from the {@link JwtAuthorityRepository} with this method invoked to reload
	 * the {@link JwtRefreshKey} instances (minus the deleted compromised
	 * {@link JwtRefreshKey} instance).
	 */
	void reloadRefreshKeys();

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
	 * Allows manually triggering a reload of the {@link JwtEncodeKey} instances.
	 * <p>
	 * Similar to {@link #reloadRefreshKeys()}, except for {@link JwtEncodeKey}
	 * instances.
	 */
	void reloadAccessKeys();

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