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
