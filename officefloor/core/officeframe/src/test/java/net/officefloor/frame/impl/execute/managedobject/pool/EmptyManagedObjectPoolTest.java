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
