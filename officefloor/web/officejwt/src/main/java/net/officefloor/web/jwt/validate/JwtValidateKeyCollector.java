package net.officefloor.web.jwt.validate;

import java.util.concurrent.TimeUnit;

/**
 * Collects {@link JwtValidateKey} instances for JWT validation.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtValidateKeyCollector {

	/**
	 * Obtains the current {@link JwtValidateKey} instances.
	 * 
	 * @return Current {@link JwtValidateKey} instances.
	 */
	JwtValidateKey[] getCurrentKeys();

	/**
	 * Specifies the {@link JwtValidateKey} instances.
	 * 
	 * @param keys {@link JwtValidateKey} instances.
	 */
	void setKeys(JwtValidateKey... keys);

	/**
	 * Indicates failure in retrieving the {@link JwtValidateKey} instances.
	 * 
	 * @param cause           Cause of the failure.
	 * @param timeToNextCheck Allows overriding the default poll refresh interval.
	 *                        This typically allows retrying earlier than the
	 *                        default refresh period.
	 * @param unit            {@link TimeUnit} for the next time to check.
	 */
	void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit);

}