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

import org.junit.Assert;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * Mock implementation of the {@link Job} for testing.
 * 
 * @author Daniel Sagenschneider
 */
class MockJob implements Job {

	/**
	 * Lock.
	 */
	private final Object lock = new Object();

	/**
	 * {@link Team}.
	 */
	private Team team;

	/**
	 * Flag indicating if this {@link Job} has been started.
	 */
	private boolean isStarted = false;

	/**
	 * Number of invocations of {@link Job}.
	 */
	public volatile int doTaskInvocationCount = 0;

	/**
	 * Obtains the lock for conditional waits.
	 * 
	 * @return Lock for conditional waits.
	 */
	public Object getLock() {
		return this.lock;
	}

	/**
	 * Assigns this {@link MockJob} to a {@link Team}.
	 * 
	 * @param team
	 *            {@link Team} to assign this.
	 * @param waitTime
	 *            Wait time in seconds.
	 */
	public void assignJobToTeam(Team team, int waitTime) {
		synchronized (this.lock) {
			// Store team
			this.team = team;

			// Assign job to team
			this.team.assignJob(this);

			// Wait on processing to start
			try {
				this.lock.wait(waitTime * 1000);
			} catch (InterruptedException ex) {
				Assert.fail("Interrupted: " + ex.getMessage());
			}

			// Ensure this job is started
			Assert.assertTrue("Job must be started", this.isStarted);
		}
	}

	/*
	 * ========================== Job ======================================
	 */

	@Override
	public void run() {

		// Notify processing so assignJobToTeam may return
		synchronized (this.lock) {
			this.isStarted = true;
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
	}

	@Override
	public void cancel(Throwable cause) {
		/*
		 * At moment, not seeing loads to require this as a priority.
		 * 
		 * TODO implement to allow admission control algorithms.
		 */
		throw new UnsupportedOperationException("TODO implement Job.cancelJob");
	}

	@Override
	public Object getProcessIdentifier() {
		return this;
	}

}