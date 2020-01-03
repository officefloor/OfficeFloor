package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * {@link TeamSource} utilising a fixed {@link ExecutorService}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutorFixedTeamSource extends AbstractExecutorTeamSource {

	/*
	 * ===================== AbstractExecutorTeamSource =====================
	 */

	@Override
	protected ExecutorServiceFactory createExecutorServiceFactory(TeamSourceContext context,
			final ThreadFactory threadFactory) throws Exception {

		// Obtain the team details
		final int teamSize = context.getTeamSize();
		if (teamSize < 1) {
			throw new IllegalArgumentException("Team size must be one or more");
		}

		// Create and return the factory
		return new FixedExecutorServiceFactory(teamSize, threadFactory);
	}

	/**
	 * {@link ExecutorServiceFactory} for a fixed size.
	 */
	private static class FixedExecutorServiceFactory implements ExecutorServiceFactory {

		/**
		 * Size of the {@link Team}.
		 */
		private final int teamSize;

		/**
		 * {@link ThreadFactory}.
		 */
		private final ThreadFactory threadFactory;

		/**
		 * Initiate.
		 * 
		 * @param teamSize      Size of the {@link Team}.
		 * @param threadFactory {@link ThreadFactory}.
		 */
		public FixedExecutorServiceFactory(int teamSize, ThreadFactory threadFactory) {
			this.teamSize = teamSize;
			this.threadFactory = threadFactory;
		}

		/*
		 * ================== ExecutorServiceFactory ========================
		 */

		@Override
		public ExecutorService createExecutorService() {
			return Executors.newFixedThreadPool(this.teamSize, this.threadFactory);
		}
	}

}