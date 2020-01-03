package net.officefloor.frame.api.team;

/**
 * Team of workers to execute the assigned {@link Job} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface Team {

	/**
	 * Indicates for the {@link Team} to start working.
	 */
	void startWorking();

	/**
	 * Assigns a {@link Job} to be executed by this {@link Team}.
	 * 
	 * @param job {@link Job}.
	 * @throws TeamOverloadException Indicating the {@link Team} is overloaded and
	 *                               that back pressure should be applied to
	 *                               gracefully handle overload.
	 * @throws Exception             For other {@link Exception} instances to again
	 *                               indicate back pressure.
	 */
	void assignJob(Job job) throws TeamOverloadException, Exception;

	/**
	 * <p>
	 * Indicates for the {@link Team} to stop working.
	 * <p>
	 * This method should block and only return control when the {@link Team} has
	 * stopped working and is no longer assigned {@link Job} instances to complete.
	 */
	void stopWorking();

}