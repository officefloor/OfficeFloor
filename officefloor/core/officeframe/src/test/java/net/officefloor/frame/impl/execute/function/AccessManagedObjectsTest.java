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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to obtain access to the various {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public class AccessManagedObjectsTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to access the {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void testProcessStateBoundManagedObject() throws Exception {
		this.doBoundManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure able to access the {@link ThreadState} bound
	 * {@link ManagedObject}.
	 */
	public void testThreadStateBoundManagedObject() throws Exception {
		this.doBoundManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure able to access the {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void testFunctionBoundManagedObject() throws Exception {
		this.doBoundManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the bound {@link ManagedObject} test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doBoundManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Create the process bound managed object
		TestObject object = new TestObject();
		this.constructManagedObject(object, "MO");

		// Construct function to access managed object
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		function.buildObject("MO", ManagedObjectScope.PROCESS);

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure object loaded
		assertSame("Should have object loaded", object, work.object);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public TestObject object = null;

		public void task(TestObject object) {
			this.object = object;
		}
	}

	/**
	 * Test {@link ManagedObject}.
	 */
	public static class TestObject {
	}

}
