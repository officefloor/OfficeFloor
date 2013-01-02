/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.integrate.managedobject;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests construction scenarios of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectTest extends AbstractOfficeConstructTestCase {

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
	 * {@link Task} of the {@link Office} but is not used by the {@link Office}.
	 */
	public void testManagedObjectOutsideOffice() throws Throwable {
		this.doTest(true, false, null, 0);
	}

	/**
	 * Ensures construction of a {@link AsynchronousManagedObject} that invokes
	 * a {@link Task} of the {@link Office} but is not used by the
	 * {@link Office}.
	 */
	public void testAsynchronousManagedObjectOutsideOffice() throws Throwable {
		this.doTest(true, false, null, 10);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound
	 * {@link ManagedObject} that is a dependency of a {@link Task} within the
	 * {@link Office}.
	 */
	public void testProcessManagedObjectInsideOffice() throws Throwable {
		this.doTest(false, true, ManagedObjectScope.PROCESS, 0);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound
	 * {@link AsynchronousManagedObject} that is a dependency of a {@link Task}
	 * within the {@link Office}.
	 */
	public void testAsynchronousProcessManagedObjectInsideOffice()
			throws Throwable {
		this.doTest(false, true, ManagedObjectScope.PROCESS, 10);
	}

	/**
	 * Ensures construction of a {@link Work} bound {@link ManagedObject} that
	 * is a dependency of a {@link Task} within the {@link Office}.
	 */
	public void testWorkManagedObjectInsideOffice() throws Throwable {
		this.doTest(false, true, ManagedObjectScope.WORK, 0);
	}

	/**
	 * Ensures construction of a {@link Work} bound
	 * {@link AsynchronousManagedObject} that is a dependency of a {@link Task}
	 * within the {@link Office}.
	 */
	public void testAsynchronousWorkManagedObjectInsideOffice()
			throws Throwable {
		this.doTest(false, true, ManagedObjectScope.WORK, 10);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound
	 * {@link ManagedObject} that both:
	 * <ol>
	 * <li>triggers a {@link Task} in the {@link Office}, and</li>
	 * <li>has a {@link Task} dependent on it.</li>
	 * </ol>
	 */
	public void testProcessManagedObjectOutsideAndInsideOffice()
			throws Throwable {
		this.doTest(true, true, ManagedObjectScope.PROCESS, 0);
	}

	/**
	 * Ensures construction of a {@link ProcessState} bound
	 * {@link AsynchronousManagedObject} that both:
	 * <ol>
	 * <li>triggers a {@link Task} in the {@link Office}, and</li>
	 * <li>has a {@link Task} dependent on it.</li>
	 * </ol>
	 */
	public void testAsynchronousProcessManagedObjectOutsideAndInsideOffice()
			throws Throwable {
		this.doTest(true, true, ManagedObjectScope.PROCESS, 10);
	}

	/**
	 * Ensures construction of a {@link Work} bound {@link ManagedObject} that
	 * both:
	 * <ol>
	 * <li>triggers a {@link Task} in the {@link Office}, and</li>
	 * <li>has a {@link Task} dependent on it.</li>
	 * </ol>
	 */
	public void testWorkManagedObjectOutsideAndInsideOffice() throws Throwable {
		this.doTest(true, true, ManagedObjectScope.WORK, 0);
	}

	/**
	 * Ensures construction of a {@link Work} bound
	 * {@link AsynchronousManagedObject} that both:
	 * <ol>
	 * <li>triggers a {@link Task} in the {@link Office}, and</li>
	 * <li>has a {@link Task} dependent on it.</li>
	 * </ol>
	 */
	public void testAsynchronousWorkManagedObjectOutsideAndInsideOffice()
			throws Throwable {
		this.doTest(true, true, ManagedObjectScope.WORK, 10);
	}

	/**
	 * Does the test given the input parameters.
	 * 
	 * @param isManagedObjectOutside
	 *            Flag indicating the {@link ManagedObject} is handling external
	 *            events.
	 * @param isManagedObjectInside
	 *            Flag indicating a {@link Task} is dependent on the
	 *            {@link ManagedObject}.
	 * @param scope
	 *            {@link ManagedObjectScope} when inside {@link Office}.
	 * @param timeout
	 *            Timeout. If greater than zero will have the
	 *            {@link ManagedObject} be an {@link AsynchronousManagedObject}.
	 */
	private void doTest(boolean isManagedObjectOutside,
			boolean isManagedObjectInside, ManagedObjectScope scope,
			long timeout) throws Throwable {
		String officeName = this.getOfficeName();
		this.initiateOfficeFloor(isManagedObjectOutside, isManagedObjectInside,
				scope, timeout);
		if (isManagedObjectOutside) {
			// As managed object outside, validate can handle external event
			this.ensureCanTriggerExternalEvent();
		}
		if (isManagedObjectInside) {
			// Available inside, so trigger work depending on managed object
			this.ensureCanInvokeWork(officeName);
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
	 * Tests invoking the {@link Work}.
	 */
	private void ensureCanInvokeWork(String officeName) throws Throwable {
		this.resetTask();
		Object parameter = new Object();
		WorkManager workManager = this.officeFloor.getOffice(officeName)
				.getWorkManager("WORK");
		workManager.invokeWork(parameter);
		this.validateTaskInvoked(parameter, managedObjectSource);
	}

	/**
	 * Resets the {@link Task} to test invoking again.
	 */
	private void resetTask() {
		this.work.isTaskInvoked = false;
		this.work.parameter = null;
		this.work.managedObject = null;
	}

	/**
	 * Validates the {@link Task} was invoked.
	 * 
	 * @param parameter
	 *            Expected parameter.
	 * @param managedObject
	 *            Expected {@link ManagedObject}.
	 * @throws Throwable
	 *             If failure invoking {@link Task}.
	 */
	private void validateTaskInvoked(Object parameter,
			ManagedObject managedObject) throws Throwable {

		// Ensure no escalation failures invoking task
		this.validateNoTopLevelEscalation();

		// Validates the task was invoked
		assertTrue("Task should be executed", this.work.isTaskInvoked);
		assertEquals("Incorrect parameter to task", parameter,
				this.work.parameter);
		assertEquals("Incorrect managed object", managedObject,
				this.work.managedObject);
	}

	/**
	 * Initiates the {@link OfficeFloor} with the {@link ManagedObject}
	 * available as per input flags.
	 * 
	 * @param isManagedObjectOutside
	 *            Flag indicating the {@link ManagedObject} is handling external
	 *            events.
	 * @param isManagedObjectInside
	 *            Flag indicating a {@link Task} is dependent on the
	 *            {@link ManagedObject}.
	 * @param scope
	 *            {@link ManagedObjectScope} when inside {@link Office}.
	 * @param timeout
	 *            Timeout. If greater than zero will have the
	 *            {@link ManagedObject} be an {@link AsynchronousManagedObject}.
	 * @throws Exception
	 *             If fails to initialise the {@link OfficeFloor}.
	 */
	private void initiateOfficeFloor(boolean isManagedObjectOutside,
			boolean isManagedObjectInside, ManagedObjectScope scope,
			long timeout) throws Exception {

		final String EXTERNAL_EVENT_TASK = "externalEvent";
		final String INVOKED_TASK = "invokedTask";

		// Obtain the name of the office
		String officeName = this.getOfficeName();

		// Create and register the managed object source
		ManagedObjectBuilder<Flows> managedObjectBuilder = this
				.getOfficeFloorBuilder().addManagedObject("MO",
						TestManagedObjectSource.class);
		ManagingOfficeBuilder<Flows> managingOfficeBuilder = managedObjectBuilder
				.setManagingOffice(officeName);
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
			managingOfficeBuilder.linkProcess(Flows.FLOW, "WORK",
					EXTERNAL_EVENT_TASK);
		}

		// Create and register the work
		this.work = new TestWork();
		ReflectiveWorkBuilder workBuilder = this.constructWork(this.work,
				"WORK", (isManagedObjectInside ? INVOKED_TASK : null));
		if (isManagedObjectOutside) {
			// Provide the externally executed task from managed object
			ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask(
					EXTERNAL_EVENT_TASK, "TEAM");
			taskBuilder.buildParameter();
		}
		if (isManagedObjectInside) {
			// Provide the invoked task dependent on managed object
			ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask(
					INVOKED_TASK, "TEAM");
			taskBuilder.buildParameter();
			this.getOfficeBuilder().registerManagedObjectSource("OFFICE_MO",
					"MO");
			if (isManagedObjectOutside) {
				// Already registered via Input ManagedObject
				taskBuilder.buildObject("OFFICE_MO");
			} else {
				// Not registered, so must add to scope
				taskBuilder.buildObject("OFFICE_MO", scope);
			}
		}
		this.constructTeam("TEAM", new PassiveTeam());

		// Construct and open the office floor
		this.officeFloor = this.constructOfficeFloor();
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class TestManagedObjectSource extends
			AbstractManagedObjectSource<None, Flows> implements ManagedObject,
			AsynchronousManagedObject {

		/**
		 * {@link ManagedObject} class.
		 */
		public static Class<? extends ManagedObject> managedObjectClass = ManagedObject.class;

		/**
		 * Flag indicating to link the {@link JobSequence}.
		 */
		public static boolean isLoadFlow = true;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Flows> executeContext;

		/**
		 * Initiate.
		 */
		public TestManagedObjectSource() {
			// Should only be instantiated once
			assertNull("Managd Object Source should only be instantiated once",
					ManagedObjectTest.managedObjectSource);
			ManagedObjectTest.managedObjectSource = this;
		}

		/**
		 * {@link ManagedObjectSource} has an external event that triggers
		 * functionality to handle it.
		 * 
		 * @param parameter
		 *            Parameter providing detail of the event to be passed to
		 *            the initial {@link Task}.
		 */
		public void triggerByExternalEvent(Object parameter) {
			executeContext.invokeProcess(Flows.FLOW, parameter, this, 0);
		}

		/*
		 * ================ ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No requirements
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context)
				throws Exception {

			// Specify the managed object class
			context.setManagedObjectClass(managedObjectClass);

			// Object for testing
			context.setObjectClass(TestManagedObjectSource.class);

			// Determine if load the flow
			if (isLoadFlow) {
				// Load the flow
				Labeller labeller = context.addFlow(Flows.FLOW, Object.class);
				assertEquals("Incorrect flow index", Flows.FLOW.ordinal(),
						labeller.getIndex());
			}
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context)
				throws Exception {
			this.executeContext = context;
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
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			// Do nothing
		}
	}

	/**
	 * {@link JobSequence} keys.
	 */
	public enum Flows {
		FLOW
	}

	/**
	 * Test reflective {@link Work}.
	 */
	public static class TestWork {

		/**
		 * Flags if {@link #task()} was invoked.
		 */
		public volatile boolean isTaskInvoked = false;

		/**
		 * Parameter of the {@link Task}.
		 */
		public volatile Object parameter = null;

		/**
		 * {@link TestManagedObjectSource}.
		 */
		public volatile TestManagedObjectSource managedObject = null;

		/**
		 * {@link Task} executed by the external event.
		 * 
		 * @param parameter
		 *            Parameter to the {@link Task}.
		 */
		public void externalEvent(Object parameter) {
			this.isTaskInvoked = true;
			this.parameter = parameter;
		}

		/**
		 * {@link Task} invoked that depends on {@link ManagedObject}.
		 * 
		 * @param parameter
		 *            Parameter to the {@link Task}.
		 * @param managedObject
		 *            {@link ManagedObject}.
		 */
		public void invokedTask(Object parameter,
				TestManagedObjectSource managedObject) {
			this.isTaskInvoked = true;
			this.parameter = parameter;
			this.managedObject = managedObject;
		}
	}

}