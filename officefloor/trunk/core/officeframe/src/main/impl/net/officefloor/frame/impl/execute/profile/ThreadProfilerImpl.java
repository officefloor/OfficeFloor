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

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.profile.ProfiledJob;
import net.officefloor.frame.api.profile.ProfiledThread;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.spi.team.Job;

/**
 * {@link ThreadProfiler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadProfilerImpl implements ThreadProfiler, ProfiledThread {

	/**
	 * Start time stamp.
	 */
	private final long startTimestamp;

	/**
	 * {@link ProfiledJob} instances.
	 */
	private final List<ProfiledJob> jobs = new ArrayList<ProfiledJob>(32);

	/**
	 * Initiate.
	 * 
	 * @param startTimestamp
	 *            Start time stamp.
	 */
	public ThreadProfilerImpl(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/*
	 * ====================== ThreadProfiler ============================
	 */

	@Override
	public void profileJob(JobMetaData jobMetaData) {

		// Obtain the start time stamp
		long startTimestamp = System.nanoTime();

		// Obtain the job name
		String jobName = jobMetaData.getJobName();

		// Obtain the executing thread name
		String executingThreadName = Thread.currentThread().getName();

		// Create and add the profiled job
		this.jobs.add(new ProfiledJobImpl(jobName, startTimestamp,
				executingThreadName));
	}

	/*
	 * ======================= ProfiledThread =================================
	 */

	@Override
	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	@Override
	public List<ProfiledJob> getProfiledJobs() {
		return this.jobs;
	}

	/**
	 * {@link ProfiledJob} implementation.
	 */
	private static class ProfiledJobImpl implements ProfiledJob {

		/**
		 * {@link Job} name.
		 */
		private final String jobName;

		/**
		 * Start time stamp.
		 */
		private final long startTimestamp;

		/**
		 * Name of the executing {@link Thread}.
		 */
		private final String executingThreadName;

		/**
		 * Initiate.
		 * 
		 * @param jobName
		 *            {@link Job} name.
		 * @param startTimestamp
		 *            Start time stamp.
		 */
		public ProfiledJobImpl(String jobName, long startTimestamp,
				String executingThreadName) {
			this.jobName = jobName;
			this.startTimestamp = startTimestamp;
			this.executingThreadName = executingThreadName;
		}

		/*
		 * ====================== ProfiledJob =====================
		 */

		@Override
		public String getJobName() {
			return this.jobName;
		}

		@Override
		public long getStartTimestamp() {
			return this.startTimestamp;
		}

		@Override
		public String getExecutingThreadName() {
			return this.executingThreadName;
		}
	}

}