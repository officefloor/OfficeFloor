package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion} within the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalateImmediateAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion} within the {@link ManagedFunction}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();

		// Ensure handle asynchronous flow failing on managed function thread
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("triggerAsynchronousFlow", null, (error) -> escalation.value = error);
		assertSame("Incorrect escalation", work.failure, escalation.value);
	}

	public class TestWork {

		private final Throwable failure = new Throwable("TEST");

		public void triggerAsynchronousFlow(AsynchronousFlow flow) {
			flow.complete(() -> {
				// Fail on the managed function thread
				throw this.failure;
			});
		}
	}

}