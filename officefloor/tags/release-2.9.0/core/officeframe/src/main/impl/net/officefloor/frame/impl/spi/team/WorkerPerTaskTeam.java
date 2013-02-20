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

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link Team} that uses a specific new worker dedicated to each new
 * {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerTaskTeam extends ThreadGroup implements Team {

	/**
	 * {@link TeamIdentifier} of this {@link Team}.
	 */
	private final TeamIdentifier teamIdentifier;

	/**
	 * Priority for the worker {@link Thread} instances.
	 */
	private final int threadPriority;

	/**
	 * Indicates to continue working.
	 */
	private volatile boolean continueWorking = true;

	/**
	 * Count of the {@link Thread} instances created to obtain next index.
	 */
	private AtomicLong threadIndex = new AtomicLong(0);

	/**
	 * Initiate {@link Team}.
	 * 
	 * @param teamName
	 *            Name of this team.
	 * @param threadPriority
	 *            Priority for the worker {@link Thread} instances.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier} of this {@link Team}.
	 */
	public WorkerPerTaskTeam(String teamName, TeamIdentifier teamIdentifier,
			int threadPriority) {
		super(teamName);
		this.teamIdentifier = teamIdentifier;
		this.threadPriority = threadPriority;
	}

	/**
	 * Initiate {@link Team} with normal priority.
	 * 
	 * @param teamName
	 *            Name of this team.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier} of this {@link Team}.
	 */
	public WorkerPerTaskTeam(String teamName, TeamIdentifier teamIdentifier) {
		this(teamName, teamIdentifier, Thread.NORM_PRIORITY);
	}

	/*
	 * ======================== Team ==========================================
	 */

	@Override
	public void startWorking() {
		// No initial workers as hired when required
	}

	@Override
	public void assignJob(Job task, TeamIdentifier assignerTeam) {
		// Hire worker to execute the task
		long threadIndex = this.threadIndex.getAndIncrement();
		String threadName = this.getClass().getSimpleName() + "_"
				+ this.getName() + "_" + String.valueOf(threadIndex);
		Thread thread = new Thread(this, new DedicatedWorker(task), threadName);
		if (thread.getPriority() != this.threadPriority) {
			thread.setPriority(this.threadPriority);
		}
		if (!(thread.isDaemon())) {
			thread.setDaemon(true);
		}
		thread.start();
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
		public TeamIdentifier getCurrentTeam() {
			return WorkerPerTaskTeam.this.teamIdentifier;
		}

		@Override
		public boolean continueExecution() {
			return WorkerPerTaskTeam.this.continueWorking;
		}
	}

}