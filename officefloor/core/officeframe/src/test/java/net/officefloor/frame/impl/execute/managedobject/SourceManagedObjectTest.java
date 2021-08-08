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

package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
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
