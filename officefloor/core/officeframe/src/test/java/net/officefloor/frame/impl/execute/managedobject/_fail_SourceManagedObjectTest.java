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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link Escalation} occurs for failing to source the
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_SourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can handle failure to source the {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_setFailure_ProcessState() throws Exception {
		this.doFailSourceManagedObjectTest(ManagedObjectScope.PROCESS, false);
	}

	/**
	 * Ensure can handle thrown failure to source the {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_throwFailure_ProcessState() throws Exception {
		this.doFailSourceManagedObjectTest(ManagedObjectScope.PROCESS, true);
	}

	/**
	 * Ensure can handle failure to source the {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_setFailure_ThreadState() throws Exception {
		this.doFailSourceManagedObjectTest(ManagedObjectScope.THREAD, false);
	}

	/**
	 * Ensure can handle thrown failure to source the {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_throwFailure_ThreadState() throws Exception {
		this.doFailSourceManagedObjectTest(ManagedObjectScope.THREAD, true);
	}

	/**
	 * Ensure can handle failure to source the {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_setFailure_MangedFunction() throws Exception {
		this.doFailSourceManagedObjectTest(ManagedObjectScope.FUNCTION, false);
	}

	/**
	 * Ensure can handle thrown failure to source the {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_throwFailure_MangedFunction() throws Exception {
		this.doFailSourceManagedObjectTest(ManagedObjectScope.FUNCTION, true);
	}

	/**
	 * Undertakes the source {@link ManagedObject} test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	private void doFailSourceManagedObjectTest(ManagedObjectScope scope, boolean isPropagateFailure) throws Exception {

		RuntimeException failure = new RuntimeException("TEST");

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		if (isPropagateFailure) {
			object.sourcePropagateFailure = failure;
		} else {
			object.sourceFailure = failure;
		}

		// Construct task
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		try {
			// Invoke function
			this.invokeFunction("task", null);
			fail("Should not be successful");

		} catch (Exception ex) {
			assertSame("Incorrect source failure", failure, ex);
		}

		// Task should not be invoked
		assertFalse("Task should not be invoked due to sourcing managed object failure", work.isTaskInvoked);
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