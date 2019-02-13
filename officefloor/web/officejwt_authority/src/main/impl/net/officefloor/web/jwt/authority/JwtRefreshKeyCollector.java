package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.authority.repository.JwtRefreshKey;

/**
 * <p>
 * Collects {@link JwtRefreshKey} instances for generating refresh tokens.
 * <p>
 * See {@link JwtAccessKeyCollector} for details regarding security.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see JwtAccessKeyCollector
 */
public interface JwtRefreshKeyCollector {

	/**
	 * Specifies the {@link JwtRefreshKey} instances.
	 * 
	 * @param keys {@link JwtRefreshKey} instances.
	 */
	void setKeys(JwtRefreshKey... keys);

}