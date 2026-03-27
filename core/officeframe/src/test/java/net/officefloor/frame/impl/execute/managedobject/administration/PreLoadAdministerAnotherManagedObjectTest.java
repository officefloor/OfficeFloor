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

package net.officefloor.frame.impl.execute.managedobject.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can apply pre-load {@link Administration} to an already loaded
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class PreLoadAdministerAnotherManagedObjectTest extends AbstractOfficeConstructTestCase {

	// ProcessState pre-load administration can not depend on managed objects

	/**
	 * Ensure can apply {@link Governance} to a {@link ThreadState} loaded
	 * {@link ManagedObject}.
	 */
	public void testPreLoadAdministerProcessManagedObject_from_ThreadBoundManagedObject() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.THREAD, ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link FunctionState} loaded
	 * {@link ManagedObject}.
	 */
	public void testPreLoadAdministerThreadManagedObject_from_FunctionBoundManagedObject() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.FUNCTION, ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link FunctionState} loaded
	 * {@link ManagedObject}.
	 */
	public void testPreLoadAdministerProcessManagedObject_from_FunctionBoundManagedObject() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.FUNCTION, ManagedObjectScope.PROCESS);
	}

	/**
	 * Undertakes the test.
	 */
	public void doPreLoadAdministerManagedObjectTest(ManagedObjectScope loadScope, ManagedObjectScope administerScope)
			throws Exception {

		// Construct the object
		TestObject load = new TestObject("MO", this);

		// Construct administered object
		TestObject administered = new TestObject("ADMINISTERED", this);
		administered.isCoordinatingManagedObject = true;
		administered.enhanceMetaData = (metaData) -> {
			metaData.addDependency(TestObject.class);
			metaData.addManagedObjectExtension(TestObject.class, (mo) -> (TestObject) mo);
		};
		DependencyMappingBuilder extension;
		switch (administerScope) {
		case PROCESS:
			extension = this.getOfficeBuilder().addProcessManagedObject("ADMINISTERED", "ADMINISTERED");
			break;
		case THREAD:
			extension = this.getOfficeBuilder().addProcessManagedObject("ADMINISTERED", "ADMINISTERED");
			break;
		default:
			fail("Illegal administered scope " + administerScope);
			return;
		}

		// Construct dependency
		TestObject dependency = new TestObject("DEPENDENCY", this);
		this.getOfficeBuilder().addProcessManagedObject("DEPENDENCY", "DEPENDENCY");
		extension.mapDependency(0, "DEPENDENCY");

		// Construct the functions
		TestWork work = new TestWork(load, administered, dependency);
		this.constructFunction(work, "function").buildObject("MO", loadScope)
				.preLoadAdminister("ADMIN", TestObject.class, () -> work).administerManagedObject("ADMINISTERED");

		// Invoke the function
		this.invokeFunctionAndValidate("function", null, "function");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork implements Administration<TestObject, None, None> {

		private final TestObject object;

		private final TestObject administered;

		private final TestObject dependency;

		private boolean isPreLoadAdministered = false;

		public TestWork(TestObject object, TestObject administered, TestObject dependency) {
			this.object = object;
			this.administered = administered;
			this.dependency = dependency;
		}

		@Override
		public void administer(AdministrationContext<TestObject, None, None> context) throws Throwable {
			assertEquals("Should be an administered managed object", 1, context.getExtensions().length);
			TestObject extension = context.getExtensions()[0];
			assertSame("Incorrect administered managed object", this.administered, extension);
			Object dependency = extension.objectRegistry.getObject(0);
			assertSame("Incorrect administered dependency", this.dependency, dependency);
			this.isPreLoadAdministered = true;
		}

		public void function(TestObject object) {
			assertSame("Incorrect object", this.object, object);
			assertTrue("Should be pre-load administered", this.isPreLoadAdministered);
		}
	}

}
