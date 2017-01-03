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
package net.officefloor.frame.impl.execute.governance;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.governance.MockTransactionalAdministratorSource.TransactionDutyKey;
import net.officefloor.frame.impl.execute.governance.MockTransactionalAdministratorSource.TransactionGovernanceKey;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests flow for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceFlowTest extends AbstractGovernanceTestCase {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(GovernanceFlowTest.class);
	}

	/**
	 * {@link TransactionalObject}.
	 */
	private final TransactionalObject object = this.createSynchronizedMock(TransactionalObject.class);

	/**
	 * Flag indicating whether to provide commit after {@link ManagedFunction}.
	 */
	private boolean isCommit = false;

	/**
	 * Flag indicating whether to rollback after {@link ManagedFunction}.
	 */
	private boolean isRollback = false;

	/**
	 * Ensure able to commit transaction.
	 */
	public void testCommitTransaction() throws Throwable {

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
	 * Ensure able to rollback transaction.
	 */
	public void testRollbackTransaction() throws Throwable {

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
	 * Ensure able to tidy up transaction.
	 */
	public void testTidyUpTransaction() throws Throwable {

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

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		this.constructTeams();

		// Flag for manual governance management
		officeBuilder.setManuallyManageGovernance(true);

		// Configure the Managed Object
		this.constructManagedObject(this.object, "MO", officeName);

		// Configure the Work
		TransactionalWork work = new TransactionalWork();

		// Configure the Task
		ReflectiveFunctionBuilder task = this.constructFunction(work, "doTask");
		task.getBuilder().setTeam(TEAM_TASK);
		task.getBuilder().linkPreFunctionAdministration("ADMIN", "BEGIN");
		if (this.isCommit) {
			task.getBuilder().linkPostFunctionAdministration("ADMIN", "COMMIT");
		}
		if (this.isRollback) {
			task.getBuilder().linkPostFunctionAdministration("ADMIN", "ROLLBACK");
		}
		DependencyMappingBuilder dependencies = task.buildObject("MO", ManagedObjectScope.PROCESS);

		// Configure the governance flow tasks
		ReflectiveFunctionBuilder beginTask = this.constructFunction(work, "flowBegin");
		beginTask.getBuilder().setTeam(TEAM_TASK);
		beginTask.buildObject("MO");
		beginTask.buildParameter();
		ReflectiveFunctionBuilder commitTask = this.constructFunction(work, "flowCommit");
		commitTask.getBuilder().setTeam(TEAM_TASK);
		commitTask.buildObject("MO");
		commitTask.buildParameter();
		ReflectiveFunctionBuilder rollbackTask = this.constructFunction(work, "flowRollback");
		rollbackTask.getBuilder().setTeam(TEAM_TASK);
		rollbackTask.buildObject("MO");
		rollbackTask.buildParameter();

		// Configure handling test failures
		ReflectiveFunctionBuilder handleException = this.constructFunction(work, "handleException");
		handleException.getBuilder().setTeam(TEAM_TASK);
		handleException.buildParameter();
		officeBuilder.addEscalation(Throwable.class, "handleException");

		// Configure the Governance
		GovernanceBuilder<GovernanceFlowKeys> governance = this.getOfficeBuilder().addGovernance("GOVERNANCE",
				MockTransaction.class, new FlowGovernanceFactory());
		governance.setTeam(TEAM_GOVERNANCE);
		governance.linkFlow(GovernanceFlowKeys.BEGIN, "flowBegin", String.class, false);
		governance.linkFlow(GovernanceFlowKeys.COMMIT, "flowCommit", String.class, false);
		governance.linkFlow(GovernanceFlowKeys.ROLLBACK, "flowRollback", String.class, true);
		dependencies.mapGovernance("GOVERNANCE");

		// Configure the Administration
		AdministratorBuilder<TransactionDutyKey> admin = this.constructAdministrator("ADMIN",
				MockTransactionalAdministratorSource.class, TEAM_ADMINISTRATION);
		admin.administerManagedObject("MO");
		admin.addDuty("BEGIN").linkGovernance(TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("COMMIT").linkGovernance(TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("ROLLBACK").linkGovernance(TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");

		// Execute the function
		this.invokeFunction("flowBegin", null);

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
	 * Transactional functionality.
	 */
	public class TransactionalWork {

		/**
		 * Indicates if the {@link ManagedFunction} was invoked.
		 */
		public volatile boolean isTaskInvoked = false;

		/**
		 * {@link ManagedFunction}.
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
	private static class FlowGovernanceFactory implements GovernanceFactory<MockTransaction, GovernanceFlowKeys> {

		/*
		 * ================== GovernanceFactory ======================
		 */

		@Override
		public Governance<MockTransaction, GovernanceFlowKeys> createGovernance() throws Throwable {
			return new FlowGovernance();
		}
	}

	/**
	 * {@link Governance}.
	 */
	private static class FlowGovernance implements Governance<MockTransaction, GovernanceFlowKeys> {

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
			context.doFlow(GovernanceFlowKeys.BEGIN, "BEGIN", null);
		}

		@Override
		public void enforceGovernance(GovernanceContext<GovernanceFlowKeys> context) throws Exception {

			// Commit transaction
			for (MockTransaction transaction : this.transactions) {
				transaction.commit();
			}
			this.transactions.clear();

			// Trigger flow
			context.doFlow(GovernanceFlowKeys.COMMIT, "COMMIT", null);
		}

		@Override
		public void disregardGovernance(GovernanceContext<GovernanceFlowKeys> context) throws Exception {

			// Rollback transaction
			for (MockTransaction transaction : this.transactions) {
				transaction.rollback();
			}
			this.transactions.clear();

			// Trigger flow
			context.doFlow(GovernanceFlowKeys.ROLLBACK, "ROLLBACK", null);
		}
	}

}