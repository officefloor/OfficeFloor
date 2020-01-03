package net.officefloor.frame.stress.thread;

import junit.framework.TestSuite;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link ThreadLocalAwareTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareTeamStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ThreadLocalAwareTeamStressTest.class);
	}

	/**
	 * {@link ThreadLocalAwareValue}.
	 */
	private final ThreadLocal<ThreadLocalAwareValue> value = new ThreadLocal<ThreadLocalAwareValue>() {
		@Override
		protected ThreadLocalAwareValue initialValue() {
			return new ThreadLocalAwareValue();
		}
	};

	@Override
	protected int getIterationCount() {
		return 100000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadResponsibleTeam(task.getBuilder());
		task.buildFlow("spawn", null, true);
		task.buildFlow("task", null, false);
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.getBuilder().setResponsibleTeam("THREAD_LOCAL");

		// Construct thread local aware team
		this.constructTeam("THREAD_LOCAL", ThreadLocalAwareTeamSource.class);

		// Test
		context.setInitialFunction("task", null);

		// Validate updated thread local value
		context.setValidation(() -> assertEquals("Incorrect thread local value", context.getMaximumIterations(),
				this.value.get().invokeCount));
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(ReflectiveFlow spawn, ReflectiveFlow repeat) {

			// Spawn the thread
			spawn.doFlow(null, (escalation) -> {
			});

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Spawn the thread
			repeat.doFlow(null, null);
		}

		public void spawn() {
			ThreadLocalAwareTeamStressTest.this.value.get().invokeCount++;
		}
	}

	/**
	 * {@link ThreadLocal} aware value.
	 */
	private class ThreadLocalAwareValue {

		private int invokeCount = 0;
	}

}