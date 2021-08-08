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

package net.officefloor.frame.stress.function;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests invoking a next {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class NextFunctionStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(NextFunctionStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the next task invoker
		NextTaskInvoker functionality = new NextTaskInvoker(context);

		// Register the next tasks
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.buildParameter();
		trigger.setNextFunction("nextTask");
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder nextTask = this.constructFunction(functionality, "nextTask");
		nextTask.buildParameter();
		nextTask.buildFlow("trigger", Integer.class, false);
		context.loadResponsibleTeam(nextTask.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", 1);
	}

	/**
	 * Test functionality.
	 */
	public class NextTaskInvoker {

		private final StressContext context;

		public NextTaskInvoker(StressContext context) {
			this.context = context;
		}

		public Integer trigger(Integer count) {
			return count;
		}

		public void nextTask(Integer callCount, ReflectiveFlow flow) {

			assertEquals("Incorrect iteration", callCount.intValue(), this.context.incrementIteration());

			// Determine if complete
			if (callCount.intValue() >= context.getMaximumIterations()) {
				return;
			}

			// Trigger for another next task
			flow.doFlow(callCount.intValue() + 1, null);
		}
	}

}
