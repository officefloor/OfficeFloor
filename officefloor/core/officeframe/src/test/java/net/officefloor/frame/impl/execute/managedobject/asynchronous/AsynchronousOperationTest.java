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

import net.officefloor.frame.api.managedobject.AsynchronousOperation;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can run {@link AsynchronousOperation},
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousOperationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can run {@link AsynchronousOperation}.
	 */
	public void testAsynchronousOperation() throws Exception {

		// Construct managed objects
		TestObject object = new TestObject("MO", this);
		object.isAsynchronousManagedObject = true;
		object.managedObjectBuilder.setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.PROCESS);

		// Test
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.triggerFunction("task", null, (escalation) -> {
			assertTrue("Asynchronous operation complete before process completes", work.isCompleted);
			complete.run(escalation);
		});

		// Ensure asynchronous operations undertaken
		assertTrue("Should have started asynchronous operation", work.isStarted);
		assertTrue("Should have completed asynchronous operation", work.isCompleted);
		complete.assertComplete();
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isStarted = false;

		private boolean isCompleted = false;

		public void task(TestObject object) {

			// Undertake asynchronous operation
			object.asynchronousContext.start(() -> {
				this.isStarted = true;

				// Complete asynchronous operation
				object.asynchronousContext.complete(() -> {
					assertTrue("Should be started", this.isStarted);
					this.isCompleted = true;
				});
			});
		}
	}

}
