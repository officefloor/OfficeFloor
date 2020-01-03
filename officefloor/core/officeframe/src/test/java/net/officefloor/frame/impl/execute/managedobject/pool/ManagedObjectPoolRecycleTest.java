package net.officefloor.frame.impl.execute.managedobject.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestManagedObject;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handle lost {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolRecycleTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TestManagedObject}.
	 */
	private TestObject object;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Load the managed object
		this.object = new TestObject("MO", this, true);
		this.object.isRecycleFunction = true;

		// Construct the function
		this.constructFunction(new TestWork(), "task").buildObject("MO", ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can re-use {@link ManagedObject}.
	 */
	public void testReuseManagedObject() throws Exception {
		this.object.isRecycle = true;
		this.invokeFunction("task", null);
		assertSame("Managed object should be re-used", object, object.pooledReturnedManagedObject);
	}

	/**
	 * Ensure can handle lost {@link ManagedObject} due to not re-use.
	 */
	public void testLostManagedObjectDueToNonReuse() throws Exception {
		this.object.isRecycle = false;
		this.invokeFunction("task", null);
		assertSame("Managed object should be lost", object, object.pooledLostManagedObject);
		assertNull("Should be no cause of lost, as just not re-used", object.pooledLostCause);
	}

	/**
	 * Ensure can handle lost {@link ManagedObject} due to recycle failure.
	 */
	public void testLostManagedObjectDueToRecycleFailure() throws Exception {
		this.object.recycleFailure = new Exception("RECYCLE_FAILURE");
		this.invokeFunction("task", null);
		assertSame("Managed object should be lost", object, object.pooledLostManagedObject);
		assertSame("Incorrect cause of lost managed object", this.object.recycleFailure, object.pooledLostCause);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {
		public void task(TestObject object) {
		}
	}

}