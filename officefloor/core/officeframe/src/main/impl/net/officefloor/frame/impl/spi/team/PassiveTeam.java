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

import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * <p>
 * Passive {@link Team} which uses the invoking {@link Thread} to execute the
 * {@link Job}.
 * <p>
 * Note that using this team will block the invoking {@link Thread} until the
 * {@link Job} is complete.
 * 
 * @author Daniel Sagenschneider
 */
public class PassiveTeam implements Team {

	/**
	 * Indicates if should continue working.
	 */
	private volatile boolean continueWorking = true;

	/*
	 * ==================== Team =====================================
	 */

	@Override
	public void startWorking() {
		// No workers as passive
	}

	@Override
	public void assignJob(Job task, TeamIdentifier assignerTeam) {
		task.doJob(new PassiveJobContext(assignerTeam));
	}

	@Override
	public void stopWorking() {
		// Flag to stop working
		this.continueWorking = false;
	}

	/**
	 * Passive {@link JobContext}.
	 */
	protected class PassiveJobContext implements JobContext {

		/**
		 * Value indicating the time is not specified.
		 */
		private static final long TIME_NOT_SET = -1;

		/**
		 * {@link TeamIdentifier}.
		 */
		private final TeamIdentifier teamIdentifier;

		/**
		 * Current time for execution.
		 */
		private long time = TIME_NOT_SET;

		/**
		 * Initiate.
		 * 
		 * @param teamIdentifier
		 *            {@link TeamIdentifier}.
		 */
		public PassiveJobContext(TeamIdentifier teamIdentifier) {
			this.teamIdentifier = teamIdentifier;
		}

		/*
		 * ================= JobContext ====================
		 */

		@Override
		public long getTime() {

			// Ensure the time is specified
			if (this.time == TIME_NOT_SET) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		@Override
		public TeamIdentifier getCurrentTeam() {
			return this.teamIdentifier;
		}

		@Override
		public boolean continueExecution() {
			return PassiveTeam.this.continueWorking;
		}
	}

}