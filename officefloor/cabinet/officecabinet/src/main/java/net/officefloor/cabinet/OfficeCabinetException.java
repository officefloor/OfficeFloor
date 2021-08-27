package net.officefloor.cabinet;

/**
 * Generic {@link OfficeCabinet} {@link Exception}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeCabinetException extends RuntimeException {

	/**
	 * Serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 * @param cause   Cause.
	 */
	public OfficeCabinetException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public OfficeCabinetException(String message) {
		super(message);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public OfficeCabinetException(Throwable cause) {
		super(cause);
	}

}