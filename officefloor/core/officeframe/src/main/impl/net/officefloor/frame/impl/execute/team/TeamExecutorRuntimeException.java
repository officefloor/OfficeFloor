package net.officefloor.frame.impl.execute.team;

/**
 * {@link TeamExecutor} {@link RuntimeException}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamExecutorRuntimeException extends RuntimeException {

	/**
	 * Serialise identifier.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param cause Cause.
	 */
	public TeamExecutorRuntimeException(Throwable cause) {
		super(cause);
	}
}