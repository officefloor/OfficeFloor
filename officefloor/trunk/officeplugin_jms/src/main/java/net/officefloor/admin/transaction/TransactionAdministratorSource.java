/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.admin.transaction;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource;

/**
 * {@link AdministratorSource} to administer extension interface
 * {@link Transaction}.
 * 
 * @author Daniel
 */
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
		context.setDutyKeys(TransactionDutiesEnum.class);
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
	public Duty<Transaction, ?> getDuty(TransactionDutiesEnum key) {
		switch (key) {
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
	private class BeginDuty implements Duty<Transaction, Indexed> {
		@Override
		public void doDuty(DutyContext<Transaction, Indexed> context)
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
	private class CommitDuty implements Duty<Transaction, Indexed> {
		@Override
		public void doDuty(DutyContext<Transaction, Indexed> context)
				throws Exception {
			// Commit the transaction
			for (Transaction transaction : context.getExtensionInterfaces()) {
				transaction.commit();
			}
		}
	}

	/**
	 * {@link Duty} to rollback the transaction.
	 */
	private class RollbackDuty implements Duty<Transaction, Indexed> {
		@Override
		public void doDuty(DutyContext<Transaction, Indexed> context)
				throws Exception {
			// Rollback the transaction
			for (Transaction transaction : context.getExtensionInterfaces()) {
				transaction.rollback();
			}
		}
	}

}