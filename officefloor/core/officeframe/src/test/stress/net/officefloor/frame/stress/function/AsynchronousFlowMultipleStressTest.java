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
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests {@link AsynchronousFlow} for the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowMultipleStressTest extends AbstractStressTestCase {

	protected int getIterationCount() {
		return 1000;
	}

	public static TestSuite suite() {
		return createSuite(AsynchronousFlowMultipleStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the asynchronous flow invoker
		AsynchronousFlowInvoker functionality = new AsynchronousFlowInvoker(context);

		// Register the functions
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.buildManagedFunctionContext();
		trigger.buildFlow("nextTask", null, false);
		context.loadResponsibleTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder nextTask = this.constructFunction(functionality, "nextTask");
		context.loadOtherTeam(nextTask.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", 1);
	}

	/**
	 * Test functionality.
	 */
	public class AsynchronousFlowInvoker {

		private final StressContext context;

		public AsynchronousFlowInvoker(StressContext context) {
			this.context = context;
		}

		public void trigger(ManagedFunctionContext<?, ?> mfContext, ReflectiveFlow nextTask) {

			// Trigger all flows in parallel
			for (int i = 0; i < this.context.getMaximumIterations(); i++) {

				// Undertake asynchronous flow
				AsynchronousFlow flow = mfContext.createAsynchronousFlow();
				mfContext.getExecutor().execute(() -> {
					flow.complete(() -> nextTask.doFlow(null, null));
				});
			}
		}

		public void nextTask() {
			this.context.incrementIterationAndIsComplete();
		}
	}

}
