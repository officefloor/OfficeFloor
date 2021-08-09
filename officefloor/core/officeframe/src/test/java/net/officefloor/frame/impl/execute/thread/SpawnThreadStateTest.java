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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure able to spawn a {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class SpawnThreadStateTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure can spawn a {@link ThreadState}.
	 */
	@Test
	public void spawnThreadState() throws Exception {

		// Construct functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "trigger");
		trigger.buildFlow("spawn", null, true);
		this.construct.constructFunction(work, "spawn");

		// Ensure spawn thread state
		this.construct.invokeFunctionAndValidate("trigger", null, "trigger", "spawn");

		// Ensure appropriate threading
		assertEquals(Thread.currentThread(), work.triggerThread, "Should trigger on invoking thread");
		assertNotNull(work.spawnThread, "Should have spawn thread");
		assertNotEquals(work.triggerThread, work.spawnThread, "Should be different thread when spawning");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private volatile Thread triggerThread;

		private volatile Thread spawnThread;

		public void trigger(ReflectiveFlow flow) {
			this.triggerThread = Thread.currentThread();
			flow.doFlow(null, null);
		}

		public void spawn() {

			// Spawn thread should be managed
			assertTrue(ManagedExecutionFactoryImpl.isCurrentThreadManaged(), "Spawned thread should be managed");

			// Capture spawn thread
			this.spawnThread = Thread.currentThread();
		}
	}

}
