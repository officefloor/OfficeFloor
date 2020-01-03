package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for the {@link PassiveTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class PassiveTeamSource extends AbstractTeamSource {

	/**
	 * Convenience method to create the passive {@link Team}.
	 * 
	 * @return Passive {@link Team}.
	 */
	public static Team createPassiveTeam() {
		return new PassiveTeam();
	}

	/*
	 * ==================== AbstractTeamSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return new PassiveTeam();
	}

	/**
	 * Passive {@link Team}.
	 */
	private static class PassiveTeam implements Team {

		/*
		 * ==================== Team =====================================
		 */

		@Override
		public void startWorking() {
			// No workers as passive
		}

		@Override
		public void assignJob(Job job) {
			// Run with invoking thread
			job.run();
		}

		@Override
		public void stopWorking() {
		}
	}

}