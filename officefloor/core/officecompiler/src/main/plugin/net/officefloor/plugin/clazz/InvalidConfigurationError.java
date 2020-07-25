package net.officefloor.plugin.clazz;

/**
 * Indicates an incorrect configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class InvalidConfigurationError extends Error {

	/**
	 * Serialisation.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public InvalidConfigurationError(String message) {
		super(message);
	}

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 * @param cause   Cause.
	 */
	public InvalidConfigurationError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public InvalidConfigurationError(Throwable cause) {
		super(cause);
	}
}