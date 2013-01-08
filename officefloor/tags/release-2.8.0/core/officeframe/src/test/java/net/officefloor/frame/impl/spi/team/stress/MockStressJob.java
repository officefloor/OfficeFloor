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
package net.officefloor.frame.impl.spi.team.stress;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;

/**
 * Mock {@link Job} used for stress testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class MockStressJob implements Job {

	/**
	 * Default construct to allow creation and allow cloning.
	 */
	public MockStressJob() {
		this.cloneCount = new CloneCount(); // overwritten if cloned
	}

	/**
	 * Convenience constructor for creating populated.
	 * 
	 * @param team
	 *            {@link Team}.
	 * @param max
	 *            Max.
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
			clonedJob = (MockStressJob) this.getClass().newInstance();
		} catch (Throwable ex) {
			// Fail testing if can not instantiate new instance
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			TestCase.fail("Failed cloning " + this.getClass().getName() + "\n"
					+ stackTrace);
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
	 * {@link JobContext}.
	 */
	private JobContext jobContext;

	/**
	 * Obtains the {@link JobContext}.
	 * 
	 * @return {@link JobContext}.
	 */
	public synchronized JobContext getJobContext() {
		return this.jobContext;
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
	 * @param team
	 *            {@link Team}.
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
	 * @param max
	 *            Max value.
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
	 * @return Number of times the doJob is invoked for this {@link Job}
	 *         instance.
	 */
	public synchronized int getDoJobCount() {
		return this.doJobCount;
	}

	/**
	 * {@link CloneCount}.
	 */
	private CloneCount cloneCount;

	/**
	 * Obtains the number of times the doJob method is invoked for all
	 * {@link Job} clones.
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
	public final boolean doJob(JobContext jobContext) {

		// Synchronised as typically locked on ThreadState when running
		synchronized (this) {

			// Ensure the next job is null
			TestCase.assertNull("Next job should not be set on running a job",
					this.nextJob);

			// Increment the job runs
			this.cloneCount.incrementDoJobCount();
			this.doJobCount++;

			// Specify the job context and run the job
			this.jobContext = jobContext;
			return this.runJob();
		}
	}

	/**
	 * Runs the {@link Job}.
	 * 
	 * @return <code>true</code> if {@link Job} is complete.
	 */
	protected abstract boolean runJob();

	@Override
	public Object getProcessIdentifier() {
		return this;
	}

	/**
	 * Next {@link Job}.
	 */
	private Job nextJob = null;

	@Override
	public final Job getNextJob() {
		return this.nextJob;
	}

	@Override
	public final void setNextJob(Job job) {
		this.nextJob = job;
	}

	/**
	 * Maintains counts for all clones.
	 */
	private class CloneCount {

		/**
		 * Number of times the doJob method is invoked for all {@link Job}
		 * clones.
		 */
		private int doJobCount;

		/**
		 * Obtains the number of times the doJob method is invoked for all
		 * {@link Job} clones.
		 * 
		 * @return Number of times the doJob method is invoked for all
		 *         {@link Job} clones.
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