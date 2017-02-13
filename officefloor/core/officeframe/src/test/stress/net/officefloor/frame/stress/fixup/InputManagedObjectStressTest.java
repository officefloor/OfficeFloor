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
package net.officefloor.frame.stress.fixup;

import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests using Input {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class InputManagedObjectStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Number of instances for the input {@link ManagedObject}.
	 */
	private static final int INSTANCE_COUNT = 200;

	/**
	 * Number of processes to be invoked.
	 */
	private static final int INVOKE_PROCESS_COUNT = 1000000;

	/**
	 * Ensures no issues arising in stress input {@link ManagedObject} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressInputManagedObject_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress input {@link ManagedObject} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressInputManagedObject_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", 2, 100));
	}

	/**
	 * Ensures no issues arising in stress input {@link ManagedObject} with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressInputManagedObject_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", 2));
	}

	/**
	 * Does the asynchronous operation stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	private void doTest(Team team) throws Exception {

		fail("TODO fix infinite loop");
		
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Reset for testing
		InputManagedObjectSource.reset();

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		OfficeFloorBuilder officeFloorBuilder = this.getOfficeFloorBuilder();

		// Construct the team
		this.constructTeam("TEAM", team);

		final String dependency = "DEPENDENCY";

		// Create the task for processing input managed object
		InputWork work = new InputWork(dependency);
		ReflectiveFunctionBuilder taskBuilder = this.constructFunction(work, "task");
		taskBuilder.getBuilder().setResponsibleTeam("TEAM");
		taskBuilder.buildObject("INPUT_MO");

		// Create the task for running the test
		ReflectiveFunctionBuilder run = this.constructFunction(new RunWork(work), "run");
		run.getBuilder().setResponsibleTeam("TEAM");
		run.buildFlow("run", null, false);

		// Create and register the dependency
		this.constructManagedObject(dependency, "DEPENDENCY_MOS", officeName);
		officeBuilder.addProcessManagedObject("DEPENDENCY", "DEPENDENCY_MOS");

		// Register the input managed object sources
		for (int i = 0; i < INSTANCE_COUNT; i++) {
			ManagedObjectBuilder<FlowKey> moBuilder = officeFloorBuilder.addManagedObject("INPUT_" + i,
					InputManagedObjectSource.class);
			moBuilder.addProperty("instance.index", String.valueOf(i));
			ManagingOfficeBuilder<FlowKey> managingBuilder = moBuilder.setManagingOffice(officeName);
			managingBuilder.linkProcess(FlowKey.TRIGGER_PROCESS, "task");
			managingBuilder.setInputManagedObjectName("INPUT_MO").mapDependency(DependencyKey.DEPENDENCY, "DEPENDENCY");
		}

		// Specify last input as bound
		officeBuilder.setBoundInputManagedObject("INPUT_MO", "INPUT_" + (INSTANCE_COUNT - 1));

		// Run the asynchronous operations
		this.invokeFunction("run", null, MAX_RUN_TIME);

		// Ensure correct number of invocations
		assertEquals("Incorrect number of invocations", INVOKE_PROCESS_COUNT, work.invocationCount.get());
	}

	/**
	 * Runs the {@link InputWork}.
	 */
	public class RunWork {

		/**
		 * {@link InputWork}.
		 */
		private final InputWork inputWork;

		/**
		 * Flag indicating if triggered processing.
		 */
		private boolean isTriggeredProcessing = false;

		/**
		 * Initiate.
		 * 
		 * @param inputWork
		 *            {@link InputWork}.
		 */
		public RunWork(InputWork inputWork) {
			this.inputWork = inputWork;
		}

		/**
		 * Runs.
		 * 
		 * @param repeat
		 *            {@link ReflectiveFlow} to repeat.
		 */
		public void run(ReflectiveFlow repeat) {

			// Trigger processing on first invocation
			if (!this.isTriggeredProcessing) {
				InputManagedObjectSource.triggerProcessing();
				this.isTriggeredProcessing = true;
			}

			// Keep processing until invocations complete
			if (this.inputWork.invocationCount.get() < INVOKE_PROCESS_COUNT) {
				repeat.doFlow(null, null);
			}
		}
	}

	/**
	 * Input functionality.
	 */
	public class InputWork {

		/**
		 * Keeps track of number of times invoked.
		 */
		public final AtomicInteger invocationCount = new AtomicInteger(0);

		/**
		 * Expected dependency on the {@link InputManagedObject}.
		 */
		private final String expectedDependency;

		/**
		 * Previous {@link InputManagedObjectSource}.
		 */
		private InputManagedObjectSource previousInputManagedObjectSource = null;

		/**
		 * Initiate.
		 * 
		 * @param expectedDependency
		 *            Expected dependency on the {@link InputManagedObject}.
		 */
		public InputWork(String expectedDependency) {
			this.expectedDependency = expectedDependency;
		}

		/**
		 * Task for processing.
		 * 
		 * @param mo
		 *            {@link InputManagedObject}.
		 */
		public void task(InputManagedObject mo) {

			// Ensure different source for input managed object
			assertNotSame("Should be different managed object source for input", this.previousInputManagedObjectSource,
					mo.source);
			this.previousInputManagedObjectSource = mo.source;

			// Ensure correct dependency
			assertEquals("Incorrect dependency", this.expectedDependency, mo.dependency);

			// Increment the next process
			this.invocationCount.incrementAndGet();
			mo.triggerNextProcess();
		}
	}

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKey {
		DEPENDENCY
	}

	/**
	 * Flow keys.
	 */
	public static enum FlowKey {
		TRIGGER_PROCESS
	}

	/**
	 * Input {@link ManagedObject} for testing.
	 */
	public static class InputManagedObject implements CoordinatingManagedObject<DependencyKey> {

		/**
		 * {@link InputManagedObjectSource}.
		 */
		public final InputManagedObjectSource source;

		/**
		 * Invocation count inputting this {@link InputManagedObject}.
		 */
		private final int invocationCount;

		/**
		 * Dependency.
		 */
		public String dependency = null;

		/**
		 * Initiate.
		 * 
		 * @param source
		 *            {@link InputManagedObjectSource}.
		 * @param invocationCount
		 *            Invocation count inputting this.
		 */
		public InputManagedObject(InputManagedObjectSource source, int invocationCount) {
			this.source = source;
			this.invocationCount = invocationCount;
		}

		/**
		 * Triggers running the next process.
		 */
		public void triggerNextProcess() {
			this.source.triggerNextProcess(this.invocationCount + 1);
		}

		/*
		 * ============ CoordinatingManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKey> registry) throws Throwable {
			// Obtain the dependency
			this.dependency = (String) registry.getObject(DependencyKey.DEPENDENCY);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * Input {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class InputManagedObjectSource extends AbstractManagedObjectSource<DependencyKey, FlowKey> {

		/**
		 * Listing of {@link InputManagedObjectSource} instances.
		 */
		private static final InputManagedObjectSource[] instances = new InputManagedObjectSource[INSTANCE_COUNT];

		/**
		 * Resets the instances for next test.
		 */
		public static void reset() {
			synchronized (instances) {
				for (int i = 0; i < instances.length; i++) {
					instances[i] = null;
				}
			}
		}

		/**
		 * Triggers processing.
		 */
		public static void triggerProcessing() {

			// Obtain the first instance
			InputManagedObjectSource instance;
			synchronized (instances) {
				instance = instances[0];
			}

			// Trigger the first process
			instance.triggerNextProcess(0);
		}

		/**
		 * Index of this instance.
		 */
		private int instanceIndex;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<FlowKey> executeContext;

		/**
		 * Runs the next process.
		 * 
		 * @param invocationCount
		 *            Invocation count including the process to invoke.
		 */
		public void triggerNextProcess(int invocationCount) {

			// Determine if all processes invoked
			if (invocationCount >= INVOKE_PROCESS_COUNT) {
				return; // all process invoked
			}

			// Obtain the next instance
			InputManagedObjectSource instance;
			int instanceIndex;
			synchronized (instances) {
				instanceIndex = (this.instanceIndex + 1) % instances.length;
				instance = instances[instanceIndex];
			}

			// Trigger the next process from next input managed object
			instance.executeContext.invokeProcess(FlowKey.TRIGGER_PROCESS, new Integer(instanceIndex),
					new InputManagedObject(instance, invocationCount), 0, null);
		}

		/*
		 * =============== AbstractManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("instance.index");
		}

		@Override
		protected void loadMetaData(MetaDataContext<DependencyKey, FlowKey> context) throws Exception {
			ManagedObjectSourceContext<FlowKey> mosContext = context.getManagedObjectSourceContext();

			// Register this instance
			this.instanceIndex = Integer.parseInt(mosContext.getProperty("instance.index"));
			synchronized (instances) {
				instances[this.instanceIndex] = this;
			}

			// Load the meta-data
			context.setManagedObjectClass(InputManagedObject.class);
			context.setObjectClass(InputManagedObject.class);
			context.addFlow(FlowKey.TRIGGER_PROCESS, null);
			context.addDependency(DependencyKey.DEPENDENCY, String.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<FlowKey> context) throws Exception {
			synchronized (instances) {
				this.executeContext = context;
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not source Input Managed Object");
			return null;
		}
	}

}