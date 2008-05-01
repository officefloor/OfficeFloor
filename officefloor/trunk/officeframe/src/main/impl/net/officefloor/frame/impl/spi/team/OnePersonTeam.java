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

import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.spi.team.Team;

/**
 * <p>
 * Team having only one person.
 * </p>
 * <p>
 * Single threaded execution pool.
 * </p>
 * 
 * @author Daniel
 */
public class OnePersonTeam implements Team {

	/**
	 * Time to wait in milliseconds for a {@link TaskContainer}.
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
	 *            Time to wait in milliseconds for a {@link TaskContainer}.
	 */
	public OnePersonTeam(long waitTime) {
		// Store state
		this.waitTime = waitTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#startWorking()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#assignTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void assignTask(TaskContainer task) {
		this.taskQueue.enqueue(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#stopWorking()
	 */
	public synchronized void stopWorking() {
		if (this.person != null) {
			// Stop the Person working
			this.person.continueWorking = false;

			// Wait on Person to stop working
			while (!this.person.finished) {
				Thread.yield();
			}

			// Fire the Person
			this.person = null;
		}
	}

}

/**
 * The individual comprising the {@link Team}.
 */
class OnePerson implements Runnable, ExecutionContext {

	/**
	 * {@link TaskQueue}.
	 */
	protected final TaskQueue taskQueue;

	/**
	 * Time to wait in milliseconds for a {@link TaskContainer}.
	 */
	protected final long waitTime;

	/**
	 * Flag indicating to continue to work.
	 */
	protected volatile boolean continueWorking = true;

	/**
	 * Flag to indicate finished.
	 */
	protected volatile boolean finished = false;

	/**
	 * Time.
	 */
	protected long time;

	/**
	 * Initiate.
	 * 
	 * @param taskQueue
	 *            {@link TaskQueue}.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link TaskContainer}.
	 */
	public OnePerson(TaskQueue taskQueue, long waitTime) {
		// Store state
		this.taskQueue = taskQueue;
		this.waitTime = waitTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (this.continueWorking) {

				// Specify the time
				this.time = System.currentTimeMillis();

				// Obtain the next task
				TaskContainer task = this.taskQueue
						.dequeue(this, this.waitTime);

				if (task == null) {
					// Wait some time for a Task
					this.taskQueue.waitForTask(this.waitTime);

				} else {
					// Have task therefore execute it
					if (!task.doTask(this)) {
						// Task needs to be re-executed
						this.taskQueue.enqueue(task);
					}
				}
			}
		} finally {
			// Flag finished
			this.finished = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#getTime()
	 */
	public long getTime() {
		return this.time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#continueExecution()
	 */
	public boolean continueExecution() {
		return this.continueWorking;
	}

}
