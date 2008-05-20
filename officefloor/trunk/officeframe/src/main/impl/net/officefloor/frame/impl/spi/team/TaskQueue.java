/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;

/**
 * Queue of {@link net.officefloor.frame.spi.team.Job} instances.
 * 
 * @author Daniel
 */
class TaskQueue {

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
	 * @param task
	 *            {@link Job} to add to the queue.
	 */
	public void enqueue(Job task) {
		synchronized (this.lock) {
			if (this.head == null) {
				// Empty list, therefore make first
				this.head = task;
				this.tail = task;

				// Item just added to list thus notify the dequeuer
				this.lock.notify();

			} else {
				// Non-empty list, therefore make last
				this.tail.setNextJob(task);
				this.tail = task;
			}

			// Last Task so ensure not point to another Task
			task.setNextJob(null);
		}
	}

	/**
	 * Thread-safe dequeuing the next {@link Job} to execute.
	 * 
	 * @param executionContext
	 *            {@link JobContext} to determine if task is ready.
	 * @return Next {@link Job} to execute.
	 */
	public Job dequeue(JobContext executionContext) {
		synchronized (this.lock) {
			return this.dequeue0(executionContext);
		}
	}

	/**
	 * Thread-safe dequeuing the next {@link Job} to execute. This
	 * will block for <code>timeout</code> milliseconds for a
	 * {@link Job} to become available.
	 * 
	 * @param executionContext
	 *            {@link JobContext} to determine if task is ready.
	 * @return Next {@link Job} to execute.
	 */
	public Job dequeue(JobContext executionContext, long timeout) {
		synchronized (this.lock) {

			// Wait on a Task to be in queue
			this.waitForTask0(timeout);

			// Attempt to dequeue a Task
			return this.dequeue0(executionContext);
		}
	}

	/**
	 * Waits the input period of time for another {@link Job} to be
	 * added.
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
	 * Waits the input period of time for another {@link Job} to be
	 * added.
	 * </p>
	 * <p>
	 * Before invoking this method, the {@link #lock} must be synchronized on.
	 * </p>
	 * 
	 * @param timeout
	 *            Time to wait in milliseconds.
	 */
	private void waitForTask0(long timeout) {
		// Wait on a Task to be in queue
		if (this.head == null) {
			try {
				this.lock.wait(timeout);
			} catch (InterruptedException ex) {
				// Continue processing on interupt
			}
		}
	}

	/**
	 * Dequeues the next {@link Job} to execute.
	 * 
	 * @param executionContext
	 *            {@link JobContext} to determine if task is ready.
	 * @return Next {@link Job} to execute.
	 */
	private Job dequeue0(JobContext executionContext) {

		// Check if contains any tasks
		if (this.head == null) {
			// No tasks
			return null;
		}

		// Obtain task to return
		Job returnTask = this.head;

		// Check if only task
		if (this.head == this.tail) {
			// No further tasks
			this.head = null;
			this.tail = null;

		} else {
			// Further tasks
			this.head = this.head.getNextJob();
		}

		// Return task has no next task as about to be executed
		returnTask.setNextJob(null);

		// Return the return task
		return returnTask;
	}

}