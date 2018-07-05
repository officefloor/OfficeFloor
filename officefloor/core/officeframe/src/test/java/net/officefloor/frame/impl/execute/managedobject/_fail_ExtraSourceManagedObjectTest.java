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

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handles {@link ManagedObject} being loaded twice to the
 * {@link ManagedObjectUser}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_ExtraSourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure log failure if provided after sourcing the {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_setFailure_AfterSourced() throws Exception {

		// Construct managed object
		new TestObject("MO", this);

		// Construct function
		TestFailAfterWork work = new TestFailAfterWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION);
		task.buildParameter();

		// Invoke the function
		this.invokeFunction("task", new Exception("TEST"));
		assertTrue("Ensure the task was invoked", work.isTaskInvoked);
	}

	/**
	 * Test functionality.
	 */
	public class TestFailAfterWork {

		public boolean isTaskInvoked = false;

		public void task(TestObject object, Throwable failure) {
			this.isTaskInvoked = true;

			// Load the failure (after sourcing the managed object)
			String log = _fail_ExtraSourceManagedObjectTest.this
					.captureLoggerOutput(() -> object.managedObjectUser.setFailure(failure));
			assertTrue("Should log the failure", log.contains(failure.getMessage()));
		}
	}

	/**
	 * Ensure log failure if provided after failure.
	 */
	public void test_SourceManagedObject_setFailure_AfterUnload() throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		object.isDelaySource = true;

		// Construct function
		TestAfterWork work = new TestAfterWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Invoke the function
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> failure.value = escalation);

		// Fail the source
		Exception first = new Exception("FIRST");
		assertNull("Should not have failed process", failure.value);
		object.managedObjectUser.setFailure(first);
		assertSame("Should fail to source", first, failure.value);

		// Fail again
		Exception second = new Exception("SECOND");
		String log = this.captureLoggerOutput(() -> object.managedObjectUser.setFailure(second));
		assertTrue("Should log second failure", log.contains(second.getMessage()));
		assertSame("Should still be failure on first failure", first, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestAfterWork {

		public void task(TestObject object) {
		}
	}

}
