/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.spring.data;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;

/**
 * Spring Data transaction {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataTransactionGovernance implements Governance<PlatformTransactionManager, None> {

	/**
	 * {@link PlatformTransactionManager}.
	 */
	private PlatformTransactionManager transactionManager = null;

	/**
	 * {@link TransactionStatus}.
	 */
	private TransactionStatus transaction = null;

	/*
	 * ================== Governance ========================
	 */

	@Override
	public void governManagedObject(PlatformTransactionManager managedObjectExtension, GovernanceContext<None> context)
			throws Throwable {

		// Should only be one platform transaction manager
		if (this.transactionManager != null) {
			throw new IllegalStateException(
					"More than one " + PlatformTransactionManager.class.getSimpleName() + " registered");
		}

		// Start the transaction
		this.transactionManager = managedObjectExtension;
		this.transaction = managedObjectExtension.getTransaction(null);
	}

	@Override
	public void enforceGovernance(GovernanceContext<None> context) throws Throwable {

		// Commit the possible transaction
		if (this.transactionManager != null) {
			this.transactionManager.commit(this.transaction);
		}

		// Clear for possible further governance
		this.transactionManager = null;
		this.transaction = null;
	}

	@Override
	public void disregardGovernance(GovernanceContext<None> context) throws Throwable {

		// Rollback the possible transaction
		if (this.transactionManager != null) {
			this.transactionManager.rollback(this.transaction);
		}

		// Clear for possible further governance
		this.transactionManager = null;
		this.transaction = null;
	}

}