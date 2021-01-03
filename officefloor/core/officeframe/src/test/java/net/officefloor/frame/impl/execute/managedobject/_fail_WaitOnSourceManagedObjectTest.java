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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Ensure handle failure on wait on sourcing of {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _fail_WaitOnSourceManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure multiple tasks can wait on the {@link ManagedObject} to be sourced but
	 * fails to source.
	 */
	@Test
	public void delaySourceManagedObject_setFailure_MultipleFunctionsWaiting() throws Exception {

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
		ThreadSafeClosure<Throwable> failure = new ThreadSafeClosure<>();
		this.construct.triggerFunction("trigger", numberOfFlows, (escalation) -> failure.set(escalation));

		// Ensure flows invoked (but waiting on managed object)
		assertEquals(numberOfFlows, work.flowsInvoked, "Incorrect number of flows invoked");
		assertEquals(0, work.failures.size(), "All tasks should be waiting on process bound managed object");

		// Fail the managed object (releasing all tasks)
		Exception escalation = new Exception("TEST");
		this.threading.waitForTrue(() -> object.managedObjectUser != null);
		object.managedObjectUser.setFailure(escalation);

		// Wait for completion
		Throwable completionEscalation = failure.waitAndGet();

		// Ensure all spawned tasks run (with failure)
		assertEquals(numberOfFlows, work.failures.size(), "All tasks should be run (failed)");
		for (int i = 0; i < numberOfFlows; i++) {
			assertSame(escalation, work.failures.poll(), "Incorrect failure " + i);
		}
		assertNull(completionEscalation, "Should handle all failures with callback");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final Deque<Throwable> failures = new ConcurrentLinkedDeque<>();

		private int flowsInvoked = 0;

		public void trigger(Integer numberOfFlows, ReflectiveFlow flow) {
			for (int i = 0; i < numberOfFlows; i++) {
				this.flowsInvoked++;
				flow.doFlow(null, (escalation) -> failures.add(escalation));
			}
		}

		public void spawnedTask(TestObject object) {
		}
	}

}
