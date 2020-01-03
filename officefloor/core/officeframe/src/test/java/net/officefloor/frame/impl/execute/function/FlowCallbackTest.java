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
public class FlowCallbackTest extends AbstractOfficeConstructTestCase {

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
				}
			});
		}

		public void flow() {
			assertFalse("Should invoke flow only once", this.isFlowInvoked);
			this.isFlowInvoked = true;
		}
	}

}
