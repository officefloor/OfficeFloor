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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.EscalationProcedure;
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
public class _fail_GovernanceEnforceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure enforce {@link Governance} failure handled by
	 * {@link EscalationProcedure}.
	 */
	public void testEnforceGovernanceFailure_handledBy_EscalationProcedure() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");
		task.getBuilder().addEscalation(Exception.class, "functionHandler");
		this.constructFunction(work, "functionHandler").buildParameter();
		this.constructFunction(work, "threadHandler").buildParameter();

		// Handle governance escalation
		this.getOfficeBuilder().addEscalation(Exception.class, "threadHandler");

		// Provide governance
		TestGovernance govern = new TestGovernance(new Exception("TEST"));
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce");

		// Invoke the function (ensuring enforcement)
		this.invokeFunctionAndValidate("task", null, "task", "enforce", "threadHandler");

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);

		// Ensure escalation handled
		assertSame("Should handle escalation", govern.exception, work.handledException);
	}

	/**
	 * Ensure enforce {@link Governance} failure handled by
	 * {@link FlowCallback}.
	 */
	public void testEnforceGovernanceFailure_handledBy_Callback() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		TestGovernance govern = new TestGovernance(new Exception("TEST"));
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce");

		// Invoke the function (ensuring enforcement)
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> failure.value = escalation);
		this.validateReflectiveMethodOrder("task", "enforce");

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);

		// Ensure escalation handled
		assertSame("Should handle escalation", govern.exception, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private Exception handledException = null;

		public void task(TestObject object) {
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
		}

		public void functionHandler(Exception escalation) {
			fail("Should not be handled by function");
		}

		public void threadHandler(Exception escalation) {
			this.handledException = escalation;
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private final Exception exception;

		public TestGovernance(Exception exception) {
			this.exception = exception;
		}

		public void enforce(TestObject[] extensions) throws Exception {
			throw this.exception;
		}
	}

}
