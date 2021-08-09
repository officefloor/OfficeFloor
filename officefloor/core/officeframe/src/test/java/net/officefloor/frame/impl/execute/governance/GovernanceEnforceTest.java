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

package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.function.ManagedFunction;
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
	 */
	public void doEnforceGovernanceTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
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

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);
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
			assertEquals("Incorrect number of extensions", 1, extensions.length);
			assertNull("Should not clean up managed object until after enforcing governance",
					extensions[0].recycledManagedObject);
			this.enforced = extensions;
		}
	}

}
