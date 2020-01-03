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
