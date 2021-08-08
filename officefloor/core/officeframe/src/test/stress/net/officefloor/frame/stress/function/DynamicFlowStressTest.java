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
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure dynamically invoking {@link ManagedFunction} instances is stress
 * tested.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicFlowStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(DynamicFlowStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 1000000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildManagedFunctionContext();
		trigger.buildParameter();
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder dynamic = this.constructFunction(work, "dynamic");
		dynamic.buildFlow("trigger", null, false);
		dynamic.buildParameter();
		context.loadResponsibleTeam(dynamic.getBuilder());

		// Trigger
		context.setInitialFunction("trigger", 1);
	}

	/**
	 * Test functionality
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void trigger(ManagedFunctionContext<?, ?> context, Integer iteration) throws Exception {
			// Invoke the dynamic flow
			context.doFlow("dynamic", iteration, null);
		}

		public void dynamic(ReflectiveFlow flow, Integer iteration) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete(iteration)) {
				return;
			}

			// Trigger again
			flow.doFlow(iteration.intValue() + 1, null);
		}
	}

}
