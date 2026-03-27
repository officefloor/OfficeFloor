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

package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure delay {@link Governance} on a {@link ManagedObject} involved in an
 * {@link AsynchronousManagedObject} operation.
 *
 * @author Daniel Sagenschneider
 */
public class DelayGovernanceForManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure delay {@link Governance} until {@link ProcessState} bound
	 * {@link AsynchronousContext} is complete.
	 */
	public void testDelayGovernanceDueToAsynchronousOperation_boundTo_Process() throws Exception {
		this.doDelayGovernanceDueToAsynchronousOperationTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure delay {@link Governance} until {@link ThreadState} bound
	 * {@link AsynchronousContext} is complete.
	 */
	public void testDelayGovernanceDueToAsynchronousOperation_boundTo_Thread() throws Exception {
		this.doDelayGovernanceDueToAsynchronousOperationTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure delay {@link Governance} until {@link ManagedFunctionContainer}
	 * bound {@link AsynchronousContext} is complete.
	 */
	public void testDelayGovernanceDueToAsynchronousOperation_boundTo_Function() throws Exception {
		this.doDelayGovernanceDueToAsynchronousOperationTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure delay {@link Governance} until {@link AsynchronousContext}
	 * complete.
	 */
	private void doDelayGovernanceDueToAsynchronousOperationTest(ManagedObjectScope scope) throws Exception {

		// Construct managed object
		TestObject object = new TestObject("MO", this);
		object.isAsynchronousManagedObject = true;
		object.managedObjectBuilder.setTimeout(100);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);

		// Construct the functions
		TestWork work = new TestWork(object);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		this.bindManagedObject("MO", scope, task.getBuilder()).mapGovernance("GOVERNANCE");
		task.buildObject("MO");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Construct the governance
		TestGovernance governance = new TestGovernance(null);
		ReflectiveGovernanceBuilder govern = this.constructGovernance(governance, "GOVERNANCE");
		govern.register("register");
		govern.enforce("enforce");

		// Undertake initial registration
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.triggerFunction("task", null, complete);

		// Should be waiting after registration
		complete.assertNotComplete();
		this.validateReflectiveMethodOrder("register");

		// Continue to execute task
		object.asynchronousContext.complete(null);
		complete.assertNotComplete();
		this.validateReflectiveMethodOrder("register", "task");

		// Continue to enforce governance
		object.asynchronousContext.complete(null);
		complete.assertComplete();
		this.validateReflectiveMethodOrder("register", "task", "enforce");
	}

	/**
	 * Ensure delay {@link Governance} until a {@link ProcessState} bound
	 * dependent {@link AsynchronousContext} is complete.
	 */
	public void testDelayGovernanceDueToDependencyAsynchronousOperation_boundTo_Process() throws Exception {
		this.doDelayGovernanceDueToDependencyAsynchronousOperationTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure delay {@link Governance} until a {@link ThreadState} bound
	 * dependent {@link AsynchronousContext} is complete.
	 */
	public void testDelayGovernanceDueToDependencyAsynchronousOperation_boundTo_Thread() throws Exception {
		this.doDelayGovernanceDueToDependencyAsynchronousOperationTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure delay {@link Governance} until a {@link ManagedFunctionContainer}
	 * bound dependent {@link AsynchronousContext} is complete.
	 */
	public void testDelayGovernanceDueToDependencyAsynchronousOperation_boundTo_Function() throws Exception {
		this.doDelayGovernanceDueToDependencyAsynchronousOperationTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure delay {@link Governance} until dependent
	 * {@link AsynchronousContext} complete.
	 */
	private void doDelayGovernanceDueToDependencyAsynchronousOperationTest(ManagedObjectScope scope) throws Exception {

		// Asynchronous managed object
		TestObject transitiveDependentObject = new TestObject("TRANSITIVE", this);

		// Construct the functions
		TestWork work = new TestWork(transitiveDependentObject);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Construct directly used managed object
		TestObject object = new TestObject("MO", this);
		object.isCoordinatingManagedObject = true;
		object.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);
		object.isRecycleFunction = true;
		this.bindManagedObject("MO", scope, task.getBuilder()).mapDependency(0, "DEPENDENT");

		// Construct dependent managed object that is governed
		TestObject dependentObject = new TestObject("DEPENDENT", this);
		dependentObject.isCoordinatingManagedObject = true;
		dependentObject.enhanceMetaData = (metaData) -> {
			metaData.addDependency(TestObject.class);
			metaData.addManagedObjectExtension(TestObject.class,
					(managedObject) -> (TestObject) managedObject);
		};
		dependentObject.isRecycleFunction = true;
		DependencyMappingBuilder dependencies = this.bindManagedObject("DEPENDENT", scope, task.getBuilder());
		dependencies.mapDependency(0, "TRANSITIVE");
		dependencies.mapGovernance("GOVERNANCE");

		// Construct the descendant managed object with asynchronous operation
		transitiveDependentObject.isAsynchronousManagedObject = true;
		transitiveDependentObject.managedObjectBuilder.setTimeout(100);
		transitiveDependentObject.isRecycleFunction = true;
		this.bindManagedObject("TRANSITIVE", scope, task.getBuilder());

		// Construct the governance
		TestGovernance governance = new TestGovernance(transitiveDependentObject);
		ReflectiveGovernanceBuilder govern = this.constructGovernance(governance, "GOVERNANCE");
		govern.register("register");
		govern.enforce("enforce");

		// Ensure the managed objects are not recycled
		Runnable ensureNotRecycled = () -> {
			assertNull("Used managed object should not be recycled", object.recycledManagedObject);
			assertNull("Dependent object should not be recycled", dependentObject.recycledManagedObject);
			assertNull("Transitive object should not be recycled", transitiveDependentObject.recycledManagedObject);
		};

		// Undertake initial registration
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		CompleteFlowCallback complete = new CompleteFlowCallback();
		this.triggerFunction("task", null, complete);

		// Should be waiting after registration
		complete.assertNotComplete();
		this.validateReflectiveMethodOrder("register");
		ensureNotRecycled.run();

		// Continue to execute task
		transitiveDependentObject.asynchronousContext.complete(null);
		complete.assertNotComplete();
		this.validateReflectiveMethodOrder("register", "task");
		ensureNotRecycled.run();

		// Continue to enforce governance
		transitiveDependentObject.asynchronousContext.complete(null);
		complete.assertComplete();
		this.validateReflectiveMethodOrder("register", "task", "enforce");
		assertNotNull("Used managed object should be recycled", object.recycledManagedObject);
		assertNotNull("Dependent object should be recycled", dependentObject.recycledManagedObject);
		assertNotNull("Transitive object should be recycled", transitiveDependentObject.recycledManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject object;

		public TestWork(TestObject object) {
			this.object = object;
		}

		public void task(TestObject object) {
			this.object.asynchronousContext.start(null);
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private final TestObject object;

		public TestGovernance(TestObject object) {
			this.object = object;
		}

		public void register(TestObject object) {
			if (this.object != null) {
				object = this.object;
			}
			object.asynchronousContext.start(null);
		}

		public void enforce(TestObject[] extensions) {
		}
	}

}
