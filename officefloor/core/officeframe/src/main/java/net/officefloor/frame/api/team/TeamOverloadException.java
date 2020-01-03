package net.officefloor.frame.api.team;

import java.util.concurrent.RejectedExecutionException;

/**
 * <p>
 * Indicates the {@link Team} is overloaded.
 * <p>
 * By convention {@link Team} instances should throw this to indicate back
 * pressure, as load on the {@link Team} is too high.
 * <p>
 * This is similar to {@link RejectedExecutionException}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamOverloadException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 * @param cause   Cause.
	 */
	public TeamOverloadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public TeamOverloadException(String message) {
		super(message);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public TeamOverloadException(Throwable cause) {
		super(cause);
	}

}