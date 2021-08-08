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
 * Ensure able to spawn multiple {@link ThreadState} instances.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class SpawnMutlipleThreadStatesTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure can spawn a {@link ThreadState}.
	 */
	@Test
	public void spawnMultipleThreadStates() throws Exception {

		final int SPAWN_COUNT = 10;

		// Construct functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildFlow("spawn", null, true);
		this.construct.constructFunction(work, "spawn");

		// Ensure spawn multiple thread states
		this.construct.invokeFunction("trigger", SPAWN_COUNT);
		assertEquals(SPAWN_COUNT, work.spawnCount.get(), "Incorrect number of spawn threads");
		assertEquals(SPAWN_COUNT, work.callbackCount.get(), "Incorrect number of callbacks");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final AtomicInteger spawnCount = new AtomicInteger(0);

		private final AtomicInteger callbackCount = new AtomicInteger(0);

		public void trigger(Integer spawnCount, ReflectiveFlow flow) {
			for (int i = 0; i < spawnCount.intValue(); i++) {
				flow.doFlow(null, (escalation) -> {
					this.callbackCount.incrementAndGet();
				});
			}
		}

		public void spawn() {
			this.spawnCount.incrementAndGet();
		}
	}

}
