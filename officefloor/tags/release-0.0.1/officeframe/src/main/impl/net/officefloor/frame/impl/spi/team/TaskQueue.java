/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

/**
 * Queue of {@link Job} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskQueue {

	/**
	 * Object to lock and wait on.
	 */
	protected final Object lock = new Object();

	/**
	 * Head {@link Job} of the queue.
	 */
	protected Job head = null;

	/**
	 * Tail {@link Job} of the queue.
	 */
	protected Job tail = null;

	/**
	 * Thread-safe enqueues a {@link Job} to the queue.
	 * 
	 * @param job
	 *            {@link Job} to add to the queue.
	 */
	public void enqueue(Job job) {
		synchronized (this.lock) {
			if (this.head == null) {
				// Empty list, therefore make first
				this.head = job;
				this.tail = job;

				// Item just added to list thus notify the dequeuer
				this.lock.notify();

			} else {
				// Non-empty list, therefore make last
				this.tail.setNextJob(job);
				this.tail = job;
			}

			// Last job so ensure not point to another job
			job.setNextJob(null);
		}
	}

	/**
	 * Thread-safe dequeuing the next {@link Job} to execute.
	 * 
	 * @param executionContext
	 *            {@link JobContext} to determine if {@link Job} is ready.
	 * @return Next {@link Job} to execute.
	 */
	public Job dequeue(JobContext executionContext) {
		synchronized (this.lock) {
			return this.dequeue0(executionContext);
		}
	}

	/**
	 * Thread-safe dequeuing the next {@link Job} to execute. This will block
	 * for <code>timeout</code> milliseconds for a {@link Job} to become
	 * available.
	 * 
	 * @param executionContext
	 *            {@link JobContext} to determine if {@link Job} is ready.
	 * @return Next {@link Job} to execute.
	 */
	public Job dequeue(JobContext executionContext, long timeout) {
		synchronized (this.lock) {

			// Wait on a job to be in queue
			this.waitForTask0(timeout);

			// Attempt to dequeue a job
			return this.dequeue0(executionContext);
		}
	}

	/**
	 * Waits the input period of time for another {@link Job} to be added.
	 * 
	 * @param timeout
	 *            Time to wait in milliseconds.
	 */
	public void waitForTask(long timeout) {
		synchronized (this.lock) {
			this.waitForTask0(timeout);
		}
	}

	/**
	 * <p>
	 * Waits the input period of time for another {@link Job} to be added.
	 * <p>
	 * Before invoking this method, the {@link #lock} must be synchronised on.
	 * 
	 * @param timeout
	 *            Time to wait in milliseconds.
	 */
	private void waitForTask0(long timeout) {
		// Wait on a job to be in queue
		if (this.head == null) {
			try {
				this.lock.wait(timeout);
			} catch (InterruptedException ex) {
				// Continue processing on interrupt
			}
		}
	}

	/**
	 * Dequeues the next {@link Job} to execute.
	 * 
	 * @param executionContext
	 *            {@link JobContext} to determine if {@link Job} is ready.
	 * @return Next {@link Job} to execute.
	 */
	private Job dequeue0(JobContext executionContext) {

		// Check if contains any jobs
		if (this.head == null) {
			// No jobs
			return null;
		}

		// Obtain job to return
		Job returnJob = this.head;

		// Check if only job
		if (this.head == this.tail) {
			// No further jobs
			this.head = null;
			this.tail = null;

		} else {
			// Further jobs
			this.head = this.head.getNextJob();
		}

		// Clear next job as about to be executed
		returnJob.setNextJob(null);

		// Return the return job
		return returnJob;
	}

}