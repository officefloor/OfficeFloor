/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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