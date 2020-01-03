package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to halt execution until {@link AsynchronousFlow} completes.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to halt execution until {@link AsynchronousFlow} completes.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "servicingComplete");

		// Ensure halts execution until flow completes
		this.triggerFunction("triggerAsynchronousFlow", null, null);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);

		// Complete flow confirming completes flow
		work.flow.complete(null);
		assertTrue("Should be complete servicing", work.isServicingComplete);
	}

	public class TestWork {

		private boolean isServicingComplete = false;

		private AsynchronousFlow flow;

		public void triggerAsynchronousFlow(AsynchronousFlow flow) {
			this.flow = flow;
		}

		public void servicingComplete() {
			this.isServicingComplete = true;
		}
	}

}