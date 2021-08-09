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
