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
public class WaitOnDescendantAsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

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

		// Construct the asynchronous managed object
		String childName = "ASYNCHRONOUS";
		TestObject asynchronous = new TestObject(childName, this);
		asynchronous.isAsynchronousManagedObject = true;
		asynchronous.managedObjectBuilder.setTimeout(10);
		this.getOfficeBuilder().addProcessManagedObject(childName, childName);

		// Construct the parent managed objects
		for (int i = 0; i < 10; i++) {
			String parentName = "PARENT_" + i;
			TestObject object = new TestObject(parentName, this);
			object.isCoordinatingManagedObject = true;
			object.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);
			this.getOfficeBuilder().addProcessManagedObject(parentName, parentName).mapDependency(0, childName);
			childName = parentName;
		}

		// Construct the used managed object
		TestObject used = new TestObject("USED", this);
		used.isCoordinatingManagedObject = true;
		used.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);

		// Construct functions
		TestWork work = new TestWork(asynchronous);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("USED", scope).mapDependency(0, childName);
		task.setNextFunction("next");
		this.constructFunction(work, "next").setNextFunction("await");
		ReflectiveFunctionBuilder wait = this.constructFunction(work, "await");
		if (scope == ManagedObjectScope.FUNCTION) {
			wait.buildObject("USED", scope).mapDependency(0, childName);
		} else {
			wait.buildObject("USED");
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
		asynchronous.asynchronousContext.complete(null);

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
