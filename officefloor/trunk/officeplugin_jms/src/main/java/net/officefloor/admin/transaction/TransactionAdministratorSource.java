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

import java.util.EnumMap;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

/**
 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}
 * to administer extension interface
 * {@link net.officefloor.admin.transaction.Transaction}.
 * 
 * @author Daniel
 */
public class TransactionAdministratorSource implements
		AdministratorSource<Transaction, TransactionDutiesEnum>,
		AdministratorSourceMetaData<Transaction, TransactionDutiesEnum>,
		Administrator<Transaction, TransactionDutiesEnum> {

	/**
	 * {@link Duty} instances.
	 */
	private final Map<TransactionDutiesEnum, Duty<Transaction, Indexed>> duties = new EnumMap<TransactionDutiesEnum, Duty<Transaction, Indexed>>(
			TransactionDutiesEnum.class);

	/**
	 * Initiate.
	 */
	public TransactionAdministratorSource() {
		// Initiate the duties
		this.duties.put(TransactionDutiesEnum.BEGIN, new BeginDuty());
		this.duties.put(TransactionDutiesEnum.COMMIT, new CommitDuty());
		this.duties.put(TransactionDutiesEnum.ROLLBACK, new RollbackDuty());
	}

	/*
	 * ====================================================================
	 * AdministratorSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#getSpecification()
	 */
	public AdministratorSourceSpecification getSpecification() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#init(net.officefloor.frame.spi.administration.source.AdministratorSourceContext)
	 */
	public void init(AdministratorSourceContext context) throws Exception {
		// No need for context
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#getMetaData()
	 */
	public AdministratorSourceMetaData<Transaction, TransactionDutiesEnum> getMetaData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#createAdministrator()
	 */
	public Administrator<Transaction, TransactionDutiesEnum> createAdministrator() {
		return this;
	}

	/*
	 * ====================================================================
	 * AdministratorSourceMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData#getExtensionInterface()
	 */
	public Class<Transaction> getExtensionInterface() {
		return Transaction.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData#getAministratorDutyKeys()
	 */
	public Class<TransactionDutiesEnum> getAministratorDutyKeys() {
		return TransactionDutiesEnum.class;
	}

	/*
	 * ====================================================================
	 * Administrator
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.Administrator#getDuty(A)
	 */
	public Duty<Transaction, ?> getDuty(TransactionDutiesEnum key) {
		return this.duties.get(key);
	}

}

/**
 * {@link net.officefloor.frame.spi.administration.Duty} to begin the
 * transaction.
 */
class BeginDuty implements Duty<Transaction, Indexed> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.Duty#doDuty(net.officefloor.frame.spi.administration.DutyContext)
	 */
	public void doDuty(DutyContext<Transaction, Indexed> context)
			throws Exception {
		// Begin the transaction
		for (Transaction transaction : context.getExtensionInterfaces()) {
			transaction.begin();
		}
	}
}

/**
 * {@link net.officefloor.frame.spi.administration.Duty} to commit the
 * transaction.
 */
class CommitDuty implements Duty<Transaction, Indexed> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.Duty#doDuty(net.officefloor.frame.spi.administration.DutyContext)
	 */
	public void doDuty(DutyContext<Transaction, Indexed> context)
			throws Exception {
		// Commit the transaction
		for (Transaction transaction : context.getExtensionInterfaces()) {
			transaction.commit();
		}
	}
}

/**
 * {@link net.officefloor.frame.spi.administration.Duty} to rollback the
 * transaction.
 */
class RollbackDuty implements Duty<Transaction, Indexed> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.Duty#doDuty(net.officefloor.frame.spi.administration.DutyContext)
	 */
	public void doDuty(DutyContext<Transaction, Indexed> context)
			throws Exception {
		// Rollback the transaction
		for (Transaction transaction : context.getExtensionInterfaces()) {
			transaction.rollback();
		}
	}
}
