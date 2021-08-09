/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
