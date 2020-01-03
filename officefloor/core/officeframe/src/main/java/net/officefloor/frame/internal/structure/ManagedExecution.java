package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.ProcessManager;

/**
 * Managed execution.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedExecution<E extends Throwable> {

	/**
	 * Undertakes the execution.
	 * 
	 * @return {@link ProcessManager} to manage the {@link ProcessState}.
	 * @throws E Possible {@link Throwable} from execution.
	 */
	ProcessManager managedExecute() throws E;

}