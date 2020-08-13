package net.officefloor.frame.api.manage;

/**
 * Indicates timed out waiting on the object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectTimeoutException extends Exception {

	/**
	 * Default serialisation.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bound object name.
	 */
	private final String boundObjectName;

	/**
	 * Instantiate.
	 * 
	 * @param boundObjectName     Bound object name.
	 * @param timeoutMilliseconds Time out in milliseconds.
	 */
	public ObjectTimeoutException(String boundObjectName, long timeoutMilliseconds) {
		super("Timed out waiting on object " + boundObjectName + " after " + timeoutMilliseconds + " milliseconds");
		this.boundObjectName = boundObjectName;
	}

	/**
	 * Obtains the bound object name.
	 * 
	 * @return Bound object name.
	 */
	public String getBoundObjectName() {
		return this.boundObjectName;
	}
}