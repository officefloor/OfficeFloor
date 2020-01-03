package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to spawn multiple {@link ThreadState} instances.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnMutlipleThreadStatesTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can spawn a {@link ThreadState}.
	 */
	public void testSpawnMultipleThreadStates() throws Exception {

		final int SPAWN_COUNT = 10;

		// Construct functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildFlow("spawn", null, true);
		this.constructFunction(work, "spawn");

		// Ensure spawn multiple thread states
		this.invokeFunction("trigger", SPAWN_COUNT);
		assertEquals("Incorrect number of spawn threads", SPAWN_COUNT, work.spawnCount);
		assertEquals("Incorrect number of callbacks", SPAWN_COUNT, work.callbackCount);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private int spawnCount = 0;

		private int callbackCount = 0;

		public void trigger(Integer spawnCount, ReflectiveFlow flow) {
			for (int i = 0; i < spawnCount.intValue(); i++) {
				flow.doFlow(null, (escalation) -> {
					this.callbackCount++;
				});
			}
		}

		public void spawn() {
			this.spawnCount++;
		}
	}

}