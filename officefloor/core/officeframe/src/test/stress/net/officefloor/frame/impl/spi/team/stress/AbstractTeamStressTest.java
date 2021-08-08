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

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
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
	 * Number of {@link Job} instances to run in a multiple {@link Job} stress test.
	 */
	private final int MULTIPLE_JOB_NUMBER = 10;

	/**
	 * Maximum time to wait in seconds for each {@link Job} stress test to complete.
	 */
	private final int MAX_WAIT_TIME_IN_SECONDS = 60;

	/**
	 * {@link Team} to test.
	 */
	private Team team;

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
		protected void runJob() {

			// Do not reassign if max reassigns done
			if (this.getClonesDoJobCount() >= this.getMax()) {
				return;
			}

			// Assign again (as new job)
			try {
				this.getTeam().assignJob(this.clone());
			} catch (Exception ex) {
				throw fail(ex);
			}
		}
	}

	/**
	 * Stress tests the {@link TeamSource} in repeating {@link Job} instances.
	 */
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
	public class RepeatJob extends MockStressJob {

		@Override
		protected void runJob() {

			// Do not repeat if max repeats done
			if (this.getClonesDoJobCount() >= this.getMax()) {
				return;
			}

			// Repeat job again
			try {
				this.getTeam().assignJob(this);
			} catch (Exception ex) {
				throw fail(ex);
			}
		}
	}

	/**
	 * Runs the {@link Job} instances.
	 * 
	 * @param jobs {@link Job} instances to run.
	 */
	private void runJobs(MockStressJob... jobs) {

		// Specify details on jobs to run
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setTeam(this.team);
			jobs[i].setMax(MAX_RUN_TIMES);
		}

		// Run the jobs
		try {
			for (int i = 0; i < jobs.length; i++) {
				this.team.assignJob(jobs[i]);
			}
		} catch (Exception ex) {
			throw fail(ex);
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
					msg.append("Job[" + i + "]=" + jobs[i].getClonesDoJobCount() + "  ");
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
		this.printMessage("Stress run completed in " + this.getDisplayRunTime(startTime));
	}

}
