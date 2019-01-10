package net.officefloor.web.jwt.spi;

/**
 * Collects keys for JWT validation and potential generation.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtKeyCollector {

	/**
	 * Obtains the current {@link JwtKey} instances.
	 * 
	 * @return Current {@link JwtKey} instances.
	 */
	JwtKey[] getCurrentKeys();

	/**
	 * Specifies the {@link JwtKey} instances.
	 * 
	 * @param nextCheckTime Next time to check for {@link JwtKey} instances.
	 * @param keys          {@link JwtKey} instances.
	 */
	void setKeys(long nextCheckTime, JwtKey[] keys);

	/**
	 * Indicates failure in retrieve the {@link JwtKey} instances.
	 * 
	 * @param nextCheckTime Next time to check for {@link JwtKey} instances.
	 * @param cause         Cause of the failure.
	 */
	void setFailure(long nextCheckTime, Throwable cause);

}