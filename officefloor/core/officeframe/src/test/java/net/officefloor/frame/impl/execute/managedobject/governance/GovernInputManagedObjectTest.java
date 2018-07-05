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
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure source {@link ManagedObject} respects established {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
public class GovernInputManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can apply {@link Governance} to input {@link ManagedObject}.
	 */
	public void testGovernInputManagedObject() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(String.class);
			metaData.addManagedObjectExtension(TestObject.class,
					(managedObject) -> (TestObject) managedObject);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO").mapGovernance("GOVERNANCE");
		object.managingOfficeBuilder.linkFlow(0, "task");

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO");
		task.buildParameter();
		task.getBuilder().addGovernance("GOVERNANCE");

		// Construct the governance
		ReflectiveGovernanceBuilder govern = this.constructGovernance(work, "GOVERNANCE");
		govern.register("register");
		govern.enforce("enforce");

		// Open the OfficeFloor
		this.constructOfficeFloor().openOfficeFloor();

		// Input the managed object
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		object.managedObjectExecuteContext.invokeProcess(0, "TEST", object, 0, null);

		// Ensure invoked functions
		this.validateReflectiveMethodOrder("register", "task", "enforce");
		assertSame("Incorrect input managed object", object, work.registeredObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private TestObject registeredObject = null;

		public void register(TestObject extension) {
			assertNull("Should only register once", this.registeredObject);
			this.registeredObject = extension;
		}

		public void task(TestObject object, String parameter) {
			assertSame("Object should be under governance", object, this.registeredObject);
			assertEquals("Incorrect parameter", "TEST", parameter);
		}

		public void enforce(TestObject[] extensions) {
		}
	}

}