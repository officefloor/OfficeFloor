package net.officefloor.frame.api.thread;

import net.officefloor.frame.api.team.Team;

/**
 * Synchronises the {@link ThreadLocal} instances on {@link Thread} to
 * {@link Thread} interaction between {@link Team} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadSynchroniser {

	/**
	 * Suspends the current {@link Thread} by:
	 * <ol>
	 * <li>storing {@link ThreadLocal} state, and then</li>
	 * <li>clearing state off the {@link Thread}</li>
	 * </ol>
	 */
	void suspendThread();

	/**
	 * Resumes the {@link Thread} by loading the suspended state into the
	 * {@link ThreadLocal} instances of the current {@link Thread}.
	 */
	void resumeThread();

}