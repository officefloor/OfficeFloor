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

import junit.framework.Assert;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;

/**
 * Mock implementation of the {@link Job} for testing.
 * 
 * @author Daniel Sagenschneider
 */
class MockTaskContainer implements Job {

	/**
	 * Lock.
	 */
	protected final Object lock = new Object();

	/**
	 * {@link Team}.
	 */
	protected Team team;
	
	/**
	 * Flag indicating to stop processing.
	 */
	public volatile boolean stopProcessing = false;

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
	public void assignJobToTeam(Team team, int waitTime) {
		synchronized (this.getLock()) {
			// Store team
			this.team = team;

			// Assign job to team
			this.team.assignJob(this);

			// Wait on processing to start
			try {
				this.getLock().wait(waitTime * 1000);
			} catch (InterruptedException ex) {
				Assert.fail("Interrupted: " + ex.getMessage());
			}
		}
	}

	/*
	 * ========================== Job ======================================
	 */
	
	@Override
	public boolean doJob(JobContext executionContext) {

		// Notify processing so assignJobToTeam may return
		synchronized (this.lock) {
			this.lock.notify();
		}

		// Increment number of times invoked
		this.doTaskInvocationCount++;

		// Sleep some time to mimic processing
		try {
			Thread.sleep(10);
		} catch (InterruptedException ex) {
			Assert.fail("Interrupted: Failed processing task - "
					+ ex.getMessage());
		}

		// Return when to stop processing
		return this.stopProcessing;
	}

	/**
	 * Next {@link Job}.
	 */
	protected Job nextJob = null;


	@Override
	public void setNextJob(Job job) {
		this.nextJob = job;
	}

	@Override
	public Job getNextJob() {
		return this.nextJob;
	}

}