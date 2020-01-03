package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Manages the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteManager<F extends Enum<F>> {

	/**
	 * Obtains the {@link ManagedObjectExecuteContext}.
	 * 
	 * @return {@link ManagedObjectExecuteContext}.
	 */
	ManagedObjectExecuteContext<F> getManagedObjectExecuteContext();

	/**
	 * Invoked to indicate start for the corresponding {@link ManagedObjectSource}
	 * has completed.
	 * 
	 * @return {@link ManagedObjectStartupRunnable} instances to execute once ready
	 *         to start processing.
	 */
	ManagedObjectStartupRunnable[] startComplete();

}