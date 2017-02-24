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
package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure re-use of {@link ProcessState} bound {@link AsynchronousManagedObject}
 * causes re-use to wait on the asynchronous operation time out.
 *
 * @author Daniel Sagenschneider
 */
public class _timeout_ThreadWaitOnAsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ThreadState} waits on re-use of
	 * {@link AsynchronousManagedObject} currently within an asynchronous
	 * operation that times out.
	 */
	public void testThreadStateWaitOnProcessBoundAsynchronousOperation() throws Exception {

		// Construct object
		TestObject object = new TestObject("MO", this);
		object.isAsynchronousManagedObject = true;
		object.managedObjectBuilder.setTimeout(10);

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildObject("MO", ManagedObjectScope.PROCESS);
		trigger.buildFlow("flow", null, true);
		trigger.setNextFunction("next");
		this.constructFunction(work, "next").buildObject("MO");
		this.constructFunction(work, "flow").buildObject("MO");

		// Trigger the functionality
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		final int numberOfFlows = 10;
		Office office = this.triggerFunction("trigger", numberOfFlows, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Ensure triggered flows, by they are waiting on asynchronous operation
		assertTrue("Trigger should have invoked flows", work.isTriggered);
		assertEquals("Flows should be waitin on asynchronous operation", 0, work.flowsInvoked);
		assertFalse("Callback for spawned thread should not be invoked", work.isCallback);

		// Time out the asynchronous operation
		this.adjustCurrentTimeMillis(100);
		office.runAssetChecks();

		// Timed out asynchronous operation
		assertEquals("Flows should not be invoked", 0, work.flowsInvoked);
		assertTrue("Flow callback should however be executed", work.isCallback);
		assertEquals("Incorrect number of callback escalations", numberOfFlows, work.escalations.size());
		assertTrue("Process should also complete", isComplete.value);
		assertNotNull("Should report timeout to main thread state", failure.value);
		assertEquals("Incorrect type of escalation", ManagedObjectOperationTimedOutEscalation.class,
				failure.value.getClass());
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
			object.asynchronousListener.notifyStarted();

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