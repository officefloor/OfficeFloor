package net.officefloor.web.spi.security;

/**
 * HTTP challenge.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpChallenge {

	/**
	 * Adds a parameter to the {@link HttpChallenge}.
	 * 
	 * @param name
	 *            Parameter name.
	 * @param value
	 *            Parameter value.
	 */
	void addParameter(String name, String value);

}