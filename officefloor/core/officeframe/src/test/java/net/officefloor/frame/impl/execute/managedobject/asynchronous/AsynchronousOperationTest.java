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
