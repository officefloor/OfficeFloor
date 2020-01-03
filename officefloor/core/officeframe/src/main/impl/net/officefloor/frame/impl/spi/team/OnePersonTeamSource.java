package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * {@link TeamSource} for the {@link OnePersonTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamSource extends AbstractTeamSource {

	/**
	 * Property to specify the worker {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "person.thread.priority";

	/**
	 * Property name of the max wait time in milliseconds.
	 */
	public static final String MAX_WAIT_TIME_PROPERTY_NAME = "wait";

	/**
	 * Default {@link Thread} priority.
	 */
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	/**
	 * Convenience method to create a {@link OnePersonTeam}.
	 * 
	 * @param teamName Name of the {@link Team}.
	 * @return {@link OnePersonTeam}.
	 * @throws Exception If fails to create the {@link OnePersonTeam}.
	 */
	public static OnePersonTeam createOnePersonTeam(String teamName) throws Exception {
		return (OnePersonTeam) new TeamSourceStandAlone(teamName).loadTeam(OnePersonTeamSource.class);
	}

	/*
	 * ==================== AbstractTeamSource ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(MAX_WAIT_TIME_PROPERTY_NAME, "Wait time (ms)");
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the wait time
		long waitTime = Long.parseLong(context.getProperty(MAX_WAIT_TIME_PROPERTY_NAME, "100"));

		// Obtain the thread priority
		int priority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(DEFAULT_THREAD_PRIORITY)));

		// Obtain the thread factory
		ThreadFactory threadFactory = context.getThreadFactory();
		if (priority != Thread.NORM_PRIORITY) {
			ThreadFactory delegate = threadFactory;
			threadFactory = (runnable) -> {
				Thread thread = delegate.newThread(runnable);
				thread.setPriority(priority);
				return thread;
			};
		}

		// Return the one person team
		return new OnePersonTeam(threadFactory, waitTime);
	}

}