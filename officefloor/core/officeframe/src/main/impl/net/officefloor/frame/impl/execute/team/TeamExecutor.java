package net.officefloor.frame.impl.execute.team;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;

/**
 * {@link Executor} that is backed by a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamExecutor implements Executor {

	/**
	 * {@link Team} to execute the {@link Runnable} instances.
	 */
	private final Team team;

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * Instantiate.
	 * 
	 * @param team      {@link Team}.
	 * @param executive {@link Executive}.
	 */
	public TeamExecutor(Team team, Executive executive) {
		this.team = team;
		this.executive = executive;
	}

	/*
	 * ================= Executor ========================
	 */

	@Override
	public void execute(Runnable command) {

		// Attempt to determine current thread state (to re-use process identifier)
		Object identifier = ThreadStateImpl.currentProcessIdentifier();
		if (identifier == null) {
			// Invoked outside management, so create new process to run
			identifier = this.executive.createProcessIdentifier();
		}

		// Execute the runnable
		try {
			final Object processIdentifier = identifier;
			this.team.assignJob(new Job() {

				@Override
				public Object getProcessIdentifier() {
					return processIdentifier;
				}

				@Override
				public void run() {
					command.run();
				}

				@Override
				public void cancel(Throwable cause) {
					// Propagate failure
					throw new TeamExecutorRuntimeException(cause);
				}
			});
		} catch (Exception ex) {
			// Propagate failure
			throw new TeamExecutorRuntimeException(ex);
		}
	}

}