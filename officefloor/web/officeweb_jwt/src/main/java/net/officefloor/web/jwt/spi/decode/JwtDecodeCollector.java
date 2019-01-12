package net.officefloor.web.jwt.spi.decode;

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
	 * @param timeToNextCheck Time in milliseconds to collect {@link JwtDecodeKey}
	 *                        instances again.
	 * @param keys            {@link JwtDecodeKey} instances.
	 */
	void setKeys(long timeToNextCheck, JwtDecodeKey[] keys);

	/**
	 * Indicates failure in retrieving the {@link JwtDecodeKey} instances.
	 * 
	 * @param timeToNextCheck Time in milliseconds to collect {@link JwtDecodeKey}
	 *                        instances again.
	 * @param cause           Cause of the failure.
	 */
	void setFailure(long timeToNextCheck, Throwable cause);

}