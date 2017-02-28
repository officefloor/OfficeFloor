/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link FlowCallback} waits for a dependent
 * {@link AsynchronousManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_CallbackWaitOnDependentAsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ProcessState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	public void test_FailCallback_WaitOn_AsynchronousDependentProcessBound() throws Exception {
		this.doAsynchronousFailCallbackTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure {@link ThreadState} bound {@link AsynchronousManagedObject} stops
	 * {@link FlowCallback} until {@link AsynchronousContext} flags completion.
	 */
	public void test_FailCallback_WaitOn_AsynchronousDependentThreadBound() throws Exception {
		this.doAsynchronousFailCallbackTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedFunction} bound {@link AsynchronousManagedObject}
	 * stops {@link FlowCallback} until {@link AsynchronousContext} flags
	 * completion.
	 */
	public void test_FailCallback_WaitOn_AsynchronousDependentFunctionBound() throws Exception {
		this.doAsynchronousFailCallbackTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doAsynchronousFailCallbackTest(ManagedObjectScope scope) throws Exception {

		// Construct the asynchronous managed object
		String childName = "ASYNCHRONOUS";
		TestObject asynchronous = new TestObject(childName, this);
		asynchronous.isAsynchronousManagedObject = true;
		asynchronous.managedObjectBuilder.setTimeout(10);
		this.getOfficeBuilder().addProcessManagedObject(childName, childName);

		// Construct the used managed object
		TestObject used = new TestObject("COORDINATE", this);
		used.isCoordinatingManagedObject = true;
		used.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);

		// Construct functions
		TestWork work = new TestWork(asynchronous, new Exception("TEST"));
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("COORDINATE", scope).mapDependency(0, childName);
		task.buildFlow("spawn", null, true);
		this.constructFunction(work, "spawn");

		// Trigger function
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.triggerFunction("task", null, complete);

		// The callback should not be invoked
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertTrue("Spawn should be invoked", work.isSpawnInvoked);
		assertFalse("Callback should be awaiting", work.isCallbackInvoked);
		complete.assertNotComplete();

		// Complete the asynchronous operation
		asynchronous.asynchronousContext.complete(null);

		// Callback should now be invoked
		assertTrue("Callback should now complete", work.isCallbackInvoked);
		assertSame("Incorrect callback escalation", work.failure, work.escalation);
		complete.assertComplete();
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject dependency;

		private final Exception failure;

		public boolean isTaskInvoked = false;

		public boolean isSpawnInvoked = false;

		public boolean isCallbackInvoked = false;

		public Throwable escalation = null;

		public TestWork(TestObject dependency, Exception failure) {
			this.dependency = dependency;
			this.failure = failure;
		}

		public void task(TestObject object, ReflectiveFlow spawn) {
			this.isTaskInvoked = true;
			this.dependency.asynchronousContext.start(null);

			spawn.doFlow(this.dependency, (escalation) -> {
				this.isCallbackInvoked = true;
				this.escalation = escalation;
			});
		}

		public void spawn() throws Exception {
			this.isSpawnInvoked = true;
			throw this.failure;
		}
	}

}