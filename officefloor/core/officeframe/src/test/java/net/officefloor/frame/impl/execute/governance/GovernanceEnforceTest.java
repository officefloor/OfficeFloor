/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can enforce the {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
public class GovernanceEnforceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can enforce {@link Governance} on a {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	public void testEnforceGovernance_boundTo_ProcessState() throws Exception {
		
		fail("TODO find out why hanging");
		
		this.doEnforceGovernanceTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can enforce {@link Governance} on a {@link ManagedObject} bound to
	 * {@link ThreadState}.
	 */
	public void testEnforceGovernance_boundTo_ThreadState() throws Exception {
		this.doEnforceGovernanceTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can enforce {@link Governance} on a {@link ManagedObject} bound to
	 * {@link ManagedFunction}.
	 */
	public void testEnforceGovernance_boundTo_FunctionState() throws Exception {
		this.doEnforceGovernanceTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can enforce the {@link Governance}.
	 * 
	 */
	public void doEnforceGovernanceTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtensionInterface(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", scope).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce");

		// Invoke the function (ensuring enforcement)
		this.invokeFunctionAndValidate("task", null, "task", "enforce");

		// Ensure the managed object extension is enforced
		assertEquals("Incorrect number of extensions", 1, govern.enforced.length);
		assertSame("Incorrect extension", object, govern.enforced[0]);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private TestObject[] enforced;

		public void enforce(TestObject[] extensions) {
			this.enforced = extensions;
		}
	}

}