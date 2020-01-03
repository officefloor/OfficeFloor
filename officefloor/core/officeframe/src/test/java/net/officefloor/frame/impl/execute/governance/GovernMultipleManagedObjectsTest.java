package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can provide {@link Governance} over multiple {@link ManagedObject}
 * instances.
 *
 * @author Daniel Sagenschneider
 */
public class GovernMultipleManagedObjectsTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to provide {@link Governance} over multiple
	 * {@link ManagedObject} instances.
	 */
	public void testGovernMultipleManagedObjects() throws Exception {

		// Construct the managed objects
		TestObject functionObject = new TestObject("F_MO", this);
		functionObject.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		TestObject threadObject = new TestObject("T_MO", this);
		threadObject.enhanceMetaData = functionObject.enhanceMetaData;
		TestObject processObject = new TestObject("P_MO", this);
		processObject.enhanceMetaData = functionObject.enhanceMetaData;

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("F_MO", ManagedObjectScope.FUNCTION).mapGovernance("GOVERNANCE");
		task.buildObject("T_MO", ManagedObjectScope.THREAD).mapGovernance("GOVERNANCE");
		task.buildObject("P_MO", ManagedObjectScope.PROCESS).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Construct the governance
		TestGovernance governance = new TestGovernance();
		ReflectiveGovernanceBuilder govern = this.constructGovernance(governance, "GOVERNANCE");
		govern.enforce("enforce");

		// Invoke the function
		this.invokeFunctionAndValidate("task", null, "task", "enforce");

		// Ensure appropriate extensions
		assertEquals("Incorrect number of extensions", 3, governance.extensions.length);
		assertEquals("Incorrect first extension", functionObject, governance.extensions[0]);
		assertEquals("Incorrect second extension", threadObject, governance.extensions[1]);
		assertEquals("Incorrect third extension", processObject, governance.extensions[2]);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject functionObject, TestObject threadObject, TestObject processObject) {
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		public TestObject[] extensions;

		public void enforce(TestObject[] extensions) {
			this.extensions = extensions;
		}
	}

}
