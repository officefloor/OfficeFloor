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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can source the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class SourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can source the {@link ProcessState} bound {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_BoundTo_ProcessState() throws Exception {
		this.doSourceManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can source the {@link ProcessState} bound {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_BoundTo_ThreadState() throws Exception {
		this.doSourceManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can source the {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_BoundTo_MangedFunction() throws Exception {
		this.doSourceManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the source {@link ManagedObject} test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	private void doSourceManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);

		// Construct task
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		// Invoke function
		this.invokeFunction("task", null);

		// Ensure source the managed object
		assertSame("Should source the managed object", object, work.object);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public TestObject object;

		public void task(TestObject object) {
			this.object = object;
		}
	}

}