/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.integrate.governance.MockTransaction;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionDutyKey;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionGovernanceKey;
import net.officefloor.frame.integrate.governance.MockTransactionalGovernanceFactory;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * <p>
 * Ensure stress test the {@link Governance} functionality.
 * <p>
 * This includes both invoking {@link Governance} but also not in prematurely
 * unloading the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link GovernanceStressTest}.
	 */
	private static GovernanceStressTest instance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		instance = this;
	}

	/**
	 * Ensures no issues arising in stress {@link Governance} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressGovernance_OnePersonTeam() throws Throwable {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress {@link Governance} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressGovernance_LeaderFollowerTeam() throws Throwable {
		this.doTest(new LeaderFollowerTeam("TEST", 3, 100));
	}

	/**
	 * Failure of testing.
	 */
	private volatile Throwable failure = null;

	/**
	 * Does the {@link Governance} stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link Task} instances.
	 */
	private void doTest(Team team) throws Throwable {

		int OPERATION_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Resets for testing
		MockManagedObjectSource.reset();

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the team
		this.constructTeam("TEAM", team);

		// Create and register the managed object
		ManagedObjectBuilder<GovernanceScenario> mo = this
				.constructManagedObject("MO", MockManagedObjectSource.class);
		mo.addProperty(MockManagedObjectSource.PROPERTY_MAX_INVOCATIONS,
				String.valueOf(OPERATION_COUNT));
		mo.setTimeout(MAX_RUN_TIME * 1000 * 2);
		ManagingOfficeBuilder<GovernanceScenario> managingOffice = mo
				.setManagingOffice(officeName);
		DependencyMappingBuilder dependencies = managingOffice
				.setInputManagedObjectName("MO");
		managingOffice.linkProcess(GovernanceScenario.COMMIT, "WORK",
				"doCommitTask");
		managingOffice.linkProcess(GovernanceScenario.ROLLBACK, "WORK",
				"doRollbackTask");
		managingOffice.linkProcess(GovernanceScenario.TIDY_UP, "WORK",
				"doTidyUpTask");

		// Create and register the work and tasks
		ReflectiveWorkBuilder work = this.constructWork(new MockWork(), "WORK",
				"setupTask");

		// Build tasks
		ReflectiveTaskBuilder setupTask = work.buildTask("setupTask", "TEAM");
		setupTask.buildObject("MO");
		setupTask.setNextTaskInFlow("completeTask");
		ReflectiveTaskBuilder completeTask = work.buildTask("completeTask",
				"TEAM");
		completeTask.buildObject("MO");

		// Build escalation handling
		ReflectiveTaskBuilder escalationTask = work.buildTask(
				"handleException", "TEAM");
		escalationTask.buildParameter();
		escalationTask.buildObject("MO");
		officeBuilder.addEscalation(Throwable.class, "WORK", "handleException");

		// Build the commit task
		ReflectiveTaskBuilder commitTask = work.buildTask("doCommitTask",
				"TEAM");
		commitTask.buildObject("MO");
		commitTask.getBuilder().linkPreTaskAdministration("ADMIN",
				TransactionDutyKey.BEGIN);
		commitTask.getBuilder().linkPostTaskAdministration("ADMIN",
				TransactionDutyKey.COMMIT);

		// Build the rollback task
		ReflectiveTaskBuilder rollbackTask = work.buildTask("doRollbackTask",
				"TEAM");
		rollbackTask.buildObject("MO");
		rollbackTask.getBuilder().linkPreTaskAdministration("ADMIN",
				TransactionDutyKey.BEGIN);
		rollbackTask.getBuilder().linkPostTaskAdministration("ADMIN",
				TransactionDutyKey.ROLLBACK);

		// Build the tidy up task
		ReflectiveTaskBuilder tidyUpTask = work.buildTask("doTidyUpTask",
				"TEAM");
		tidyUpTask.buildObject("MO");
		tidyUpTask.getBuilder().linkPreTaskAdministration("ADMIN",
				TransactionDutyKey.BEGIN);

		// Configure the Governance
		GovernanceBuilder<None> governance = this.getOfficeBuilder()
				.addGovernance("GOVERNANCE",
						new MockTransactionalGovernanceFactory(),
						MockTransaction.class);
		governance.setTeamName("TEAM");
		dependencies.mapGovernance("GOVERNANCE");

		// Configure the Administration
		AdministratorBuilder<TransactionDutyKey> admin = this
				.constructAdministrator("ADMIN",
						MockTransactionalAdministratorSource.class, "TEAM");
		admin.administerManagedObject("MO");
		admin.addDuty("BEGIN").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("COMMIT").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("ROLLBACK").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");

		// Trigger invoking functionality
		this.invokeWork("WORK", null, MAX_RUN_TIME);

		// Ensure no failure
		if (this.failure != null) {
			throw this.failure;
		}

		// Ensure all managed objects completed their steps
		MockManagedObjectSource.ensureNoActiveManagedObjects();

		// Ensure correct number of processes invoked
		assertEquals("Incorrect number of operations for task one",
				OPERATION_COUNT,
				MockManagedObjectSource.getProcessInvokeCount());
	}

	/**
	 * Mock object from the {@link MockManagedObjectSource}.
	 */
	public static interface MockObject extends MockTransaction {

		/**
		 * Starts the {@link ProcessState} instances for testing.
		 */
		void start();

		/**
		 * Undertake the functionality.
		 */
		void doFunctionality();

		/**
		 * Triggers immediate completion of the test.
		 */
		void forceComplete();
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class MockManagedObjectSource extends
			AbstractManagedObjectSource<None, GovernanceScenario> implements
			ExtensionInterfaceFactory<MockTransaction> {

		/**
		 * Property name of the maximum number of invocations.
		 */
		public static final String PROPERTY_MAX_INVOCATIONS = "MAX_INVOCATIONS";

		/**
		 * Resets for testing.
		 */
		public static void reset() {
			instance = null;
		}

		/**
		 * Ensures there are no active {@link ManagedObject} instances still to
		 * complete their steps.
		 */
		public static void ensureNoActiveManagedObjects() {
			synchronized (instance) {
				assertEquals("Should be no active managed objects", 0,
						instance.activeManagedObjects.size());
			}
		}

		/**
		 * Obtains the number of times a {@link ProcessState} was invoked.
		 * 
		 * @return Number of times a {@link ProcessState} was invoked.
		 */
		public static int getProcessInvokeCount() {
			synchronized (instance) {
				return instance.invocationCount;
			}
		}

		/**
		 * {@link MockManagedObjectSource}.
		 */
		private static MockManagedObjectSource instance;

		/**
		 * Active {@link ManagedObject} instances that have yet to complete
		 * their steps.
		 */
		private final List<ManagedObject> activeManagedObjects = new LinkedList<ManagedObject>();

		/**
		 * Maximum number of invocations.
		 */
		private int maxInvocations;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<GovernanceScenario> executeContext;

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * Number of invocations of functionality.
		 */
		private int invocationCount = 0;

		/**
		 * Specifies the instance.
		 */
		public MockManagedObjectSource() {
			assertNull("Should only be one instance created",
					MockManagedObjectSource.instance);
			MockManagedObjectSource.instance = this;
		}

		/**
		 * Starts invocations of the {@link ProcessState}.
		 * 
		 * @param listener
		 *            {@link AsynchronousListener}.
		 * @param triggerManagedObject
		 *            {@link ManagedObject} created to trigger invocations.
		 */
		public void startInvocations(AsynchronousListener listener,
				ManagedObject triggerManagedObject) {

			synchronized (this) {
				// Specify the listener (to notify completion)
				this.listener = listener;

				// Remove triggering managed object
				this.activeManagedObjects.remove(triggerManagedObject);
			}

			// Invoke first process
			this.invokeProcess();

			// Trigger wait
			this.listener.notifyStarted();
		}

		/**
		 * Invoke {@link ProcessState}.
		 */
		public void invokeProcess() {

			synchronized (this) {
				// Determine if further invoke count
				if (this.invocationCount < this.maxInvocations) {
					// Increment number of invocations
					this.invocationCount++;

					// Determine if provide progress
					if ((this.invocationCount % (this.maxInvocations / 10)) == 0) {
						GovernanceStressTest.instance
								.printMessage("Invocations "
										+ this.invocationCount);
					}

					// Specify the scenario
					GovernanceScenario scenario = null;
					switch (this.invocationCount % 3) {
					case 0:
						scenario = GovernanceScenario.COMMIT;
						break;
					case 1:
						scenario = GovernanceScenario.ROLLBACK;
						break;
					case 2:
						scenario = GovernanceScenario.TIDY_UP;
						break;
					}

					// Create the managed object
					ManagedObject managedObject = this
							.createManagedObject(scenario);

					// Invoke the process
					this.executeContext.invokeProcess(scenario, null,
							managedObject, 0);

					// Process invoked
					return;
				}
			}
		}

		/**
		 * Flag steps for {@link ManagedObject} complete.
		 * 
		 * @param managedObject
		 *            Completed {@link ManagedObject}.
		 */
		public void managedObjectComplete(ManagedObject managedObject) {

			// Remove the completed managed object and determine size
			boolean isComplete = false;
			synchronized (this) {
				// Remove the completed managed object
				this.activeManagedObjects.remove(managedObject);

				// Determine if complete
				if ((this.invocationCount >= this.maxInvocations)
						&& (this.activeManagedObjects.size() == 0)) {
					isComplete = true;
				}
			}

			// If all managed objects complete then flag completion
			if (isComplete) {
				this.forceComplete();
			}
		}

		/**
		 * Triggers completion.
		 */
		public void forceComplete() {
			this.listener.notifyComplete();
		}

		/*
		 * ====================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_MAX_INVOCATIONS);
		}

		@Override
		protected void loadMetaData(
				MetaDataContext<None, GovernanceScenario> context)
				throws Exception {
			ManagedObjectSourceContext<GovernanceScenario> mosContext = context
					.getManagedObjectSourceContext();

			// Obtain maximum number of invocations
			this.maxInvocations = Integer.parseInt(mosContext
					.getProperty(PROPERTY_MAX_INVOCATIONS));

			// Load meta-data
			context.setObjectClass(MockObject.class);
			context.setManagedObjectClass(MockManagedObject.class);

			// Provide flow to invoke task
			context.addFlow(GovernanceScenario.COMMIT, null);
			context.addFlow(GovernanceScenario.ROLLBACK, null);
			context.addFlow(GovernanceScenario.TIDY_UP, null);

			// Provide extension interface
			context.addManagedObjectExtensionInterface(MockTransaction.class,
					this);
		}

		@Override
		public void start(
				ManagedObjectExecuteContext<GovernanceScenario> context)
				throws Exception {
			this.executeContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() {
			// Should only step with the input managed object
			return this.createManagedObject(null);
		}

		/**
		 * Creates the {@link ManagedObject} for the {@link GovernanceScenario}.
		 * 
		 * @param scenario
		 *            {@link GovernanceScenario}.
		 * @return {@link ManagedObject}.
		 */
		private ManagedObject createManagedObject(GovernanceScenario scenario) {

			// Create the Managed Object
			MockManagedObject managedObject = new MockManagedObject(this,
					scenario);

			// Register new managed object
			synchronized (this) {
				this.activeManagedObjects.add(managedObject);
			}

			// Return the managed object
			return managedObject;
		}

		/*
		 * ====================== ExtensionInterfaceFactory ==================
		 */

		@Override
		public MockTransaction createExtensionInterface(
				ManagedObject managedObject) {
			return (MockTransaction) managedObject;
		}
	}

	/**
	 * Invocation steps expected on the {@link MockObject}.
	 */
	private static enum InvokeSteps {
		START, BEGIN, DO_FUNCTIONALITY, COMMIT, ROLLBACK
	}

	/**
	 * Scenarios to test.
	 */
	private static enum GovernanceScenario {
		COMMIT, ROLLBACK, TIDY_UP
	}

	/**
	 * Mock {@link ManagedObject}.
	 */
	public static class MockManagedObject implements AsynchronousManagedObject,
			MockObject {

		/**
		 * {@link MockManagedObjectSource}.
		 */
		private final MockManagedObjectSource mos;

		/**
		 * {@link GovernanceScenario}.
		 */
		private final GovernanceScenario scenario;

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * Identifies the current step. Initially no step taken.
		 */
		private InvokeSteps step = InvokeSteps.START;

		/**
		 * Initiate.
		 * 
		 * @param mos
		 *            {@link MockManagedObjectSource}.
		 * @param scenario
		 *            {@link GovernanceScenario}.
		 */
		public MockManagedObject(MockManagedObjectSource mos,
				GovernanceScenario scenario) {
			this.mos = mos;
			this.scenario = scenario;
		}

		/*
		 * ======================== ManagedObject =============================
		 */

		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ====================== MockObject ==========================
		 */

		@Override
		public void start() {
			this.mos.startInvocations(this.listener, this);
		}

		@Override
		public synchronized void begin() throws SQLException {
			assertEquals("Incorrect step", InvokeSteps.START, this.step);
			this.step = InvokeSteps.BEGIN;
		}

		@Override
		public void doFunctionality() {

			// Validate the step
			synchronized (this) {
				assertEquals("Incorrect step", InvokeSteps.BEGIN, this.step);
				this.step = InvokeSteps.DO_FUNCTIONALITY;
			}

			// Trigger next process
			this.mos.invokeProcess();
		}

		@Override
		public void commit() throws SQLException {
			assertEquals("Incorrect step", InvokeSteps.DO_FUNCTIONALITY,
					this.step);
			assertEquals("Incorrect scenario", GovernanceScenario.COMMIT,
					this.scenario);
			this.mos.managedObjectComplete(this);
		}

		@Override
		public void rollback() throws SQLException {
			assertEquals("Incorrect step", InvokeSteps.DO_FUNCTIONALITY,
					this.step);
			switch (this.scenario) {
			case ROLLBACK:
			case TIDY_UP:
				break; // correct handling of scenario
			default:
				fail("Should not rollback for scenario " + this.scenario);
			}
			this.mos.managedObjectComplete(this);
		}

		@Override
		public void forceComplete() {
			this.mos.forceComplete();
		}
	}

	/**
	 * Mock {@link Work}.
	 */
	public class MockWork {

		/**
		 * Setup {@link Task} to trigger testing.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void setupTask(MockObject object) {
			object.start();
		}

		/**
		 * Invoked once testing is complete. Allows waiting on the
		 * {@link AsynchronousManagedObject}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void completeTask(MockObject object) {
		}

		/**
		 * Commit {@link Task}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void doCommitTask(MockObject object) {
			object.doFunctionality();
		}

		/**
		 * Rollback {@link Task}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void doRollbackTask(MockObject object) {
			object.doFunctionality();
		}

		/**
		 * Tidy up {@link Task}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void doTidyUpTask(MockObject object) {
			object.doFunctionality();
		}

		/**
		 * Handles exception.
		 * 
		 * @param ex
		 *            {@link Throwable}.
		 * @param object
		 *            {@link MockObject}.
		 */
		public void handleException(Throwable ex, MockObject object) {

			// Flag failure
			GovernanceStressTest.this.failure = ex;

			// Force complete
			object.forceComplete();
		}
	}

}