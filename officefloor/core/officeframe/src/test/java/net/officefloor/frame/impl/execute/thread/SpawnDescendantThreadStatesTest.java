package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to spawn a {@link ThreadState} that recursively spawns further
 * {@link ThreadState} instances.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnDescendantThreadStatesTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can spawn a {@link ThreadState}.
	 */
	public void testSpawnDescendantThreadStates() throws Exception {

		final int DESCENDANT_DEPTH = 3;
		final int SPAWN_BURST = 10;

		// Construct functions
		TestWork work = new TestWork(DESCENDANT_DEPTH, SPAWN_BURST);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildFlow("spawn", null, true);
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.buildParameter();
		spawn.buildFlow("trigger", null, false);

		// Ensure spawn multiple thread states
		this.invokeFunction("trigger", 0);

		// Determine the number of spawn thread states expected
		int expectedSpawnCount = 0;
		for (int i = 0; i < DESCENDANT_DEPTH; i++) {
			expectedSpawnCount = expectedSpawnCount + (int) (Math.pow(SPAWN_BURST, i + 1));
		}

		// Ensure correct number of spawned thread states
		assertEquals("Incorrect number of spawned thread state instances", expectedSpawnCount, work.spawnCount);
		assertEquals("Incorrect number of callbacks", expectedSpawnCount, work.spawnCount);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final int descendantDepth;

		private final int spawnBurst;

		private int spawnCount = 0;

		private int callbackCount = 0;

		public TestWork(int descendantDepth, int spawnBurst) {
			this.descendantDepth = descendantDepth;
			this.spawnBurst = spawnBurst;
		}

		public void trigger(Integer depth, ReflectiveFlow flow) {

			// Determine if reached maximum depth
			if (depth.intValue() >= this.descendantDepth) {
				return;
			}

			// Spawn thread state instances
			for (int i = 0; i < this.spawnBurst; i++) {
				flow.doFlow(depth.intValue() + 1, (escalation) -> {
					this.callbackCount++;
				});
			}
		}

		public void spawn(Integer depth, ReflectiveFlow flow) {
			this.spawnCount++;
			flow.doFlow(depth, null);
		}
	}

}