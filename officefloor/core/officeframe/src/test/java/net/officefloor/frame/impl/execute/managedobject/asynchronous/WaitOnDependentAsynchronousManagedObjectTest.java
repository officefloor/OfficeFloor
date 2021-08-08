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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Tests loading the {@link ManagedObject} asynchronously.
 * 
 * @author Daniel Sagenschneider
 */
public class WaitOnDependentAsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} flags completion.
	 */
	public void test_AsynchronousOperation_WaitOn_DependentProcessBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} flags completion.
	 */
	public void test_AsynchronousOperation_WaitOn_DependentThreadBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject}
	 * stops execution until {@link AsynchronousContext} flags completion.
	 */
	public void test_AsynchronousOperation_WaitOn_DependentFunctionBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doAsynchronousOperationTest(ManagedObjectScope scope) throws Exception {

		// Construct the dependency managed object
		TestObject dependency = new TestObject("DEPENDENCY", this);
		dependency.isAsynchronousManagedObject = true;
		dependency.managedObjectBuilder.setTimeout(10);
		this.getOfficeBuilder().addProcessManagedObject("DEPENDENCY", "DEPENDENCY");

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isCoordinatingManagedObject = true;
		object.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);

		// Construct functions
		TestWork work = new TestWork(dependency);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", scope).mapDependency(0, "DEPENDENCY");
		task.setNextFunction("next");
		this.constructFunction(work, "next").setNextFunction("await");
		ReflectiveFunctionBuilder wait = this.constructFunction(work, "await");
		if (scope == ManagedObjectScope.FUNCTION) {
			wait.buildObject("MO", scope).mapDependency(0, "DEPENDENCY");
		} else {
			wait.buildObject("MO");
		}

		// Trigger function
		Closure<Boolean> isComplete = new Closure<>(false);
		this.triggerFunction("task", null, (escalation) -> isComplete.value = true);

		// Only the task should be invoked
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertTrue("Next should be invoked, as not dependent on managed object", work.isNextInvoked);
		assertFalse("Wait should be waiting on asynchronous operation", work.isAwaitInvoked);
		assertFalse("Process should not be complete", isComplete.value);

		// Complete the asynchronous operation
		dependency.asynchronousContext.complete(null);

		// Wait should now complete
		assertTrue("Wait should now complete", work.isAwaitInvoked);
		assertTrue("Process should be complete", isComplete.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject dependency;

		public boolean isTaskInvoked = false;

		public boolean isNextInvoked = false;

		public boolean isAwaitInvoked = false;

		public TestWork(TestObject dependency) {
			this.dependency = dependency;
		}

		public void task(TestObject object) {
			this.isTaskInvoked = true;
			this.dependency.asynchronousContext.start(null);
		}

		public void next() {
			this.isNextInvoked = true;
		}

		public void await(TestObject object) {
			this.isAwaitInvoked = true;
		}
	}

}
