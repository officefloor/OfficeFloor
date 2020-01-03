package net.officefloor.frame.impl.execute.managedobject.contextaware;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handle failure on loading {@link ManagedObjectContext} to
 * {@link ContextAwareManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_ContextAwareManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure on loading name to {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void testFailSetName_boundTo_Process() throws Exception {
		this.doFailSetNameTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure handle failure on loading name to {@link ThreadState} bound
	 * {@link ManagedObject}.
	 */
	public void testFailSetName_boundTo_Thread() throws Exception {
		this.doFailSetNameTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure handle failure on loading name to {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void testFailSetName_boundTo_Function() throws Exception {
		this.doFailSetNameTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can obtain bound {@link ManagedObject} name.
	 */
	private void doFailSetNameTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isContextAwareManagedObject = true;
		object.contextAwareFailure = new RuntimeException("TEST");

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		// Ensure invoke function
		try {
			this.invokeFunction("task", null);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Incorrect cause", object.contextAwareFailure, ex);
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			fail("Should not be invoked as fails to set name");
		}
	}

}