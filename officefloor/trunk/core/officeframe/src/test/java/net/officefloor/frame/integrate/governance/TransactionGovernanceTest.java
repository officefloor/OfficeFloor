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

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
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
	 * Ensure able to commit transaction.
	 */
	public void testTransaction() throws Exception {

		// Mocks
		final TransactionalObject object = this
				.createMock(TransactionalObject.class);

		// Ensure transaction governing functionality
		object.begin();
		object.doFunctionality();
		object.commit();

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
		this.constructTeam("TEAM", new PassiveTeam());
		this.constructManagedObject(object, "MO", officeName);
		TransactionalWork work = new TransactionalWork();
		ReflectiveWorkBuilder builder = this.constructWork(work, "WORK",
				"doTask");
		ReflectiveTaskBuilder task = builder.buildTask("doTask", "TEAM");
		task.getBuilder().linkPreTaskAdministration("ADMIN", "BEGIN");
		task.getBuilder().linkPostTaskAdministration("ADMIN", "COMMIT");
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		AdministratorBuilder<Indexed> admin = this.constructAdministrator(
				"ADMIN", MockTransactionalAdministratorSource.class, "TEAM");
		admin.administerManagedObject("MO");
		admin.addDuty("BEGIN");
		admin.addDuty("COMMIT");

		// Execute the work
		this.invokeWork("WORK", null);

		// Verify
		this.verifyMockObjects();

		// Ensure the task is invoked
		assertTrue("Ensure task invoked", work.isTaskInvoked);
	}

	/**
	 * Transactional {@link Work}.
	 */
	public static class TransactionalWork {

		/**
		 * Indicates if the {@link Task} was invoked.
		 */
		public boolean isTaskInvoked = false;

		/**
		 * {@link Task}.
		 */
		public void doTask(TransactionalObject object) {

			// Do the functionality
			object.doFunctionality();

			// Flag task invoked
			this.isTaskInvoked = true;
		}
	}

	/**
	 * Transactional {@link ManagedObject}.
	 */
	public static interface TransactionalObject extends MockTransaction {

		/**
		 * Inovked to undertake some functionality.
		 */
		void doFunctionality();
	}

	/**
	 * Mock transactional {@link AdministratorSource}.
	 */
	public static class MockTransactionalAdministratorSource extends
			AbstractAdministratorSource<MockTransaction, Indexed> implements
			Administrator<MockTransaction, Indexed> {

		/*
		 * =================== AdministratorSource ====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(
				MetaDataContext<MockTransaction, Indexed> context)
				throws Exception {
			context.setExtensionInterface(MockTransaction.class);
			context.addDuty("BEGIN");
			context.addDuty("COMMIT");
		}

		@Override
		public Administrator<MockTransaction, Indexed> createAdministrator()
				throws Throwable {
			return this;
		}

		/*
		 * ====================== Administrator =======================
		 */

		@Override
		public Duty<MockTransaction, ?, ?> getDuty(DutyKey<Indexed> dutyKey) {
			return new MockTransactionalDuty(dutyKey);
		}
	}

	/**
	 * Mock transactional {@link Duty}.
	 */
	public static class MockTransactionalDuty implements
			Duty<MockTransaction, None, Indexed> {

		/**
		 * {@link DutyKey}.
		 */
		private final DutyKey<Indexed> key;

		/**
		 * Initiate.
		 * 
		 * @param key
		 *            {@link DutyKey}.
		 */
		public MockTransactionalDuty(DutyKey<Indexed> key) {
			this.key = key;
		}

		/*
		 * =========================== Duty ===========================
		 */

		@Override
		public void doDuty(DutyContext<MockTransaction, None, Indexed> context)
				throws Throwable {
			switch (this.key.getIndex()) {
			case 0:

				// TODO remove to do within governance
				for (MockTransaction transaction : context
						.getExtensionInterfaces()) {
					transaction.begin();
				}

				// Begin transaction
				// context.getGovernance(0).activateGovernance();
				break;

			case 1:

				// TODO remove to do within governance
				for (MockTransaction transaction : context
						.getExtensionInterfaces()) {
					transaction.commit();
				}

				// Commit transaction
				// context.getGovernance(0).enforceGovernance();
				break;
			}
		}
	}

	/**
	 * Mock transactional {@link GovernanceSource}.
	 */
	public static class MockTransactionalGovernanceSource extends
			AbstractGovernanceSource<MockTransaction, Indexed> {

		/*
		 * ===================== GovernanceSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties
		}

		@Override
		protected void loadMetaData(
				MetaDataContext<MockTransaction, Indexed> context)
				throws Exception {
			context.setExtensionInterface(MockTransaction.class);
		}

		@Override
		public Governance<MockTransaction, Indexed> createGovernance()
				throws Throwable {
			return new MockTransactionalGovernance();
		}
	}

	/**
	 * Mock transactional {@link Governance}.
	 */
	private static class MockTransactionalGovernance implements
			Governance<MockTransaction, Indexed> {

		/*
		 * ====================== Governance =======================
		 */

		@Override
		public void governManagedObject(MockTransaction extensionInterface,
				GovernanceContext<Indexed> context) throws Exception {
			// TODO implement
			// Governance<MockTransaction,Indexed>.governManagedObject
			throw new UnsupportedOperationException(
					"TODO implement Governance<MockTransaction,Indexed>.governManagedObject");
		}

		@Override
		public void enforceGovernance(GovernanceContext<Indexed> context) {
			// TODO implement
			// Governance<MockTransaction,Indexed>.enforceGovernance
			throw new UnsupportedOperationException(
					"TODO implement Governance<MockTransaction,Indexed>.enforceGovernance");
		}

		@Override
		public void disregardGovernance(GovernanceContext<Indexed> context) {
			// TODO implement
			// Governance<MockTransaction,Indexed>.disregardGovernance
			throw new UnsupportedOperationException(
					"TODO implement Governance<MockTransaction,Indexed>.disregardGovernance");
		}
	}

	/**
	 * Mock transaction interface.
	 */
	public static interface MockTransaction {

		/**
		 * Mock begin transaction.
		 */
		void begin();

		/**
		 * Mock commit transaction.
		 */
		void commit();
	}

}