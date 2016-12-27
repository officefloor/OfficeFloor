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
package net.officefloor.frame.integrate.stress;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Stress tests coordination of {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CoordinateStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress coordination with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressCoordination_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * Ensures no issues arising in stress coordination with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressCoordination_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 3, 100));
	}

	/**
	 * Ensures no issues arising in stress coordination with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressCoordination_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 3));
	}

	/**
	 * Does the coordination stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction} instances.
	 */
	private void doTest(Team team) throws Exception {

		boolean isDebugging = false;

		final int MANAGED_OBJECT_WAIT_TIME = 1000 * (isDebugging ? 1000 : 1);
		final int TASK_INVOKE_COUNT = 100000;
		final int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Obtain the office name and builder
		String officeName = this.getOfficeName();

		// Construct the team
		this.constructTeam("TEAM", team);

		// Construct the direct use managed object
		this.constructManagedObject("DIRECT_USE",
				DirectUseManagedObjectSource.class).setManagingOffice(
				officeName);

		// Construct the dependency managed object
		ManagedObjectBuilder<Indexed> dependencyBuilder = this
				.constructManagedObject("DEPENDENCY",
						DependencyManagedObjectSource.class);
		dependencyBuilder.setManagingOffice(officeName)
				.setInputManagedObjectName("PROCESS_BOUND");
		dependencyBuilder.setTimeout(MANAGED_OBJECT_WAIT_TIME);
		this.constructTeam("of-DEPENDENCY.MO_TEAM", new OnePersonTeam(
				"MO_TEAM", MockTeamSource.createTeamIdentifier(), 100));

		// Construct the work
		CoordinateWork work = new CoordinateWork(TASK_INVOKE_COUNT);
		ReflectiveWorkBuilder workBuilder = this.constructWork(work,
				"COORDINATE_WORK", "task");
		workBuilder.getBuilder()
				.addWorkManagedObject("DIRECT_USE", "DIRECT_USE")
				.mapDependency(0, "DEPENDENCY");
		workBuilder.getBuilder().addWorkManagedObject("DEPENDENCY",
				"DEPENDENCY");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("task",
				"TEAM");
		taskBuilder.buildObject("DIRECT_USE");
		taskBuilder.buildFlow("task", FlowInstigationStrategyEnum.SEQUENTIAL,
				null);

		// Run the coordination
		this.invokeWork("COORDINATE_WORK", null, MAX_RUN_TIME);

		// Ensure correct number of invocations
		assertEquals("Incorrect number of task invocations", TASK_INVOKE_COUNT,
				work.invokeCount);
	}

	/**
	 * {@link Work}.
	 */
	public class CoordinateWork {

		/**
		 * Maximum number of times to invoke another {@link ManagedFunction}.
		 */
		private final int maxInvokes;

		/**
		 * Number of times invoked.
		 */
		public volatile int invokeCount = 0;

		/**
		 * Previous {@link DirectUseManagedObject}.
		 */
		private volatile DirectUseManagedObject previousDirectUse;

		/**
		 * Previous {@link DependencyManagedObject}.
		 */
		private volatile DependencyManagedObject previousDependency;

		/**
		 * Initiate.
		 * 
		 * @param maxInvokes
		 *            Maximum number of times to invoke another {@link ManagedFunction}.
		 */
		public CoordinateWork(int maxInvokes) {
			this.maxInvokes = maxInvokes;
		}

		/**
		 * Task to run.
		 * 
		 * @param directUse
		 *            {@link DependencyManagedObject}.
		 * @param flow
		 *            {@link ReflectiveFlow} to invoke.
		 */
		public void task(DirectUseManagedObject directUse, ReflectiveFlow flow) {

			// Ensure not the same previous direct use object
			assertNotSame("Should not be same previous direct use",
					this.previousDirectUse, directUse);
			this.previousDirectUse = directUse;

			// Obtain the dependency and ensure not same as previous
			DependencyManagedObject dependency = directUse.getDependency();
			assertNotNull("Ensure have dependency", dependency);
			assertNotSame("Should not be same previous dependency",
					this.previousDependency, dependency);
			this.previousDependency = dependency;

			// Ensure asynchronous operations complete
			assertEquals("Dependency should be ready",
					TransitionState.COORDINATE_COMPLETE,
					dependency.transitionState);

			// Increment the number of times invoked
			this.invokeCount++;
			if (this.invokeCount < this.maxInvokes) {
				flow.doFlow(null);
			}

			// Indicate progress
			if ((this.invokeCount % (this.maxInvokes / 10)) == 0) {
				System.out.println("Task invoked " + this.invokeCount
						+ " times");
			}
		}
	}

	/**
	 * {@link ManagedObject} used directly by {@link ManagedFunction}.
	 */
	public static class DirectUseManagedObject implements
			CoordinatingManagedObject<Indexed> {

		/**
		 * {@link DependencyManagedObject}.
		 */
		private DependencyManagedObject dependency;

		/**
		 * Obtain the {@link DependencyManagedObject}.
		 * 
		 * @return {@link DependencyManagedObject}.
		 */
		public DependencyManagedObject getDependency() {
			return this.dependency;
		}

		/*
		 * ==================== CoordinatingManagedObject ====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry)
				throws Throwable {
			// Start operation to indicate object not ready for use by task
			this.dependency = (DependencyManagedObject) registry.getObject(0);
			this.dependency.startAsynchronousOperation(
					TransitionState.LOAD_COMPLETE,
					TransitionState.COORDINATE_START);
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

	/**
	 * {@link ManagedObjectSource} for the {@link DirectUseManagedObject}.
	 */
	public static class DirectUseManagedObjectSource extends
			AbstractManagedObjectSource<Indexed, None> {

		/*
		 * =================== AbstractManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// Nothing required
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, None> context)
				throws Exception {
			context.setObjectClass(DirectUseManagedObject.class);
			context.setManagedObjectClass(DirectUseManagedObject.class);

			// Register dependency
			context.addDependency(DependencyManagedObject.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new DirectUseManagedObject();
		}
	}

	/**
	 * Dependency {@link ManagedObject}.
	 */
	public static class DependencyManagedObject implements
			AsynchronousManagedObject {

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Indexed> executeContext;

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * {@link TransitionState}.
		 */
		public volatile TransitionState transitionState = TransitionState.INIT;

		/**
		 * Initiate.
		 * 
		 * @param executeContext
		 *            {@link ManagedObjectExecuteContext}.
		 */
		public DependencyManagedObject(
				ManagedObjectExecuteContext<Indexed> executeContext) {
			this.executeContext = executeContext;
		}

		/**
		 * Starts the asynchronous operation.
		 * 
		 * @param expectedState
		 *            Expected {@link TransitionState}.
		 * @param nextState
		 *            Next {@link TransitionState}.
		 */
		public void startAsynchronousOperation(TransitionState expectedState,
				TransitionState nextState) {
			// Flag asynchronous operation occurring
			this.listener.notifyStarted();

			// Ensure correct state and move to next state
			assertEquals("Incorrect state on start", expectedState,
					this.transitionState);
			this.transitionState = nextState;

			// Run process for asynchronous operation
			this.executeContext.invokeProcess(0, this, this, 0);
		}

		/**
		 * Completes the asynchronous operation.
		 * 
		 * @param nextState
		 *            Next {@link TransitionState}.
		 */
		public void completeAsynchronousOperation(TransitionState nextState) {

			// Flag asynchronous operation complete
			this.transitionState = nextState;
			this.listener.notifyComplete();
		}

		/*
		 * ================== AsynchronousManagedObject ====================
		 */

		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			// Only load the first listener (as second from invoked process)
			if (this.listener == null) {
				this.listener = listener;

				// Start operation on sourcing Managed Object.
				// In other words not ready to coordinate.
				this.startAsynchronousOperation(TransitionState.INIT,
						TransitionState.LOAD_START);
			}
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

	/**
	 * Dependency {@link ManagedObjectSource}.
	 */
	public static class DependencyManagedObjectSource extends
			AbstractManagedObjectSource<None, Indexed> implements
			WorkFactory<DependencyManagedObjectSource>, Work,
			ManagedFunctionFactory<DependencyManagedObjectSource, Indexed, None>,
			ManagedFunction<DependencyManagedObjectSource, Indexed, None> {

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Indexed> executeContext;

		/*
		 * =================== AbstractManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// Nothing required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context)
				throws Exception {
			ManagedObjectSourceContext<Indexed> mosContext = context
					.getManagedObjectSourceContext();

			// Registry types
			context.setObjectClass(DependencyManagedObject.class);
			context.setManagedObjectClass(DependencyManagedObject.class);

			// Register task to active object
			ManagedObjectWorkBuilder<DependencyManagedObjectSource> workBuilder = mosContext
					.addWork("WORK", this);
			ManagedObjectFunctionBuilder<Indexed, None> taskBuilder = workBuilder
					.addTask("TASK", this);
			taskBuilder.linkParameter(0, DependencyManagedObject.class);
			taskBuilder.setTeam("MO_TEAM");

			// Register flow to run task to activate object
			context.addFlow(DependencyManagedObject.class);
			mosContext.linkProcess(0, "WORK", "TASK");
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context)
				throws Exception {
			this.executeContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new DependencyManagedObject(this.executeContext);
		}

		/*
		 * =============== WorkFactory, TaskFactory, Task ===================
		 */

		@Override
		public DependencyManagedObjectSource createWork() {
			return this;
		}

		@Override
		public ManagedFunction<DependencyManagedObjectSource, Indexed, None> createTask(
				DependencyManagedObjectSource work) {
			return work;
		}

		@Override
		public Object execute(
				ManagedFunctionContext<DependencyManagedObjectSource, Indexed, None> context)
				throws Throwable {

			// Obtain the dependency managed object
			DependencyManagedObject dependency = (DependencyManagedObject) context
					.getObject(0);

			// Indicate flag next state
			switch (dependency.transitionState) {
			case LOAD_START:
				dependency
						.completeAsynchronousOperation(TransitionState.LOAD_COMPLETE);
				break;
			case COORDINATE_START:
				dependency
						.completeAsynchronousOperation(TransitionState.COORDINATE_COMPLETE);
				break;
			default:
				fail("Invalid state " + dependency.transitionState);
			}

			// No further processing
			return null;
		}
	}

	/**
	 * States indicating state of coordination.
	 */
	private enum TransitionState {
		INIT, LOAD_START, LOAD_COMPLETE, COORDINATE_START, COORDINATE_COMPLETE
	}

}