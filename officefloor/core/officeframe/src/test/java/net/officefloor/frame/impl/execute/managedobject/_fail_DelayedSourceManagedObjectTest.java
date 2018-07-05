/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.TestObject;

/**
 * Delayed failure to source the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_DelayedSourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can delay failure in sourcing the {@link ManagedObject} that is
	 * bound to the {@link ProcessState}.
	 */
	public void test_DelaySourceManagedObject_setFailure_ProcessState() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can delay failure in sourcing the {@link ManagedObject} that is
	 * bound to the {@link ThreadState}.
	 */
	public void test_DelaySourceManagedObject_setFailure_ThreadState() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can delay failure in sourcing the {@link ManagedObject} that is
	 * bound to the {@link ManagedFunction}.
	 */
	public void test_DelaySourceManagedObject_setFailure_ManagedFunction() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doDelaySourceManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Create the object
		TestObject object = new TestObject("MO", this);
		object.isDelaySource = true;

		// Create the function
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		// Invoke the function
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Should not invoke the task
		assertFalse("Should be waiting on managed object", work.isTaskInvoked);
		assertFalse("Process should not be complete", isComplete.value);

		// Provide the failure
		Exception exception = new Exception("TEST");
		object.managedObjectUser.setFailure(exception);

		// Should propagate failure
		assertFalse("Task should not be invoked", work.isTaskInvoked);
		assertTrue("Process should now be complete", isComplete.value);
		assertSame("Should propagate the failure", exception, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTaskInvoked = false;

		public void task(TestObject object) {
			this.isTaskInvoked = true;
		}
	}

}
