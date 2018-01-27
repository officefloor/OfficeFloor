/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.managedobject.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can apply pre-load {@link Administration} to an already loaded
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class PreLoadAdministerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can apply {@link Governance} to a {@link ProcessState} loaded
	 * {@link ManagedObject}.
	 */
	public void testAdministerManagedObject_boundTo_Process() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link ThreadState} loaded
	 * {@link ManagedObject}.
	 */
	public void testAdministerManagedObject_boundTo_Thread() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can apply {@link Governance} to a {@link FunctionState} loaded
	 * {@link ManagedObject}.
	 */
	public void testAdministerManagedObject_boundTo_Function() throws Exception {
		this.doPreLoadAdministerManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the test.
	 */
	public void doPreLoadAdministerManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this);

		// Construct the functions
		TestWork work = new TestWork(object);
		this.constructFunction(work, "function").buildObject("MO", scope).preLoadAdminister("ADMIN", TestObject.class,
				() -> work);

		// Invoke the function
		this.invokeFunctionAndValidate("function", null, "function");
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
			assertEquals("Should be no administered managed objects", 0, context.getExtensions().length);
			this.isPreLoadAdministered = true;
		}

		public void function(TestObject object) {
			assertSame("Incorrect object", this.object, object);
			assertTrue("Should be pre-load administered", this.isPreLoadAdministered);
		}
	}

}
