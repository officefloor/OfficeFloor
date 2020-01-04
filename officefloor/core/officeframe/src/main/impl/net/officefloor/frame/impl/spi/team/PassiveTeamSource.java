/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
