package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadedEscalateAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildManagedFunctionContext();

		// Ensure provides exception
		try {
			this.invokeFunction("triggerAsynchronousFlow", null);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Incorrect exception", FAILURE, ex);
		}
	}

	private static final Exception FAILURE = new Exception("TEST");

	public class TestWork {

		public void triggerAsynchronousFlow(ManagedFunctionContext<?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> flow.complete(() -> {
				throw FAILURE;
			}));
		}
	}

}