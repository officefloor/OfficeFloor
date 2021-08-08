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

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to create {@link AsynchronousFlow} within {@link FlowCallback}.
 * 
 * @author Daniel Sagenschneider
 */
public class CallbackAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to create {@link AsynchronousFlow} within {@link FlowCallback}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildFlow("flow", null, false);
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "flow");
		this.constructFunction(work, "servicingComplete");

		// Ensure halts execution until flow completes
		this.triggerFunction("triggerAsynchronousFlow", null, null);
		assertTrue("Should execute the flow", work.isFlowComplete);
		assertFalse("Should halt on async flow in callback and not complete servicing", work.isServicingComplete);

		// Complete flow confirming completes
		work.asyncFlow.complete(null);
		assertTrue("Should be complete servicing", work.isServicingComplete);
	}

	public class TestWork {

		private boolean isFlowComplete = false;

		private boolean isServicingComplete = false;

		private AsynchronousFlow asyncFlow;

		public void triggerAsynchronousFlow(ReflectiveFlow flow, ManagedFunctionContext<?, ?> context) {
			flow.doFlow(null, (escalation) -> {
				this.asyncFlow = context.createAsynchronousFlow();
			});
		}

		public void flow() {
			this.isFlowComplete = true;
		}

		public void servicingComplete() {
			this.isServicingComplete = true;
		}
	}

}
