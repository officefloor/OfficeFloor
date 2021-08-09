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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Ensure {@link FlowCallback} waits for a dependent
 * {@link AsynchronousManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class CallbackWaitOnDependentAsynchronousManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	@Test
	public void callback_WaitOn_AsynchronousDependentProcessBound() throws Exception {
		this.doAsynchronousCallbackTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	@Test
	public void callback_WaitOn_AsynchronousDependentThreadBound() throws Exception {
		this.doAsynchronousCallbackTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	@Test
	public void callback_WaitOn_AsynchronousDependentFunctionBound() throws Exception {
		this.doAsynchronousCallbackTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes test.
	 * 
	 * @param scope {@link ManagedObjectScope}.
	 */
	public void doAsynchronousCallbackTest(ManagedObjectScope scope) throws Exception {

		// Construct the asynchronous managed object
		String childName = "ASYNCHRONOUS";
		TestObject asynchronous = new TestObject(childName, this.construct);
		asynchronous.isAsynchronousManagedObject = true;
		asynchronous.managedObjectBuilder.setTimeout(10);
		this.construct.getOfficeBuilder().addProcessManagedObject(childName, childName);

		// Construct the used managed object
		TestObject used = new TestObject("COORDINATE", this.construct);
		used.isCoordinatingManagedObject = true;
		used.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);

		// Construct functions
		TestWork work = new TestWork(asynchronous);
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
		task.buildObject("COORDINATE", scope).mapDependency(0, childName);
		task.buildFlow("spawn", null, true);
		this.construct.constructFunction(work, "spawn");

		// Trigger function
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.construct.triggerFunction("task", null, complete);

		// The callback should not be invoked
		assertTrue(work.isTaskInvoked, "Task should be invoked");
		this.threading.waitForTrue(() -> work.isSpawnInvoked, "Spawn should be invoked");
		assertFalse(work.isCallbackInvoked, "Callback should be awaiting");
		complete.assertNotComplete();

		// Complete the asynchronous operation
		asynchronous.asynchronousContext.complete(null);

		// Should complete
		complete.assertComplete(this.threading);

		// Callback should be invoked
		assertTrue(work.isCallbackInvoked, "Callback should now complete");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject dependency;

		public boolean isTaskInvoked = false;

		public boolean isSpawnInvoked = false;

		public boolean isCallbackInvoked = false;

		public TestWork(TestObject dependency) {
			this.dependency = dependency;
		}

		public void task(TestObject object, ReflectiveFlow spawn) {
			this.isTaskInvoked = true;
			this.dependency.asynchronousContext.start(null);

			spawn.doFlow(this.dependency, (escalation) -> {
				this.isCallbackInvoked = true;
			});
		}

		public void spawn() {
			this.isSpawnInvoked = true;
		}
	}

}
