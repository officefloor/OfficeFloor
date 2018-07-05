/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.stress;

import junit.framework.TestSuite;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Simple test to validate the {@link AbstractStressTestCase}.
 *
 * @author Daniel Sagenschneider
 */
public class ValidateStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ValidateStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the function
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.buildFlow("flow", null, false);
		context.loadOtherTeam(task.getBuilder());
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		context.loadResponsibleTeam(flow.getBuilder());

		// Indicate start point
		context.setInitialFunction("task", context.getMaximumIterations());
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(Integer iterations, ReflectiveFlow flow) {
			for (int i = 0; i < iterations; i++) {
				flow.doFlow(null, null);
			}
		}

		public void flow() {
			this.context.incrementIteration();
		}
	}

}