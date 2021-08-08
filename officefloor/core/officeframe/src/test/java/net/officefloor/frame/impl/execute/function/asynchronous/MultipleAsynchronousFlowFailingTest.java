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

package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates only the first {@link Escalation} from any
 * {@link AsynchronousFlowCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleAsynchronousFlowFailingTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates only the first {@link Escalation} from any
	 * {@link AsynchronousFlowCompletion}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork(10);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlows");
		trigger.buildManagedFunctionContext();
		trigger.buildFlow("servicingComplete", null, false);
		this.constructFunction(work, "servicingComplete");

		// Ensure halts execution until flow completes
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("triggerAsynchronousFlows", null, (error) -> escalation.value = error);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);
		assertNull("Should be no escalation", escalation.value);

		// Complete first with failure
		final Exception exception = new Exception("TEST");
		work.flows[0].complete(() -> {
			throw exception;
		});
		assertSame("Incorrect escalation", exception, escalation.value);
		assertFalse("Should not complete servicing", work.isServicingComplete);

		// Complete the remaining flows (ensuring all ignored, as failed)
		for (int i = 1; i < work.flows.length; i++) {
			final int flowIndex = i;
			work.flows[flowIndex].complete(() -> {
				switch (flowIndex % 3) {
				case 0:
					throw new Exception("TEST FAILURE " + flowIndex);

				case 1:
					work.flow.doFlow(null, null);
					break;

				case 2:
					work.context.setNextFunctionArgument("IGNORED");
					break;
				}
			});
		}

		// Ensure no changes
		assertSame("Should be first escalation only", exception, escalation.value);
		assertFalse("Should continue to not complete servicing", work.isServicingComplete);
	}

	public class TestWork {

		private final int asyncFlowCount;

		private boolean isServicingComplete = false;

		private ManagedFunctionContext<?, ?> context;

		private ReflectiveFlow flow;

		private AsynchronousFlow[] flows;

		private TestWork(int asyncFlowCount) {
			this.asyncFlowCount = asyncFlowCount;
		}

		public void triggerAsynchronousFlows(ManagedFunctionContext<?, ?> context, ReflectiveFlow flow) {
			this.context = context;
			this.flow = flow;
			this.flows = new AsynchronousFlow[this.asyncFlowCount];
			for (int i = 0; i < this.flows.length; i++) {
				this.flows[i] = context.createAsynchronousFlow();
			}
		}

		public void servicingComplete() {
			this.isServicingComplete = true;
		}
	}

}
