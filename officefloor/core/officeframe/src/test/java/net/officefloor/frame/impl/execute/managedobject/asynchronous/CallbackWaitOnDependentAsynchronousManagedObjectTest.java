package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link FlowCallback} waits for a dependent
 * {@link AsynchronousManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class CallbackWaitOnDependentAsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	public void test_Callback_WaitOn_AsynchronousDependentProcessBound() throws Exception {
		this.doAsynchronousCallbackTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	public void test_Callback_WaitOn_AsynchronousDependentThreadBound() throws Exception {
		this.doAsynchronousCallbackTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject}
	 * stops {@link FlowCallback} until {@link AsynchronousContext} flags
	 * completion.
	 */
	public void test_Callback_WaitOn_AsynchronousDependentFunctionBound() throws Exception {
		this.doAsynchronousCallbackTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doAsynchronousCallbackTest(ManagedObjectScope scope) throws Exception {

		// Construct the asynchronous managed object
		String childName = "ASYNCHRONOUS";
		TestObject asynchronous = new TestObject(childName, this);
		asynchronous.isAsynchronousManagedObject = true;
		asynchronous.managedObjectBuilder.setTimeout(10);
		this.getOfficeBuilder().addProcessManagedObject(childName, childName);

		// Construct the used managed object
		TestObject used = new TestObject("COORDINATE", this);
		used.isCoordinatingManagedObject = true;
		used.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);

		// Construct functions
		TestWork work = new TestWork(asynchronous);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("COORDINATE", scope).mapDependency(0, childName);
		task.buildFlow("spawn", null, true);
		this.constructFunction(work, "spawn");

		// Trigger function
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.triggerFunction("task", null, complete);

		// The callback should not be invoked
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertTrue("Spawn should be invoked", work.isSpawnInvoked);
		assertFalse("Callback should be awaiting", work.isCallbackInvoked);
		complete.assertNotComplete();

		// Complete the asynchronous operation
		asynchronous.asynchronousContext.complete(null);

		// Callback should now be invoked
		assertTrue("Callback should now complete", work.isCallbackInvoked);
		complete.assertComplete();
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject dependency;

		public boolean isTaskInvoked = false;

		public boolean isSpawnInvoked = false;

		public boolean isCallbackInvoked = false;

		public TestWork(TestObject dependency) {
			this.dependency = dependency;
		}

		public void task(TestObject object, ReflectiveFlow spawn) {
			this.isTaskInvoked = true;
			this.dependency.asynchronousContext.start(null);

			spawn.doFlow(this.dependency, (escalation) -> {
				this.isCallbackInvoked = true;
			});
		}

		public void spawn() {
			this.isSpawnInvoked = true;
		}
	}

}