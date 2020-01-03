package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure can complete {@link AsynchronousFlow} on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadedAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can complete {@link AsynchronousFlow} on another {@link Thread}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the object (ensure thread safe changes)
		TestObject object = new TestObject();
		this.constructManagedObject(object, "MO", this.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildManagedFunctionContext();
		trigger.buildObject("MO", ManagedObjectScope.THREAD);
		trigger.setNextFunction("servicingComplete");
		ReflectiveFunctionBuilder servicing = this.constructFunction(work, "servicingComplete");
		servicing.buildObject("MO");

		// Ensure completes flow
		this.triggerFunction("triggerAsynchronousFlow", null, null);
		this.waitForTrue(() -> work.isServicingComplete);
	}

	public class TestObject {
		private boolean isUpdated = false;
	}

	public class TestWork {

		private volatile boolean isServicingComplete = false;

		public void triggerAsynchronousFlow(ManagedFunctionContext<?, ?> context, TestObject object) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> flow.complete(() -> object.isUpdated = true));
		}

		public void servicingComplete(TestObject object) {
			assertTrue("Should be updated before continue from function", object.isUpdated);
			this.isServicingComplete = true;
		}
	}

}