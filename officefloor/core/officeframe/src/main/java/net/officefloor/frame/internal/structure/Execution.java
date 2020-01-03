package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.ProcessManager;

/**
 * Execution.
 * 
 * @author Daniel Sagenschneider
 */
public interface Execution<E extends Throwable> {

	/**
	 * Undertakes the execution.
	 * 
	 * @return {@link ProcessManager} to manage the {@link ProcessState}.
	 * @throws E Possible {@link Throwable} from execution.
	 */
	ProcessManager execute() throws E;

}