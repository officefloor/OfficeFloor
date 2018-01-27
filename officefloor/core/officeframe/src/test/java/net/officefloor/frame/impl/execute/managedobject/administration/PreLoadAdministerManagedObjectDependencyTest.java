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
public class PreLoadAdministerManagedObjectDependencyTest extends AbstractOfficeConstructTestCase {

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
		administered.enhanceMetaData = (metaData) -> metaData.addManagedObjectExtension(TestObject.class,
				(mo) -> (TestObject) mo);
		switch (administerScope) {
		case PROCESS:
			this.getOfficeBuilder().addProcessManagedObject("ADMINISTERED", "ADMINISTERED");
			break;
		case THREAD:
			this.getOfficeBuilder().addProcessManagedObject("ADMINISTERED", "ADMINISTERED");
			break;
		default:
			fail("Illegal administered scope " + administerScope);
		}

		// Construct the functions
		TestWork work = new TestWork(load, administered);
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

		private boolean isPreLoadAdministered = false;

		public TestWork(TestObject object, TestObject administered) {
			this.object = object;
			this.administered = administered;
		}

		@Override
		public void administer(AdministrationContext<TestObject, None, None> context) throws Throwable {
			assertEquals("Should be an administered managed object", 1, context.getExtensions().length);
			TestObject extension = context.getExtensions()[0];
			assertSame("Incorrect administered managed object", this.administered, extension);
			this.isPreLoadAdministered = true;
		}

		public void function(TestObject object) {
			assertSame("Incorrect object", this.object, object);
			assertTrue("Should be pre-load administered", this.isPreLoadAdministered);
		}
	}

}
