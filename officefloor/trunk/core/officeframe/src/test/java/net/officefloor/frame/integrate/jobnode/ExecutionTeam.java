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
package net.officefloor.frame.integrate.jobnode;

import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link TeamManagement} for a {@link ExecutionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionTeam extends PassiveTeam implements TeamManagement,
		TeamIdentifier {

	/**
	 * {@link ExecutionTeam} of current {@link Thread}.
	 */
	public static ThreadLocal<ExecutionTeam> threadTeam = new ThreadLocal<ExecutionTeam>();

	/*
	 * ==================== TeamManagement =======================
	 */

	@Override
	public TeamIdentifier getIdentifier() {
		return this;
	}

	@Override
	public Team getTeam() {
		return this;
	}

	/*
	 * ====================== Team ================================
	 */

	@Override
	public void assignJob(Job job, TeamIdentifier assignerTeam) {

		// Loop forever until job complete
		for (;;) {

			// Specify the team executing the job
			threadTeam.set(this);

			// Attempt to complete the Job.
			if (job.doJob(new PassiveJobContext(ExecutionTeam.this
					.getIdentifier()))) {
				// Task complete
				return;
			}
		}
	}

}