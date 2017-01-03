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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

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
		ProfiledWork work = new ProfiledWork();
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "taskOne");
		taskOne.setNextFunction("taskTwo");
		this.constructFunction(work, "taskTwo");

		// Provide the profiler
		final ProfiledProcessState[] profiledProcess = new ProfiledProcessState[1];
		this.getOfficeBuilder().setProfiler(new Profiler() {
			@Override
			public void profileProcessState(ProfiledProcessState process) {
				profiledProcess[0] = process;
			}
		});

		// Execute the function
		this.invokeFunction("taskOne", null);

		// Ensure correct profiling
		assertNotNull("Ensure have profiled process", profiledProcess[0]);
		List<ProfiledThreadState> threads = profiledProcess[0].getProfiledThreadStates();
		assertEquals("Incorrect number of threads", 1, threads.size());
		List<ProfiledManagedFunction> functions = threads.get(0).getProfiledManagedFunctions();
		assertEquals("Incorrect number of functions", 2, functions.size());
		assertEquals("Incorrect first function", "taskOne", functions.get(0).getFunctionName());
		assertEquals("Incorrect second function", "taskTwo", functions.get(1).getFunctionName());
	}

	/**
	 * Functionality for profiling.
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