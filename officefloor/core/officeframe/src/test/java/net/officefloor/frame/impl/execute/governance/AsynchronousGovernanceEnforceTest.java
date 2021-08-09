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

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can enforce the {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
public class AsynchronousGovernanceEnforceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can enforce {@link Governance} invoking {@link AsynchronousFlow}.
	 */
	public void testAsynchronousGovernance() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.THREAD).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce").buildAsynchronousFlow();

		// Invoke the function
		Closure<Throwable> escalation = new Closure<>();
		Closure<Boolean> isComplete = new Closure<>(false);
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		this.triggerFunction("task", null, (error) -> {
			escalation.value = error;
			isComplete.value = true;
		});
		assertNull("Should be no escalation: " + escalation.value, escalation.value);
		assertFalse("Should not be complete", isComplete.value);

		// Ensure can complete
		govern.flow.complete(null);
		assertNull("Should complete with no escalation: " + escalation.value, escalation.value);
		assertTrue("Should be complete", isComplete.value);
		this.validateReflectiveMethodOrder("task", "enforce");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			// Testing
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private AsynchronousFlow flow;

		public void enforce(TestObject[] extensions, AsynchronousFlow flow) {
			this.flow = flow;
		}
	}

}
