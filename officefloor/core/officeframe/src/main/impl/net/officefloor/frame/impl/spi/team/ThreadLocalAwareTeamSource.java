/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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