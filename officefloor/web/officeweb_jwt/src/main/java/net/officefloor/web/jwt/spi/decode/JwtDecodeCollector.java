package net.officefloor.web.jwt.spi.decode;

import java.util.concurrent.TimeUnit;

import net.officefloor.web.jwt.spi.encode.JwtEncodeCollector;

/**
 * <p>
 * Collects {@link JwtDecodeKey} instances for JWT validation.
 * <p>
 * See {@link JwtEncodeCollector} for details regarding security.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see JwtEncodeCollector
 */
public interface JwtDecodeCollector {

	/**
	 * Obtains the current {@link JwtDecodeKey} instances.
	 * 
	 * @return Current {@link JwtDecodeKey} instances.
	 */
	JwtDecodeKey[] getCurrentKeys();

	/**
	 * Specifies the {@link JwtDecodeKey} instances.
	 * 
	 * @param keys {@link JwtDecodeKey} instances.
	 */
	void setKeys(JwtDecodeKey... keys);

	/**
	 * Indicates failure in retrieving the {@link JwtDecodeKey} instances.
	 * 
	 * @param cause           Cause of the failure.
	 * @param timeToNextCheck Allows overriding the default poll refresh interval.
	 *                        This typically allows retrying earlier than the
	 *                        default refresh period.
	 * @param unit            {@link TimeUnit} for the next time to check.
	 */
	void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit);

}