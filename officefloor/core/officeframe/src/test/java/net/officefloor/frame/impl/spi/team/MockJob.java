/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.spi.team;

import org.junit.Assert;

import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * Mock implementation of the {@link Job} for testing.
 * 
 * @author Daniel Sagenschneider
 */
class MockJob implements Job, ProcessIdentifier {

	/**
	 * Lock.
	 */
	private final Object lock = new Object();

	/**
	 * Flag indicating if this {@link Job} has been started.
	 */
	private boolean isExecuted = false;

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
	 * @param team     {@link Team} to assign this.
	 * @param waitTime Wait time in seconds.
	 */
	public void assignJobToTeam(Team team, int waitTime) throws Exception {

		// Assign job to team
		team.assignJob(this);

		// Wait to be processed
		long timeoutTime = System.currentTimeMillis() + (waitTime * 1000);
		try {
			synchronized (this.lock) {
				while (!this.isExecuted) {

					// Determine if timed out
					long currentTime = System.currentTimeMillis();
					if (currentTime > timeoutTime) {
						Assert.fail("Timed out waiting on job to be executed");
					}

					// Wait to be executed
					this.lock.wait(10);
				}
			}
		} catch (InterruptedException ex) {
			Assert.fail("Interrupted: " + ex.getMessage());
		}
	}

	/*
	 * ========================== Job ======================================
	 */

	@Override
	public void run() {

		// Increment number of times invoked
		this.doTaskInvocationCount++;

		// Notify processing so assignJobToTeam may return
		synchronized (this.lock) {
			this.isExecuted = true;
			this.lock.notify();
		}
	}

	@Override
	public void cancel(Throwable cause) {
		Assert.fail("Should not cancel job");
	}

	@Override
	public ProcessIdentifier getProcessIdentifier() {
		return this;
	}

}
