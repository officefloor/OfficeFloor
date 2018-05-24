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
package net.officefloor.plugin.jpa;

import javax.persistence.EntityTransaction;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;

/**
 * {@link Governance} for JPA transaction.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaTransactionGovernance implements Governance<EntityTransaction, None> {

	/**
	 * {@link EntityTransaction} under {@link Governance}.
	 */
	private EntityTransaction transaction = null;

	/*
	 * ======================= JpaTransactionGovernance ======================
	 */

	@Override
	public void governManagedObject(EntityTransaction extensionInterface, GovernanceContext<None> context)
			throws Throwable {

		// Not implementing two-phase commits, so can only have one transaction
		if (this.transaction != null) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " may only managed one "
					+ EntityTransaction.class.getSimpleName() + " as does not support two-phase commits");
		}

		// Register the transaction
		this.transaction = extensionInterface;

		// Begin the transaction
		this.transaction.begin();
	}

	@Override
	public void enforceGovernance(GovernanceContext<None> context) throws Throwable {

		// Commit the transaction
		if (this.transaction != null) {
			this.transaction.commit();
		}

		// Release transaction
		this.transaction = null;
	}

	@Override
	public void disregardGovernance(GovernanceContext<None> context) throws Throwable {

		// Rollback the transaction
		if (this.transaction != null) {
			this.transaction.rollback();
		}

		// Release the transaction
		this.transaction = null;
	}

}