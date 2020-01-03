package net.officefloor.frame.impl.execute.function.asynchronous;

import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link Executor} runs on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousExecutorTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Executor} runs on another {@link Thread}.
	 */
	public void testExecutorOnAnotherThread() throws Exception {

		// Capture the current invoking thread
		Thread currentThread = Thread.currentThread();

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerExecutor");
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "servicingComplete");

		// Run the function
		this.invokeFunction("triggerExecutor", null);

		// Should complete servicing
		assertTrue("Should complete servicing", work.isComplete);

		// Executor thread should be different
		assertNotEquals("Executor should be invoked by different thread", currentThread, work.executorThread);
	}

	public class TestWork {

		private volatile Thread executorThread = null;

		private volatile boolean isComplete = false;

		public void triggerExecutor(ManagedFunctionContext<?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				this.executorThread = Thread.currentThread();
				flow.complete(null);
			});
		}

		public void servicingComplete() {
			assertNotNull("Should have executor thread", this.executorThread);
			this.isComplete = true;
		}
	}

}