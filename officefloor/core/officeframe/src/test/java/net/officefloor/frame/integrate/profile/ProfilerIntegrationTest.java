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
package net.officefloor.frame.integrate.profile;

import java.util.List;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveFunctionBuilder.ReflectiveFunctionBuilder;

/**
 * Tests profiling.
 * 
 * @author Daniel Sagenschneider
 */
public class ProfilerIntegrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to profile.
	 */
	public void testProfile() throws Exception {

		// Configure
		this.constructTeam("TEAM", new PassiveTeam());
		ProfiledWork work = new ProfiledWork();
		ReflectiveFunctionBuilder builder = this.constructWork(work, "WORK",
				"taskOne");
		ReflectiveFunctionBuilder taskOne = builder.buildTask("taskOne", "TEAM");
		taskOne.setNextTaskInFlow("taskTwo");
		builder.buildTask("taskTwo", "TEAM");

		// Provide the profiler
		final ProfiledProcessState[] profiledProcess = new ProfiledProcessState[1];
		this.getOfficeBuilder().setProfiler(new Profiler() {
			@Override
			public void profileProcess(ProfiledProcessState process) {
				profiledProcess[0] = process;
			}
		});

		// Execute the work
		this.invokeWork("WORK", null);

		// Ensure correct profiling
		assertNotNull("Ensure have profiled process", profiledProcess[0]);
		List<ProfiledThreadState> threads = profiledProcess[0].getProfiledThreads();
		assertEquals("Incorrect number of threads", 1, threads.size());
		List<ProfiledManagedFunction> jobs = threads.get(0).getProfiledJobs();
		assertEquals("Incorrect number of jobs", 2, jobs.size());
		assertEquals("Incorrect first job", "WORK.taskOne", jobs.get(0)
				.getJobName());
		assertEquals("Incorrect second job", "WORK.taskTwo", jobs.get(1)
				.getJobName());
	}

	/**
	 * {@link Work} for profiling.
	 */
	public static class ProfiledWork {

		/**
		 * First {@link ManagedFunction}.
		 */
		public void taskOne() {
		}

		/**
		 * Second {@link ManagedFunction}.
		 */
		public void taskTwo() {
		}
	}

}