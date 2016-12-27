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
package net.officefloor.compile.integrate.profile;

import java.util.List;

import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests making the {@link Profiler} available to the {@link OfficeFrame}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProfilerIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to configure the {@link Profiler}.
	 */
	public void testConfigureProfiler() throws Exception {

		// Configure OfficeFloor
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("SECTION", ClassSectionSource.class.getName(),
				ProfiledWork.class.getName());

		// Configure the profiler
		final ProfiledProcessState[] profiledProcess = new ProfiledProcessState[1];
		source.setProfiler(new Profiler() {
			@Override
			public void profileProcess(ProfiledProcessState process) {
				profiledProcess[0] = process;
			}
		});

		// Invoke the work
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		officeFloor.invokeTask("SECTION.WORK", "task", null);
		officeFloor.closeOfficeFloor();

		// Ensure profiled
		assertNotNull("Should be profiling office", profiledProcess[0]);
		List<ProfiledThreadState> threads = profiledProcess[0].getProfiledThreads();
		assertEquals("Should have one thread profiled", 1, threads.size());
		List<ProfiledManagedFunction> jobs = threads.get(0).getProfiledJobs();
		assertEquals("Should just be one job profiled", 1, jobs.size());
		ProfiledManagedFunction job = jobs.get(0);
		assertEquals("Incorrect profiled job", "SECTION.WORK.task",
				job.getJobName());
	}

	/**
	 * Profiled {@link Work}.
	 */
	public static class ProfiledWork {
		public void task() {
		}
	}

}