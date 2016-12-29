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
package net.officefloor.frame.integrate.managedobject.asynchronous;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveFunctionBuilder.ReflectiveFunctionBuilder;

/**
 * Tests loading the {@link ManagedObject} asynchronously.
 * 
 * @author Daniel Sagenschneider
 */
public class AsyncManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TestWork}.
	 */
	private TestWork work;

	/**
	 * {@link TestTeam}.
	 */
	private TestTeam team;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Construct the managed object
		ManagedObjectBuilder<None> moBuilder = this.constructManagedObject("MO",
				TestManagedObjectSource.class, officeName);
		moBuilder.setTimeout(1000);

		// Construct the work to execute
		this.work = new TestWork();
		ReflectiveFunctionBuilder workBuilder = this.constructWork(this.work,
				"WORK", "task");
		ReflectiveFunctionBuilder taskBuilder = workBuilder.buildTask("task",
				"TEAM");
		taskBuilder.buildObject("MO", ManagedObjectScope.WORK);
		taskBuilder.buildTaskContext();

		// Construct team to run task
		this.team = new TestTeam();
		this.constructTeam("TEAM", this.team);

		// Open the Office Floor and invoke work
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		officeFloor.getOffice(officeName).getWorkManager("WORK")
				.invokeWork(null);
	}

	/**
	 * Ensures that able to load {@link ManagedObject} asynchronously.
	 */
	public void testAsynchronousSourceManagedObject() throws Exception {

		// Execute Job, attempting to load managed object
		this.team.executeJob(false, true);

		// Load managed object later, waiting for activation by OfficeManager
		synchronized (this.team) {
			this.team.isAssignedJob = false;
			TestManagedObjectSource.loadManagedObject(new TestManagedObject());
			this.team.wait(1000);
			assertTrue("Job should be activated by OfficeManager",
					this.team.isAssignedJob);
		}

		// Managed Object available, so should complete on next execution
		this.team.executeJob(true, true);
	}

	/**
	 * Ensures able to handle asynchronous operations by the
	 * {@link ManagedObject}.
	 */
	public void testAsynchronousOperation() throws Exception {

		final TestManagedObject mo = new TestManagedObject();

		// Load managed object and trigger asynchronous operation
		TestManagedObjectSource.loadManagedObject(mo);
		this.work.isTriggerAsynchronousOperation = true;
		this.team.executeJob(true, false); // execute and need re-executing

		// Asynchronous operation in progress, so should not execute Job
		this.team.executeJob(false, true);

		// Notify asynchronous operation over, activate by OfficeManager
		synchronized (this.team) {
			// Indicate asynchronous operation over
			this.team.isAssignedJob = false;
			mo.listener.notifyComplete();
			this.team.wait(1000);
			assertTrue(
					"On completion of async operation, Job should be activated by OfficeManager",
					this.team.isAssignedJob);
		}

		// Asynchronous operation over, so should complete on next execution
		this.team.executeJob(true, true);
	}

	/**
	 * Test {@link Team}.
	 */
	private class TestTeam implements Team {

		/**
		 * {@link Job}.
		 */
		private Job job;

		/**
		 * Flag indicating if {@link Job} was assigned to this {@link Team}.
		 */
		public boolean isAssignedJob = false;

		/**
		 * Executes the {@link Job}.
		 * 
		 * @param isExpectExecute
		 *            Flag indicating whether the {@link TestWork} should be
		 *            executed.
		 * @param isExpectComplete
		 *            Flag indicating the expected return of
		 *            {@link Job#doJob(JobContext)}.
		 */
		public synchronized void executeJob(boolean isExpectExecute,
				boolean isExpectComplete) {

			// Reset the Job to not be executed
			AsyncManagedObjectTest.this.work.isJobExecuted = false;

			// Execute the job
			boolean isComplete = this.job.doJob(new JobContext() {

				private final TeamIdentifier teamIdentifier = MockTeamSource
						.createTeamIdentifier();

				@Override
				public long getTime() {
					return System.currentTimeMillis();
				}

				@Override
				public TeamIdentifier getCurrentTeam() {
					return this.teamIdentifier;
				}

				@Override
				public boolean continueExecution() {
					return true;
				}
			});

			// Validate details
			assertEquals("doJob return incorrect", isExpectComplete, isComplete);
			assertEquals("Job execution incorrect", isExpectExecute,
					AsyncManagedObjectTest.this.work.isJobExecuted);
		}

		/*
		 * ==================== Team ================================
		 */

		@Override
		public synchronized void assignJob(Job job, TeamIdentifier assignerTeam) {

			// Determine if invoke Work assignment
			if (this.job == null) {
				this.job = job;
				return;
			}

			// Assignment after managed object source change
			assertEquals("Incorrect job", this.job, job);

			// Indicate Job assigned to this team
			assertEquals("Only 1 assigning of Job expected", false,
					this.isAssignedJob);
			this.isAssignedJob = true;

			// Notify test waiting for OfficeManager activation
			this.notify();
		}

		@Override
		public void startWorking() {
			// Mock, so do nothing
		}

		@Override
		public void stopWorking() {
			// Mock, so do nothing
		}
	}

	/**
	 * Test {@link Work}.
	 */
	public class TestWork {

		/**
		 * Flag indicating if the {@link Job} was executed.
		 */
		public boolean isJobExecuted = false;

		/**
		 * Flag indicating if the {@link Job} will trigger an asynchronous
		 * operation and not complete.
		 */
		public boolean isTriggerAsynchronousOperation = false;

		/**
		 * {@link Job} for execution.
		 * 
		 * @param object
		 *            Object from the {@link ManagedObject}.
		 * @param taskContext
		 *            {@link ManagedFunctionContext}.
		 */
		public void task(Object object, ManagedFunctionContext<?, ?, ?> taskContext) {

			// Flag that the Job was executed
			this.isJobExecuted = true;

			// Determine if trigger an asynchronous operation
			if (this.isTriggerAsynchronousOperation) {
				// Trigger asynchronous operation and not complete
				TestManagedObject mo = (TestManagedObject) object;
				mo.listener.notifyStarted();
				taskContext.setComplete(false);

				// Clear trigger flag (as triggered)
				this.isTriggerAsynchronousOperation = false;
			}
		}
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class TestManagedObjectSource extends
			AbstractAsyncManagedObjectSource<None, None> {

		/**
		 * {@link ManagedObjectUser}.
		 */
		private static ManagedObjectUser managedObjectUser = null;

		/**
		 * {@link ManagedObject} to be loaded immediately.
		 */
		private static ManagedObject managedObject = null;

		/**
		 * Loads the {@link ManagedObject}.
		 * 
		 * @param managedObject
		 *            {@link ManagedObject}.
		 */
		public static void loadManagedObject(ManagedObject managedObject) {
			if (managedObjectUser != null) {
				// Load managed object
				managedObjectUser.setManagedObject(managedObject);
			} else {
				// Store to source immediately
				TestManagedObjectSource.managedObject = managedObject;
			}
		}

		/**
		 * Initiate.
		 */
		public TestManagedObjectSource() {
			// Reset for next test
			managedObjectUser = null;
			managedObject = null;
		}

		/*
		 * ============== AbstractAsyncManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			context.setManagedObjectClass(TestManagedObject.class);
			context.setObjectClass(Object.class);
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			// Determine if load immediately
			if (managedObject != null) {
				// Load immediately
				user.setManagedObject(managedObject);
			} else {
				// Store for later loading
				managedObjectUser = user;
			}
		}
	}

	/**
	 * Test {@link AsynchronousManagedObject}.
	 */
	private class TestManagedObject implements AsynchronousManagedObject {

		/**
		 * {@link AsynchronousListener}.
		 */
		public AsynchronousListener listener;

		/*
		 * ================= AsynchronousManagedObject =======================
		 */

		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

}