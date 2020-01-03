package net.officefloor.frame.impl.execute.managedobject;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure wait on sourcing of {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class WaitOnSourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure multiple tasks can wait on the {@link ManagedObject} to be
	 * sourced.
	 */
	public void test_DelaySourceManagedObject_With_MultipleFunctionsWaiting() throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this);
		object.isDelaySource = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildFlow("spawnedTask", null, true);
		this.constructFunction(work, "spawnedTask").buildObject("MO", ManagedObjectScope.PROCESS);

		// Trigger the function
		final int numberOfFlows = 10;
		this.triggerFunction("trigger", numberOfFlows, null);

		// Ensure flows invoked (but waiting on managed object)
		assertEquals("Incorrect number of flows invoked", numberOfFlows, work.flowsInvoked);
		assertEquals("All tasks should be waiting on process bound managed object", 0, work.spawnedTasksRun);

		// Load the managed object (releasing all tasks)
		object.managedObjectUser.setManagedObject(object);

		// Ensure all spawned tasks run (with the managed object object)
		assertEquals("All tasks should be run", numberOfFlows, work.spawnedTasksRun);
		for (int i = 0; i < numberOfFlows; i++) {
			assertSame("Incorrect managed object " + i, object, work.objects.get(i));
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final List<TestObject> objects = new LinkedList<>();

		private int flowsInvoked = 0;

		private int spawnedTasksRun = 0;

		public void trigger(Integer numberOfFlows, ReflectiveFlow flow) {
			for (int i = 0; i < numberOfFlows; i++) {
				this.flowsInvoked++;
				flow.doFlow(null, null);
			}
		}

		public void spawnedTask(TestObject object) {
			this.spawnedTasksRun++;
			this.objects.add(object);
		}
	}

}