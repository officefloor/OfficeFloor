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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests {@link FlowCallback} is invoked on completion of a {@link Flow}.
 *
 * @author Daniel Sagenschneider
 */
public class FlowCallbackExceptionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Test non-spawned {@link FlowCallback}.
	 */
	public void testCallback() throws Exception {
		this.doCallbackTest(false);
	}

	/**
	 * Test spawned {@link ThreadState} {@link FlowCallback}.
	 */
	public void testSpawnedThreadStateCallback() throws Exception {
		this.doCallbackTest(true);
	}

	/**
	 * Ensure {@link FlowCallback} is invoked on completion of the {@link Flow}.
	 */
	public void doCallbackTest(boolean isSpawnThreadState) throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildFlow("flow", null, isSpawnThreadState);
		this.constructFunction(work, "flow");

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure callback is invoked
		assertTrue("Should invoke callback", work.isCallbackInvoked);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {
		
		private final Exception exception = new Exception("TEST");

		private boolean isFlowInvoked = false;

		private boolean isCallbackInvoked = false;

		public void task(ReflectiveFlow flow) {
			assertFalse("Flow should not be invoked", this.isFlowInvoked);
			flow.doFlow(null, new FlowCallback() {
				@Override
				public void run(Throwable escalation) throws Throwable {
					assertTrue("Flow should be invoked before callback", TestWork.this.isFlowInvoked);
					assertFalse("Should invoke callback only once", TestWork.this.isCallbackInvoked);
					TestWork.this.isCallbackInvoked = true;
					assertSame("Should be passed escalation of flow", TestWork.this.exception, escalation);
				}
			});
		}

		public void flow() throws Exception {
			assertFalse("Should invoke flow only once", this.isFlowInvoked);
			this.isFlowInvoked = true;
			throw this.exception;
		}
	}

}
