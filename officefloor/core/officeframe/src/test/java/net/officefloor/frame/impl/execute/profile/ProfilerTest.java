package net.officefloor.frame.impl.execute.profile;

import java.util.List;

import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests {@link Profiler}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProfilerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to profile the main {@link ThreadState}.
	 */
	public void testProfileMainThreadState() throws Exception {

		// Configure
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "taskOne");
		taskOne.setNextFunction("taskTwo");
		this.constructFunction(work, "taskTwo");

		// Provide the profiler
		Closure<ProfiledProcessState> profile = new Closure<>();
		this.getOfficeBuilder().setProfiler((process) -> profile.value = process);

		// Execute the function
		this.invokeFunction("taskOne", null);

		// Ensure correct profiling
		assertNotNull("Ensure have profiled process", profile.value);
		List<ProfiledThreadState> threads = profile.value.getProfiledThreadStates();
		assertEquals("Incorrect number of threads", 1, threads.size());
		List<ProfiledManagedFunction> functions = threads.get(0).getProfiledManagedFunctions();
		assertEquals("Incorrect number of functions", 2, functions.size());
		assertEquals("Incorrect first function", "taskOne", functions.get(0).getFunctionName());
		assertEquals("Incorrect second function", "taskTwo", functions.get(1).getFunctionName());
	}

	/**
	 * Ensure able to profile a spawned {@link ThreadState}.
	 */
	public void testProfileSpawnedThreadState() throws Exception {

		// Configure
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "taskOne");
		taskOne.setNextFunction("spawn");
		this.constructFunction(work, "spawn").buildFlow("taskTwo", null, true);
		this.constructFunction(work, "taskTwo");

		// Provide the profiler
		Closure<ProfiledProcessState> profile = new Closure<>();
		this.getOfficeBuilder().setProfiler((process) -> profile.value = process);

		// Execute the function
		this.invokeFunction("taskOne", null);

		// Ensure correct profiling
		assertNotNull("Ensure have profiled process", profile.value);
		List<ProfiledThreadState> threads = profile.value.getProfiledThreadStates();
		assertEquals("Incorrect number of threads", 2, threads.size());
		List<ProfiledManagedFunction> mainThread = threads.get(0).getProfiledManagedFunctions();
		assertEquals("Incorrect number of functions for main thread", 2, mainThread.size());
		assertEquals("Incorrect first function of main thread", "taskOne", mainThread.get(0).getFunctionName());
		assertEquals("Incorrect second function of main thread", "spawn", mainThread.get(1).getFunctionName());
		List<ProfiledManagedFunction> spawnedThread = threads.get(1).getProfiledManagedFunctions();
		assertEquals("Incorrect number of functions for spawned thread", 1, spawnedThread.size());
		assertEquals("Incorrect function for spawned thread", "taskTwo", spawnedThread.get(0).getFunctionName());
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