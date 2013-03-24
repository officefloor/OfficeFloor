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
package net.officefloor.admin.transaction;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource;

/**
 * {@link AdministratorSource} to administer extension interface
 * {@link Transaction}.
 * 
 * @author Daniel Sagenschneider
 */
// TODO move to officeplugin_transaction (allow use by other plug-ins)
public class TransactionAdministratorSource extends
		AbstractAdministratorSource<Transaction, TransactionDutiesEnum>
		implements Administrator<Transaction, TransactionDutiesEnum> {

	/*
	 * ===================== AbstractAdministratorSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification details
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<Transaction, TransactionDutiesEnum> context)
			throws Exception {
		context.addDuty(TransactionDutiesEnum.BEGIN);
		context.addDuty(TransactionDutiesEnum.ROLLBACK);
		context.addDuty(TransactionDutiesEnum.COMMIT);
		context.setExtensionInterface(Transaction.class);
	}

	@Override
	public Administrator<Transaction, TransactionDutiesEnum> createAdministrator() {
		return this;
	}

	/*
	 * =================== Administrator ==================================
	 */

	@Override
	public Duty<Transaction, TransactionDutiesEnum, None> getDuty(
			DutyKey<TransactionDutiesEnum> key) {
		switch (key.getKey()) {
		case BEGIN:
			return new BeginDuty();
		case ROLLBACK:
			return new RollbackDuty();
		case COMMIT:
			return new CommitDuty();
		default:
			throw new IllegalStateException("Unknown key " + key);
		}
	}

	/**
	 * {@link Duty} to begin the transaction.
	 */
	private class BeginDuty implements
			Duty<Transaction, TransactionDutiesEnum, None> {
		@Override
		public void doDuty(
				DutyContext<Transaction, TransactionDutiesEnum, None> context)
				throws Exception {
			// Begin the transaction
			for (Transaction transaction : context.getExtensionInterfaces()) {
				transaction.begin();
			}
		}
	}

	/**
	 * {@link Duty} to commit the transaction.
	 */
	private class CommitDuty implements
			Duty<Transaction, TransactionDutiesEnum, None> {
		@Override
		public void doDuty(
				DutyContext<Transaction, TransactionDutiesEnum, None> context)
				throws Exception {
			// Commit the transaction
			for (Transaction transaction : context.getExtensionInterfaces()) {
				transaction.commit();
			}
		}
	}

	/**
	 * {@link Duty} to roll back the transaction.
	 */
	private class RollbackDuty implements
			Duty<Transaction, TransactionDutiesEnum, None> {
		@Override
		public void doDuty(
				DutyContext<Transaction, TransactionDutiesEnum, None> context)
				throws Exception {
			// Roll back the transaction
			for (Transaction transaction : context.getExtensionInterfaces()) {
				transaction.rollback();
			}
		}
	}

}