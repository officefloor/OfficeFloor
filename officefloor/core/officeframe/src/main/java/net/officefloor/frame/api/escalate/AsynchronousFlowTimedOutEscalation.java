package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link Escalation} indicating the {@link AsynchronousFlow} timed out.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowTimedOutEscalation extends ManagedFunctionEscalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 */
	public AsynchronousFlowTimedOutEscalation(String managedFunctionName) {
		super(managedFunctionName);
	}

}