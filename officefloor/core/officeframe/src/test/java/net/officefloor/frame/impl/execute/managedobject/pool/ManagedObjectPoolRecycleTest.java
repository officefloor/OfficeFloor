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

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestManagedObject;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handle lost {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolRecycleTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TestManagedObject}.
	 */
	private TestObject object;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Load the managed object
		this.object = new TestObject("MO", this, true);
		this.object.isRecycleFunction = true;

		// Construct the function
		this.constructFunction(new TestWork(), "task").buildObject("MO", ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can re-use {@link ManagedObject}.
	 */
	public void testReuseManagedObject() throws Exception {
		this.object.isRecycle = true;
		this.invokeFunction("task", null);
		assertSame("Managed object should be re-used", object, object.pooledReturnedManagedObject);
	}

	/**
	 * Ensure can handle lost {@link ManagedObject} due to not re-use.
	 */
	public void testLostManagedObjectDueToNonReuse() throws Exception {
		this.object.isRecycle = false;
		this.invokeFunction("task", null);
		assertSame("Managed object should be lost", object, object.pooledLostManagedObject);
		assertNull("Should be no cause of lost, as just not re-used", object.pooledLostCause);
	}

	/**
	 * Ensure can handle lost {@link ManagedObject} due to recycle failure.
	 */
	public void testLostManagedObjectDueToRecycleFailure() throws Exception {
		this.object.recycleFailure = new Exception("RECYCLE_FAILURE");
		this.invokeFunction("task", null);
		assertSame("Managed object should be lost", object, object.pooledLostManagedObject);
		assertSame("Incorrect cause of lost managed object", this.object.recycleFailure, object.pooledLostCause);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {
		public void task(TestObject object) {
		}
	}

}
