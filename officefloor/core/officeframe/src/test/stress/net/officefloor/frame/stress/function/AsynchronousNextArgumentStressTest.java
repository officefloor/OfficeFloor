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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests asynchronous next argument for the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousNextArgumentStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousNextArgumentStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the asynchronous flow invoker
		AsynchronousFlowInvoker functionality = new AsynchronousFlowInvoker(context);

		// Register the functions
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("nextTask");
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder nextTask = this.constructFunction(functionality, "nextTask");
		nextTask.buildParameter();
		nextTask.buildFlow("trigger", Integer.class, false);
		context.loadResponsibleTeam(nextTask.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", 1);
	}

	private final ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	protected void tearDown() throws Exception {
		try {
			this.executor.shutdown();
		} finally {
			super.tearDown();
		}
	}

	/**
	 * Test functionality.
	 */
	public class AsynchronousFlowInvoker {

		private final StressContext context;

		public AsynchronousFlowInvoker(StressContext context) {
			this.context = context;
		}

		public String trigger(ManagedFunctionContext<?, ?> context) {

			// Create the asynchronous flow
			AsynchronousFlow flow = context.createAsynchronousFlow();

			// Undertake asynchronous flow
			AsynchronousNextArgumentStressTest.this.executor.execute(() -> {
				flow.complete(() -> context.setNextFunctionArgument("nextArgument"));
			});

			// Next argument asynchronously available
			return null;
		}

		public void nextTask(String parameter, ReflectiveFlow flow) {

			// Determine if correct next function argument
			assertEquals("Incorrect async flow state", "nextArgument", parameter);

			// Determine if complete
			if (context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger for another
			flow.doFlow(null, null);
		}
	}

}
