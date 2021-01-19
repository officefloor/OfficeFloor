/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
