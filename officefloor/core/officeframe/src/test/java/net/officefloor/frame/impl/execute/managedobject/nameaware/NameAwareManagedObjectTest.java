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
package net.officefloor.frame.impl.execute.managedobject.nameaware;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link NameAwareManagedObject} is loaded within its bound name.
 *
 * @author Daniel Sagenschneider
 */
public class NameAwareManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can obtain bound {@link ManagedObject} name when bound to
	 * {@link ProcessState}.
	 */
	public void testNameAwareManagedObject_boundTo_Process() throws Exception {
		this.doNameAwareManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can obtain bound {@link ManagedObject} name when bound to
	 * {@link ThreadState}.
	 */
	public void testNameAwareManagedObject_boundTo_Thread() throws Exception {
		this.doNameAwareManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can obtain bound {@link ManagedObject} name when bound to
	 * {@link ManagedFunction}.
	 */
	public void testNameAwareManagedObject_boundTo_Function() throws Exception {
		this.doNameAwareManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can obtain bound {@link ManagedObject} name.
	 */
	private void doNameAwareManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isNameAwareManagedObject = true;

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		// Ensure invoke function
		this.invokeFunction("task", null);

		// Ensure appropriate bound name
		assertEquals("Incorrect bound name", "MO", object.boundManagedObjectName);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			assertEquals("Should have name bound", "MO", object.boundManagedObjectName);
		}
	}

}