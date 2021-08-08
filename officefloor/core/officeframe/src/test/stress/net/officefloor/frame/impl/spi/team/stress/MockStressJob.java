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

package net.officefloor.frame.impl.spi.team.stress;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * Mock {@link Job} used for stress testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class MockStressJob implements Job, ProcessIdentifier {

	/**
	 * Default construct to allow creation and allow cloning.
	 */
	public MockStressJob() {
		this.cloneCount = new CloneCount(); // overwritten if cloned
	}

	/**
	 * Convenience constructor for creating populated.
	 * 
	 * @param team {@link Team}.
	 * @param max  Max.
	 */
	public MockStressJob(Team team, int max) {
		this();
		this.team = team;
		this.max = max;
	}

	/**
	 * Creates a clone of this {@link MockStressJob}.
	 * 
	 * @return Cloned {@link MockStressJob}.
	 */
	public synchronized MockStressJob clone() {

		// Create a new instance of this job
		MockStressJob clonedJob;
		try {
			clonedJob = (MockStressJob) this.getClass().getDeclaredConstructor().newInstance();
		} catch (Throwable ex) {
			// Fail testing if can not instantiate new instance
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			TestCase.fail("Failed cloning " + this.getClass().getName() + "\n" + stackTrace);
			return null; // fail will throw problem
		}

		// Specify state of cloned job
		synchronized (clonedJob) {
			clonedJob.cloneCount = this.cloneCount;
			clonedJob.max = this.max;
			clonedJob.team = this.team;
		}

		// Return the cloned job
		return clonedJob;
	}

	/**
	 * Provide ability to store the {@link Team}.
	 */
	private Team team;

	/**
	 * Obtains the {@link Team}.
	 * 
	 * @return {@link Team}.
	 */
	public synchronized Team getTeam() {
		return this.team;
	}

	/**
	 * Specifies the {@link Team}.
	 * 
	 * @param team {@link Team}.
	 */
	public synchronized void setTeam(Team team) {
		this.team = team;
	}

	/**
	 * Provide a max value typically required in stress testing.
	 */
	private int max;

	/**
	 * Obtains the max value.
	 * 
	 * @return Max value.
	 */
	public synchronized int getMax() {
		return this.max;
	}

	/**
	 * Specifies the max value.
	 * 
	 * @param max Max value.
	 */
	public synchronized void setMax(int max) {
		this.max = max;
	}

	/**
	 * Number of times the doJob is invoked for this {@link Job} instance.
	 */
	private int doJobCount = 0;

	/**
	 * Obtains the number of times the doJob is invoked for this {@link Job}
	 * instance.
	 * 
	 * @return Number of times the doJob is invoked for this {@link Job} instance.
	 */
	public synchronized int getDoJobCount() {
		return this.doJobCount;
	}

	/**
	 * {@link CloneCount}.
	 */
	private CloneCount cloneCount;

	/**
	 * Obtains the number of times the doJob method is invoked for all {@link Job}
	 * clones.
	 * 
	 * @return Number of times the doJob method is invoked for all {@link Job}
	 *         clones.
	 */
	public synchronized int getClonesDoJobCount() {
		return this.cloneCount.getDoJobCount();
	}

	/*
	 * ========================= Job =========================================
	 */

	@Override
	public final void run() {

		// Synchronised as typically locked on ThreadState when running
		synchronized (this) {

			// Increment the job runs
			this.cloneCount.incrementDoJobCount();
			this.doJobCount++;

			// Run the job
			this.runJob();
		}
	}

	@Override
	public void cancel(Throwable cause) {
		Assert.fail("Should not cancel job");
	}

	/**
	 * Runs the {@link Job}.
	 */
	protected abstract void runJob();

	@Override
	public ProcessIdentifier getProcessIdentifier() {
		return this;
	}

	/**
	 * Maintains counts for all clones.
	 */
	private class CloneCount {

		/**
		 * Number of times the doJob method is invoked for all {@link Job} clones.
		 */
		private int doJobCount;

		/**
		 * Obtains the number of times the doJob method is invoked for all {@link Job}
		 * clones.
		 * 
		 * @return Number of times the doJob method is invoked for all {@link Job}
		 *         clones.
		 */
		public synchronized int getDoJobCount() {
			return this.doJobCount;
		}

		/**
		 * Increments the number of times the doJob method is invoked for all
		 * {@link Job} clones.
		 */
		public synchronized void incrementDoJobCount() {
			this.doJobCount++;
		}
	}

}
