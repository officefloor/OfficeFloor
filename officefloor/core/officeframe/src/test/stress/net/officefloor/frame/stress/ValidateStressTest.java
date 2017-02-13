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
package net.officefloor.frame.stress;

import java.util.concurrent.atomic.AtomicInteger;

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
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadResponsibleTeam(task.getBuilder());
		task.buildParameter();
		task.buildFlow("flow", null, false);
		this.constructFunction(work, "flow");

		// Indicate start point
		context.setInitialFunction("task", this.getIterationCount());

		// Validate correct
		context.setValidation(
				() -> assertEquals("Incorrect number of iterations", this.getIterationCount(), work.invocations.get()));
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private AtomicInteger invocations = new AtomicInteger(0);

		public void task(Integer iterations, ReflectiveFlow flow) {
			for (int i = 0; i < iterations; i++) {
				flow.doFlow(null, null);
			}
		}

		public void flow() {
			invocations.incrementAndGet();
		}
	}

}