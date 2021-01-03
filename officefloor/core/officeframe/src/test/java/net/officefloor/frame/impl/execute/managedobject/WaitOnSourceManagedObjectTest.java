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

package net.officefloor.frame.impl.execute.managedobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Ensure wait on sourcing of {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class WaitOnSourceManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();
	
	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure multiple tasks can wait on the {@link ManagedObject} to be sourced.
	 */
	@Test
	public void delaySourceManagedObject_With_MultipleFunctionsWaiting() throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this.construct);
		object.isDelaySource = true;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "trigger");
		trigger.buildParameter();
		trigger.buildFlow("spawnedTask", null, true);
		this.construct.constructFunction(work, "spawnedTask").buildObject("MO", ManagedObjectScope.PROCESS);

		// Trigger the function
		final int numberOfFlows = 10;
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.construct.triggerFunction("trigger", numberOfFlows, complete);

		// Ensure flows invoked (but waiting on managed object)
		assertEquals(numberOfFlows, work.flowsInvoked, "Incorrect number of flows invoked");
		assertEquals(0, work.spawnedTasksRun.get(), "All tasks should be waiting on process bound managed object");

		// Load the managed object (releasing all tasks)
		object.managedObjectUser.setManagedObject(object);
		
		// Should complete
		complete.assertComplete(this.threading);

		// Ensure all spawned tasks run (with the managed object object)
		assertEquals(numberOfFlows, work.spawnedTasksRun.get(), "All tasks should be run");
		for (int i = 0; i < numberOfFlows; i++) {
			assertSame(object, work.objects.poll(), "Incorrect managed object " + i);
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final Deque<TestObject> objects = new ConcurrentLinkedDeque<>();

		private int flowsInvoked = 0;

		private final AtomicInteger spawnedTasksRun = new AtomicInteger(0);

		public void trigger(Integer numberOfFlows, ReflectiveFlow flow) {
			for (int i = 0; i < numberOfFlows; i++) {
				this.flowsInvoked++;
				flow.doFlow(null, null);
			}
		}

		public void spawnedTask(TestObject object) {
			this.spawnedTasksRun.incrementAndGet();
			this.objects.add(object);
		}
	}

}
