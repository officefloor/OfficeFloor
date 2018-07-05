/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
	 * {@link Flow} may only be invoked during execution of
	 * {@link FunctionState}.
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
			assertEquals("Can not invoke flow outside function/callback execution (state: COMPLETED)", ex.getMessage());
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