package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.spi.repository.JwtRefreshKey;

/**
 * <p>
 * Collects {@link JwtRefreshKey} instances for generating refresh tokens.
 * <p>
 * See {@link JwtEncodeCollector} for details regarding security.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see JwtEncodeCollector
 */
public interface JwtRefreshCollector {

	/**
	 * Specifies the {@link JwtRefreshKey} instances.
	 * 
	 * @param keys {@link JwtRefreshKey} instances.
	 */
	void setKeys(JwtRefreshKey... keys);

}