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

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Tests flow for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Governance} can invoke a {@link Flow}.
	 */
	public void testGovernanceFlow() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");
		task.setNextFunction("next");
		this.constructFunction(work, "flow");
		this.constructFunction(work, "next");

		// Construct the govenance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce").buildFlow("flow", null, false);

		// Undertake functionality
		this.invokeFunctionAndValidate("task", null, "task", "enforce", "flow", "next");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
		}

		public void flow() {
		}

		public void next() {
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		public void enforce(TestObject[] extensions, ReflectiveFlow flow) {
			flow.doFlow(null, null);
		}
	}

}
