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
package net.officefloor.plugin.transaction;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * {@link GovernanceSource} to govern extension interface {@link Transaction}.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionGovernanceSource extends AbstractGovernanceSource<Transaction, None>
		implements GovernanceFactory<Transaction, None> {

	/*
	 * ===================== AbstractGovernanceSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification details
	}

	@Override
	protected void loadMetaData(MetaDataContext<Transaction, None> context) throws Exception {
		context.setExtensionInterface(Transaction.class);
		context.setGovernanceFactory(this);

	}

	/*
	 * ==================== GovernanceFactory ==============================
	 */
	@Override
	public Governance<Transaction, None> createGovernance() throws Throwable {
		return new TransactionGovernance();
	}

	/**
	 * {@link Transaction} {@link Governance}.
	 */
	private static class TransactionGovernance implements Governance<Transaction, None> {

		/**
		 * {@link Transaction} instances.
		 */
		private List<Transaction> transactions = new ArrayList<>();

		/*
		 * ==================== Governance ==================================
		 */

		@Override
		public void governManagedObject(Transaction managedObjectExtension, GovernanceContext<None> context)
				throws Throwable {
			managedObjectExtension.begin();
			this.transactions.add(managedObjectExtension);
		}

		@Override
		public void enforceGovernance(GovernanceContext<None> context) throws Throwable {
			for (Transaction transaction : this.transactions) {
				transaction.commit();
			}
			this.transactions.clear();
		}

		@Override
		public void disregardGovernance(GovernanceContext<None> context) throws Throwable {
			for (Transaction transaction : this.transactions) {
				transaction.rollback();
			}
			this.transactions.clear();
		}
	}

}