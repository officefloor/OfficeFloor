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
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to asynchronously provide the next {@link ManagedFunction}
 * argument.
 * 
 * @author Daniel Sagenschneider
 */
public class InvalidAsynchronousNextFunctionArgumentTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to asynchronously provide the next {@link ManagedFunction}
	 * argument.
	 */
	public void testAsynchronousNextFunctionArgument() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousNextFunctionArgument");
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("servicingComplete");
		ReflectiveFunctionBuilder complete = this.constructFunction(work, "servicingComplete");
		complete.buildParameter();

		// Ensure halts execution until flow completes
		this.triggerFunction("triggerAsynchronousNextFunctionArgument", null, null);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);
		assertNull("Should not be next function argument", work.nextFunctionArgument);

		// Complete flow confirming overwrite next function argument
		final String ARGUMENT = "ARGUMENT";
		work.flow.complete(() -> work.context.setNextFunctionArgument(ARGUMENT));
		assertTrue("Should be complete servicing", work.isServicingComplete);
		assertEquals("Should have next function argument", ARGUMENT, work.nextFunctionArgument);
	}

	public class TestWork {

		private ManagedFunctionContext<?, ?> context;

		private AsynchronousFlow flow;

		private boolean isServicingComplete = false;

		private Object nextFunctionArgument = null;

		public Object triggerAsynchronousNextFunctionArgument(ManagedFunctionContext<?, ?> context) {
			this.context = context;
			this.flow = context.createAsynchronousFlow();
			return null; // no return synchronously available
		}

		public void servicingComplete(Object argument) {
			this.isServicingComplete = true;
			this.nextFunctionArgument = argument;
		}
	}

}
