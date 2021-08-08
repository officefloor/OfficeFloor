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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadSafeClosure;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ThreadedEscalateAsynchronousFlowTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion}.
	 */
	@Test
	public void asynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildManagedFunctionContext();

		// Trigger the function
		ThreadSafeClosure<Throwable> capture = new ThreadSafeClosure<>();
		this.construct.triggerFunction("triggerAsynchronousFlow", null, (escalation) -> {
			capture.set(escalation);
		});

		// Ensure provide escalation
		Throwable escalation = capture.waitAndGet();
		assertSame(FAILURE, escalation, "Incorrect exception");
	}

	private static final RuntimeException FAILURE = new RuntimeException("TEST");

	public class TestWork {

		public void triggerAsynchronousFlow(ManagedFunctionContext<?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> flow.complete(() -> {
				throw FAILURE;
			}));
		}
	}

}
