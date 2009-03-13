/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.integrate.managedobject;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests construction scenarios of a {@link ManagedObject}.
 * 
 * @author Daniel
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Initiate for construction
		super.setUp();

		// Reset static state between tests
		managedObjectSource = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.test.AbstractOfficeConstructTestCase#tearDown()
	 */
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
	 * @param defaultTimeout
	 *            Default timeout. If greater than zero will have the
	 *            {@link ManagedObject} be an {@link AsynchronousManagedObject}.
	 */
	private void doTest(boolean isManagedObjectOutside,
			boolean isManagedObjectInside, ManagedObjectScope scope,
			long defaultTimeout) throws Throwable {
		String officeName = this.getOfficeName();
		this.initiateOfficeFloor(isManagedObjectOutside, isManagedObjectInside,
				scope, defaultTimeout);
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
	 * Scope for the {@link ManagedObject}.
	 */
	private enum ManagedObjectScope {
		WORK, PROCESS
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
	 * @param defaultTimeout
	 *            Default timeout. If greater than zero will have the
	 *            {@link ManagedObject} be an {@link AsynchronousManagedObject}.
	 * @throws Exception
	 *             If fails to initialise the {@link OfficeFloor}.
	 */
	private void initiateOfficeFloor(boolean isManagedObjectOutside,
			boolean isManagedObjectInside, ManagedObjectScope scope,
			long defaultTimeout) throws Exception {

		final String EXTERNAL_EVENT_TASK = "externalEvent";
		final String INVOKED_TASK = "invokedTask";

		// Obtain the name of the office
		String officeName = this.getOfficeName();

		// Create and register the managed object source
		ManagedObjectBuilder<HandlerKey> managedObjectBuilder = this
				.getOfficeFloorBuilder().addManagedObject("MO",
						TestManagedObjectSource.class);
		ManagingOfficeBuilder managingOfficeBuilder = managedObjectBuilder
				.setManagingOffice(officeName);
		if (isManagedObjectOutside) {
			managingOfficeBuilder.setProcessBoundManagedObjectName("OFFICE_MO");
		}

		// Specify whether asynchronous
		if (defaultTimeout > 0) {
			// Asynchronous managed object
			managedObjectBuilder.setDefaultTimeout(defaultTimeout);
			TestManagedObjectSource
					.setManagedObjectClass(AsynchronousManagedObject.class);
		} else {
			// Not asynchronous managed object
			TestManagedObjectSource.setManagedObjectClass(ManagedObject.class);
		}

		// Only provide handler if outside
		TestManagedObjectSource.setLoadHandler(isManagedObjectOutside);
		if (isManagedObjectOutside) {
			ManagedObjectHandlerBuilder<HandlerKey> moHandlerBuilder = managedObjectBuilder
					.getManagedObjectHandlerBuilder();
			HandlerBuilder<HandlerProcess> handlerBuilder = moHandlerBuilder
					.registerHandler(HandlerKey.HANDLER, HandlerProcess.class);
			handlerBuilder.linkProcess(HandlerProcess.TASK, "WORK",
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
			// Provide the invoked task dependent on process managed object
			ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask(
					INVOKED_TASK, "TEAM");
			taskBuilder.buildParameter();

			// Register managed object to task based on scope
			switch (scope) {
			case PROCESS:
				// Register as process managed object
				taskBuilder.buildObject("DEPENDENCY", "MO_LINK");

				// Register the process managed object within the office
				this.getOfficeBuilder().addProcessManagedObject("MO_LINK",
						"OFFICE_MO");
				this.getOfficeBuilder().registerManagedObjectSource(
						"OFFICE_MO", "MO");
				break;
			case WORK:
				// Register as work bound managed object
				taskBuilder.buildObject("MO");

				// Register the managed object within the office
				this.getOfficeBuilder().registerManagedObjectSource("MO", "MO");
				break;
			default:
				fail("Unknown managed object scope " + scope);
			}
		}
		this.constructTeam("TEAM", new PassiveTeam());

		// Construct and open the office floor
		this.officeFloor = this.constructOfficeFloor();
		this.officeFloor.openOfficeFloor();

		// Ensure the managed object source and handler created
		assertNotNull("Managed Object Source not created", managedObjectSource);
		if (isManagedObjectOutside) {
			assertNotNull("Handler not created", managedObjectSource
					.getHandler());
		} else {
			assertNull("Handler should not be created", managedObjectSource
					.getHandler());
		}
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	public static class TestManagedObjectSource extends
			AbstractManagedObjectSource<None, HandlerKey> implements
			ManagedObject, AsynchronousManagedObject {

		/**
		 * {@link ManagedObject} class.
		 */
		private static Class<? extends ManagedObject> managedObjectClass = ManagedObject.class;

		/**
		 * Flag indicating to load the {@link Handler}.
		 */
		private static boolean isLoadHandler = true;

		/**
		 * Specifies the {@link ManagedObject} class.
		 * 
		 * @param managedObjectClass
		 *            {@link ManagedObject} class.
		 */
		public static void setManagedObjectClass(
				Class<? extends ManagedObject> managedObjectClass) {
			TestManagedObjectSource.managedObjectClass = managedObjectClass;
		}

		/**
		 * Flags whether to load the {@link Handler}.
		 * 
		 * @param isLoadHandler
		 *            <code>true</code> to load the {@link Handler}.
		 */
		public static void setLoadHandler(boolean isLoadHandler) {
			TestManagedObjectSource.isLoadHandler = isLoadHandler;
		}

		/**
		 * {@link Handler}.
		 */
		private TestHandler handler;

		/**
		 * Initiate.
		 */
		public TestManagedObjectSource() {

			// Should only be instantiated once
			assertNull("Managd Object Source should only be instantiated once",
					ManagedObjectTest.managedObjectSource);

			// Specify managed object source
			ManagedObjectTest.managedObjectSource = this;
		}

		/**
		 * Obtains the {@link TestHandler}.
		 * 
		 * @return {@link TestHandler}.
		 */
		public TestHandler getHandler() {
			return this.handler;
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
			HandlerContext<HandlerProcess> handlerContext = this.handler
					.getHandlerContext();
			handlerContext.invokeProcess(HandlerProcess.TASK, parameter, this);
		}

		/*
		 * ================ ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No requirements
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, HandlerKey> context)
				throws Exception {

			// Specify the managed object class
			context.setManagedObjectClass(managedObjectClass);

			// Determine if load the handlers
			if (isLoadHandler) {
				// Load the handlers
				HandlerLoader<HandlerKey> handlerLoader = context
						.getHandlerLoader(HandlerKey.class);
				handlerLoader.mapHandlerType(HandlerKey.HANDLER, Handler.class);

				// Provide the handler
				ManagedObjectHandlerBuilder<HandlerKey> moHandlerBuilder = context
						.getManagedObjectSourceContext().getHandlerBuilder();
				HandlerBuilder<HandlerProcess> handlerBuilder = moHandlerBuilder
						.registerHandler(HandlerKey.HANDLER,
								HandlerProcess.class);
				handlerBuilder.setHandlerFactory(new TestHandler());
			}
		}

		@Override
		protected void start(StartContext<HandlerKey> startContext)
				throws Exception {
			if (isLoadHandler) {
				// Obtain the handler
				this.handler = (TestHandler) startContext.getContext(
						HandlerKey.class).getHandler(HandlerKey.HANDLER);
			}
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
	 * Test {@link Handler} keys.
	 */
	public enum HandlerKey {
		HANDLER
	}

	/**
	 * Test {@link Handler} flows.
	 */
	public enum HandlerProcess {
		TASK
	}

	/**
	 * Test {@link Handler}.
	 */
	private static class TestHandler implements HandlerFactory<HandlerProcess>,
			Handler<HandlerProcess> {

		/**
		 * {@link HandlerContext}.
		 */
		private HandlerContext<HandlerProcess> context;

		/**
		 * Obtains the {@link HandlerContext}.
		 * 
		 * @return {@link HandlerContext}.
		 */
		public HandlerContext<HandlerProcess> getHandlerContext() {
			return this.context;
		}

		/*
		 * =============== HandlerFactory ==================
		 */

		@Override
		public Handler<HandlerProcess> createHandler() {
			return this;
		}

		/*
		 * =============== Handler ==================
		 */

		@Override
		public void setHandlerContext(HandlerContext<HandlerProcess> context)
				throws Exception {
			this.context = context;
		}
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