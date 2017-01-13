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
package net.officefloor.frame.stress;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousListener;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests asynchronous operations by {@link AsynchronousManagedObject}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousOperationStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress asynchronous operations with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressAsynchronousOperation_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress asynchronous operations with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressAsynchronousOperation_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource.createTeamIdentifier(), 2, 100));
	}

	/**
	 * Ensures no issues arising in stress asynchronous operations with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressAsynchronousOperation_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", MockTeamSource.createTeamIdentifier(), 2));
	}

	/**
	 * Does the asynchronous operation stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	private void doTest(Team team) throws Exception {

		int OPERATION_COUNT = 1000000;
		int MAX_RUN_TIME = 200;
		this.setVerbose(true);

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the team
		this.constructTeam("TEAM", team);

		// Create and register the two asynchronous managed objects
		MockManagedObject moOne = new MockManagedObject();
		this.constructManagedObject("MO_ONE", moOne, officeName).setTimeout(100000);
		officeBuilder.addProcessManagedObject("MO_ONE", "MO_ONE");
		MockManagedObject moTwo = new MockManagedObject();
		this.constructManagedObject("MO_TWO", moTwo, officeName).setTimeout(100000);
		officeBuilder.addProcessManagedObject("MO_TWO", "MO_TWO");

		// Create and register the setup task
		SetupWork setupWork = new SetupWork();
		ReflectiveFunctionBuilder setupTask = this.constructFunction(setupWork, "setup");
		setupTask.getBuilder().setTeam("TEAM");
		setupTask.buildFlow("task", null, true);
		setupTask.buildFlow("task", null, true);
		setupTask.buildFlow("setup", null, false);

		// Create and register the two tasks that will start and stop each
		// others asynchronous operations.
		AsynchronousOperationWork workOne = new AsynchronousOperationWork(moTwo, OPERATION_COUNT, setupWork);
		ReflectiveFunctionBuilder taskOne = this.constructFunction(workOne, "task");
		taskOne.getBuilder().setTeam("TEAM");
		taskOne.buildObject("MO_ONE");
		taskOne.buildManagedFunctionContext();
		AsynchronousOperationWork workTwo = new AsynchronousOperationWork(moOne, OPERATION_COUNT, null);
		ReflectiveFunctionBuilder taskTwo = this.constructFunction(workTwo, "task");
		taskTwo.getBuilder().setTeam("TEAM");
		taskTwo.buildObject("MO_TWO");
		taskTwo.buildFlow("task", null, false);

		// Run the asynchronous operations
		this.invokeFunction("setup", null, MAX_RUN_TIME);

		// Ensure correct number of asynchronous operations
		synchronized (workOne) {
			assertEquals("Incorrect number of operations for task one", OPERATION_COUNT, workOne.repeatCount);
		}
		synchronized (workTwo) {
			assertEquals("Incorrect number of operations for task two", OPERATION_COUNT, workTwo.repeatCount);
		}
	}

	/**
	 * Functionality to setup running the asynchronous operations.
	 */
	public static class SetupWork {

		/**
		 * Flag indicating if {@link ManagedFunction} one is running.
		 */
		private boolean isTaskOneRunning = false;

		/**
		 * Flag indicating if the asynchronous operation has started.
		 */
		private boolean isAsynchronousOperationStarted = false;

		/**
		 * {@link ManagedFunction}.
		 * 
		 * @param taskOne
		 *            {@link ManagedFunction} one.
		 * @param taskTwo
		 *            {@link ManagedFunction} two.
		 * @param repeat
		 *            {@link ReflectiveFlow} to repeat.
		 */
		public synchronized void setup(ReflectiveFlow taskOne, ReflectiveFlow taskTwo, ReflectiveFlow repeat) {

			// Trigger task one
			if (!this.isTaskOneRunning) {
				taskOne.doFlow(null, null);
				this.isTaskOneRunning = true;
			}

			// Determine if asynchronous operation started
			if (this.isAsynchronousOperationStarted) {
				// Setup done and trigger task two
				taskTwo.doFlow(null, null);
			} else {
				// Wait for asynchronous operation to be started
				repeat.doFlow(null, null);
			}
		}

		/**
		 * Flags that the asynchronous operation has started.
		 */
		public synchronized void asynchronousOperationStarted() {
			this.isAsynchronousOperationStarted = true;
		}
	}

	/**
	 * Functionality to run asynchronous operations on the
	 * {@link MockManagedObject}.
	 */
	public class AsynchronousOperationWork {

		/**
		 * {@link MockManagedObject} for the other {@link ManagedFunction}.
		 */
		private final MockManagedObject otherTaskManagedObject;

		/**
		 * Maximum number of repeats.
		 */
		private final int maxRepeats;

		/**
		 * Flag indicating if to print output.
		 */
		private final boolean isPrintOutput;

		/**
		 * Number repeats made so far.
		 */
		public int repeatCount = 0;

		/**
		 * {@link SetupWork}.
		 */
		private SetupWork setupWork;

		/**
		 * Initiate.
		 * 
		 * @param otherTaskManagedObject
		 *            {@link MockManagedObject} for the other
		 *            {@link ManagedFunction}.
		 * @param maxRepeats
		 *            Maximum number of repeats.
		 * @param setupWork
		 *            {@link SetupWork} for first {@link ManagedFunction} and
		 *            <code>null</code> for second {@link ManagedFunction}.
		 */
		public AsynchronousOperationWork(MockManagedObject otherTaskManagedObject, int maxRepeats,
				SetupWork setupWork) {
			this.otherTaskManagedObject = otherTaskManagedObject;
			this.maxRepeats = maxRepeats;
			this.setupWork = setupWork;
			this.isPrintOutput = (this.setupWork != null);
		}

		/**
		 * {@link ManagedFunction}.
		 * 
		 * @param ownManagedObject
		 *            {@link MockManagedObject} instance for this
		 *            {@link ManagedFunction}.
		 * @param repeat
		 *            {@link ReflectiveFlow} to repeat.
		 */
		public void task(MockManagedObject ownManagedObject, ReflectiveFlow repeat) {

			// Output progress
			if (this.isPrintOutput) {
				if ((this.repeatCount > 0) & ((this.repeatCount % 100000) == 0)) {
					AsynchronousOperationStressTest.this.printMessage("Asynchronous operation " + this.repeatCount);
				}
			}

			// Start asynchronous operation for this task
			ownManagedObject.listener.notifyStarted();

			// Handle if setting up
			if (this.setupWork != null) {

				// Inform setup that first asynchronous operation running
				this.setupWork.asynchronousOperationStarted();

				// Setup finished and start processing
				this.setupWork = null;
				repeat.doFlow(null, null);
				return;
			}

			// Complete asynchronous operation for other task
			this.otherTaskManagedObject.listener.notifyComplete();

			// Repeat again if necessary
			this.repeatCount++;
			if (this.repeatCount < this.maxRepeats) {
				repeat.doFlow(null, null);
			} else {
				// Ensure repeat count thread safe for checking
				int count = this.repeatCount;
				synchronized (this) {
					this.repeatCount = count;
				}
			}
		}
	}

	/**
	 * Mock {@link AsynchronousManagedObject}.
	 */
	public class MockManagedObject extends AbstractManagedObjectSource<None, None>
			implements AsynchronousManagedObject {

		/**
		 * {@link AsynchronousListener}.
		 */
		public AsynchronousListener listener;

		/*
		 * ================= ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ================= AsynchronousManagedObject ======================
		 */

		@Override
		public void registerAsynchronousListener(AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

}