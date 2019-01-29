package net.officefloor.web.jwt.spi.repository;

import java.time.Instant;
import java.util.List;

import net.officefloor.web.jwt.spi.encode.JwtEncodeKey;
import net.officefloor.web.jwt.spi.refresh.JwtRefreshKey;

/**
 * JWT repository.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthorityRepository {

	List<JwtEncodeKey> retrieveJwtEncodeKeys(Instant activeAfter);

	void saveJwtEncodeKey(JwtEncodeKey encodeKey);

	List<JwtRefreshKey> retrieveJwtRefreshKeys(Instant activeAfter);

	void saveJwtRefreshKey(JwtRefreshKey refreshKey);

}