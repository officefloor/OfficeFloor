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
package net.officefloor.frame.manage;

import java.sql.Connection;
import java.util.Arrays;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Ensures the external management API of {@link OfficeFloor} responses
 * correctly.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExternalManagementTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * {@link Office} name.
	 */
	private String officeName;

	/**
	 * {@link Work} name.
	 */
	private final String workName = "WORK";

	/**
	 * {@link MockWork} instance.
	 */
	private MockWork mockWork;

	/**
	 * Differentiator.
	 */
	private final Object differentiator = "Differentiator";

	/**
	 * No initial {@link Task} {@link Work} name.
	 */
	private final String noInitialTaskWorkName = "NO_INITIAL_TASK_WORK";

	/**
	 * {@link OfficeFloor} to test management.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the Office name
		this.officeName = this.getOfficeName();

		// Construct Team
		final String TEAM_NAME = "TEAM";
		this.constructTeam("TEAM", new PassiveTeam());

		// Construct the Work
		this.mockWork = new MockWork();
		ReflectiveWorkBuilder workBuilder = this.constructWork(this.mockWork,
				this.workName, "initialTask");
		ReflectiveTaskBuilder initialTask = workBuilder.buildTask(
				"initialTask", TEAM_NAME);
		initialTask.buildParameter();
		initialTask.getBuilder().setDifferentiator(this.differentiator);
		workBuilder.buildTask("anotherTask", TEAM_NAME).buildParameter();

		// Construct the Work (with no initial task)
		MockWork noInitialTaskWork = new MockWork();
		this.constructWork(noInitialTaskWork, this.noInitialTaskWorkName, null);

		// Compile OfficeFloor
		this.officeFloor = this.constructOfficeFloor();

		// Ensure open to allow obtaining meta-data
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Ensure that not able to open {@link OfficeFloor} instance twice.
	 */
	public void testOpenOfficeFloorOnce() throws Exception {
		try {
			this.officeFloor.openOfficeFloor();
			fail("Should not be able open twice");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", ex.getMessage(),
					"OfficeFloor is already open");
		}
	}

	/**
	 * Ensure able to obtain {@link Office} listing.
	 */
	public void testOfficeListing() throws UnknownOfficeException {

		// Ensure correct office listing
		String[] officeNames = this.officeFloor.getOfficeNames();
		this.assertNames(officeNames, this.officeName);

		// Ensure able to obtain Office
		Office office = this.officeFloor.getOffice(this.officeName);
		assertNotNull("Must have Office", office);
	}

	/**
	 * Ensure throws exception on unknown {@link Office} requested.
	 */
	public void testUnknownOffice() {
		final String unknownOfficeName = "Unknown Office - adding name to ensure different "
				+ this.officeName;
		try {
			this.officeFloor.getOffice(unknownOfficeName);
			fail("Should not be able to obtain unknown Office");
		} catch (UnknownOfficeException ex) {
			assertEquals("Incorrect office", unknownOfficeName, ex
					.getUnknownOfficeName());
			assertEquals("Incorrect cause", "Unknown Office '"
					+ unknownOfficeName + "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain {@link Work} listing.
	 */
	public void testWorkListing() throws UnknownOfficeException,
			UnknownWorkException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Ensure correct work list
		String[] workNames = office.getWorkNames();
		this.assertNames(workNames, this.workName, this.noInitialTaskWorkName);

		// Ensure able to obtain WorkManager
		WorkManager work = office.getWorkManager(this.workName);
		assertNotNull("Must have WorkManager", work);
	}

	/**
	 * Ensures throws exception on unknown {@link WorkManager}.
	 */
	public void testUnknownWork() throws UnknownOfficeException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		final String unknownWorkName = "Unknown Work";
		try {
			office.getWorkManager(unknownWorkName);
			fail("Should not be able to obtain unknown WorkManager");
		} catch (UnknownWorkException ex) {
			assertEquals("Incorrect work", unknownWorkName, ex
					.getUnknownWorkName());
			assertEquals("Incorrect cause", "Unknown Work '" + unknownWorkName
					+ "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain {@link Work} initial parameter type.
	 */
	public void testWorkParameterType() throws UnknownOfficeException,
			UnknownWorkException, NoInitialTaskException {

		// Obtain the Work Manager
		WorkManager work = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName);

		// Ensure correct work parameter type
		assertEquals("Incorrect parameter type", Connection.class, work
				.getWorkParameterType());
	}

	/**
	 * Ensures the initial {@link Task} is invoked.
	 */
	public void testInvokeWork() throws UnknownOfficeException,
			UnknownWorkException, NoInitialTaskException,
			InvalidParameterTypeException {

		// Obtain the Work Manager
		WorkManager work = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName);

		// Invoke the Work
		Connection connection = this.createMock(Connection.class);
		ProcessFuture future = work.invokeWork(connection);

		// Ensure initial task invoked
		assertTrue("Process should be complete (as passive team)", future
				.isComplete());
		assertTrue("Initial task should be invoked",
				this.mockWork.isInitialTaskInvoked);
	}

	/**
	 * Ensure not able to use {@link Work} directly.
	 */
	public void testFailWorkAsNoInitialTask() throws UnknownOfficeException,
			UnknownWorkException, InvalidParameterTypeException {

		// Obtain the Work Manager
		WorkManager work = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.noInitialTaskWorkName);

		// Ensure not able to obtain initial parameter type
		try {
			work.getWorkParameterType();
			fail("Should not able to obtain work parameter type");
		} catch (NoInitialTaskException ex) {
			assertEquals("Incorrect cause", "No initial task for work '"
					+ this.noInitialTaskWorkName + "'", ex.getMessage());
		}

		// Ensure not able to invoke Work
		try {
			work.invokeWork(null);
			fail("Should not able to invoke Work");
		} catch (NoInitialTaskException ex) {
			assertEquals("Incorrect cause", "No initial task for work '"
					+ this.noInitialTaskWorkName + "'", ex.getMessage());
		}
	}

	/**
	 * Ensure indicates if invalid parameter type.
	 */
	public void testFailInvokeWorkAsInvalidParameterType()
			throws UnknownOfficeException, UnknownWorkException,
			NoInitialTaskException {

		// Obtain the Work Manager
		WorkManager work = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName);

		// Ensure fail on invalid parameter
		try {
			work.invokeWork("Invalid parameter type");
			fail("Should not be successful on invalid parameter type");
		} catch (InvalidParameterTypeException ex) {
			assertEquals("Incorrect failure", "Invalid parameter type (input="
					+ String.class.getName() + ", required="
					+ Connection.class.getName() + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain {@link Task} listing.
	 */
	public void testTaskListing() throws UnknownOfficeException,
			UnknownWorkException, UnknownTaskException {

		// Obtain the Work
		WorkManager work = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName);

		// Ensure correct task list
		String[] taskNames = work.getTaskNames();
		this.assertNames(taskNames, "initialTask", "anotherTask");

		// Ensure able to obtain TaskManager
		TaskManager task = work.getTaskManager("anotherTask");
		assertNotNull("Must have TaskManager", task);
	}

	/**
	 * Ensures throws exception on unknown {@link TaskManager}.
	 */
	public void testUnknownTask() throws UnknownOfficeException,
			UnknownWorkException {

		// Obtain the Work
		WorkManager work = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName);

		final String unknownTaskName = "Unknown Task";
		try {
			work.getTaskManager(unknownTaskName);
			fail("Should not be able to obtain unknown TaskManager");
		} catch (UnknownTaskException ex) {
			assertEquals("Incorrect task", unknownTaskName, ex
					.getUnknownTaskName());
			assertEquals("Incorrect cause", "Unknown Task '" + unknownTaskName
					+ "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain {@link Task} differentiator.
	 */
	public void testTaskDifferentiator() throws UnknownOfficeException,
			UnknownWorkException, UnknownTaskException {

		// Obtain the Task Manager
		TaskManager task = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName).getTaskManager("initialTask");

		// Ensure correct differentiator
		assertEquals("Incorrect differentiator", this.differentiator, task
				.getDifferentiator());
	}

	/**
	 * Ensure able to obtain {@link Task} parameter type.
	 */
	public void testTaskParameterType() throws UnknownOfficeException,
			UnknownWorkException, UnknownTaskException {

		// Obtain the Task Manager
		TaskManager task = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName).getTaskManager("initialTask");

		// Ensure correct parameter type
		assertEquals("Incorrect parameter type", Connection.class, task
				.getParameterType());
	}

	/**
	 * Ensure able to invoke the {@link Task}.
	 */
	public void testInvokeTask() throws UnknownOfficeException,
			UnknownWorkException, UnknownTaskException,
			InvalidParameterTypeException {

		// Obtain the Task Manager
		TaskManager task = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName).getTaskManager("initialTask");

		// Invoke the Task
		Connection connection = this.createMock(Connection.class);
		ProcessFuture future = task.invokeTask(connection);

		// Ensure task invoked
		assertTrue("Process should be complete (as passive team)", future
				.isComplete());
		assertTrue("Task should be invoked", this.mockWork.isInitialTaskInvoked);
	}

	/**
	 * Ensure indicates if invalid parameter type.
	 */
	public void testFailInvokeTaskAsInvalidParameterType()
			throws UnknownOfficeException, UnknownWorkException,
			UnknownTaskException {

		// Obtain the Task Manager
		TaskManager task = this.officeFloor.getOffice(this.officeName)
				.getWorkManager(this.workName).getTaskManager("initialTask");

		// Ensure fail on invalid parameter
		try {
			task.invokeTask("Invalid parameter type");
			fail("Should not be successful on invalid parameter type");
		} catch (InvalidParameterTypeException ex) {
			assertEquals("Incorrect failure", "Invalid parameter type (input="
					+ String.class.getName() + ", required="
					+ Connection.class.getName() + ")", ex.getMessage());
		}
	}

	/**
	 * Asserts the list of names match.
	 * 
	 * @param actual
	 *            Actual names.
	 * @param expected
	 *            Expected names.
	 */
	private void assertNames(String[] actual, String... expected) {

		// Ensure same length
		assertEquals("Incorrect number of names", expected.length,
				actual.length);

		// Sort
		Arrays.sort(expected);
		Arrays.sort(actual);

		// Ensure values the same
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Incorrect name " + i, expected[i], actual[i]);
		}
	}

	/**
	 * Mock {@link Work}.
	 */
	public class MockWork {

		/**
		 * Indicates if the initial {@link Task} has been invoked.
		 */
		public boolean isInitialTaskInvoked = false;

		/**
		 * Initial {@link Task}.
		 * 
		 * @param parameter
		 *            Parameter with distinct type for identifying in testing.
		 */
		public void initialTask(Connection parameter) {
			this.isInitialTaskInvoked = true;
		}

		/**
		 * Another task to possibly execute.
		 * 
		 * @param parameter
		 *            Parameter with distinct type for identifying in testing.
		 */
		public void anotherTask(Integer parameter) {
		}
	}

}