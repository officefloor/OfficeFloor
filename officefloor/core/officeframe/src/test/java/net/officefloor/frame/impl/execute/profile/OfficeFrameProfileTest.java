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
package net.officefloor.frame.impl.execute.profile;

import java.util.List;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.integrate.jobnode.AbstractTaskNodeTestCase;
import net.officefloor.frame.spi.team.Job;

/**
 * Ensure able to receive {@link Profiler} information.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFrameProfileTest extends AbstractTaskNodeTestCase<Work> {

	/**
	 * Ensure profile execution.
	 */
	public void testSimpleProfile() {
		ValidateProfiler profiler = new ValidateProfiler("1");
		this.setProfiler(profiler);
		this.execute();
		profiler.validateProfiled();
	}

	/**
	 * Ensure profile execution of multiple {@link Job} instances.
	 */
	public void testMultipleJobsProfile() {

		// Set up execution of jobs
		this.bindNextNode(this.getInitialNode(), this.getInitialTeam(),
				this.getContinueTeam());

		// Ensure execute both jobs
		ValidateProfiler profiler = new ValidateProfiler("1", "2");
		this.setProfiler(profiler);
		this.execute();
		profiler.validateProfiled();
	}

	/**
	 * Validate {@link Profiler}.
	 */
	public static class ValidateProfiler implements Profiler {

		/**
		 * Names of the expected {@link ProfiledManagedFunction} instances.
		 */
		private String[] jobNames;

		/**
		 * Start time.
		 */
		private long startTime = System.nanoTime();

		/**
		 * {@link ProfiledProcessState}.
		 */
		private ProfiledProcessState process = null;

		/**
		 * Initiate.
		 * 
		 * @param jobNames
		 *            Names of the expected {@link ProfiledManagedFunction} instances.
		 */
		public ValidateProfiler(String... jobNames) {
			this.jobNames = jobNames;
		}

		/**
		 * Validates profiled.
		 */
		public void validateProfiled() {

			// Ensure just one thread
			List<ProfiledThreadState> threads = this.process.getProfiledThreads();
			assertEquals("Should just be the one thread", 1, threads.size());
			ProfiledThreadState thread = threads.get(0);

			// Validate the jobs
			List<ProfiledManagedFunction> jobs = thread.getProfiledJobs();
			assertEquals("Incorrect number of jobs", this.jobNames.length,
					jobs.size());
			for (int i = 0; i < this.jobNames.length; i++) {

				// Validate the job
				String jobName = this.jobNames[i];
				ProfiledManagedFunction job = jobs.get(i);
				assertEquals("Incorrect job name", jobName, job.getJobName());

				// Ensure job executed after start time
				assertTrue("Job should be executed after start time",
						(this.startTime < job.getStartTimestamp()));

				// Validate thread executing job
				String threadName = Thread.currentThread().getName();
				assertEquals("Incorrect thread name", threadName,
						job.getExecutingThreadName());
			}
		}

		/*
		 * =================== Profiler ===========================
		 */

		@Override
		public void profileProcess(ProfiledProcessState process) {
			assertNull("Should only profile one process", this.process);
			this.process = process;
		}
	}

}