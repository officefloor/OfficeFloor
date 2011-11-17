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
package net.officefloor.frame.integrate.governance;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionDutyKey;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionGovernanceKey;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests flow for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TransactionalObject}.
	 */
	private final TransactionalObject object = this
			.createSynchronizedMock(TransactionalObject.class);

	/**
	 * Flag indicating whether to provide commit after {@link Task}.
	 */
	private boolean isCommit = false;

	/**
	 * Flag indicating whether to rollback after {@link Task}.
	 */
	private boolean isRollback = false;

	/**
	 * Flag indicating whether to use multi-threaded {@link Team} instances.
	 */
	private boolean isMultiThreaded = false;

	/**
	 * Ensure able to commit transaction with {@link PassiveTeam}.
	 */
	public void test_Passive_CommitTransaction() throws Throwable {

		// Commit
		this.isCommit = true;

		// Record committing the transaction
		this.object.begin();
		this.object.flowBegin();
		this.object.doFunctionality();
		this.object.commit();
		this.object.flowCommit();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to commit transaction with {@link LeaderFollowerTeam}.
	 */
	public void test_LeaderFollower_CommitTransaction() throws Throwable {

		// Multi-threaded Commit
		this.isMultiThreaded = true;
		this.isCommit = true;

		// Record committing the transaction
		this.object.begin();
		this.object.flowBegin();
		this.object.doFunctionality();
		this.object.commit();
		this.object.flowCommit();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to rollback transaction with {@link PassiveTeam}.
	 */
	public void test_Passive_RollbackTransaction() throws Throwable {

		// Rollback
		this.isRollback = true;

		// Record rolling back the transaction
		this.object.begin();
		this.object.flowBegin();
		this.object.doFunctionality();
		this.object.rollback();
		this.object.flowRollback();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to rollback transaction with {@link LeaderFollowerTeam}.
	 */
	public void test_LeaderFollower_RollbackTransaction() throws Throwable {

		// Multi-threaded Rollback
		this.isMultiThreaded = true;
		this.isRollback = true;

		// Record rolling back the transaction
		this.object.begin();
		this.object.flowBegin();
		this.object.doFunctionality();
		this.object.rollback();
		this.object.flowRollback();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to tidy up transaction.
	 */
	public void test_Passive_TidyUpTransaction() throws Throwable {

		// Ensure transaction governing functionality
		this.object.begin();
		this.object.flowBegin();
		this.object.doFunctionality();
		this.object.rollback();
		this.object.flowRollback();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to tidy up transaction.
	 */
	public void test_LeaderFollower_TidyUpTransaction() throws Throwable {

		// Multi-threaded
		this.isMultiThreaded = true;

		// Ensure transaction governing functionality
		this.object.begin();
		this.object.flowBegin();
		this.object.doFunctionality();
		this.object.rollback();
		this.object.flowRollback();

		// Test
		this.doTest();
	}

	/**
	 * Failure in testing.
	 */
	private volatile Throwable failure = null;

	/**
	 * Undertake the test.
	 */
	private void doTest() throws Throwable {

		// Create the teams
		Team taskTeam;
		Team governanceTeam;
		if (this.isMultiThreaded) {
			taskTeam = new LeaderFollowerTeam("TASK", 5, 100);
			governanceTeam = new LeaderFollowerTeam("GOVERNANCE", 2, 100);
		} else {
			taskTeam = new PassiveTeam();
			governanceTeam = taskTeam;
		}

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		this.constructTeam("TASK_TEAM", taskTeam);
		this.constructTeam("GOVERNANCE_TEAM", governanceTeam);

		// Configure the Managed Object
		this.constructManagedObject(this.object, "MO", officeName);

		// Configure the Work
		TransactionalWork work = new TransactionalWork();
		ReflectiveWorkBuilder builder = this.constructWork(work, "WORK",
				"doTask");

		// Configure the Task
		ReflectiveTaskBuilder task = builder.buildTask("doTask", "TASK_TEAM");
		task.getBuilder().linkPreTaskAdministration("ADMIN", "BEGIN");
		if (this.isCommit) {
			task.getBuilder().linkPostTaskAdministration("ADMIN", "COMMIT");
		}
		if (this.isRollback) {
			task.getBuilder().linkPostTaskAdministration("ADMIN", "ROLLBACK");
		}
		DependencyMappingBuilder dependencies = task.buildObject("MO",
				ManagedObjectScope.PROCESS);

		// Configure the governance flow tasks
		ReflectiveTaskBuilder beginTask = builder.buildTask("flowBegin",
				"TASK_TEAM");
		beginTask.buildObject("MO");
		beginTask.buildParameter();
		ReflectiveTaskBuilder commitTask = builder.buildTask("flowCommit",
				"TASK_TEAM");
		commitTask.buildObject("MO");
		commitTask.buildParameter();
		ReflectiveTaskBuilder rollbackTask = builder.buildTask("flowRollback",
				"TASK_TEAM");
		rollbackTask.buildObject("MO");
		rollbackTask.buildParameter();

		// Configure handling test failures
		ReflectiveTaskBuilder handleException = builder.buildTask(
				"handleException", "TASK_TEAM");
		handleException.buildParameter();
		officeBuilder.addEscalation(Throwable.class, "WORK", "handleException");

		// Configure the Governance
		GovernanceBuilder<GovernanceFlowKeys> governance = this
				.getOfficeBuilder().addGovernance("GOVERNANCE",
						new FlowGovernanceFactory(), MockTransaction.class);
		governance.setTeamName("GOVERNANCE_TEAM");
		governance.linkFlow(GovernanceFlowKeys.BEGIN, "WORK", "flowBegin",
				FlowInstigationStrategyEnum.SEQUENTIAL, String.class);
		governance.linkFlow(GovernanceFlowKeys.COMMIT, "WORK", "flowCommit",
				FlowInstigationStrategyEnum.PARALLEL, String.class);
		governance.linkFlow(GovernanceFlowKeys.ROLLBACK, "WORK",
				"flowRollback", FlowInstigationStrategyEnum.ASYNCHRONOUS,
				String.class);
		dependencies.mapGovernance("GOVERNANCE");

		// Configure the Administration
		AdministratorBuilder<TransactionDutyKey> admin = this
				.constructAdministrator("ADMIN",
						MockTransactionalAdministratorSource.class,
						"GOVERNANCE_TEAM");
		admin.administerManagedObject("MO");
		admin.addDuty("BEGIN").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("COMMIT").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("ROLLBACK").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");

		// Execute the work
		this.invokeWork("WORK", null);

		// Propagate any failure
		if (this.failure != null) {
			throw this.failure;
		}

		// Verify
		this.verifyMockObjects();

		// Ensure the task is invoked
		assertTrue("Ensure task invoked", work.isTaskInvoked);
	}

	/**
	 * Transactional {@link Work}.
	 */
	public class TransactionalWork {

		/**
		 * Indicates if the {@link Task} was invoked.
		 */
		public volatile boolean isTaskInvoked = false;

		/**
		 * {@link Task}.
		 */
		public void doTask(TransactionalObject object) {

			// Do the functionality
			object.doFunctionality();

			// Flag task invoked
			this.isTaskInvoked = true;
		}

		/**
		 * Begin flow.
		 */
		public void flowBegin(TransactionalObject object, String parameter) {
			assertEquals("Incorrect begin parameter", "BEGIN", parameter);
			object.flowBegin();
		}

		/**
		 * Commit flow.
		 */
		public void flowCommit(TransactionalObject object, String parameter) {
			assertEquals("Incorrect commit parameter", "COMMIT", parameter);
			object.flowCommit();
		}

		/**
		 * Rollback flow.
		 */
		public void flowRollback(TransactionalObject object, String parameter) {
			assertEquals("Incorrect rollback parameter", "ROLLBACK", parameter);
			object.flowRollback();
		}

		/**
		 * Handles failure in testing.
		 */
		public void handleException(Throwable cause) {
			GovernanceFlowTest.this.failure = cause;
		}
	}

	/**
	 * Transactional {@link ManagedObject}.
	 */
	public static interface TransactionalObject extends MockTransaction {

		/**
		 * Invoked to undertake some functionality.
		 */
		void doFunctionality();

		/**
		 * Invoked from begin.
		 */
		void flowBegin();

		/**
		 * Invoked from commit.
		 */
		void flowCommit();

		/**
		 * Invoked from rollback.
		 */
		void flowRollback();
	}

	public static enum GovernanceFlowKeys {
		BEGIN, COMMIT, ROLLBACK
	}

	/**
	 * {@link GovernanceFactory}.
	 */
	private static class FlowGovernanceFactory implements
			GovernanceFactory<MockTransaction, GovernanceFlowKeys> {

		/*
		 * ================== GovernanceFactory ======================
		 */

		@Override
		public Governance<MockTransaction, GovernanceFlowKeys> createGovernance()
				throws Throwable {
			return new FlowGovernance();
		}
	}

	/**
	 * {@link Governance}.
	 */
	private static class FlowGovernance implements
			Governance<MockTransaction, GovernanceFlowKeys> {

		/**
		 * {@link MockTransaction} instances.
		 */
		private final List<MockTransaction> transactions = new LinkedList<MockTransaction>();

		/*
		 * ====================== Governance =======================
		 */

		@Override
		public void governManagedObject(MockTransaction extensionInterface,
				GovernanceContext<GovernanceFlowKeys> context) throws Exception {

			// Begin transaction
			extensionInterface.begin();
			this.transactions.add(extensionInterface);

			// Trigger flow
			context.doFlow(GovernanceFlowKeys.BEGIN, "BEGIN");
		}

		@Override
		public void enforceGovernance(
				GovernanceContext<GovernanceFlowKeys> context) throws Exception {

			// Commit transaction
			for (MockTransaction transaction : this.transactions) {
				transaction.commit();
			}
			this.transactions.clear();

			// Trigger flow
			context.doFlow(GovernanceFlowKeys.COMMIT, "COMMIT");
		}

		@Override
		public void disregardGovernance(
				GovernanceContext<GovernanceFlowKeys> context) throws Exception {

			// Rollback transaction
			for (MockTransaction transaction : this.transactions) {
				transaction.rollback();
			}
			this.transactions.clear();

			// Trigger flow
			context.doFlow(GovernanceFlowKeys.ROLLBACK, "ROLLBACK");
		}
	}

}