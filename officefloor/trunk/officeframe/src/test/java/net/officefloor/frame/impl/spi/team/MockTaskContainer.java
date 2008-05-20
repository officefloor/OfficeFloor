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

import junit.framework.Assert;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Mock implementation of the
 * {@link net.officefloor.frame.spi.team.Job} for testing.
 * 
 * @author Daniel
 */
class MockTaskContainer implements Job {

	/**
	 * Lock.
	 */
	protected final Object lock = new Object();

	/**
	 * Next {@link Job}.
	 */
	protected Job nextTask = null;

	/**
	 * {@link Team}.
	 */
	protected Team team;

	/**
	 * Number of invocations of {@link #doJob(JobContext)}.
	 */
	public volatile int doTaskInvocationCount;

	/**
	 * Obtains the lock for conditional waits.
	 * 
	 * @return Lock for conditional waits.
	 */
	public Object getLock() {
		return this.lock;
	}

	/**
	 * Assigns this {@link MockTaskContainer} to a {@link Team}.
	 * 
	 * @param team
	 *            {@link Team} to assign this.
	 * @param waitTime
	 *            Wait time in seconds to return if
	 *            {@link #isTaskReady(JobContext)} is not called.
	 */
	public void assignTaskToTeam(Team team, int waitTime) {
		synchronized (this.getLock()) {
			// Store team
			this.team = team;

			// Assign task to team
			this.team.assignJob(this);

			// Wait on processing start
			try {
				this.getLock().wait(waitTime * 1000);
			} catch (InterruptedException ex) {
				Assert.fail("Interrupted: " + ex.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#doTask(net.officefloor.frame.spi.team.ExecutionContext)
	 */
	public boolean doJob(JobContext executionContext) {

		// Notify running
		synchronized (this.lock) {
			this.lock.notify();
		}

		// Increment number of times invoked
		this.doTaskInvocationCount++;

		// Sleep some time to mimic processing
		try {
			Thread.sleep(10);
		} catch (InterruptedException ex) {
			Assert.fail("Interrupted: Failed processing task - " + ex.getMessage());
		}

		// Never complete
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getThreadState()
	 */
	public ThreadState getThreadState() {
		// Possibly required but for current testing not implementing
		throw new UnsupportedOperationException(
				"Implement if necessary for testing");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#activeTask()
	 */
	public void activateJob() {
		synchronized (this.getLock()) {
			this.team.assignJob(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#setNextTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void setNextJob(Job task) {
		this.nextTask = task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getNextTask()
	 */
	public Job getNextJob() {
		return this.nextTask;
	}

}