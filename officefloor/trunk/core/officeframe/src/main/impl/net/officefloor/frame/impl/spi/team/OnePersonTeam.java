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
 * Team having only one {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeam implements Team {

	/**
	 * Name of this {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link TeamIdentifier} of the {@link Team}.
	 */
	private final TeamIdentifier teamIdentifier;

	/**
	 * Time to wait in milliseconds for a {@link Job}.
	 */
	private final long waitTime;

	/**
	 * {@link OnePerson} of this {@link Team}.
	 */
	protected OnePerson person = null;

	/**
	 * {@link JobQueue}.
	 */
	private final JobQueue taskQueue = new JobQueue();

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link Team}.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier} of this {@link Team}.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public OnePersonTeam(String teamName, TeamIdentifier teamIdentifier,
			long waitTime) {
		this.teamName = teamName;
		this.teamIdentifier = teamIdentifier;
		this.waitTime = waitTime;
	}

	/*
	 * =================== Team =========================================
	 */

	@Override
	public synchronized void startWorking() {
		if (this.person != null) {
			throw new IllegalStateException("Team " + this.getClass().getName()
					+ " has already started working");
		}

		// Hire the person for the team
		this.person = new OnePerson(this.taskQueue, this.waitTime);

		// Start the person working
		String threadName = this.getClass().getSimpleName() + "_"
				+ this.teamName;
		Thread thread = new Thread(this.person, threadName);
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void assignJob(Job job, TeamIdentifier assignerTeam) {
		this.taskQueue.enqueue(job);
	}

	@Override
	public synchronized void stopWorking() {
		if (this.person != null) {
			try {
				// Stop the Person working
				this.person.continueWorking = false;

				// Wait on Person to stop working
				while (!this.person.finished) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						return;
					}
				}
			} finally {
				// Release the person
				this.person = null;
			}
		}
	}

	/**
	 * The individual comprising the {@link Team}.
	 */
	public class OnePerson implements Runnable, JobContext {

		/**
		 * Indicates no time is set.
		 */
		private static final long NO_TIME = 0;

		/**
		 * {@link JobQueue}.
		 */
		private final JobQueue taskQueue;

		/**
		 * Time to wait in milliseconds for a {@link Job}.
		 */
		private final long waitTime;

		/**
		 * Flag indicating to continue to work.
		 */
		private volatile boolean continueWorking = true;

		/**
		 * Flag to indicate finished.
		 */
		protected volatile boolean finished = false;

		/**
		 * Time.
		 */
		private long time = NO_TIME;

		/**
		 * Initiate.
		 * 
		 * @param taskQueue
		 *            {@link JobQueue}.
		 * @param waitTime
		 *            Time to wait in milliseconds for a {@link Job}.
		 */
		public OnePerson(JobQueue taskQueue, long waitTime) {
			this.taskQueue = taskQueue;
			this.waitTime = waitTime;
		}

		/*
		 * ======================== Runnable =============================
		 */

		@Override
		public void run() {
			try {
				while (this.continueWorking) {

					// Reset to no time
					this.time = NO_TIME;

					// Obtain the next job
					Job job = this.taskQueue.dequeue(this.waitTime);
					if (job != null) {
						// Have job therefore execute it
						if (!job.doJob(this)) {
							// Task needs to be re-executed
							this.taskQueue.enqueue(job);
						}
					}
				}
			} finally {
				// Flag finished
				this.finished = true;
			}
		}

		/*
		 * ==================== ExecutionContext ===========================
		 */

		@Override
		public long getTime() {

			// Ensure time is set
			if (this.time == NO_TIME) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		@Override
		public TeamIdentifier getCurrentTeam() {
			return OnePersonTeam.this.teamIdentifier;
		}

		@Override
		public boolean continueExecution() {
			return this.continueWorking;
		}
	}

}