package net.officefloor.web.jwt.spi.refresh;

import java.util.concurrent.TimeUnit;

import net.officefloor.web.jwt.spi.encode.JwtEncodeCollector;

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
	 * Obtains the current {@link JwtRefreshKey} instances.
	 * 
	 * @return Current {@link JwtRefreshKey} instances.
	 */
	JwtRefreshKey[] getCurrentKeys();

	/**
	 * Specifies the {@link JwtRefreshKey} instances.
	 * 
	 * @param keys {@link JwtRefreshKey} instances.
	 */
	void setKeys(JwtRefreshKey... keys);

	/**
	 * Indicates failure in retrieving the {@link JwtRefreshKey} instances.
	 * 
	 * @param cause           Cause of the failure.
	 * @param timeToNextCheck Allows overriding the default poll refresh interval.
	 *                        This typically allows retrying earlier than the
	 *                        default refresh period.
	 * @param unit            {@link TimeUnit} for the next time to check.
	 */
	void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit);

}