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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

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
public class _fail_CallbackWaitOnDependentAsynchronousManagedObjectTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	@Test
	public void failCallback_WaitOn_AsynchronousDependentProcessBound() throws Exception {
		this.doAsynchronousFailCallbackTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	@Test
	public void failCallback_WaitOn_AsynchronousDependentThreadBound() throws Exception {
		this.doAsynchronousFailCallbackTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	@Test
	public void failCallback_WaitOn_AsynchronousDependentFunctionBound() throws Exception {
		this.doAsynchronousFailCallbackTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes test.
	 * 
	 * @param scope {@link ManagedObjectScope}.
	 */
	public void doAsynchronousFailCallbackTest(ManagedObjectScope scope) throws Exception {

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
		TestWork work = new TestWork(asynchronous, new Exception("TEST"));
		ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
		task.buildObject("COORDINATE", scope).mapDependency(0, childName);
		task.buildFlow("spawn", null, true);
		this.construct.constructFunction(work, "spawn");

		// Trigger function
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.construct.triggerFunction("task", null, complete);

		// The callback should not be invoked
		this.threading.waitForTrue(() -> work.isTaskInvoked, "Task should be invoked");
		this.threading.waitForTrue(() -> work.isSpawnInvoked, "Spawn should be invoked");
		assertFalse(work.isCallbackInvoked, "Callback should be awaiting");
		complete.assertNotComplete();

		// Complete the asynchronous operation
		asynchronous.asynchronousContext.complete(null);

		// Callback should now be invoked
		// (Note: spawned thread state may pick up asynchronous completion)
		this.threading.waitForTrue(() -> work.isCallbackInvoked, "Callback should now complete");
		assertSame(work.failure, work.escalation, "Incorrect callback escalation");
		complete.assertComplete(this.threading);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject dependency;

		private final Exception failure;

		public boolean isTaskInvoked = false;

		public volatile boolean isCallbackInvoked = false;

		public volatile Throwable escalation = null;

		public volatile boolean isSpawnInvoked = false;

		public TestWork(TestObject dependency, Exception failure) {
			this.dependency = dependency;
			this.failure = failure;
		}

		public void task(TestObject object, ReflectiveFlow spawn) {
			this.isTaskInvoked = true;
			this.dependency.asynchronousContext.start(null);

			spawn.doFlow(this.dependency, (escalation) -> {
				this.escalation = escalation;
				this.isCallbackInvoked = true;
			});
		}

		public void spawn() throws Exception {
			this.isSpawnInvoked = true;
			throw this.failure;
		}
	}

}
