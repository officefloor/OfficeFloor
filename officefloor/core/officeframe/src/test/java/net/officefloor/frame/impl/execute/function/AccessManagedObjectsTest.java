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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

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
		TestObject object = new TestObject("MO", this);

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

}
