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
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion} within the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalateImmediateAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion} within the {@link ManagedFunction}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();

		// Ensure handle asynchronous flow failing on managed function thread
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("triggerAsynchronousFlow", null, (error) -> escalation.value = error);
		assertSame("Incorrect escalation", work.failure, escalation.value);
	}

	public class TestWork {

		private final Throwable failure = new Throwable("TEST");

		public void triggerAsynchronousFlow(AsynchronousFlow flow) {
			flow.complete(() -> {
				// Fail on the managed function thread
				throw this.failure;
			});
		}
	}

}
