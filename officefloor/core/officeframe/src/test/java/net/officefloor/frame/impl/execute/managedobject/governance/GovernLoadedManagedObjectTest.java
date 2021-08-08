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
