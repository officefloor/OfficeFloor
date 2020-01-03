package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareContext;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for the {@link ThreadLocalAwareTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareTeamSource extends AbstractTeamSource {

	/*
	 * =================== TeamSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Create the team
		ThreadLocalAwareTeamImpl team = new ThreadLocalAwareTeamImpl();

		// Return the created team
		return team;
	}

	/**
	 * {@link ThreadLocalAwareTeam} implementation.
	 */
	private static class ThreadLocalAwareTeamImpl implements ThreadLocalAwareTeam {

		/**
		 * {@link ThreadLocalAwareContext}.
		 */
		private ThreadLocalAwareContext threadLocalAwareContext;

		/*
		 * ================== ThreadLocalAware =====================
		 */

		@Override
		public void setThreadLocalAwareness(ThreadLocalAwareContext context) {
			this.threadLocalAwareContext = context;
		}

		/*
		 * ========================== Team ====================================
		 */

		@Override
		public void startWorking() {
			// Nothing to start
		}

		@Override
		public void assignJob(Job job) {

			// Execute the job
			this.threadLocalAwareContext.execute(job);
		}

		@Override
		public void stopWorking() {
			// Nothing to stop
		}
	}

}