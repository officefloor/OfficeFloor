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
package net.officefloor.frame.integrate.managedobject.asynchronous;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests loading the {@link ManagedObject} asynchronously.
 * 
 * @author Daniel
 */
public class AsyncManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link TestWork}.
	 */
	private TestWork work;

	/**
	 * {@link TestTeam}.
	 */
	private TestTeam team;

	/**
	 * {@link Job}.
	 */
	private Job taskContainer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Setup
		super.setUp();

		String officeName = this.getOfficeName();

		// Construct the managed object
		ManagedObjectBuilder<?> moBuilder = this.constructManagedObject("MO",
				TestManagedObjectSource.class, officeName);
		moBuilder.setDefaultTimeout(1000);

		// Construct the work to execute
		this.work = new TestWork();
		ReflectiveWorkBuilder workBuilder = this.constructWork(this.work,
				"WORK", "task");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("task",
				"TEAM");
		taskBuilder.buildObject("MO");

		// Construct team to run task
		this.team = new TestTeam();
		this.constructTeam("TEAM", this.team);

		// Open the Office Floor
		this.officeFloor = this.constructOfficeFloor();
		this.officeFloor.openOfficeFloor();

		// Execute the task (to obtain the task container)
		WorkManager workManager = this.officeFloor.getOffice(officeName)
				.getWorkManager("WORK");
		workManager.invokeWork(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.test.AbstractOfficeConstructTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {

		// Close the Office Floor
		this.officeFloor.closeOfficeFloor();

		// Tear down
		super.tearDown();
	}

	/**
	 * Ensures that able to load {@link ManagedObject} asynchronously.
	 */
	public void testAsynchronousLoadManagedObject() {

		// Execute task, attempting to load managed object
		this.executeTask(false, true);

		// Asynchronously load the task, ensuring task wakes up
		this.loadManagedObject(new TestManagedObject(), true);

		// Task awake, so on execution should complete
		this.executeTask(true, true);
	}

	/**
	 * Ensures able to handle asynchronous operations by the
	 * {@link ManagedObject}.
	 */
	public void testAsynchronousOperation() {

		final TestManagedObject mo = new TestManagedObject();

		// Load the managed object
		this.executeTask(false, true);
		this.loadManagedObject(mo, true);

		// Indicate asynchronous operation, so should not execute task
		mo.startAsynchronousOperation();
		this.executeTask(false, true);

		// Asynchronous operation over, wake up the task
		this.team.isAssignedTask = false;
		mo.completeAsynchronousOperation();
		assertTrue("Ensure task woken on asynchronous operation completion",
				this.team.isAssignedTask);
	}

	/**
	 * Executes the {@link Job}.
	 * 
	 * @param isExpectExecute
	 *            Flag indicating whether the {@link TestWork} task should be
	 *            executed.
	 * @param isExpectComplete
	 *            Flag indicating the expected return of
	 *            {@link Job#doJob(JobContext)}.
	 */
	private void executeTask(boolean isExpectExecute, boolean isExpectComplete) {

		// Reset the task being executed
		this.work.isTaskExecuted = false;

		// Execute the task
		boolean isComplete = this.taskContainer.doJob(new JobContext() {

			@Override
			public boolean continueExecution() {
				return true;
			}

			@Override
			public long getTime() {
				return System.currentTimeMillis();
			}
		});

		// Ensure correct completion value
		assertEquals("Task incorrect completion return", isExpectComplete,
				isComplete);

		// Ensure whether task should be executed
		assertEquals("Task incorrect execution", isExpectExecute,
				this.work.isTaskExecuted);
	}

	/**
	 * Loads the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link AsynchronousManagedObject}.
	 * @param isExpectWakeup
	 *            Flag indicating if {@link Task} is to be assigned to a
	 *            {@link Team} (ultimately being waken).
	 */
	private void loadManagedObject(AsynchronousManagedObject managedObject,
			boolean isExpectWakeup) {

		// Reset task being assigned
		this.team.isAssignedTask = false;

		// Load the managed object
		TestManagedObjectSource.loadManagedObject(managedObject);

		// Ensure wake up of task
		assertEquals("Task incorrect wakeup", isExpectWakeup,
				this.team.isAssignedTask);
	}

	/**
	 * Test {@link Work}.
	 */
	public class TestWork {

		/**
		 * Flag indicating if the task was executed.
		 */
		public boolean isTaskExecuted = false;

		/**
		 * Task for execution.
		 * 
		 * @param object
		 *            Object from the {@link ManagedObject}.
		 */
		public void task(Object object) {
			// Flag that the task was executed
			this.isTaskExecuted = true;
		}
	}

	/**
	 * Test {@link Team}.
	 */
	private class TestTeam implements Team {

		/**
		 * Flag indicating if task was assigned to this {@link Team}.
		 */
		public boolean isAssignedTask = false;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.spi.team.Team#assignTask(net.officefloor.frame
		 * .spi.team.TaskContainer)
		 */
		@Override
		public void assignJob(Job task) {
			// Determine if setup call
			if (AsyncManagedObjectTest.this.taskContainer == null) {
				// Setup call, specify task and return
				AsyncManagedObjectTest.this.taskContainer = task;
				return;
			}

			// Ensure always the same task (as only one)
			assertEquals("Incorrect task",
					AsyncManagedObjectTest.this.taskContainer, task);

			// Indicate task assigned to this team
			assertEquals("Only 1 assigning of task expected", false,
					this.isAssignedTask);
			this.isAssignedTask = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.team.Team#startWorking()
		 */
		@Override
		public void startWorking() {
			// Mock, so do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.team.Team#stopWorking()
		 */
		@Override
		public void stopWorking() {
			// Mock, so do nothing
		}
	}

	/**
	 * Test {@link AsynchronousManagedObject}.
	 */
	private class TestManagedObject implements AsynchronousManagedObject {

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * Flag started an asynchronous operation.
		 */
		public void startAsynchronousOperation() {
			this.listener.notifyStarted();
		}

		/**
		 * Flag completed an asynchronous operation.
		 */
		public void completeAsynchronousOperation() {
			this.listener.notifyComplete();
		}

		/*
		 * ======================================================================
		 * ====== AsynchronousManagedObject
		 * ======================================
		 * ======================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.spi.managedobject.AsynchronousManagedObject
		 * #registerAsynchronousCompletionListener
		 * (net.officefloor.frame.spi.managedobject.AsynchronousListener)
		 */
		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			this.listener = listener;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
		 */
		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

}
