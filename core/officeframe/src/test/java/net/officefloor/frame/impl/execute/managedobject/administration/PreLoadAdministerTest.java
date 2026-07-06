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
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensure can apply pre-load {@link Administration} to an already loaded
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class PreLoadAdministerTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure can apply {@link Governance} to a {@link ProcessState} loaded
	 * {@link ManagedObject}.
	 */
	@Test
	public void administerManagedObject_boundTo_Process() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link ThreadState} loaded
	 * {@link ManagedObject}.
	 */
	@Test
	public void administerManagedObject_boundTo_Thread() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link FunctionState} loaded
	 * {@link ManagedObject}.
	 */
	@Test
	public void administerManagedObject_boundTo_Function() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the test.
	 */
	public void doPreLoadAdministerManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this.construct);

		// Construct the functions
		TestWork work = new TestWork(object);
		this.construct.constructFunction(work, "function").buildObject("MO", scope).preLoadAdminister("ADMIN", TestObject.class,
				() -> work);

		// Invoke the function
		this.construct.invokeFunctionAndValidate("function", null, "function");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork implements Administration<TestObject, None, None> {

		private final TestObject object;

		private boolean isPreLoadAdministered = false;

		public TestWork(TestObject object) {
			this.object = object;
		}

		@Override
		public void administer(AdministrationContext<TestObject, None, None> context) throws Throwable {
			assertNull(context.getManagedFunctionAnnotations(), "Managed Object administration so should be no annotations");
			assertEquals(0, context.getExtensions().length, "Should be no administered managed objects");
			this.isPreLoadAdministered = true;
		}

		public void function(TestObject object) {
			assertSame(this.object, object, "Incorrect object");
			assertTrue(this.isPreLoadAdministered, "Should be pre-load administered");
		}
	}

}
