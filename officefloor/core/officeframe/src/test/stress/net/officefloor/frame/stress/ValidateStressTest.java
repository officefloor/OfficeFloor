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
