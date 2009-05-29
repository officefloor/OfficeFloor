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
 * Team having only one {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeam implements Team {

	/**
	 * Time to wait in milliseconds for a {@link Job}.
	 */
	protected final long waitTime;

	/**
	 * {@link OnePerson} of this {@link Team}.
	 */
	protected OnePerson person = null;

	/**
	 * {@link TaskQueue}.
	 */
	protected TaskQueue taskQueue;

	/**
	 * Initiate.
	 * 
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public OnePersonTeam(long waitTime) {
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

		// Create the queue of tasks
		this.taskQueue = new TaskQueue();

		// Hire the person for the team
		this.person = new OnePerson(this.taskQueue, this.waitTime);

		// Start the person working
		new Thread(this.person, this.getClass().getSimpleName()).start();
	}

	@Override
	public void assignJob(Job task) {
		this.taskQueue.enqueue(task);
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
	public static class OnePerson implements Runnable, JobContext {

		/**
		 * Indicates no time is set.
		 */
		private static final long NO_TIME = 0;

		/**
		 * {@link TaskQueue}.
		 */
		private final TaskQueue taskQueue;

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
		 *            {@link TaskQueue}.
		 * @param waitTime
		 *            Time to wait in milliseconds for a {@link Job}.
		 */
		public OnePerson(TaskQueue taskQueue, long waitTime) {
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
					Job job = this.taskQueue.dequeue(this, this.waitTime);
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
		public boolean continueExecution() {
			return this.continueWorking;
		}
	}

}