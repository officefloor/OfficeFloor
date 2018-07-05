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
package net.officefloor.frame.impl.execute.managedobject.asynchronous;

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
 * causes re-use to wait on the asynchronous operation.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadWaitOnAsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ThreadState} waits on re-use of
	 * {@link AsynchronousManagedObject} currently within an asynchronous
	 * operation.
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
		this.constructFunction(work, "flow").buildObject("MO");

		// Trigger the functionality
		Closure<Boolean> isComplete = new Closure<>(false);
		final int numberOfFlows = 10;
		this.triggerFunction("trigger", numberOfFlows, (escalation) -> isComplete.value = true);

		// Ensure triggered flows, by they are waiting on asynchronous operation
		assertTrue("Trigger should have invoked flows", work.isTriggered);
		assertEquals("Flows should be waitin on asynchronous operation", 0, work.flowsInvoked);

		// Complete asynchronous operation
		object.asynchronousContext.complete(null);
		assertEquals("Flows should now be invoked", numberOfFlows, work.flowsInvoked);
		assertEquals("Flows should also complete", numberOfFlows, work.flowsComplete);
		assertTrue("Process should also complete", isComplete.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTriggered = false;

		public int flowsInvoked = 0;

		public int flowsComplete = 0;

		public void trigger(Integer numberOfFlows, TestObject object, ReflectiveFlow flow) {
			object.asynchronousContext.start(null);
			for (int i = 0; i < numberOfFlows; i++) {
				flow.doFlow(null, (escalation) -> this.flowsComplete++);
			}
			this.isTriggered = true;
		}

		public void flow(TestObject object) {
			this.flowsInvoked++;
		}
	}

}