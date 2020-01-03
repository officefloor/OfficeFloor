package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can provide <code>pre</code> and <code>post</code>
 * {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public class UnloadManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the {@link ManagedFunction} unloads the {@link ManagedObject}.
	 */
	public void testPreAdministrationWithFunctionUnloadingManagedObject() throws Exception {

		// Create the object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;

		// Create the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION);
		task.setNextFunction("next");
		this.constructFunction(work, "next");

		// Create administration
		task.preAdminister("preTask");

		// Test
		this.invokeFunctionAndValidate("task", null, "preTask", "task", "next");

		// Ensure managed object instantiated and recycled
		assertSame("Managed object should be recycled", object, object.recycledManagedObject);
	}

	/**
	 * Ensure the last {@link Administration} unloads the {@link ManagedObject}.
	 */
	public void testPostAdministrationUnloadingManagedObject() throws Exception {

		// Create the object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;

		// Create the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION);
		task.setNextFunction("next");
		this.constructFunction(work, "next");

		// Create administration
		task.postAdminister("postTask");

		// Test
		this.invokeFunctionAndValidate("task", null, "task", "postTask", "next");

		// Ensure managed object instantiated and recycled
		assertSame("Managed object should be recycled", object, object.recycledManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private TestObject object = null;

		public void preTask(Object[] extensions) {
		}

		public void task(TestObject object) {
			this.object = object;
			assertNull("Object should not yet be unloaded", this.object.recycledManagedObject);
		}

		public void postTask(Object[] extensions) {
			assertNull("Object should not yet be unloaded", this.object.recycledManagedObject);
		}

		public void next() {
			assertSame("Should have unloaded the managed object", this.object, this.object.recycledManagedObject);
		}
	}

}