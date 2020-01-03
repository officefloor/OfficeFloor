package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * <p>
 * {@link Escalation} from managing a {@link ManagedFunction}.
 * <p>
 * This enables generic handling of {@link ManagedFunction} {@link Escalation}
 * failures.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ManagedFunctionEscalation extends Escalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String managedFunctionName;

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 */
	public ManagedFunctionEscalation(String managedFunctionName) {
		this.managedFunctionName = managedFunctionName;
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 * @param cause               Cause of the {@link Escalation}.
	 */
	public ManagedFunctionEscalation(String managedFunctionName, Throwable cause) {
		super(cause);
		this.managedFunctionName = managedFunctionName;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	public String getManagedFunctionName() {
		return this.managedFunctionName;
	}

}