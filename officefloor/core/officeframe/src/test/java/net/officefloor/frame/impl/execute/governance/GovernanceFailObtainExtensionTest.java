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
 * Ensure handle failing to obtain the extension for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceFailObtainExtensionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure to obtain extension via
	 * {@link EscalationProcedure}.
	 */
	public void testGovernanceObtainExtensionFailure_handledBy_EscalationProcedure() throws Exception {

		// Construct the managed object
		Exception failure = new Exception("TEST");
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> {
					throw failure;
				});
		object.isRecycleFunction = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.buildObject("MO", ManagedObjectScope.FUNCTION).mapGovernance("GOVERNANCE");
		function.getBuilder().addGovernance("GOVERNANCE");
		this.constructFunction(work, "threadHandler").buildParameter();

		// Handle governance escalation
		this.getOfficeBuilder().addEscalation(Exception.class, "threadHandler");

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce");

		// Invoke the function (ensuring enforcement)
		this.invokeFunctionAndValidate("function", null, "threadHandler");

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);

		// Ensure escalation handled
		assertSame("Should handle escalation", failure, work.handledException);
	}

	/**
	 * Ensure handle failure to obtain extension via {@link FlowCallback}.
	 */
	public void testGovernanceObtainExtensionFailure_handledBy_Callback() throws Exception {
		// Construct the managed object
		Exception failure = new Exception("TEST");
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> {
					throw failure;
				});
		object.isRecycleFunction = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.buildObject("MO", ManagedObjectScope.FUNCTION).mapGovernance("GOVERNANCE");
		function.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce");

		// Invoke the function (ensuring enforcement)
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("function", null, (exception) -> escalation.value = exception);
		this.validateReflectiveMethodOrder();

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);

		// Ensure escalation handled
		assertSame("Should handle escalation", failure, escalation.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private Exception handledException = null;

		public void function(TestObject object) {
			fail("Should never get to invoking function");
		}

		public void threadHandler(Exception escalation) {
			this.handledException = escalation;
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		public void enforce(TestObject[] extensions) throws Exception {
			fail("Should not be enforced");
		}
	}

}
