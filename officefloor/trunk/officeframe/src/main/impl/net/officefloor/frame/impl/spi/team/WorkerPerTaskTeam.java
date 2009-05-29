/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link Team} that uses a specific new worker dedicated to each new
 * {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerTaskTeam extends ThreadGroup implements Team {

	/**
	 * Indicates to continue working.
	 */
	protected volatile boolean continueWorking = true;

	/**
	 * Initiate team.
	 * 
	 * @param teamName
	 *            Name of this team.
	 */
	public WorkerPerTaskTeam(String teamName) {
		super(teamName);
	}

	/*
	 * ======================== Team ==========================================
	 */

	@Override
	public void startWorking() {
		// No initial workers as hired when required
	}

	@Override
	public void assignJob(Job task) {
		// Hire worker to execute the task
		new Thread(this, new DedicatedWorker(task)).start();
	}

	@Override
	public void stopWorking() {
		// Flag to workers to stop working
		this.continueWorking = false;
	}

	/**
	 * Worker dedicated to executing a {@link Job}.
	 */
	private class DedicatedWorker implements Runnable, JobContext {

		/**
		 * Indicates not obtained time.
		 */
		private static final long NO_TIME = 0;

		/**
		 * {@link Job} to execute.
		 */
		private final Job taskContainer;

		/**
		 * Current time for execution.
		 */
		private long time = NO_TIME;

		/**
		 * Initiate worker.
		 * 
		 * @param taskContainer
		 *            {@link Job} to execute.
		 */
		public DedicatedWorker(Job taskContainer) {
			this.taskContainer = taskContainer;
		}

		/*
		 * ======================= Runnable =============================
		 */

		@Override
		public void run() {
			// Loop until task is complete or stop executing
			do {

				// Reset to no time
				this.time = NO_TIME;

				// Attempt to complete task
				if (this.taskContainer.doJob(this)) {
					// Task complete
					return;
				}

			} while (WorkerPerTaskTeam.this.continueWorking);
		}

		/*
		 * ================ ExecutionContext ===============================
		 */

		@Override
		public long getTime() {

			// Ensure have the time
			if (this.time == NO_TIME) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		@Override
		public boolean continueExecution() {
			return WorkerPerTaskTeam.this.continueWorking;
		}
	}

}