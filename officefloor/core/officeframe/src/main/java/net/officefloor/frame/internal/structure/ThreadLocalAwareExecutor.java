package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Job;

/**
 * Executes {@link Job} instances to enable access to the invoking
 * {@link ProcessState} {@link Thread} {@link ThreadLocal} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareExecutor {

	/**
	 * <p>
	 * Runs the {@link ProcessState} within context to enable the
	 * {@link ThreadLocal} instances of the current {@link Thread} to be
	 * available.
	 * <p>
	 * This will block the current {@link Thread} until the {@link ProcessState}
	 * and all subsequent {@link ProcessState} instances invoked by the current
	 * {@link Thread} are complete.
	 * 
	 * @param function
	 *            Initial {@link FunctionState} of the {@link ProcessState}.
	 * @param loop
	 *            {@link FunctionLoop}.
	 */
	void runInContext(FunctionState function, FunctionLoop loop);

	/**
	 * Executes the {@link Job} by the {@link Thread} registered to its
	 * {@link ProcessState}.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	void execute(Job job);

	/**
	 * Flags the {@link ProcessState} as complete.
	 * 
	 * @param processState
	 *            {@link ProcessState}.
	 */
	void processComplete(ProcessState processState);

}