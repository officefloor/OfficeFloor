package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.Executive;

/**
 * Factory for the {@link ManagedExecution}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedExecutionFactory {

	/**
	 * Creates the {@link ManagedExecution}.
	 * 
	 * @param           <E> Possible {@link Escalation} from {@link Execution}.
	 * @param executive {@link Executive}.
	 * @param execution {@link Execution}.
	 * @return {@link ManagedExecution}.
	 */
	<E extends Throwable> ManagedExecution<E> createManagedExecution(Executive executive, Execution<E> execution);

}