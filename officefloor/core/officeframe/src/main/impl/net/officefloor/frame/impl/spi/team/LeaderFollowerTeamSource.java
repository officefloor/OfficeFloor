package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for a {@link LeaderFollowerTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeamSource extends AbstractTeamSource {

	/**
	 * Property to specify the worker {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "person.thread.priority";

	/**
	 * Default {@link Thread} priority.
	 */
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	/*
	 * =================== AbstractTeamSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the required configuration
		int teamSize = context.getTeamSize();
		if (teamSize < 1) {
			throw new IllegalArgumentException("Team size must be one or more");
		}

		// Obtain the optional configuration
		long waitTime = Long.parseLong(context.getProperty("wait.time", "100"));

		// Obtain the thread priority
		int priority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(DEFAULT_THREAD_PRIORITY)));

		// Create and return the team
		ThreadFactory threadFactory = context.getThreadFactory();
		if (priority != DEFAULT_THREAD_PRIORITY) {
			final ThreadFactory delegate = threadFactory;
			threadFactory = (runnable) -> {
				Thread thread = delegate.newThread(runnable);
				thread.setPriority(priority);
				return thread;
			};
		}
		return new LeaderFollowerTeam(teamSize, threadFactory, waitTime);
	}

}