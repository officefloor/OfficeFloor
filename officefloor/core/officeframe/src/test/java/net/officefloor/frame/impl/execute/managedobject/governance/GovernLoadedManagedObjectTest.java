/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.managedobject.governance;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can apply {@link Governance} to an already loaded
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class GovernLoadedManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can apply {@link Governance} to a {@link ProcessState} loaded
	 * {@link ManagedObject}.
	 */
	public void testGovernLoadedManagedObject_boundTo_Process() throws Exception {
		this.doGovernLoadedManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link ThreadState} loaded
	 * {@link ManagedObject}.
	 */
	public void testGovernLoadedManagedObject_boundTo_Thread() throws Exception {
		this.doGovernLoadedManagedObjectTest(ManagedObjectScope.THREAD);
	}

	// Note: function bound always loads new ManagedObject

	/**
	 * Undertakes the test.
	 */
	public void doGovernLoadedManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> metaData.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		this.bindManagedObject("MO", scope, null).mapGovernance("GOVERNANCE");

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder beforeGovernanceTask = this.constructFunction(work, "beforeGovernanceTask");
		beforeGovernanceTask.buildObject("MO");
		beforeGovernanceTask.setNextFunction("underGovernanceTask");
		ReflectiveFunctionBuilder underGovernanceTask = this.constructFunction(work, "underGovernanceTask");
		underGovernanceTask.buildObject("MO");
		underGovernanceTask.getBuilder().addGovernance("GOVERNANCE");

		// Construct the governance
		ReflectiveGovernanceBuilder govern = this.constructGovernance(work, "GOVERNANCE");
		govern.register("register");
		govern.enforce("enforce");

		// Invoke the functions
		this.invokeFunctionAndValidate("beforeGovernanceTask", null, "beforeGovernanceTask", "register",
				"underGovernanceTask", "enforce");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private TestObject registeredObject = null;

		public void beforeGovernanceTask(TestObject object) {
			assertNull("Obtain should not be registered for governance", this.registeredObject);
		}

		public void register(TestObject extension) {
			assertNull("Object should only be registered once", this.registeredObject);
			this.registeredObject = extension;
		}

		public void underGovernanceTask(TestObject object) {
			assertSame("Object should be under governance", this.registeredObject, object);
		}

		public void enforce(TestObject[] extensions) {
		}
	}

}
