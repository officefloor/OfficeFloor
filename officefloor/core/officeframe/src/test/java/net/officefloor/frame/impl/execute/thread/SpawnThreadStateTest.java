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
