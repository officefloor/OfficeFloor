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
package net.officefloor.frame.impl.execute.managedobject.processaware;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Tests the {@link ProcessAwareManagedObject} being used on the non-main
 * {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class NotMainThreadStateProcessAwareManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to undertake {@link ProcessSafeOperation} for a
	 * {@link ProcessAwareManagedObject} bound to the {@link ProcessState}.
	 */
	public void testNotMainThreadState_ProcessAwareManagedObject_boundTo_Process() throws Exception {
		this.doNotMainThreadState_ProcessAwareManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure able to undertake {@link ProcessSafeOperation} for a
	 * {@link ProcessAwareManagedObject} bound to the {@link ThreadState}.
	 */
	public void testNotMainThreadState_ProcessAwareManagedObject_boundTo_Thread() throws Exception {
		this.doNotMainThreadState_ProcessAwareManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure able to undertake {@link ProcessSafeOperation} for a
	 * {@link ProcessAwareManagedObject} bound to the {@link ManagedFunction}.
	 */
	public void testNotMainThreadState_ProcessAwareManagedObject_boundTo_Function() throws Exception {
		this.doNotMainThreadState_ProcessAwareManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure able to undertake {@link ProcessSafeOperation}.
	 */
	public void doNotMainThreadState_ProcessAwareManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isProcessAwareManagedObject = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.buildFlow("task", null, true);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO");
		this.bindManagedObject("MO", scope, task.getBuilder());

		// Invoke the function
		this.invokeFunction("spawn", null);
		assertTrue("Function should be invoked", work.isTaskRun);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTaskRun = false;

		public void spawn(ReflectiveFlow flow) {
			flow.doFlow(null, null);
		}

		public void task(TestObject object) {

			// Obtain the process aware context
			ProcessAwareContext context = object.processAwareContext;
			assertNotNull("Should have process aware context", context);

			// Ensure run
			int value = context.run(() -> 1);
			assertEquals("Incorrect return value", 1, value);

			// Indicate task run
			this.isTaskRun = true;
		}
	}

}