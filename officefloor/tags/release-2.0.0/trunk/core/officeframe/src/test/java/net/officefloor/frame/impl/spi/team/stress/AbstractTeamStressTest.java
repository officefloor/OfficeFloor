/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Stress tests the {@link TeamSource} by reassigning a {@link Job} over and
 * over.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTeamStressTest extends OfficeFrameTestCase {

	/**
	 * Maximum number of times to run the {@link Job} in each stress test.
	 */
	private final int MAX_RUN_TIMES = 1000000;

	/**
	 * Number of {@link Job} instances to run in a multiple {@link Job} stress
	 * test.
	 */
	private final int MULTIPLE_JOB_NUMBER = 10;

	/**
	 * Maximum time to wait in seconds for each {@link Job} stress test to
	 * complete.
	 */
	private final int MAX_WAIT_TIME_IN_SECONDS = 60;

	/**
	 * {@link Team} to test.
	 */
	private Team team;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Flag whether verbose running
		this.setVerbose(true);

		// Obtain the team to test and start it working
		this.team = this.getTeamToTest();
		this.team.startWorking();
	}

	/**
	 * Obtains the {@link Team} to test.
	 * 
	 * @return {@link Team} to test.
	 */
	protected abstract Team getTeamToTest() throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// Ensure team is stopped
		this.team.stopWorking();
	}

	/**
	 * Stress tests the {@link TeamSource} in reassigning {@link Job} instances.
	 */
	@StressTest
	public void testReassignSingleJob() throws Exception {

		// Create the reassign job
		ReassignJob job = new ReassignJob();

		// Run the reassignment stress test
		this.runJobs(job);
	}

	/**
	 * Stress tests the {@link TeamSource} in reassigning multiple {@link Job}
	 * instances.
	 */
	@StressTest
	public void testReassignMutlipleJobs() throws Exception {

		// Create the reassign jobs
		ReassignJob[] jobs = new ReassignJob[MULTIPLE_JOB_NUMBER];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new ReassignJob();
		}

		// Run the reassignment stress test
		this.runJobs(jobs);
	}

	/**
	 * Reassign {@link Job}.
	 */
	public static class ReassignJob extends MockStressJob {

		@Override
		protected boolean runJob() {

			// Do not reassign if max reassigns done
			if (this.getClonesDoJobCount() >= this.getMax()) {
				return true;
			}

			// Assign again (as new job)
			this.getTeam().assignJob(this.clone());
			return true;
		}
	}

	/**
	 * Stress tests the {@link TeamSource} in repeating {@link Job} instances.
	 */
	@StressTest
	public void testRepeatSingleJob() throws Exception {

		// Create the repeat job
		RepeatJob job = new RepeatJob();

		// Run the repeat stress test
		this.runJobs(job);
	}

	/**
	 * Stress tests the {@link TeamSource} in repeating multiple {@link Job}
	 * instances.
	 */
	@StressTest
	public void testRepeatMutlipleJobs() throws Exception {

		// Create the repeat jobs
		RepeatJob[] jobs = new RepeatJob[MULTIPLE_JOB_NUMBER];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new RepeatJob();
		}

		// Run the repeat stress test
		this.runJobs(jobs);
	}

	/**
	 * Repeat {@link Job}.
	 */
	public static class RepeatJob extends MockStressJob {

		@Override
		protected boolean runJob() {

			// Do not repeat if max repeats done
			if (this.getClonesDoJobCount() >= this.getMax()) {
				return true;
			}

			// Repeat job again
			return false;
		}
	}

	/**
	 * Runs the {@link Job} instances.
	 * 
	 * @param jobs
	 *            {@link Job} instances to run.
	 */
	private void runJobs(MockStressJob... jobs) {

		// Specify details on jobs to run
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setTeam(this.team);
			jobs[i].setMax(MAX_RUN_TIMES);
		}

		// Run the jobs
		for (int i = 0; i < jobs.length; i++) {
			this.team.assignJob(jobs[i]);
		}

		// Capture start time for timing out
		long startTime = System.currentTimeMillis();

		// Allow team to do processing
		Thread.yield();

		// Wait until all expected job runs are done or times out
		boolean isComplete = false;
		while (!isComplete) {

			// Print details of the job runs
			StringBuilder msg = new StringBuilder();
			msg.append("Clone Job Runs");
			if (jobs.length == 1) {
				// Provide the single job details
				msg.append("=" + jobs[0].getClonesDoJobCount());
			} else {
				// Provide each job run details
				msg.append(": ");
				for (int i = 0; i < jobs.length; i++) {
					msg.append("Job[" + i + "]="
							+ jobs[i].getClonesDoJobCount() + "  ");
				}
			}
			this.printMessage(msg.toString());
			this.printHeapMemoryDiagnostics();

			// Determine if jobs done
			isComplete = true;
			for (int i = 0; i < jobs.length; i++) {
				if (jobs[i].getClonesDoJobCount() < MAX_RUN_TIMES) {
					isComplete = false;
				}
			}

			// Wait some time if not yet complete
			if (!isComplete) {
				this.sleep(1);
			}

			// Ensure not timed out
			this.timeout(startTime, MAX_WAIT_TIME_IN_SECONDS);
		}
		this.printMessage("Stress run completed in "
				+ this.getDisplayRunTime(startTime));
	}

}