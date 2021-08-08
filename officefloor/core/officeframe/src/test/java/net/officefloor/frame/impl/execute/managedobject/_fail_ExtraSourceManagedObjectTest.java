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

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handles {@link ManagedObject} being loaded twice to the
 * {@link ManagedObjectUser}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_ExtraSourceManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure log failure if provided after sourcing the {@link ManagedObject}.
	 */
	public void test_SourceManagedObject_setFailure_AfterSourced() throws Exception {

		// Construct managed object
		new TestObject("MO", this);

		// Construct function
		TestFailAfterWork work = new TestFailAfterWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.FUNCTION);
		task.buildParameter();

		// Invoke the function
		this.invokeFunction("task", new Exception("TEST"));
		assertTrue("Ensure the task was invoked", work.isTaskInvoked);
	}

	/**
	 * Test functionality.
	 */
	public class TestFailAfterWork {

		public boolean isTaskInvoked = false;

		public void task(TestObject object, Throwable failure) {
			this.isTaskInvoked = true;

			// Load the failure (after sourcing the managed object)
			String log = _fail_ExtraSourceManagedObjectTest.this
					.captureLoggerOutput(() -> object.managedObjectUser.setFailure(failure));
			assertTrue("Should log the failure", log.contains(failure.getMessage()));
		}
	}

	/**
	 * Ensure log failure if provided after failure.
	 */
	public void test_SourceManagedObject_setFailure_AfterUnload() throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		object.isDelaySource = true;

		// Construct function
		TestAfterWork work = new TestAfterWork();
		this.constructFunction(work, "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Invoke the function
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> failure.value = escalation);

		// Fail the source
		Exception first = new Exception("FIRST");
		assertNull("Should not have failed process", failure.value);
		object.managedObjectUser.setFailure(first);
		assertSame("Should fail to source", first, failure.value);

		// Fail again
		Exception second = new Exception("SECOND");
		String log = this.captureLoggerOutput(() -> object.managedObjectUser.setFailure(second));
		assertTrue("Should log second failure", log.contains(second.getMessage()));
		assertSame("Should still be failure on first failure", first, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestAfterWork {

		public void task(TestObject object) {
		}
	}

}
