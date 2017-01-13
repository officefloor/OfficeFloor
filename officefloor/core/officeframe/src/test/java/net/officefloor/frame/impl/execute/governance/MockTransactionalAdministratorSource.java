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

import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.build.None;

/**
 * {@link MockTransaction} by {@link MockTransactionalGovernanceFactory}
 * {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTransactionalAdministratorSource
		extends
		AbstractAdministratorSource<MockTransaction, MockTransactionalAdministratorSource.TransactionDutyKey>
		implements
		Administration<MockTransaction, MockTransactionalAdministratorSource.TransactionDutyKey> {

	/**
	 * Key to obtain the {@link GovernanceManager}.
	 */
	public static enum TransactionGovernanceKey {
		TRANSACTION
	}

	/**
	 * {@link AdministrationDuty} keys for the {@link MockTransaction}.
	 */
	public static enum TransactionDutyKey {
		BEGIN, COMMIT, ROLLBACK
	}

	/*
	 * =================== AdministratorSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<MockTransaction, TransactionDutyKey> context)
			throws Exception {
		context.setExtensionInterface(MockTransaction.class);
		context.addDuty(TransactionDutyKey.BEGIN);
		context.addDuty(TransactionDutyKey.COMMIT);
		context.addDuty(TransactionDutyKey.ROLLBACK);
	}

	@Override
	public Administration<MockTransaction, TransactionDutyKey> createAdministrator()
			throws Throwable {
		return this;
	}

	/*
	 * ====================== Administrator =======================
	 */

	@Override
	public AdministrationDuty<MockTransaction, ?, ?> getDuty(
			DutyKey<TransactionDutyKey> dutyKey) {
		return new MockTransactionalDuty(dutyKey);
	}

	/**
	 * Mock transactional {@link AdministrationDuty}.
	 */
	public static class MockTransactionalDuty implements
			AdministrationDuty<MockTransaction, None, TransactionGovernanceKey> {

		/**
		 * {@link DutyKey}.
		 */
		private final DutyKey<TransactionDutyKey> key;

		/**
		 * Initiate.
		 * 
		 * @param key
		 *            {@link DutyKey}.
		 */
		public MockTransactionalDuty(DutyKey<TransactionDutyKey> key) {
			this.key = key;
		}

		/*
		 * =========================== Duty ===========================
		 */

		@Override
		public void doDuty(
				AdministrationContext<MockTransaction, None, TransactionGovernanceKey> context)
				throws Throwable {

			// Obtain the governance manager
			GovernanceManager governance = context
					.getGovernance(TransactionGovernanceKey.TRANSACTION);

			// Do duty for governance
			switch (this.key.getKey()) {
			case BEGIN:
				// Begin transaction
				governance.activateGovernance();
				break;

			case COMMIT:
				// Commit transaction
				governance.enforceGovernance();
				break;

			case ROLLBACK:
				// Rollback transaction
				governance.disregardGovernance();
				break;
			}
		}
	}

}