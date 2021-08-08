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

package net.officefloor.frame.impl.execute.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure able to spawn a {@link ThreadState} that recursively spawns further
 * {@link ThreadState} instances.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class SpawnDescendantThreadStatesTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure can spawn a {@link ThreadState}.
	 */
	@Test
	public void spawnDescendantThreadStates() throws Exception {

		final int DESCENDANT_DEPTH = 3;
		final int SPAWN_BURST = 10;

		// Construct functions
		TestWork work = new TestWork(DESCENDANT_DEPTH, SPAWN_BURST);
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildFlow("spawn", null, true);
		ReflectiveFunctionBuilder spawn = this.construct.constructFunction(work, "spawn");
		spawn.buildParameter();
		spawn.buildFlow("trigger", null, false);

		// Ensure spawn multiple thread states
		this.construct.invokeFunction("trigger", 0);

		// Determine the number of spawn thread states expected
		int expectedSpawnCount = 0;
		for (int i = 0; i < DESCENDANT_DEPTH; i++) {
			expectedSpawnCount = expectedSpawnCount + (int) (Math.pow(SPAWN_BURST, i + 1));
		}

		// Ensure correct number of spawned thread states
		assertEquals(expectedSpawnCount, work.spawnCount.intValue(),
				"Incorrect number of spawned thread state instances");
		assertEquals(expectedSpawnCount, work.callbackCount.intValue(), "Incorrect number of callbacks");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final int descendantDepth;

		private final int spawnBurst;

		private final AtomicInteger spawnCount = new AtomicInteger(0);

		private final AtomicInteger callbackCount = new AtomicInteger(0);

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
					this.callbackCount.incrementAndGet();
				});
			}
		}

		public void spawn(Integer depth, ReflectiveFlow flow) {
			this.spawnCount.incrementAndGet();
			flow.doFlow(depth, null);
		}
	}

}
