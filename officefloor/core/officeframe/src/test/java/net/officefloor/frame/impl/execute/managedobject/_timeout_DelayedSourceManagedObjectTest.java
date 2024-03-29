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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Time out to source the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _timeout_DelayedSourceManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure time out in sourcing the {@link ManagedObject} that is bound to the
	 * {@link ProcessState}.
	 */
	@Test
	public void delaySourceManagedObject_timeOut_ProcessState() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure time out in sourcing the {@link ManagedObject} that is bound to the
	 * {@link ThreadState}.
	 */
	@Test
	public void delaySourceManagedObject_timeOut_ThreadState() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure time out in sourcing the {@link ManagedObject} that is bound to the
	 * {@link ManagedFunction}.
	 */
	@Test
	public void delaySourceManagedObject_timeOut_ManagedFunction() throws Exception {
		this.doDelaySourceManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param scope {@link ManagedObjectScope}.
	 */
	public void doDelaySourceManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Create the object
		TestObject object = new TestObject("MO", this.construct);
		object.isDelaySource = true;
		object.managedObjectBuilder.setTimeout(10);

		// Create the function
		TestWork work = new TestWork();
		this.construct.constructFunction(work, "task").buildObject("MO", scope);

		// Invoke the function
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.construct.triggerFunction("task", null, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Should not invoke the task
		assertFalse(work.isTaskInvoked, "Should be waiting on managed object");
		assertFalse(isComplete.value, "Process should not be complete");

		// Time out sourcing the managed object
		this.construct.adjustCurrentTimeMillis(100);
		officeManager.getOfficeManager(0).runAssetChecks();

		// Should propagate failure
		assertFalse(work.isTaskInvoked, "Task should not be invoked");
		assertTrue(isComplete.value, "Process should now be complete");
		assertTrue(failure.value instanceof SourceManagedObjectTimedOutEscalation, "Should propagate time out failure");
		SourceManagedObjectTimedOutEscalation timeout = (SourceManagedObjectTimedOutEscalation) failure.value;
		assertEquals(TestObject.class, timeout.getObjectType(), "Incorrect object timed out");
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
