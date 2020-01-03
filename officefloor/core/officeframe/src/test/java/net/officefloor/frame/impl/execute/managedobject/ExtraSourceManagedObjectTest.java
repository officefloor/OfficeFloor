package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handles {@link ManagedObject} being loaded twice to the
 * {@link ManagedObjectUser}.
 *
 * @author Daniel Sagenschneider
 */
public class ExtraSourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure return {@link ManagedObject} if second provided.
	 */
	public void test_SourceManagedObject_Twice() throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;

		// Construct function
		TestTwiceWork work = new TestTwiceWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Invoke the function
		this.invokeFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public class TestTwiceWork {

		public void task(TestObject object) {

			// Load the object again
			object.managedObjectUser.setManagedObject(object);

			// Should recycled second immediately
			assertSame("Should recycle second managed object", object, object.recycledManagedObject);
		}
	}

	/**
	 * Ensure return {@link ManagedObject} if already failed.
	 */
	public void test_SourceManagedObject_AfterFailure() throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;
		object.sourceFailure = new Exception("TEST");

		// Construct function
		TestAfterWork work = new TestAfterWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Invoke the function
		this.triggerFunction("task", null, (escalation) -> {
			// Attempt to set a managed object now that unloaded
			object.recycledManagedObject = null;
			object.managedObjectUser.setManagedObject(object);
		});
		assertSame("Should recycle managed object immediately", object, object.recycledManagedObject);
	}

	/**
	 * Ensure return {@link ManagedObject} if second provided after unload.
	 */
	public void test_SourceManagedObject_AfterUnload() throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;

		// Construct function
		TestAfterWork work = new TestAfterWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure unloaded
		assertSame("Should be recycled on completion of process", object, object.recycledManagedObject);

		// Attempt to set a managed object now that unloaded
		object.recycledManagedObject = null;
		object.managedObjectUser.setManagedObject(object);
		assertSame("Should recycle managed object immediately", object, object.recycledManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestAfterWork {

		public void task(TestObject object) {
		}
	}

}
