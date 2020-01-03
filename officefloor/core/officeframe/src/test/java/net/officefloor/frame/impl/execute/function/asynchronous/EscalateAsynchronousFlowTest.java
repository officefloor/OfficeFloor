package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalateAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "servicingComplete");

		// Ensure halts execution until flow completes
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("triggerAsynchronousFlow", null, (error) -> escalation.value = error);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);
		assertNull("Should be no escalation", escalation.value);

		// Complete with failure
		final Exception exception = new Exception("TEST");
		work.flow.complete(() -> {
			throw exception;
		});
		assertSame("Incorrect escalation", exception, escalation.value);
		assertFalse("Should not complete servicing", work.isServicingComplete);
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