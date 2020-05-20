package net.officefloor.jaxrs.chain;

/**
 * Just in time dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class JustInTimeDependency {

	private final String message;

	public JustInTimeDependency(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}