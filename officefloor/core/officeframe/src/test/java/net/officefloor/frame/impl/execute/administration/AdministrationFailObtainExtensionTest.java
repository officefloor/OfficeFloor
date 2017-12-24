/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		object.enhanceMetaData = (context) -> context.addManagedObjectExtensionInterface(TestObject.class,
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
		object.enhanceMetaData = (context) -> context.addManagedObjectExtensionInterface(TestObject.class,
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
		object.enhanceMetaData = (context) -> context.addManagedObjectExtensionInterface(TestObject.class,
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
		object.enhanceMetaData = (context) -> context.addManagedObjectExtensionInterface(TestObject.class,
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