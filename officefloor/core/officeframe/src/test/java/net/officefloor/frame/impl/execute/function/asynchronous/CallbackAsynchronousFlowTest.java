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
