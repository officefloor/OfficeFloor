/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
			metaData.addManagedObjectExtension(TestObject.class, (managedObject) -> (TestObject) managedObject);
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
		object.managedObjectServiceContext.invokeProcess(0, "TEST", object, 0, null);

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
