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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
public class AsynchronousFlowMultipleFailStressTest extends AbstractStressTestCase {

	protected int getIterationCount() {
		return 1000;
	}

	public static TestSuite suite() {
		return createSuite(AsynchronousFlowMultipleFailStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the asynchronous flow invoker
		AsynchronousFlowInvoker functionality = new AsynchronousFlowInvoker(context);

		// Register the functions
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.buildFlow("triggerAsync", null, false);
		ReflectiveFunctionBuilder triggerAsync = this.constructFunction(functionality, "triggerAsync");
		triggerAsync.buildManagedFunctionContext();
		context.loadResponsibleTeam(triggerAsync.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", 1);

		// Ensure only a single failure thrown
		context.setValidation(() -> {

			// Wait for all async flows to be triggered
			this.waitForTrue(() -> context.isComplete());

			// Ensure correct reporting of failure
			assertEquals("Should only be one failure propagated", 1, functionality.failures.size());
		});
	}

	/**
	 * Test functionality.
	 */
	public class AsynchronousFlowInvoker {

		private final StressContext context;

		private final Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

		public AsynchronousFlowInvoker(StressContext context) {
			this.context = context;
		}

		public void trigger(ReflectiveFlow flow) {
			flow.doFlow(null, (failure) -> {
				this.failures.add(failure);
			});
		}

		public void triggerAsync(ManagedFunctionContext<?, ?> mfContext) {

			// Create all the flows (to avoid failure issues)
			AsynchronousFlow[] flows = new AsynchronousFlow[this.context.getMaximumIterations()];
			for (int i = 0; i < flows.length; i++) {
				flows[i] = mfContext.createAsynchronousFlow();
			}

			// Trigger all flows in parallel
			for (int i = 0; i < flows.length; i++) {

				// Undertake asynchronous flow
				AsynchronousFlow flow = flows[i];
				mfContext.getExecutor().execute(() -> {
					flow.complete(() -> {
						this.context.incrementIterationAndIsComplete();
						throw new Exception("TEST");
					});
				});
			}
		}
	}

}
