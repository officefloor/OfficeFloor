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
package net.officefloor.frame.integrate.governance;

import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionDutyKey;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionGovernanceKey;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveFunctionBuilder.ReflectiveFunctionBuilder;

/**
 * Typical use of {@link Governance} is for transaction management. This test to
 * provide transaction management example to ensure container management of
 * {@link Governance} handles transaction.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionGovernanceTest extends AbstractGovernanceTestCase {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(TransactionGovernanceTest.class);
	}

	/**
	 * {@link TransactionalObject}.
	 */
	private final TransactionalObject object = this
			.createSynchronizedMock(TransactionalObject.class);

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
	public void testCommitTransaction() throws Exception {

		// Commit
		this.isCommit = true;

		// Record committing the transaction
		this.object.begin();
		this.object.doFunctionality();
		this.object.commit();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to rollback transaction.
	 */
	public void testRollbackTransaction() throws Exception {

		// Rollback
		this.isRollback = true;

		// Record rolling back the transaction
		this.object.begin();
		this.object.doFunctionality();
		this.object.rollback();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to tidy up transaction.
	 */
	public void testTidyUpTransaction() throws Exception {

		// Ensure transaction governing functionality
		this.object.begin();
		this.object.doFunctionality();
		this.object.rollback();

		// Test
		this.doTest();
	}

	/**
	 * Ensure handle {@link Escalation} from transaction failure.
	 */
	public void testTransactionEscalation() throws Exception {

		final SQLException exception = new SQLException("TEST");

		// Commit attempt
		this.isCommit = true;

		// Record transaction failed
		this.object.begin();
		this.object.doFunctionality();
		this.object.commit();
		this.control(this.object).setThrowable(exception);

		// Test
		try {
			this.doTest();
			fail("Should not be successful");
		} catch (SQLException ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}

	/**
	 * Undertake the test.
	 */
	private void doTest() throws Exception {

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
		this.constructTeams();

		// Flag for manual governance management
		this.getOfficeBuilder().setManuallyManageGovernance(true);

		// Configure the Managed Object
		this.constructManagedObject(this.object, "MO", officeName);

		// Configure the Work
		TransactionalWork work = new TransactionalWork();
		ReflectiveFunctionBuilder builder = this.constructWork(work, "WORK",
				"doTask");

		// Configure the Task
		ReflectiveFunctionBuilder task = builder.buildTask("doTask", TEAM_TASK);
		task.getBuilder().linkPreTaskAdministration("ADMIN", "BEGIN");
		if (this.isCommit) {
			task.getBuilder().linkPostTaskAdministration("ADMIN", "COMMIT");
		}
		if (this.isRollback) {
			task.getBuilder().linkPostTaskAdministration("ADMIN", "ROLLBACK");
		}
		DependencyMappingBuilder dependencies = task.buildObject("MO",
				ManagedObjectScope.PROCESS);

		// Configure the Escalation
		ReflectiveFunctionBuilder escalation = builder.buildTask(
				"handleEscalation", TEAM_TASK);
		escalation.buildParameter();

		// Configure the Governance
		GovernanceBuilder<None> governance = this.getOfficeBuilder()
				.addGovernance("GOVERNANCE",
						new MockTransactionalGovernanceFactory(),
						MockTransaction.class);
		governance.setTeam(TEAM_GOVERNANCE);
		governance
				.addEscalation(SQLException.class, "WORK", "handleEscalation");
		dependencies.mapGovernance("GOVERNANCE");

		// Configure the Administration
		AdministratorBuilder<TransactionDutyKey> admin = this
				.constructAdministrator("ADMIN",
						MockTransactionalAdministratorSource.class,
						TEAM_ADMINISTRATION);
		admin.administerManagedObject("MO");
		admin.addDuty("BEGIN").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("COMMIT").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
		admin.addDuty("ROLLBACK").linkGovernance(
				TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");

		// Execute the work
		try {
			this.invokeWork("WORK", null);
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Verify
		this.verifyMockObjects();

		// Ensure the task is invoked
		assertTrue("Ensure task invoked", work.isTaskInvoked);

		// Throw handled exception
		if (work.exception != null) {
			throw work.exception;
		}
	}

	/**
	 * Transactional {@link Work}.
	 */
	public static class TransactionalWork {

		/**
		 * Indicates if the {@link ManagedFunction} was invoked.
		 */
		public volatile boolean isTaskInvoked = false;

		/**
		 * Handled {@link SQLException}.
		 */
		public volatile SQLException exception = null;

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
		 * {@link EscalationFlow}.
		 */
		public void handleEscalation(SQLException ex) {
			this.exception = ex;
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
	}

}