package net.officefloor.frame.api.team;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Context for the {@link ThreadLocalAwareTeam} {@link Team}.
 *
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareContext {

	/**
	 * Executes the {@link Job} within the invoking {@link Thread} of the
	 * {@link ProcessState} to respect the {@link ThreadLocal} instances.
	 * 
	 * @param job
	 *            {@link Job} to be executed.
	 */
	void execute(Job job);

}