package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import net.officefloor.frame.api.managedobject.AsynchronousOperation;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can run {@link AsynchronousOperation},
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousOperationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can run {@link AsynchronousOperation}.
	 */
	public void testAsynchronousOperation() throws Exception {

		// Construct managed objects
		TestObject object = new TestObject("MO", this);
		object.isAsynchronousManagedObject = true;
		object.managedObjectBuilder.setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.PROCESS);

		// Test
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.triggerFunction("task", null, (escalation) -> {
			assertTrue("Asynchronous operation complete before process completes", work.isCompleted);
			complete.run(escalation);
		});

		// Ensure asynchronous operations undertaken
		assertTrue("Should have started asynchronous operation", work.isStarted);
		assertTrue("Should have completed asynchronous operation", work.isCompleted);
		complete.assertComplete();
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isStarted = false;

		private boolean isCompleted = false;

		public void task(TestObject object) {

			// Undertake asynchronous operation
			object.asynchronousContext.start(() -> {
				this.isStarted = true;

				// Complete asynchronous operation
				object.asynchronousContext.complete(() -> {
					assertTrue("Should be started", this.isStarted);
					this.isCompleted = true;
				});
			});
		}
	}

}