/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Tests {@link Profiler}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ProfilerTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure able to profile the main {@link ThreadState}.
	 */
	@Test
	public void profileMainThreadState() throws Exception {

		// Configure
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder taskOne = this.construct.constructFunction(work, "taskOne");
		taskOne.setNextFunction("taskTwo");
		this.construct.constructFunction(work, "taskTwo");

		// Provide the profiler
		Closure<ProfiledProcessState> profile = new Closure<>();
		this.construct.getOfficeBuilder().setProfiler((process) -> profile.value = process);

		// Execute the function
		this.construct.invokeFunction("taskOne", null);

		// Ensure correct profiling
		assertNotNull(profile.value, "Ensure have profiled process");
		List<ProfiledThreadState> threads = profile.value.getProfiledThreadStates();
		assertEquals(1, threads.size(), "Incorrect number of threads");
		List<ProfiledManagedFunction> functions = threads.get(0).getProfiledManagedFunctions();
		assertEquals(2, functions.size(), "Incorrect number of functions");
		assertEquals("taskOne", functions.get(0).getFunctionName(), "Incorrect first function");
		assertEquals("taskTwo", functions.get(1).getFunctionName(), "Incorrect second function");
	}

	/**
	 * Ensure able to profile a spawned {@link ThreadState}.
	 */
	@Test
	public void profileSpawnedThreadState() throws Exception {

		// Configure
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder taskOne = this.construct.constructFunction(work, "taskOne");
		taskOne.setNextFunction("spawn");
		this.construct.constructFunction(work, "spawn").buildFlow("taskTwo", null, true);
		this.construct.constructFunction(work, "taskTwo");

		// Provide the profiler
		Closure<ProfiledProcessState> profile = new Closure<>();
		this.construct.getOfficeBuilder().setProfiler((process) -> profile.value = process);

		// Execute the function
		this.construct.invokeFunction("taskOne", null);

		// Ensure correct profiling (may complete on another thread)
		this.threading.waitForTrue(() -> profile.value != null,
				"Ensure have profiled process (happens after process completion notification so need to wait for it)");
		List<ProfiledThreadState> threads = profile.value.getProfiledThreadStates();
		assertEquals(2, threads.size(), "Incorrect number of threads");
		List<ProfiledManagedFunction> mainThread = threads.get(0).getProfiledManagedFunctions();
		assertEquals(2, mainThread.size(), "Incorrect number of functions for main thread");
		assertEquals("taskOne", mainThread.get(0).getFunctionName(), "Incorrect first function of main thread");
		assertEquals("spawn", mainThread.get(1).getFunctionName(), "Incorrect second function of main thread");
		List<ProfiledManagedFunction> spawnedThread = threads.get(1).getProfiledManagedFunctions();
		assertEquals(1, spawnedThread.size(), "Incorrect number of functions for spawned thread");
		assertEquals("taskTwo", spawnedThread.get(0).getFunctionName(), "Incorrect function for spawned thread");
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		public void taskOne() {
		}

		public void spawn(ReflectiveFlow flow) {
			flow.doFlow(null, null);
		}

		public void taskTwo() {
		}
	}

}
