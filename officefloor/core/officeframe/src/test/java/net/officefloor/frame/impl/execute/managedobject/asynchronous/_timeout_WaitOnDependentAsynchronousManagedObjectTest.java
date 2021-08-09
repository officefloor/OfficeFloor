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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests loading the {@link ManagedObject} asynchronously.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class _timeout_WaitOnDependentAsynchronousManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} tmes out.
	 */
	@Test
	public void asynchronousOperation_WaitOn_DependentProcessBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} tmes out.
	 */
	@Test
	public void asynchronousOperation_WaitOn_DependentThreadBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject} stops
	 * execution until {@link AsynchronousContext} tmes out.
	 */
	@Test
	public void asynchronousOperation_WaitOn_DependentFunctionBound() throws Exception {
		this.doAsynchronousOperationTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Test {@link AsynchronousManagedObject}.
	 */
	private TestObject dependency;

	/**
	 * Undertakes test.
	 * 
	 * @param scope {@link ManagedObjectScope}.
	 */
	public void doAsynchronousOperationTest(ManagedObjectScope scope) throws Exception {

		// Construct the dependency managed object
		this.dependency = new TestObject("DEPENDENCY", this.construct);
		this.dependency.isAsynchronousManagedObject = true;
		this.dependency.managedObjectBuilder.setTimeout(10);
		this.construct.getOfficeBuilder().addProcessManagedObject("DEPENDENCY", "DEPENDENCY");

		// Construct the managed object
		TestObject object = new TestObject("MO", this.construct);
		object.isCoordinatingManagedObject = true;
		object.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);

		// Construct functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
		task.buildObject("MO", scope).mapDependency(0, "DEPENDENCY");
		task.setNextFunction("next");
		this.construct.constructFunction(work, "next").setNextFunction("await");
		ReflectiveFunctionBuilder wait = this.construct.constructFunction(work, "await");
		if (scope == ManagedObjectScope.FUNCTION) {
			wait.buildObject("MO", scope).mapDependency(0, "DEPENDENCY");
		} else {
			wait.buildObject("MO");
		}

		// Trigger function
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.construct.triggerFunction("task", null, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Only the task should be invoked
		assertTrue(work.isTaskInvoked, "Task should be invoked");
		assertTrue(work.isNextInvoked, "Next should be invoked, as not dependent on managed object");
		assertFalse(work.isAwaitInvoked, "Wait should be waiting on asynchronous operation");
		assertFalse(isComplete.value, "Process should not be complete");

		// Time out the asynchronous operation
		this.construct.adjustCurrentTimeMillis(100);
		this.officeManager.getOfficeManager(0).runAssetChecks();

		// Wait should now complete
		assertFalse(work.isAwaitInvoked, "Should escalate time out and not continue flow");
		assertTrue(isComplete.value, "Process should be complete");
		assertTrue(failure.value instanceof ManagedObjectOperationTimedOutEscalation,
				"Should escalate time out from process");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		boolean isTaskInvoked = false;

		public boolean isNextInvoked = false;

		public boolean isAwaitInvoked = false;

		public void task(TestObject object) {
			this.isTaskInvoked = true;
			_timeout_WaitOnDependentAsynchronousManagedObjectTest.this.dependency.asynchronousContext.start(null);
		}

		public void next() {
			this.isNextInvoked = true;
		}

		public void await(TestObject object) {
			this.isAwaitInvoked = true;
		}
	}

}
