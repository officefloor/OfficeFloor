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
		this.threading.waitForTrue(() -> object.managedObjectUser != null);
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
