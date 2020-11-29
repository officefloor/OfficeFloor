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

package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests {@link AsynchronousManagedObject} timing out.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _timeout_WaitOnAsynchronousManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} timed out.
	 */
	@Test
	public void asynchronousOperation_TimeOut_ProcessBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} timed out.
	 */
	@Test
	public void asynchronousOperation_TimeOut_ThreadBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} timed out.
	 */
	@Test
	public void asynchronousOperation_TimeOut_FunctionBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes test.
	 * 
	 * @param scope {@link ManagedObjectScope}.
	 */
	public void doAsynchronousOperationTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this.construct);
		object.isAsynchronousManagedObject = true;
		object.managedObjectBuilder.setTimeout(10);

		// Construct functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
		task.buildObject("MO", scope);
		task.setNextFunction("next");
		this.construct.constructFunction(work, "next").setNextFunction("await");
		ReflectiveFunctionBuilder wait = this.construct.constructFunction(work, "await");
		if (scope == ManagedObjectScope.FUNCTION) {
			wait.buildObject("MO", scope);
		} else {
			wait.buildObject("MO");
		}

		// Trigger function
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.construct.triggerFunction("task", null, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Only the task should be invoked
		assertTrue(work.isTaskInvoked, "Task should be invoked");
		assertTrue(work.isNextInvoked, "Next should be invoked, as not dependent on managed object");

		// Different object if bound to function, so not wait
		if (scope == ManagedObjectScope.FUNCTION) {
			assertTrue(isComplete.value, "Should not wait, as different object");
			assertNull(failure.value, "Should not have timeout failure");
			return;
		}

		// Other scopes should wait
		assertFalse(work.isAwaitInvoked, "Wait should be waiting on asynchronous operation");
		assertFalse(isComplete.value, "Process should not be complete");

		// Time out the asynchronous operation
		this.construct.adjustCurrentTimeMillis(100);
		this.officeManager.getOfficeManager().runAssetChecks();

		// Wait should now complete
		assertTrue(work.isNextInvoked, "Await should not be invoked due to time out");
		assertTrue(isComplete.value, "Process should be complete");
		assertTrue(failure.value instanceof ManagedObjectOperationTimedOutEscalation, "Time out failure");
		ManagedObjectOperationTimedOutEscalation escalation = (ManagedObjectOperationTimedOutEscalation) failure.value;
		assertEquals(TestObject.class, escalation.getObjectType(), "Incorrect object timed out");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		boolean isTaskInvoked = false;

		public boolean isNextInvoked = false;

		public boolean isAwaitInvoked = false;

		public void task(TestObject object) {
			this.isTaskInvoked = true;
			object.asynchronousContext.start(null);
		}

		public void next() {
			this.isNextInvoked = true;
		}

		public void await(TestObject object) {
			this.isAwaitInvoked = true;
		}
	}

}
