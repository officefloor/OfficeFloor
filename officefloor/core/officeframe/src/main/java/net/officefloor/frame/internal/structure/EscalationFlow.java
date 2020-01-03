package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link Flow} to handle an {@link Escalation} from a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface EscalationFlow {

	/**
	 * Obtains the type of cause handled by this {@link EscalationFlow}.
	 * 
	 * @return Type of cause handled by this {@link EscalationFlow}.
	 */
	Class<? extends Throwable> getTypeOfCause();

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the escalation handling
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of the escalation handling
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData();

}