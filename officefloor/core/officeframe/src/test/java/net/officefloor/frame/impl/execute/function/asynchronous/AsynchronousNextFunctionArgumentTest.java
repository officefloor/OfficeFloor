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
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure must provide correct type of next {@link ManagedFunction} argument
 * asynchronously.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousNextFunctionArgumentTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure must provide correct type of next {@link ManagedFunction} argument
	 * asynchronously.
	 */
	public void testInvalidAsynchronousNextFunctionArgument() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousNextFunctionArgument");
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("servicingComplete");
		ReflectiveFunctionBuilder complete = this.constructFunction(work, "servicingComplete");
		complete.buildParameter();

		// Capture invalid argument
		Closure<Throwable> invalidArgument = new Closure<>();
		this.getOfficeFloorBuilder().setEscalationHandler((escalation) -> invalidArgument.value = escalation);

		// Ensure halts execution until flow completes
		this.triggerFunction("triggerAsynchronousNextFunctionArgument", null, null);

		// Attempt to complete with illegal argument
		work.flow.complete(() -> work.context.setNextFunctionArgument(1));

		// Ensure escalation invalid argument
		assertTrue("Should have invalid argument escalation: " + invalidArgument.value,
				invalidArgument.value instanceof IllegalArgumentException);
		assertEquals("Incorrect cause",
				"Next expecting " + String.class.getName() + " (provided " + Integer.class.getName() + ")",
				invalidArgument.value.getMessage());

	}

	public class TestWork {

		private ManagedFunctionContext<?, ?> context;

		private AsynchronousFlow flow;

		public String triggerAsynchronousNextFunctionArgument(ManagedFunctionContext<?, ?> context) {
			this.context = context;
			this.flow = context.createAsynchronousFlow();
			return null; // no return synchronously available
		}

		public void servicingComplete(String argument) {
			fail("Should never be invoked");
		}
	}

}
