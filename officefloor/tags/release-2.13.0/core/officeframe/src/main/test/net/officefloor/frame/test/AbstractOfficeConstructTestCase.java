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
package net.officefloor.frame.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Abstract {@link TestCase} for construction testing of an Office.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeConstructTestCase extends
		OfficeFrameTestCase implements EscalationHandler {

	/**
	 * Index of the current {@link OfficeFloor} being constructed.
	 */
	private static int OFFICE_FLOOR_INDEX = 0;

	/**
	 * Index of the current {@link Office} being constructed.
	 */
	private static int OFFICE_INDEX = 0;

	/**
	 * Constructed {@link OfficeFloor} instances that need to be closed on tear
	 * down of tests.
	 */
	private final List<OfficeFloor> constructedOfficeFloors = new LinkedList<OfficeFloor>();

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	private OfficeFloorBuilder officeFloorBuilder;

	/**
	 * {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder;

	/**
	 * {@link WorkBuilder}.
	 */
	private WorkBuilder<?> workBuilder;

	/**
	 * List of method names in order they are invoked by the
	 * {@link ReflectiveTaskBuilder} instances for the test.
	 */
	private List<String> reflectiveTaskInvokedMethods = new LinkedList<String>();

	/**
	 * <p>
	 * Flag indicating whether to record the invocations of the
	 * {@link ReflectiveTaskBuilder} instances.
	 * <p>
	 * This is necessary as stress tests using the {@link ReflectiveTaskBuilder}
	 * will get {@link OutOfMemoryError} issues should every {@link Task}
	 * executed be recorded.
	 */
	private boolean isRecordReflectiveTaskMethodsInvoked = false;

	/**
	 * {@link Throwable} for the {@link EscalationHandler}.
	 */
	private Throwable exception = null;

	/**
	 * Lock for the {@link EscalationHandler}.
	 */
	private final Object exceptionLock = new Object();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	@Override
	protected void setUp() throws Exception {
		// Initiate for constructing office
		OFFICE_FLOOR_INDEX++;
		this.officeFloorBuilder = OfficeFrame.getInstance()
				.createOfficeFloorBuilder(this.getOfficeFloorName());
		OFFICE_INDEX++;
		this.officeBuilder = this.officeFloorBuilder.addOffice(this
				.getOfficeName());

		// Initiate to receive the top level escalation to report back in tests
		this.officeFloorBuilder.setEscalationHandler(this);
	}

	/*
	 * =================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		synchronized (this.exceptionLock) {
			// Indicate a office floor level escalation
			System.err.println("OFFICE FLOOR ESCALATION: "
					+ escalation.getMessage() + " ["
					+ escalation.getClass().getSimpleName() + " at "
					+ escalation.getStackTrace()[0].toString() + "]");

			// Record exception to be thrown later
			this.exception = escalation;
		}
	}

	/**
	 * <p>
	 * Validates that no top level escalation occurred.
	 * <p>
	 * This method will clear the escalation on exit.
	 */
	public void validateNoTopLevelEscalation() throws Throwable {
		synchronized (this.exceptionLock) {
			try {
				if (this.exception != null) {
					throw this.exception;
				}
			} finally {
				// Exception thrown, so have it cleared
				this.exception = null;
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try {

			// Close the constructed office floors
			for (OfficeFloor officeFloor : this.constructedOfficeFloors) {
				officeFloor.closeOfficeFloor();
			}

			// Propagate possible failure
			synchronized (this.exceptionLock) {
				// Return if no failure
				if (this.exception == null) {
					return;
				}

				// Propagate failure
				if (this.exception instanceof Exception) {
					throw (Exception) this.exception;
				} else if (this.exception instanceof Error) {
					throw (Error) this.exception;
				} else {
					StringWriter buffer = new StringWriter();
					this.exception.printStackTrace(new PrintWriter(buffer));
					fail("Unknown failure "
							+ this.exception.getClass().getName() + ": "
							+ buffer.toString());
				}
			}
		} finally {
			super.tearDown();
		}
	}

	/**
	 * Obtains the {@link OfficeFloorBuilder}.
	 * 
	 * @return {@link OfficeFloorBuilder}.
	 */
	protected OfficeFloorBuilder getOfficeFloorBuilder() {
		return this.officeFloorBuilder;
	}

	/**
	 * Obtains the {@link OfficeBuilder}.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	protected OfficeBuilder getOfficeBuilder() {
		return this.officeBuilder;
	}

	/**
	 * Obtains the name of the {@link OfficeFloor} currently being constructed.
	 * 
	 * @return Name of the {@link OfficeFloor} currently being constructed.
	 */
	protected String getOfficeFloorName() {
		return "officefloor-" + OFFICE_FLOOR_INDEX;
	}

	/**
	 * Obtains the name of the {@link Office} currently being constructed.
	 * 
	 * @return Name of the {@link Office} currently being constructed.
	 */
	protected String getOfficeName() {
		return "office-" + OFFICE_INDEX;
	}

	/**
	 * Facade method to register a {@link Work}.
	 * 
	 * @return {@link WorkBuilder} for the {@link Work}.
	 */
	public <W extends Work> WorkBuilder<W> constructWork(String workName,
			WorkFactory<W> workFactory) {

		// Construct the work
		WorkBuilder<W> workBuilder = this.officeBuilder.addWork(workName,
				workFactory);

		// Make current work builder
		this.workBuilder = workBuilder;

		// Return the work builder
		return workBuilder;
	}

	/**
	 * Facade method to register a {@link Work}.
	 * 
	 * @return {@link WorkBuilder} for the {@link Work}.
	 */
	public <W extends Work> WorkBuilder<W> constructWork(String workName,
			final W work, String initialTaskName) {

		// Create the Work Factory
		WorkFactory<W> workFactory = new WorkFactory<W>() {
			public W createWork() {
				return work;
			}
		};

		// Construct the work builder
		WorkBuilder<W> workBuilder = this.constructWork(workName, workFactory);

		// Specify the initial task (if provided)
		if (initialTaskName != null) {
			workBuilder.setInitialTask(initialTaskName);
		}

		// Return the work builder
		return workBuilder;
	}

	/**
	 * Constructs the {@link ReflectiveWorkBuilder}.
	 * 
	 * @param workObject
	 *            Work object.
	 * @param workName
	 *            Work name.
	 * @param initialTaskName
	 *            Initial task name. May be <code>null</code> if no initial
	 *            {@link Task}.
	 * @return {@link ReflectiveWorkBuilder}.
	 */
	public ReflectiveWorkBuilder constructWork(Object workObject,
			String workName, String initialTaskName) {
		// Return the created work builder
		return new ReflectiveWorkBuilder(this, workName, workObject,
				this.officeBuilder, initialTaskName);
	}

	/**
	 * <p>
	 * Specifies whether to record the invocations of the
	 * {@link ReflectiveTaskBuilder} instances.
	 * <p>
	 * This is necessary as stress tests using the {@link ReflectiveTaskBuilder}
	 * will get {@link OutOfMemoryError} issues should every {@link Task}
	 * executed be recorded.
	 * <p>
	 * By default this is <code>false</code> to not record.
	 * 
	 * @param isRecord
	 *            <code>true</code> to record the {@link Task} instances
	 *            invoked.
	 */
	public void setRecordReflectiveTaskMethodsInvoked(boolean isRecord) {
		synchronized (this.reflectiveTaskInvokedMethods) {
			this.isRecordReflectiveTaskMethodsInvoked = isRecord;
		}
	}

	/**
	 * Invoked by the {@link ReflectiveTaskBuilder} when it executes the method.
	 * 
	 * @param methodName
	 *            Name of method being invoked.
	 */
	public void recordReflectiveTaskMethodInvoked(String methodName) {
		synchronized (this.reflectiveTaskInvokedMethods) {
			if (this.isRecordReflectiveTaskMethodsInvoked) {
				this.reflectiveTaskInvokedMethods.add(methodName);
			}
		}
	}

	/**
	 * Validates the order the {@link ReflectiveTaskBuilder} invoked the
	 * methods.
	 * 
	 * @param methodNames
	 *            Order that the reflective methods should be invoked.
	 * @see #setRecordReflectiveTaskMethodInvoked(boolean)
	 */
	public void validateReflectiveMethodOrder(String... methodNames) {
		synchronized (this.reflectiveTaskInvokedMethods) {

			// Create expected method calls
			StringBuilder actualMethods = new StringBuilder();
			for (String methodName : methodNames) {
				actualMethods.append(methodName.trim() + " ");
			}

			// Create the actual method calls
			StringBuilder expectedMethods = new StringBuilder();
			for (String methodName : this.reflectiveTaskInvokedMethods) {
				expectedMethods.append(methodName.trim() + " ");
			}

			// Validate appropriate methods called
			assertEquals(
					"Incorrect methods invoked [ " + actualMethods.toString()
							+ "]", actualMethods.toString(),
					expectedMethods.toString());
		}
	}

	/**
	 * Facade method to register a {@link Task}.
	 * 
	 * @return {@link TaskBuilder} for the {@link Task}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> TaskBuilder<W, D, F> constructTask(
			String taskName, TaskFactory<W, D, F> taskFactory, String teamName) {

		// Create the Task Builder
		TaskBuilder taskBuilder = ((WorkBuilder) this.workBuilder).addTask(
				taskName, taskFactory);

		// Link to Team
		taskBuilder.setTeam(teamName);

		// Return the Task Builder
		return taskBuilder;
	}

	/**
	 * Facade method to register a {@link Task}.
	 * 
	 * @return {@link TaskBuilder} for the {@link Task}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> TaskBuilder<W, D, F> constructTask(
			String taskName, TaskFactory<W, D, F> taskFactory, String teamName,
			String moName, Class<?> moType, String nextTaskName,
			Class<?> nextTaskArgumentType) {

		// Create the Task Builder
		TaskBuilder taskBuilder = this.constructTask(taskName, taskFactory,
				teamName);

		// Register the next task and managed object
		if (nextTaskName != null) {
			taskBuilder.setNextTaskInFlow(nextTaskName, nextTaskArgumentType);
		}
		if (moName != null) {
			taskBuilder.linkManagedObject(0, moName, moType);
		}

		// Return the task builder
		return taskBuilder;
	}

	/**
	 * Facade method to register a {@link Task}.
	 * 
	 * @return {@link TaskBuilder} for the {@link Task}.
	 */
	@SuppressWarnings("rawtypes")
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> TaskBuilder constructTask(
			String taskName, final Task<W, D, F> task, String teamName,
			String nextTaskName, Class<?> nextTaskArgumentType) {

		// Create the Task Factory
		TaskFactory<W, D, F> taskFactory = new TaskFactory<W, D, F>() {
			public Task<W, D, F> createTask(W work) {
				return task;
			}
		};

		// Construct and return the Task
		return this.constructTask(taskName, taskFactory, teamName, null, null,
				nextTaskName, nextTaskArgumentType);
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 */
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> ManagedObjectBuilder<H> constructManagedObject(
			String managedObjectName, Class<MS> managedObjectSourceClass) {

		// Obtain the managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Create the Managed Object Builder
		ManagedObjectBuilder<H> managedObjectBuilder = this
				.getOfficeFloorBuilder().addManagedObject(
						managedObjectSourceName, managedObjectSourceClass);

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName,
				managedObjectSourceName);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 */
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> ManagedObjectBuilder<H> constructManagedObject(
			String managedObjectName, Class<MS> managedObjectSourceClass,
			String managingOffice) {

		// Obtain the managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Create the Managed Object Builder
		ManagedObjectBuilder<H> managedObjectBuilder = this
				.getOfficeFloorBuilder().addManagedObject(
						managedObjectSourceName, managedObjectSourceClass);

		// Flag managing office
		managedObjectBuilder.setManagingOffice(managingOffice)
				.setInputManagedObjectName(managedObjectName);

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName,
				managedObjectSourceName);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 */
	public <D extends Enum<D>, H extends Enum<H>> ManagedObjectBuilder<H> constructManagedObject(
			String managedObjectName,
			ManagedObjectSourceMetaData<D, H> metaData,
			ManagedObject managedObject, String managingOffice) {

		// Obtain managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Bind Managed Object
		ManagedObjectBuilder<H> managedObjectBuilder = MockManagedObjectSource
				.bindManagedObject(managedObjectSourceName, managedObject,
						metaData, this.getOfficeFloorBuilder());

		// Flag managing office
		managedObjectBuilder.setManagingOffice(managingOffice)
				.setInputManagedObjectName(managedObjectName);

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName,
				managedObjectSourceName);

		// Return the builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 */
	@SuppressWarnings("rawtypes")
	public ManagedObjectBuilder<?> constructManagedObject(
			String managedObjectName, ManagedObject managedObject,
			String managingOffice) {

		// Create the mock Managed Object Source meta-data
		ManagedObjectSourceMetaData<?, ?> metaData = new MockManagedObjectSourceMetaData(
				managedObject);

		// Register the Managed Object
		return this.constructManagedObject(managedObjectName, metaData,
				managedObject, managingOffice);
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 */
	public ManagedObjectBuilder<?> constructManagedObject(final Object object,
			String managedObjectName, String managingOffice) {

		// Create the wrapping Managed Object
		ManagedObject managedObject = new ManagedObject() {
			public Object getObject() {
				return object;
			}
		};

		// Register the managed object
		return this.constructManagedObject(managedObjectName, managedObject,
				managingOffice);
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param team
	 *            {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	public TeamBuilder<?> constructTeam(String teamName, Team team) {

		// Obtain the office floor team name
		String officeFloorTeamName = "of-" + teamName;

		// Bind the team into the office floor
		TeamBuilder<?> teamBuilder = MockTeamSource.bindTeamBuilder(
				this.officeFloorBuilder, officeFloorTeamName, team);

		// Link into the Office
		this.officeBuilder.registerTeam(teamName, officeFloorTeamName);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @return {@link TeamBuilder}.
	 */
	public <TS extends TeamSource> TeamBuilder<?> constructTeam(
			String teamName, Class<TS> teamSourceClass) {

		// Obtain the office floor team name
		String officeFloorTeamName = "of-" + teamName;

		// Add the team to the office floor
		TeamBuilder<?> teamBuilder = this.officeFloorBuilder.addTeam(
				officeFloorTeamName, teamSourceClass);

		// Link into the office
		this.officeBuilder.registerTeam(teamName, officeFloorTeamName);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * Facade method to create a {@link Administrator}.
	 * 
	 * @param adminName
	 *            Name of the {@link Administrator}.
	 * @param administrator
	 *            {@link Administrator}.
	 * @param administratorMetaData
	 *            {@link AdministratorSourceMetaData}.
	 * @param teamName
	 *            Name of {@link Team} for {@link Administrator} {@link Duty}
	 *            instances.
	 * @return {@link AdministratorBuilder}.
	 */
	public <I extends Object, A extends Enum<A>> AdministratorBuilder<A> constructAdministrator(
			String adminName, Administrator<I, A> administrator,
			AdministratorSourceMetaData<I, A> administratorMetaData,
			String teamName) {

		// Bind the Administrator
		AdministratorBuilder<A> adminBuilder = MockAdministratorSource
				.bindAdministrator(adminName, administrator,
						administratorMetaData, this.officeBuilder);

		// Configure the administrator
		adminBuilder.setTeam(teamName);

		// Return the administrator builder
		return adminBuilder;
	}

	/**
	 * Facade method to construct an {@link Administrator}.
	 * 
	 * @param adminName
	 *            Name of the {@link Administrator}.
	 * @param adminSource
	 *            {@link AdministratorSource} {@link Class}.
	 * @param teamName
	 *            Name of {@link Team} for {@link Administrator} {@link Duty}
	 *            instances.
	 * @return {@link AdministratorBuilder}.
	 */
	public <I extends Object, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> constructAdministrator(
			String adminName, Class<AS> adminSource, String teamName) {

		// Create the Administrator Builder
		AdministratorBuilder<A> adminBuilder = this.officeBuilder
				.addThreadAdministrator(adminName, adminSource);

		// Configure the administrator
		adminBuilder.setTeam(teamName);

		// Return the administrator builder
		return adminBuilder;
	}

	/**
	 * Facade method to create the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	protected OfficeFloor constructOfficeFloor() throws Exception {

		// Construct the Office Floor
		this.officeFloor = this.officeFloorBuilder.buildOfficeFloor();
		this.constructedOfficeFloors.add(this.officeFloor);

		// Initiate for constructing another office
		OFFICE_FLOOR_INDEX++;
		this.officeFloorBuilder = OfficeFrame.getInstance()
				.createOfficeFloorBuilder(this.getOfficeFloorName());
		OFFICE_INDEX++;
		this.officeBuilder = this.officeFloorBuilder.addOffice(this
				.getOfficeName());

		// Return the Office Floor
		return this.officeFloor;
	}

	/**
	 * Facade method to invoke work of an office. It will create the
	 * {@link OfficeFloor} if necessary and times out after 3 seconds if invoked
	 * {@link Work} is not complete.
	 * 
	 * @param workName
	 *            Name of the work to invoke.
	 * @param parameter
	 *            Parameter.
	 * @throws Exception
	 *             If fails to construct office or work invocation failure.
	 */
	protected void invokeWork(String workName, Object parameter)
			throws Exception {
		this.invokeWork(workName, parameter, 3);
	}

	/**
	 * Facade method to invoke work of an office. It will create the office
	 * floor if necessary.
	 * 
	 * @param workName
	 *            Name of the work to invoke.
	 * @param parameter
	 *            Parameter.
	 * @param secondsToRun
	 *            Seconds to run.
	 * @throws Exception
	 *             If fails to construct office or work invocation failure.
	 */
	protected void invokeWork(String workName, Object parameter,
			int secondsToRun) throws Exception {

		// Obtain the name of the office being constructed
		String officeName = this.getOfficeName();

		// Determine if required to construct work
		if (this.officeFloor == null) {
			// Construct the office floor
			this.officeFloor = this.constructOfficeFloor();

			// Open the office floor
			this.officeFloor.openOfficeFloor();
		}

		// Invoke the work
		Office office = this.officeFloor.getOffice(officeName);
		WorkManager workManager = office.getWorkManager(workName);
		ProcessFuture processFuture = workManager.invokeWork(parameter);

		// Block until flow is complete (or times out)
		int iteration = 0;
		long startBlockTime = System.currentTimeMillis();
		while (!processFuture.isComplete()) {

			// Only timeout if positive time to run
			if (secondsToRun > 0) {
				// Provide heap diagnostics and time out
				this.timeout(startBlockTime, secondsToRun);
			}

			// Output heap diagnostics after every approximate 3 seconds
			iteration++;
			if ((iteration % 30) == 0) {
				iteration = 0;
				this.printHeapMemoryDiagnostics();
			}

			// Wait some time as still executing
			synchronized (processFuture) {
				processFuture.wait(100);
			}
		}
	}

}