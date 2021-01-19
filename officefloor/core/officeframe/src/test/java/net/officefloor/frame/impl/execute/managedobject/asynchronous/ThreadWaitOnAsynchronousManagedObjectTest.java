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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Ensure re-use of {@link ProcessState} bound {@link AsynchronousManagedObject}
 * causes re-use to wait on the asynchronous operation.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ThreadWaitOnAsynchronousManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure {@link ThreadState} waits on re-use of
	 * {@link AsynchronousManagedObject} currently within an asynchronous operation.
	 */
	@Test
	public void threadStateWaitOnProcessBoundAsynchronousOperation() throws Exception {

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
		this.construct.constructFunction(work, "flow").buildObject("MO");

		// Trigger the functionality
		Closure<Boolean> isComplete = new Closure<>(false);
		final int numberOfFlows = 10;
		this.construct.triggerFunction("trigger", numberOfFlows, (escalation) -> isComplete.value = true);

		// Ensure triggered flows, by they are waiting on asynchronous operation
		assertTrue(work.isTriggered, "Trigger should have invoked flows");
		assertEquals(0, work.flowsInvoked.get(), "Flows should be waiting on asynchronous operation");

		// Complete asynchronous operation
		object.asynchronousContext.complete(null);
		this.threading.waitForTrue(() -> isComplete.value, "Process should complete");
		assertEquals(numberOfFlows, work.flowsInvoked.get(), "Flows should now be invoked");
		assertEquals(numberOfFlows, work.flowsComplete.get(), "Flows should also complete");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTriggered = false;

		public final AtomicInteger flowsInvoked = new AtomicInteger(0);

		public final AtomicInteger flowsComplete = new AtomicInteger(0);

		public void trigger(Integer numberOfFlows, TestObject object, ReflectiveFlow flow) {
			object.asynchronousContext.start(null);
			for (int i = 0; i < numberOfFlows; i++) {
				flow.doFlow(null, (escalation) -> this.flowsComplete.incrementAndGet());
			}
			this.isTriggered = true;
		}

		public void flow(TestObject object) {
			this.flowsInvoked.incrementAndGet();
		}
	}

}
