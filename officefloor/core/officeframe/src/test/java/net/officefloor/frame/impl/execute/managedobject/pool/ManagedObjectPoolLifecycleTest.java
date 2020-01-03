package net.officefloor.frame.impl.execute.managedobject.pool;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensures the {@link ManagedObject} goes through appropriate life-cycle.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolLifecycleTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure appropriate life-cycle of {@link ManagedFunction} bounds
	 * {@link ManagedObject}.
	 */
	public void testFunctionBoundManagedObjectPooledLifeCycle() throws Exception {
		this.doManagedObjectPooledLifeCycleTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure appropriate life-cycle of {@link ThreadState} bound
	 * {@link ManagedObject}.
	 */
	public void testThreadStateBoundManagedObjectPooledLifeCycle() throws Exception {
		this.doManagedObjectPooledLifeCycleTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure appropriate life-cycle of {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void testProcessStateBoundManagedObjectPooledLifeCycle() throws Exception {
		this.doManagedObjectPooledLifeCycleTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Undertakes the {@link ManagedObject} life-cycle.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope} to bind the {@link ManagedObject}.
	 */
	public void doManagedObjectPooledLifeCycleTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject lifeCycle = new TestObject("MO", this, true);

		// Construct the functionality
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		function.buildObject("MO", scope);

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure life-cycle respected for managed object
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertSame("Incorrect source managed object", lifeCycle, lifeCycle.pooledSourcedManagedObject);
		assertSame("Incorrect object", lifeCycle, work.lifeCycle);
		assertSame("Incorrect returned managed object", lifeCycle, lifeCycle.pooledReturnedManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		public boolean isTaskInvoked = false;

		public TestObject lifeCycle = null;

		public void task(TestObject lifeCycle) {
			this.isTaskInvoked = true;
			this.lifeCycle = lifeCycle;
		}
	}

}