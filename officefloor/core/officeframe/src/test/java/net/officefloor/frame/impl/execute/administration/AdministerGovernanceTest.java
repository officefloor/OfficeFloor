/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link Administration} can manually manage {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
public class AdministerGovernanceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can manually enforce {@link Governance} on {@link ManagedObject}
	 * bound to {@link ProcessState}.
	 */
	public void testManuallyEnforceGovernanceOnFunction_boundTo_Process() throws Exception {
		this.doManuallyEnforceGovernanceOnFunctionTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can manually enforce {@link Governance} on {@link ManagedObject}
	 * bound to {@link ThreadState}.
	 */
	public void testManuallyEnforceGovernanceOnFunction_boundTo_Thread() throws Exception {
		this.doManuallyEnforceGovernanceOnFunctionTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can manually enforce {@link Governance} on {@link ManagedObject}
	 * bound to {@link ManagedFunctionContainer}.
	 */
	public void testManuallyEnforceGovernanceOnFunction_boundTo_Function() throws Exception {
		this.doManuallyEnforceGovernanceOnFunctionTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can manually enforce {@link Governance}.
	 */
	public void doManuallyEnforceGovernanceOnFunctionTest(ManagedObjectScope scope) throws Exception {

		// Manually manage governance
		this.getOfficeBuilder().setManuallyManageGovernance(true);

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO");
		this.bindManagedObject("MO", scope, task.getBuilder()).mapGovernance("GOVERNANCE");

		// Construct governance
		TestGovernance governance = new TestGovernance(object);
		this.constructGovernance(governance, "GOVERNANCE").enforce("enforce");

		// Construct the administration
		task.preAdminister("preTask").buildGovernance("GOVERNANCE");
		task.postAdminister("postTaskEnforce").buildGovernance("GOVERNANCE");

		// Invoke the function
		this.invokeFunctionAndValidate("task", null, "preTask", "task", "postTaskEnforce", "enforce");

		// Ensure the managed object is governed
		assertEquals("Incorrect number of extensions", 1, governance.extensions.length);
		assertSame("Incorrect extension", object, governance.extensions[0]);
		assertSame("Should have clean up managed object", object, object.recycledManagedObject);
	}

	/**
	 * Ensure can manually disregard {@link Governance} on {@link ManagedObject}
	 * bound to {@link ProcessState}.
	 */
	public void testManuallyDisregardGovernanceOnFunction_boundTo_Process() throws Exception {
		this.doManuallyDisregardGovenranceOnFunctionTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can manually disregard {@link Governance} on {@link ManagedObject}
	 * bound to {@link ThreadState}.
	 */
	public void testManuallyDisregardGovernanceOnFunction_boundTo_Thread() throws Exception {
		this.doManuallyDisregardGovenranceOnFunctionTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can manually disregard {@link Governance} on {@link ManagedObject}
	 * bound to {@link ManagedFunctionContainer}.
	 */
	public void testManuallyDisregardGovernanceOnFunction_boundTo_Function() throws Exception {
		this.doManuallyDisregardGovenranceOnFunctionTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can manually disregard {@link Governance}.
	 */
	public void doManuallyDisregardGovenranceOnFunctionTest(ManagedObjectScope scope) throws Exception {

		// Manually manage governance
		this.getOfficeBuilder().setManuallyManageGovernance(true);

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO");
		this.bindManagedObject("MO", scope, task.getBuilder()).mapGovernance("GOVERNANCE");

		// Construct governance
		TestGovernance governance = new TestGovernance(object);
		this.constructGovernance(governance, "GOVERNANCE").disregard("disregard");

		// Construct the administration
		task.preAdminister("preTask").buildGovernance("GOVERNANCE");
		task.postAdminister("postTaskDisregard").buildGovernance("GOVERNANCE");

		// Invoke the function
		this.invokeFunctionAndValidate("task", null, "preTask", "task", "postTaskDisregard", "disregard");

		// Ensure the managed object is governed
		assertEquals("Incorrect number of extensions", 1, governance.extensions.length);
		assertSame("Incorrect extension", object, governance.extensions[0]);
		assertSame("Should have clean up managed object", object, object.recycledManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void preTask(TestObject[] extensions, GovernanceManager governance) {
			governance.activateGovernance();
		}

		public void task(TestObject object) {
		}

		public void postTaskEnforce(TestObject[] extensions, GovernanceManager governance) {
			governance.enforceGovernance();
		}

		public void postTaskDisregard(TestObject[] extensions, GovernanceManager governance) {
			governance.disregardGovernance();
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private final TestObject object;

		public TestObject[] extensions;

		public TestGovernance(TestObject object) {
			this.object = object;
		}

		public void enforce(TestObject[] extensions) {
			this.extensions = extensions;
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
		}

		public void disregard(TestObject[] extensions) {
			this.extensions = extensions;
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
		}
	}

}
