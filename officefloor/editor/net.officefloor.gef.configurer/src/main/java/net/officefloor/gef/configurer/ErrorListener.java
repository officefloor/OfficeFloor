package net.officefloor.gef.configurer;

/**
 * Listener for errors in configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ErrorListener {

	/**
	 * Informed of error message.
	 * 
	 * @param inputLabel
	 *            Label for the input.
	 * @param message
	 *            Error message.
	 */
	void error(String inputLabel, String message);

	/**
	 * Informed of {@link Throwable}.
	 * 
	 * @param inputLabel
	 *            Label for the input.
	 * @param error
	 *            {@link Throwable}.
	 */
	void error(String inputLabel, Throwable error);

	/**
	 * Informed that valid.
	 */
	void valid();

}