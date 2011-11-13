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

import java.sql.SQLException;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionDutyKey;
import net.officefloor.frame.integrate.governance.MockTransactionalAdministratorSource.TransactionGovernanceKey;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Typical use of {@link Governance} is for transaction management. This test to
 * provide transaction management example to ensure container management of
 * {@link Governance} handles transaction.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionGovernanceTest extends AbstractOfficeConstructTestCase {

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
	public void test_Passive_CommitTransaction() throws Exception {

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
	 * Ensure able to commit transaction with {@link LeaderFollowerTeam}.
	 */
	public void test_LeaderFollower_CommitTransaction() throws Exception {

		// Multi-threaded Commit
		this.isMultiThreaded = true;
		this.isCommit = true;

		// Record committing the transaction
		this.object.begin();
		this.object.doFunctionality();
		this.object.commit();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to rollback transaction with {@link PassiveTeam}.
	 */
	public void test_Passive_RollbackTransaction() throws Exception {

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
	 * Ensure able to rollback transaction with {@link LeaderFollowerTeam}.
	 */
	public void test_LeaderFollower_RollbackTransaction() throws Exception {

		// Multi-threaded Rollback
		this.isMultiThreaded = true;
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
	public void test_Passive_TidyUpTransaction() throws Exception {

		// Ensure transaction governing functionality
		this.object.begin();
		this.object.doFunctionality();
		this.object.rollback();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to tidy up transaction.
	 */
	public void test_LeaderFollower_TidyUpTransaction() throws Exception {

		// Multi-threaded
		this.isMultiThreaded = true;

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
	public void test_Passive_TransactionEscalation() throws Exception {

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
	 * Ensure handle {@link Escalation} from transaction failure.
	 */
	public void test_LeaderFollower_TransactionEscalation() throws Exception {

		final SQLException exception = new SQLException("TEST");

		// Multi-threaded commit attempt
		this.isCommit = true;
		this.isMultiThreaded = true;

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

		// Create the teams
		Team taskTeam;
		Team governanceTeam;
		if (this.isMultiThreaded) {
			taskTeam = new PassiveTeam();
			governanceTeam = taskTeam;
		} else {
			taskTeam = new LeaderFollowerTeam("TASK", 5, 100);
			governanceTeam = new LeaderFollowerTeam("GOVERNANCE", 2, 100);
		}

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
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

		// Configure the Escalation
		ReflectiveTaskBuilder escalation = builder.buildTask(
				"handleEscalation", "TASK_TEAM");
		escalation.buildParameter();

		// Configure the Governance
		GovernanceBuilder governance = this.getOfficeBuilder().addGovernance(
				"GOVERNANCE", new MockTransactionalGovernanceFactory(),
				MockTransaction.class);
		governance.setTeamName("GOVERNANCE_TEAM");
		governance
				.addEscalation(SQLException.class, "WORK", "handleEscalation");
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
		 * Indicates if the {@link Task} was invoked.
		 */
		public volatile boolean isTaskInvoked = false;

		/**
		 * Handled {@link SQLException}.
		 */
		public volatile SQLException exception = null;

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