/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.stress.thread;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests running the same {@link ManagedFunction} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(SpawnThreadStateStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 10000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildFlow("spawn", null, true);
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.buildFlow("trigger", null, false);
		context.loadResponsibleTeam(spawn.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", null);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void trigger(ReflectiveFlow spawn) {

			// Spawn thread state
			spawn.doFlow(null, (escalation) -> {
			});
		}

		public void spawn(ReflectiveFlow flow) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Spawn again
			flow.doFlow(null, null);
		}
	}

}