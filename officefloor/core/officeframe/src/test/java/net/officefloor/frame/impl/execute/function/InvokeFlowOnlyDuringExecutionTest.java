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