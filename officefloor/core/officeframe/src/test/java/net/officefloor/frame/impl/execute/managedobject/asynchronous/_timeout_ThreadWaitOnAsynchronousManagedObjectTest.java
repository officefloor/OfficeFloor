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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure re-use of {@link ProcessState} bound {@link AsynchronousManagedObject}
 * causes re-use to wait on the asynchronous operation time out.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _timeout_ThreadWaitOnAsynchronousManagedObjectTest {

	public final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure {@link ThreadState} waits on re-use of
	 * {@link AsynchronousManagedObject} currently within an asynchronous operation
	 * that times out.
	 */
	public void testThreadStateWaitOnProcessBoundAsynchronousOperation() throws Exception {

		// Construct object
		TestObject object = new TestObject("MO", this.construct);
		object.isAsynchronousManagedObject = true;
		object.managedObjectBuilder.setTimeout(10);

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildObject("MO", ManagedObjectScope.PROCESS);
		trigger.buildFlow("flow", null, true);
		trigger.setNextFunction("next");
		this.construct.constructFunction(work, "next").buildObject("MO");
		this.construct.constructFunction(work, "flow").buildObject("MO");

		// Trigger the functionality
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		final int numberOfFlows = 10;
		this.construct.triggerFunction("trigger", numberOfFlows, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Ensure triggered flows, by they are waiting on asynchronous operation
		assertTrue(work.isTriggered, "Trigger should have invoked flows");
		assertEquals(0, work.flowsInvoked, "Flows should be waitin on asynchronous operation");
		assertFalse(work.isCallback, "Callback for spawned thread should not be invoked");

		// Time out the asynchronous operation
		this.construct.adjustCurrentTimeMillis(100);
		this.officeManager.getOfficeManager().runAssetChecks();

		// Timed out asynchronous operation
		assertEquals(0, work.flowsInvoked, "Flows should not be invoked");
		assertTrue(work.isCallback, "Flow callback should however be executed");
		assertEquals(numberOfFlows, work.escalations.size(), "Incorrect number of callback escalations");
		assertTrue(isComplete.value, "Process should also complete");
		assertNotNull(failure.value, "Should report timeout to main thread state");
		assertEquals(ManagedObjectOperationTimedOutEscalation.class, failure.value.getClass(),
				"Incorrect type of escalation");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTriggered = false;

		public boolean isCallback = false;

		public List<Throwable> escalations = new ArrayList<>();

		public boolean isNextInvoked = false;

		public int flowsInvoked = 0;

		public void trigger(Integer numberOfFlows, TestObject object, ReflectiveFlow flow) {

			// Trigger asynchronous operation (that will timeout)
			object.asynchronousContext.start(null);

			for (int i = 0; i < numberOfFlows; i++) {
				flow.doFlow(null, (escalation) -> {
					assertNotNull("Must report escalation");
					this.escalations.add(escalation);
					this.isCallback = true;
				});
			}
			this.isTriggered = true;
		}

		public void next(TestObject object) {
			this.isNextInvoked = true;
		}

		public void flow(TestObject object) {
			this.flowsInvoked++;
		}
	}

}
