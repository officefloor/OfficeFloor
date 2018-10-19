/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.managedobject.pool;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure the {@link ManagedObjectPool} is emptied on closing the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class EmptyManagedObjectPoolTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ManagedObjectPool} is emptied on close of {@link OfficeFloor}.
	 */
	public void testEnsureEmptyPoolOnClose() throws Exception {

		// Create the object pooled
		TestObject object = new TestObject("MO", this, true);

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Construct function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.buildObject("MO", ManagedObjectScope.THREAD);

		// Build the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Invoke function
		officeFloor.getOffice(officeName).getFunctionManager("function").invokeProcess(null, null);
		assertSame("Should provide object", object, work.object);
		assertSame("Should recycle managed object", object, object.pooledReturnedManagedObject);
		assertFalse("Pool should not be emptied", object.poolEmptied);

		// Close OfficeFloor and ensure empty pool
		officeFloor.closeOfficeFloor();
		assertTrue("On closing, should empty the pool", object.poolEmptied);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private TestObject object;

		public void function(TestObject object) {
			this.object = object;
		}
	}

}