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
package net.officefloor.frame.integrate.cleanup;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests the clean up of {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public class CleanupManagedObjectsTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ManagedObject} instances are cleaned up in appropriate
	 * order.
	 */
	public void testCleanupManagedObjects() throws Exception {

		// Create the lock
		final Object lock = new Object();

		// Create the team for cleaning up the managed objects
		this.constructTeam("CLEANUP", ExecutorCachedTeamSource.class);

		// Register two managed objects for clean up
		TestManagedObjectSource moA = new TestManagedObjectSource(lock, "MOS_A");
		TestManagedObjectSource moB = new TestManagedObjectSource(lock, "MOS_B");
		ManagedObjectBuilder<None> moBuilderA = this.getOfficeFloorBuilder()
				.addManagedObject(moA.instanceName, moA);
		moBuilderA.setManagingOffice(this.getOfficeName());
		ManagedObjectBuilder<None> moBuilderB = this.getOfficeFloorBuilder()
				.addManagedObject(moB.instanceName, moB);
		moBuilderB.setManagingOffice(this.getOfficeName());

		// Configure the teams for recycling the managed object
		this.getOfficeBuilder().addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getFlowNodeBuilder("MOS_A", "#recycle#", "cleanup")
						.setTeam("CLEANUP");
				context.getFlowNodeBuilder("MOS_B", "#recycle#", "cleanup")
						.setTeam("CLEANUP");
			}
		});

		// Add the managed objects
		this.getOfficeBuilder().addProcessManagedObject("MO_A", "MOS_A");
		this.getOfficeBuilder().registerManagedObjectSource("MOS_A", "MOS_A");
		this.getOfficeBuilder().addProcessManagedObject("MO_B", "MOS_B");
		this.getOfficeBuilder().registerManagedObjectSource("MOS_B", "MOS_B");

		// Construct the team
		this.constructTeam("TEAM", PassiveTeamSource.class);

		// Construct the work
		ReflectiveWorkBuilder workBuilder = this.constructWork(new MockWork(),
				"WORK", "task");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("task",
				"TEAM");
		taskBuilder.buildObject("MO_A");
		taskBuilder.buildObject("MO_B");

		// Invoke the work
		this.invokeWork("WORK", null);

		// Should now be waiting on clean up
		this.assertManagedObjectCleanup(lock, moA, moB);
		this.assertManagedObjectCleanup(lock, moB, null);
	}

	/**
	 * Asserts the cleanup of the {@link ManagedObjectSource}.
	 * 
	 * @param lock
	 *            Lock.
	 * @param mo
	 *            {@link TestManagedObjectSource}.
	 * @param nextMoToCleanup
	 *            Next {@link TestManagedObjectSource} to cleanup.
	 */
	private void assertManagedObjectCleanup(Object lock,
			TestManagedObjectSource mo, TestManagedObjectSource nextMoToCleanup)
			throws InterruptedException {

		// Allow timeout calculations
		long timeout = 10 * 1000; // 10 seconds
		long startTime = System.currentTimeMillis();

		synchronized (lock) {
			
			// Allow some time for clean up tasks to be activated
			lock.wait(100);

			// Ensure the managed object is waiting
			boolean isTimedOut = false;
			while ((!mo.isWaiting) && (!isTimedOut)) {
				lock.wait(100);
				isTimedOut = ((System.currentTimeMillis() - startTime) < timeout);
			}
			if (isTimedOut) {
				fail("Timed out waiting on clean up of " + mo.instanceName);
			}

			// Ensure the next managed object is not yet triggered for cleanup
			if (nextMoToCleanup != null) {
				assertFalse("Next managed object "
						+ nextMoToCleanup.instanceName
						+ " should not have its clean up task activated",
						nextMoToCleanup.isWaiting);
			}

			// Notify to continue on to next managed object
			lock.notifyAll();
		}
	}

	/**
	 * Mock {@link Work}.
	 */
	public static class MockWork {
		public void task(TestManagedObjectSource moA,
				TestManagedObjectSource moB) {
		}
	}

	/**
	 * Test {@link ManagedObjectSource} for clean up.
	 */
	@TestSource
	public static class TestManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject,
			WorkFactory<Work>, Work, TaskFactory<Work, None, None>,
			Task<Work, None, None> {

		/**
		 * Lock.
		 */
		private final Object lock;

		/**
		 * Name to identify this instance.
		 */
		private final String instanceName;

		/**
		 * Flag indicating that waiting in clean up.
		 */
		public boolean isWaiting = false;

		/**
		 * Initiate.
		 * 
		 * @param lock
		 *            Lock.
		 * @param instanceName
		 *            Instance name.
		 */
		public TestManagedObjectSource(Object lock, String instanceName) {
			this.lock = lock;
			this.instanceName = instanceName;
		}

		/**
		 * ==================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {

			// Configure
			context.setObjectClass(this.getClass());

			// Provide recycle task
			ManagedObjectSourceContext<None> mos = context
					.getManagedObjectSourceContext();
			ManagedObjectWorkBuilder<Work> cleanup = mos.getRecycleWork(this);
			cleanup.addTask("cleanup", this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObject ===============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== WorkFactory =================================
		 */

		@Override
		public Work createWork() {
			return this;
		}

		/*
		 * ===================== TaskFactory =================================
		 */

		@Override
		public Task<Work, None, None> createTask(Work work) {
			return this;
		}

		/*
		 * ======================== Task ======================================
		 */

		@Override
		public Object doTask(TaskContext<Work, None, None> context)
				throws Throwable {

			// Wait to be notified to proceed
			synchronized (this.lock) {
				this.isWaiting = true;
				this.lock.wait();
			}

			// No further tasks
			return null;
		}
	}

}