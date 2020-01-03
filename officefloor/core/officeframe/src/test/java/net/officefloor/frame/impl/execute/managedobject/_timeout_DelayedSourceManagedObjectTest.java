package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.TestObject;

/**
 * Time out to source the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _timeout_DelayedSourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure time out in sourcing the {@link ManagedObject} that is bound to
	 * the {@link ProcessState}.
	 */
	public void test_DelaySourceManagedObject_timeOut_ProcessState() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure time out in sourcing the {@link ManagedObject} that is bound to
	 * the {@link ThreadState}.
	 */
	public void test_DelaySourceManagedObject_timeOut_ThreadState() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure time out in sourcing the {@link ManagedObject} that is bound to
	 * the {@link ManagedFunction}.
	 */
	public void test_DelaySourceManagedObject_timeOut_ManagedFunction() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doDelaySourceManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Create the object
		TestObject object = new TestObject("MO", this);
		object.isDelaySource = true;
		object.managedObjectBuilder.setTimeout(10);

		// Create the function
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		// Invoke the function
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		Office office = this.triggerFunction("task", null, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Should not invoke the task
		assertFalse("Should be waiting on managed object", work.isTaskInvoked);
		assertFalse("Process should not be complete", isComplete.value);

		// Time out sourcing the managed object
		this.adjustCurrentTimeMillis(100);
		office.runAssetChecks();

		// Should propagate failure
		assertFalse("Task should not be invoked", work.isTaskInvoked);
		assertTrue("Process should now be complete", isComplete.value);
		assertTrue("Should propagate time out failure", failure.value instanceof SourceManagedObjectTimedOutEscalation);
		SourceManagedObjectTimedOutEscalation timeout = (SourceManagedObjectTimedOutEscalation) failure.value;
		assertEquals("Incorrect object timed out", TestObject.class, timeout.getObjectType());
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTaskInvoked = false;

		public void task(TestObject object) {
			this.isTaskInvoked = true;
		}
	}

}
