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

package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveAdministrationBuilder;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handle failing to obtain the extension for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationFailObtainExtensionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure to obtain extension via
	 * {@link EscalationProcedure}.
	 */
	public void testPreAdministrationObtainExtensionFailure_handledBy_EscalationProcedure() throws Exception {

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
		function.buildObject("MO", ManagedObjectScope.FUNCTION);
		this.constructFunction(work, "handle").buildParameter();

		// Construct the administration
		ReflectiveAdministrationBuilder admin = function.preAdminister("preFunction");
		admin.administerManagedObject("MO");

		// Handle governance escalation
		this.getOfficeBuilder().addEscalation(Exception.class, "handle");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("function", null, "handle");
		assertNull("Should not invoke function", work.functionObject);
		assertSame("Incorrect handle exception", failure, work.handledException);
	}

	/**
	 * Ensure handle failure to obtain extension via {@link FlowCallback}.
	 */
	public void testPreAdministrationObtainExtensionFailure_handledBy_Callback() throws Exception {

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
		function.buildObject("MO", ManagedObjectScope.FUNCTION);

		// Construct the administration
		ReflectiveAdministrationBuilder admin = function.preAdminister("preFunction");
		admin.administerManagedObject("MO");

		// Ensure undertakes administration before
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("function", null, (exception) -> escalation.value = exception);
		this.validateReflectiveMethodOrder();

		// Ensure handle failure
		assertNull("Should not invoke function", work.functionObject);
		assertSame("Incorrect handle exception", failure, escalation.value);
	}

	/**
	 * Ensure handle failure to obtain extension via
	 * {@link EscalationProcedure}.
	 */
	public void testPostAdministrationObtainExtensionFailure_handledBy_EscalationProcedure() throws Exception {

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
		function.buildObject("MO", ManagedObjectScope.FUNCTION);
		this.constructFunction(work, "handle").buildParameter();

		// Construct the administration
		ReflectiveAdministrationBuilder admin = function.postAdminister("postFunction");
		admin.administerManagedObject("MO");

		// Handle governance escalation
		this.getOfficeBuilder().addEscalation(Exception.class, "handle");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("function", null, "function", "handle");
		assertSame("Should invoke function", object, work.functionObject);
		assertSame("Incorrect handle exception", failure, work.handledException);
	}

	/**
	 * Ensure handle failure to obtain extension via {@link FlowCallback}.
	 */
	public void testPostAdministrationObtainExtensionFailure_handledBy_Callback() throws Exception {

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
		function.buildObject("MO", ManagedObjectScope.FUNCTION);

		// Construct the administration
		ReflectiveAdministrationBuilder admin = function.postAdminister("postFunction");
		admin.administerManagedObject("MO");

		// Ensure undertakes administration before
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("function", null, (exception) -> escalation.value = exception);
		this.validateReflectiveMethodOrder("function");

		// Ensure handle failure
		assertSame("Should invoke function", object, work.functionObject);
		assertSame("Incorrect handle exception", failure, escalation.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private Exception handledException = null;

		private TestObject functionObject = null;

		public void preFunction(Object[] extensions) throws Exception {
			fail("Should not run pre-administration");
		}

		public void function(TestObject object) {
			this.functionObject = object;
		}

		public void postFunction(Object[] extensions) throws Exception {
			fail("Should not run post-administration");
		}

		public void handle(Exception exception) {
			this.handledException = exception;
		}
	}

}
