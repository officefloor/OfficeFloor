package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Handler for the completion of the {@link Flow}.
 *
 * @author Daniel Sagenschneider
 */
public interface FlowCompletion extends LinkedListSetEntry<FlowCompletion, ManagedFunctionContainer> {

	/**
	 * Obtains the {@link FunctionState} to notify completion of the {@link Flow}.
	 * 
	 * @param escalation Possible {@link Escalation} from the {@link Flow}. Will be
	 *                   <code>null</code> if {@link Flow} completed without
	 *                   {@link Escalation}.
	 * @return {@link FunctionState} to notify completion of the {@link Flow}.
	 */
	FunctionState flowComplete(Throwable escalation);

}