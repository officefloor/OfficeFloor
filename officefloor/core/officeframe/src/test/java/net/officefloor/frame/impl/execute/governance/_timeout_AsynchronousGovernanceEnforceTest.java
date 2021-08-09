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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure timeout on {@link AsynchronousFlow} for the {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _timeout_AsynchronousGovernanceEnforceTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure timeout on {@link AsynchronousFlow} invoking {@link Governance}.
	 */
	@Test
	public void asynchronousGovernance() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this.construct);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.THREAD).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.construct.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce").buildAsynchronousFlow();
		governance.getBuilder().setAsynchronousFlowTimeout(10);

		// Invoke the function
		Closure<Throwable> escalation = new Closure<>();
		this.construct.triggerFunction("task", null, (error) -> escalation.value = error);
		assertNull(escalation.value, "Should be no escalation: " + escalation.value);

		// Ensure timeout governance
		this.construct.adjustCurrentTimeMillis(100);
		officeManager.getOfficeManager(0).runAssetChecks();

		// Ensure time out
		assertTrue(escalation.value instanceof AsynchronousFlowTimedOutEscalation,
				"Should timeout: " + escalation.value);
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

		public void enforce(TestObject[] extensions, AsynchronousFlow flow) {
			// Testing
		}
	}

}
