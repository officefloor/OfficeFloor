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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;

/**
 * Ensure can invoke a {@link Flow} only during execution of
 * {@link FunctionState}.
 * 
 * @author Daniel Sagenschneider
 */
public class InvokeFlowOnlyDuringExecutionTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link Flow} may only be invoked during execution of {@link FunctionState}.
	 */
	public void testOnlyInvokeFlowDuringExecution() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildFlow("flow", null, false);
		this.constructFunction(work, "flow");

		// Test
		this.invokeFunction("task", null);

		// Ensure flows invoked in correct states
		assertEquals("Flows allowed to be invoked in appropriate state", 3, work.flowCount);

		// Ensure not able to invoke flow within invalid state
		try {
			work.flow.doFlow(null, null);
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Can not invoke flow outside function/callback execution (state: COMPLETED, function: task)",
					ex.getMessage());
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private ReflectiveFlow flow;

		private int flowCount = 0;

		public void task(ReflectiveFlow flow) {
			this.flow = flow;
			flow.doFlow(null, (escalationOne) -> {
				flow.doFlow(null, (escalationTwo) -> {
					flow.doFlow(null, null);
				});
			});
		}

		public void flow() {
			this.flowCount++;
		}
	}

}
