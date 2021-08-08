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

package net.officefloor.frame.impl.execute.managedobject.input;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests construction scenarios of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectInvokeProcessTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private static TestManagedObjectSource managedObjectSource = null;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link TestWork}.
	 */
	private TestWork work;

	@Override
	protected void setUp() throws Exception {
		// Initiate for construction
		super.setUp();

		// Reset static state between tests
		managedObjectSource = null;
	}

	@Override
	protected void tearDown() throws Exception {

		// Close the office floor if created
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Clear construction
		super.tearDown();
	}

	/**
	 * Ensures construction of a {@link ManagedObject} that invokes a
	 * {@link ManagedFunction} of the {@link Office} but is not used by the
	 * {@link Office}.
	 */
	public void testManagedObjectOutsideOffice() throws Throwable {
		this.doTest(true, false, null, 0);
	}

	/**
	 * Ensures construction of a {@link AsynchronousManagedObject} that invokes a
	 * {@link ManagedFunction} of the {@link Office} but is not used by the
	 * {@link Office}.
	 */
	public void testAsynchronousManagedObjectOutsideOffice() throws Throwable {
		this.doTest(true, false, null, 10);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound {@link ManagedObject}
	 * that is a dependency of a {@link ManagedFunction} within the {@link Office}.
	 */
	public void testProcessManagedObjectInsideOffice() throws Throwable {
		this.doTest(false, true, ManagedObjectScope.PROCESS, 0);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound
	 * {@link AsynchronousManagedObject} that is a dependency of a
	 * {@link ManagedFunction} within the {@link Office}.
	 */
	public void testAsynchronousProcessManagedObjectInsideOffice() throws Throwable {
		this.doTest(false, true, ManagedObjectScope.PROCESS, 10);
	}

	/**
	 * Ensures construction of a {@link ManagedFunction} bound {@link ManagedObject}
	 * that is a dependency of a {@link ManagedFunction} within the {@link Office}.
	 */
	public void testFunctionManagedObjectInsideOffice() throws Throwable {
		this.doTest(false, true, ManagedObjectScope.FUNCTION, 0);
	}

	/**
	 * Ensures construction of a {@link ManagedFunction} bound
	 * {@link AsynchronousManagedObject} that is a dependency of a
	 * {@link ManagedFunction} within the {@link Office}.
	 */
	public void testAsynchronousFunctionManagedObjectInsideOffice() throws Throwable {
		this.doTest(false, true, ManagedObjectScope.FUNCTION, 10);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound {@link ManagedObject}
	 * that both:
	 * <ol>
	 * <li>triggers a {@link ManagedFunction} in the {@link Office}, and</li>
	 * <li>has a {@link ManagedFunction} dependent on it.</li>
	 * </ol>
	 */
	public void testProcessManagedObjectOutsideAndInsideOffice() throws Throwable {
		this.doTest(true, true, ManagedObjectScope.PROCESS, 0);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound
	 * {@link AsynchronousManagedObject} that both:
	 * <ol>
	 * <li>triggers a {@link ManagedFunction} in the {@link Office}, and</li>
	 * <li>has a {@link ManagedFunction} dependent on it.</li>
	 * </ol>
	 */
	public void testAsynchronousProcessManagedObjectOutsideAndInsideOffice() throws Throwable {
		this.doTest(true, true, ManagedObjectScope.PROCESS, 10);
	}

	/**
	 * Ensures construction of a {@link ManagedFunction} bound {@link ManagedObject}
	 * that both:
	 * <ol>
	 * <li>triggers a {@link ManagedFunction} in the {@link Office}, and</li>
	 * <li>has a {@link ManagedFunction} dependent on it.</li>
	 * </ol>
	 */
	public void testFunctionManagedObjectOutsideAndInsideOffice() throws Throwable {
		this.doTest(true, true, ManagedObjectScope.FUNCTION, 0);
	}

	/**
	 * Ensures construction of a {@link ManagedFunction} bound
	 * {@link AsynchronousManagedObject} that both:
	 * <ol>
	 * <li>triggers a {@link ManagedFunction} in the {@link Office}, and</li>
	 * <li>has a {@link ManagedFunction} dependent on it.</li>
	 * </ol>
	 */
	public void testAsynchronousFunctionManagedObjectOutsideAndInsideOffice() throws Throwable {
		this.doTest(true, true, ManagedObjectScope.FUNCTION, 10);
	}

	/**
	 * Does the test given the input parameters.
	 * 
	 * @param isManagedObjectOutside Flag indicating the {@link ManagedObject} is
	 *                               handling external events.
	 * @param isManagedObjectInside  Flag indicating a {@link ManagedFunction} is
	 *                               dependent on the {@link ManagedObject}.
	 * @param scope                  {@link ManagedObjectScope} when inside
	 *                               {@link Office}.
	 * @param timeout                Timeout. If greater than zero will have the
	 *                               {@link ManagedObject} be an
	 *                               {@link AsynchronousManagedObject}.
	 */
	private void doTest(boolean isManagedObjectOutside, boolean isManagedObjectInside, ManagedObjectScope scope,
			long timeout) throws Throwable {
		String officeName = this.getOfficeName();
		this.initiateOfficeFloor(isManagedObjectOutside, isManagedObjectInside, scope, timeout);
		if (isManagedObjectOutside) {
			// As managed object outside, validate can handle external event
			this.ensureCanTriggerExternalEvent();
		}
		if (isManagedObjectInside) {
			// Available inside, so trigger function depending on managed object
			this.ensureCanInvokeFunction(officeName);
		}
	}

	/**
	 * Tests triggering an external event.
	 */
	private void ensureCanTriggerExternalEvent() throws Throwable {
		this.resetTask();
		Object parameter = new Object();
		managedObjectSource.triggerByExternalEvent(parameter);
		this.validateTaskInvoked(parameter, null);
	}

	/**
	 * Tests invoking the {@link ManagedFunction}.
	 */
	private void ensureCanInvokeFunction(String officeName) throws Throwable {
		this.resetTask();
		Object parameter = new Object();
		FunctionManager functionManager = this.officeFloor.getOffice(officeName).getFunctionManager("invokedTask");
		functionManager.invokeProcess(parameter, null);
		this.validateTaskInvoked(parameter, managedObjectSource);
	}

	/**
	 * Resets the {@link ManagedFunction} to test invoking again.
	 */
	private void resetTask() {
		this.work.isTaskInvoked = false;
		this.work.parameter = null;
		this.work.managedObject = null;
	}

	/**
	 * Validates the {@link ManagedFunction} was invoked.
	 * 
	 * @param parameter     Expected parameter.
	 * @param managedObject Expected {@link ManagedObject}.
	 * @throws Throwable If failure invoking {@link ManagedFunction}.
	 */
	private void validateTaskInvoked(Object parameter, ManagedObject managedObject) throws Throwable {

		// Ensure no escalation failures invoking task
		this.validateNoTopLevelEscalation();

		// Validates the task was invoked
		assertTrue("Task should be executed", this.work.isTaskInvoked);
		assertEquals("Incorrect parameter to task", parameter, this.work.parameter);
		assertEquals("Incorrect managed object", managedObject, this.work.managedObject);
	}

	/**
	 * Initiates the {@link OfficeFloor} with the {@link ManagedObject} available as
	 * per input flags.
	 * 
	 * @param isManagedObjectOutside Flag indicating the {@link ManagedObject} is
	 *                               handling external events.
	 * @param isManagedObjectInside  Flag indicating a {@link ManagedFunction} is
	 *                               dependent on the {@link ManagedObject}.
	 * @param scope                  {@link ManagedObjectScope} when inside
	 *                               {@link Office}.
	 * @param timeout                Timeout. If greater than zero will have the
	 *                               {@link ManagedObject} be an
	 *                               {@link AsynchronousManagedObject}.
	 * @throws Exception If fails to initialise the {@link OfficeFloor}.
	 */
	private void initiateOfficeFloor(boolean isManagedObjectOutside, boolean isManagedObjectInside,
			ManagedObjectScope scope, long timeout) throws Exception {

		// Obtain the name of the office
		String officeName = this.getOfficeName();

		// Create and register the managed object source
		ManagedObjectBuilder<Flows> managedObjectBuilder = this.getOfficeFloorBuilder().addManagedObject("MO",
				TestManagedObjectSource.class);
		ManagingOfficeBuilder<Flows> managingOfficeBuilder = managedObjectBuilder.setManagingOffice(officeName);
		if (isManagedObjectOutside) {
			managingOfficeBuilder.setInputManagedObjectName("OFFICE_MO");
		}

		// Specify whether asynchronous
		if (timeout > 0) {
			// Asynchronous managed object
			managedObjectBuilder.setTimeout(timeout);
			TestManagedObjectSource.managedObjectClass = AsynchronousManagedObject.class;
		} else {
			// Not asynchronous managed object
			TestManagedObjectSource.managedObjectClass = ManagedObject.class;
		}

		// Only provide flow if outside
		TestManagedObjectSource.isLoadFlow = isManagedObjectOutside;
		if (isManagedObjectOutside) {
			managingOfficeBuilder.linkFlow(Flows.FLOW, "externalEvent");
		}

		// Create and register the functions
		this.work = new TestWork();
		if (isManagedObjectOutside) {
			// Provide the externally executed function from managed object
			ReflectiveFunctionBuilder functionBuilder = this.constructFunction(this.work, "externalEvent");
			functionBuilder.buildParameter();
		}
		if (isManagedObjectInside) {
			// Provide the invoked task dependent on managed object
			ReflectiveFunctionBuilder taskBuilder = this.constructFunction(this.work, "invokedTask");
			taskBuilder.buildParameter();
			this.getOfficeBuilder().registerManagedObjectSource("OFFICE_MO", "MO");
			if (isManagedObjectOutside) {
				// Already registered via Input ManagedObject
				taskBuilder.buildObject("OFFICE_MO");
			} else {
				// Not registered, so must add to scope
				taskBuilder.buildObject("OFFICE_MO", scope);
			}
		}

		// Construct and open the office floor
		this.officeFloor = this.constructOfficeFloor();
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class TestManagedObjectSource extends AbstractManagedObjectSource<None, Flows>
			implements ManagedObject, AsynchronousManagedObject {

		/**
		 * {@link ManagedObject} class.
		 */
		public static Class<? extends ManagedObject> managedObjectClass = ManagedObject.class;

		/**
		 * Flag indicating to link the {@link Flow}.
		 */
		public static boolean isLoadFlow = true;

		/**
		 * {@link ManagedObjectServiceContext}.
		 */
		private ManagedObjectServiceContext<Flows> serviceContext;

		/**
		 * Initiate.
		 */
		public TestManagedObjectSource() {
			// Should only be instantiated once
			assertNull("Managd Object Source should only be instantiated once",
					ManagedObjectInvokeProcessTest.managedObjectSource);
			ManagedObjectInvokeProcessTest.managedObjectSource = this;
		}

		/**
		 * {@link ManagedObjectSource} has an external event that triggers functionality
		 * to handle it.
		 * 
		 * @param parameter Parameter providing detail of the event to be passed to the
		 *                  initial {@link ManagedFunction}.
		 */
		public void triggerByExternalEvent(Object parameter) {
			serviceContext.invokeProcess(Flows.FLOW, parameter, this, 0, null);
		}

		/*
		 * ================ ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No requirements
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {

			// Specify the managed object class
			context.setManagedObjectClass(managedObjectClass);

			// Object for testing
			context.setObjectClass(TestManagedObjectSource.class);

			// Determine if load the flow
			if (isLoadFlow) {
				// Load the flow
				Labeller<Flows> labeller = context.addFlow(Flows.FLOW, Object.class);
				assertEquals("Incorrect flow key", Flows.FLOW, labeller.getKey());
				assertEquals("Incorrect flow index", Flows.FLOW.ordinal(), labeller.getIndex());
			}
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.serviceContext = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ================ ManagedObject ======================
		 */

		@Override
		public Object getObject() throws Exception {
			return this;
		}

		/*
		 * ================ AsynchronousManagedObject ==============
		 */

		@Override
		public void setAsynchronousContext(AsynchronousContext listener) {
			// Do nothing
		}
	}

	/**
	 * {@link Flow} keys.
	 */
	public enum Flows {
		FLOW
	}

	/**
	 * Test reflective object.
	 */
	public static class TestWork {

		/**
		 * Flags if {@link #task()} was invoked.
		 */
		public boolean isTaskInvoked = false;

		/**
		 * Parameter of the {@link ManagedFunction}.
		 */
		public Object parameter = null;

		/**
		 * {@link TestManagedObjectSource}.
		 */
		public TestManagedObjectSource managedObject = null;

		/**
		 * {@link ManagedFunction} executed by the external event.
		 * 
		 * @param parameter Parameter to the {@link ManagedFunction}.
		 */
		public void externalEvent(Object parameter) {
			this.isTaskInvoked = true;
			this.parameter = parameter;
		}

		/**
		 * {@link ManagedFunction} invoked that depends on {@link ManagedObject}.
		 * 
		 * @param parameter     Parameter to the {@link ManagedFunction}.
		 * @param managedObject {@link ManagedObject}.
		 */
		public void invokedTask(Object parameter, TestManagedObjectSource managedObject) {
			this.isTaskInvoked = true;
			this.parameter = parameter;
			this.managedObject = managedObject;
		}
	}

}
