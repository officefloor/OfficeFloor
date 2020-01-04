/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.managedobject.recycle;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure the {@link ManagedObject} is recycled.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectRecycleTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can recycle {@link ManagedObject} bound to {@link ProcessState}.
	 */
	public void testRecycleManaged_boundTo_ProcessState() throws Exception {
		this.doRecycleTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can recycle {@link ManagedObject} bound to {@link ThreadState}.
	 */
	public void testRecycleManaged_boundTo_ThreadState() throws Exception {
		this.doRecycleTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can recycle {@link ManagedObject} bound to
	 * {@link ManagedFunction}.
	 */
	public void testRecycleManaged_boundTo_FunctionState() throws Exception {
		this.doRecycleTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can enforce the {@link Governance}.
	 */
	public void doRecycleTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isRecycleFunction = true;

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", scope);

		// Invoke the function (ensuring enforcement)
		this.invokeFunctionAndValidate("task", null, "task");

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
		}
	}

}
