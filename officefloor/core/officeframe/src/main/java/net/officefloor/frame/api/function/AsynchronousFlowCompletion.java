package net.officefloor.frame.api.function;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * <p>
 * Allows {@link ThreadState} safe logic to run on the completion of the
 * {@link AsynchronousFlow}.
 * <p>
 * As the {@link AsynchronousFlow} is very likely to use other {@link Thread}
 * instances (and likely call the completion of {@link AsynchronousFlow} on
 * another {@link Thread}), this allows {@link ThreadState} logic to synchronise
 * the results back into the {@link ManagedFunction} and its dependent
 * {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface AsynchronousFlowCompletion {

	/**
	 * Contains the {@link ThreadState} safe logic.
	 * 
	 * @throws Throwable Indicate a failure in the {@link AsynchronousFlow}.
	 */
	void run() throws Throwable;

}