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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.governance.MockTransaction;
import net.officefloor.frame.impl.execute.governance.MockTransactionalGovernanceFactory;
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
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Abstract {@link Governance} stress test.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGovernanceStressTestCase extends AbstractOfficeConstructTestCase {

	/**
	 * Name of the {@link Governance}.
	 */
	protected static final String GOVERNANCE_NAME = "GOVERNANCE";

	/**
	 * Name of the {@link Team}.
	 */
	protected static final String TEAM_NAME = "TEAM";

	/**
	 * {@link AbstractGovernanceStressTestCase}.
	 */
	private static AbstractGovernanceStressTestCase instance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		instance = this;
	}

	/**
	 * Configures particulars of the test.
	 * 
	 * @param commitTask
	 *            Commit {@link ManagedFunction}.
	 * @param rollbackTask
	 *            Rollback {@link ManagedFunction}.
	 * @param tidyUpTask
	 *            Tidy up {@link ManagedFunction}.
	 * @return <code>true</code> if managed {@link Governance}.
	 */
	protected abstract boolean configure(ReflectiveFunctionBuilder commitTask, ReflectiveFunctionBuilder rollbackTask,
			ReflectiveFunctionBuilder tidyUpTask);

	/**
	 * Failure of testing.
	 */
	private volatile Throwable failure = null;

	/**
	 * Does the {@link Governance} stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	protected void doTest(Team team) throws Throwable {

		int OPERATION_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Resets for testing
		MockManagedObjectSource.reset();

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the team
		this.constructTeam(TEAM_NAME, team);

		// Create and register the work and tasks
		MockWork workObject = new MockWork();

		// Build tasks
		ReflectiveFunctionBuilder setupTask = this.constructFunction(workObject, "setupTask");
		setupTask.getBuilder().setTeam(TEAM_NAME);
		setupTask.buildObject("MO");
		setupTask.setNextFunction("completeTask");
		ReflectiveFunctionBuilder completeTask = this.constructFunction(workObject, "completeTask");
		completeTask.getBuilder().setTeam(TEAM_NAME);
		completeTask.buildObject("MO");

		// Build the commit task
		ReflectiveFunctionBuilder commitTask = this.constructFunction(workObject, "doCommitTask");
		commitTask.getBuilder().setTeam(TEAM_NAME);
		commitTask.buildObject("MO");

		// Build the rollback task
		ReflectiveFunctionBuilder rollbackTask = this.constructFunction(workObject, "doRollbackTask");
		rollbackTask.getBuilder().setTeam(TEAM_NAME);
		rollbackTask.buildObject("MO");

		// Build the tidy up task
		ReflectiveFunctionBuilder tidyUpTask = this.constructFunction(workObject, "doTidyUpTask");
		tidyUpTask.getBuilder().setTeam(TEAM_NAME);
		tidyUpTask.buildObject("MO");

		// Configure specifics of test
		boolean isManagedGovernance = this.configure(commitTask, rollbackTask, tidyUpTask);

		// Build rollback handling
		if (isManagedGovernance) {
			ReflectiveFunctionBuilder handleRollback = this.constructFunction(workObject, "handleRollback");
			handleRollback.getBuilder().setTeam(TEAM_NAME);
			handleRollback.buildParameter();
			officeBuilder.addEscalation(SQLException.class, "handleRollback");
		}

		// Build escalation handling
		ReflectiveFunctionBuilder escalationTask = this.constructFunction(workObject, "handleException");
		escalationTask.getBuilder().setTeam(TEAM_NAME);
		escalationTask.buildParameter();
		escalationTask.buildObject("MO");
		officeBuilder.addEscalation(Throwable.class, "handleException");

		// Create and register the managed object
		ManagedObjectBuilder<GovernanceScenario> mo = this.constructManagedObject("MO", MockManagedObjectSource.class);
		mo.addProperty(MockManagedObjectSource.PROPERTY_MANAGED_GOVERNANCE, String.valueOf(isManagedGovernance));
		mo.addProperty(MockManagedObjectSource.PROPERTY_MAX_INVOCATIONS, String.valueOf(OPERATION_COUNT));
		mo.setTimeout(MAX_RUN_TIME * 1000 * 2);
		ManagingOfficeBuilder<GovernanceScenario> managingOffice = mo.setManagingOffice(officeName);
		DependencyMappingBuilder dependencies = managingOffice.setInputManagedObjectName("MO");
		managingOffice.linkProcess(GovernanceScenario.COMMIT, "doCommitTask");
		managingOffice.linkProcess(GovernanceScenario.ROLLBACK, "doRollbackTask");
		managingOffice.linkProcess(GovernanceScenario.TIDY_UP, "doTidyUpTask");

		// Configure the Governance
		GovernanceBuilder<None> governance = this.getOfficeBuilder().addGovernance(GOVERNANCE_NAME,
				MockTransaction.class, new MockTransactionalGovernanceFactory());
		governance.setTeam(TEAM_NAME);

		// Configure governance
		workObject.isManagedGovernance = isManagedGovernance;
		dependencies.mapGovernance(GOVERNANCE_NAME);

		// Trigger invoking functionality
		this.invokeFunction("setupTask", null, MAX_RUN_TIME);

		// Ensure no failure
		if (this.failure != null) {
			throw this.failure;
		}

		// Ensure all managed objects completed their steps
		MockManagedObjectSource.ensureNoActiveManagedObjects();

		// Ensure correct number of processes invoked
		assertEquals("Incorrect number of operations for task one", OPERATION_COUNT,
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
	public static class MockManagedObjectSource extends AbstractManagedObjectSource<None, GovernanceScenario>
			implements ExtensionInterfaceFactory<MockTransaction> {

		/**
		 * Property name to indicate if managed {@link Governance}.
		 */
		public static final String PROPERTY_MANAGED_GOVERNANCE = "MANAGED_GOVERNANCE";

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
				assertEquals("Should be no active managed objects", 0, instance.activeManagedObjects.size());
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
		 * Indicates if managed {@link Governance}.
		 */
		private boolean isManagedGovernance;

		/**
		 * Number of invocations of functionality.
		 */
		private int invocationCount = 0;

		/**
		 * Specifies the instance.
		 */
		public MockManagedObjectSource() {
			assertNull("Should only be one instance created", MockManagedObjectSource.instance);
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
		public void startInvocations(AsynchronousListener listener, ManagedObject triggerManagedObject) {

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
						AbstractGovernanceStressTestCase.instance.printMessage("Invocations " + this.invocationCount);
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
					ManagedObject managedObject = this.createManagedObject(scenario);

					// Invoke the process
					this.executeContext.invokeProcess(scenario, null, managedObject, 0, null);
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
				if ((this.invocationCount >= this.maxInvocations) && (this.activeManagedObjects.size() == 0)) {
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
			context.addProperty(PROPERTY_MANAGED_GOVERNANCE);
			context.addProperty(PROPERTY_MAX_INVOCATIONS);
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, GovernanceScenario> context) throws Exception {
			ManagedObjectSourceContext<GovernanceScenario> mosContext = context.getManagedObjectSourceContext();

			// Obtain property values
			this.isManagedGovernance = Boolean.parseBoolean(mosContext.getProperty(PROPERTY_MANAGED_GOVERNANCE));
			this.maxInvocations = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_INVOCATIONS));

			// Load meta-data
			context.setObjectClass(MockObject.class);
			context.setManagedObjectClass(MockManagedObject.class);

			// Provide flow to invoke task
			context.addFlow(GovernanceScenario.COMMIT, null);
			context.addFlow(GovernanceScenario.ROLLBACK, null);
			context.addFlow(GovernanceScenario.TIDY_UP, null);

			// Provide extension interface
			context.addManagedObjectExtensionInterface(MockTransaction.class, this);
		}

		@Override
		public void start(ManagedObjectExecuteContext<GovernanceScenario> context) throws Exception {
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
			MockManagedObject managedObject = new MockManagedObject(this, scenario, this.isManagedGovernance);

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
		public MockTransaction createExtensionInterface(ManagedObject managedObject) {
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
	public static class MockManagedObject implements AsynchronousManagedObject, MockObject {

		/**
		 * {@link MockManagedObjectSource}.
		 */
		private final MockManagedObjectSource mos;

		/**
		 * {@link GovernanceScenario}.
		 */
		private final GovernanceScenario scenario;

		/**
		 * Indicates if managed {@link Governance}.
		 */
		private final boolean isManagedGovernance;

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
		 * @param isManagedGovernance
		 *            Indicates if managed {@link Governance}.
		 */
		public MockManagedObject(MockManagedObjectSource mos, GovernanceScenario scenario,
				boolean isManagedGovernance) {
			this.mos = mos;
			this.scenario = scenario;
			this.isManagedGovernance = isManagedGovernance;
		}

		/*
		 * ======================== ManagedObject =============================
		 */

		@Override
		public void registerAsynchronousCompletionListener(AsynchronousListener listener) {
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
			assertEquals("Incorrect step", InvokeSteps.DO_FUNCTIONALITY, this.step);
			switch (this.scenario) {
			case COMMIT:
				/*
				 * Correctly commit due to either administrated commit or
				 * managed governance thread completion.
				 */
				break;
			case TIDY_UP:
				if (!this.isManagedGovernance) {
					fail("Should not commit on thread completion for administered governance");
				}
				break;
			default:
				fail("Should not commit for scenario " + this.scenario);
			}
			this.mos.managedObjectComplete(this);
		}

		@Override
		public void rollback() throws SQLException {
			assertEquals("Incorrect step", InvokeSteps.DO_FUNCTIONALITY, this.step);
			switch (this.scenario) {
			case ROLLBACK:
				/*
				 * Correctly rollback due to either administrated thread
				 * completion or managed governance escalation deactivation.
				 */
				break;
			case TIDY_UP:
				if (this.isManagedGovernance) {
					fail("Should not rollback on thread completion for managed governance");
				}
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
	 * Mock functionality.
	 */
	public class MockWork {

		/**
		 * Indicates if managed {@link Governance}.
		 */
		public volatile boolean isManagedGovernance = false;

		/**
		 * Setup {@link ManagedFunction} to trigger testing.
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
		 * Commit {@link ManagedFunction}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void doCommitTask(MockObject object) {
			object.doFunctionality();
		}

		/**
		 * Rollback {@link ManagedFunction}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void doRollbackTask(MockObject object) throws SQLException {
			object.doFunctionality();

			// If managed governance, through exception to trigger rollback
			if (this.isManagedGovernance) {
				throw new SQLException("TEST");
			}
		}

		/**
		 * Tidy up {@link ManagedFunction}.
		 * 
		 * @param object
		 *            {@link MockObject}.
		 */
		public void doTidyUpTask(MockObject object) {
			object.doFunctionality();
		}

		/**
		 * Gracefully handles rollback exception.
		 * 
		 * @param ex
		 *            {@link SQLException}.
		 */
		public void handleRollback(SQLException ex) {
			assertEquals("Incorrect exception", "TEST", ex.getMessage());
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
			AbstractGovernanceStressTestCase.this.failure = ex;

			// Force complete
			object.forceComplete();
		}
	}

}