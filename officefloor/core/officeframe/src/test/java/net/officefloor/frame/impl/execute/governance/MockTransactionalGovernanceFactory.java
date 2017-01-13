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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * {@link GovernanceFactory} for the {@link MockTransaction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTransactionalGovernanceFactory implements
		GovernanceFactory<MockTransaction, None> {

	/*
	 * ===================== GovernanceFactory =======================
	 */

	@Override
	public Governance<MockTransaction, None> createGovernance()
			throws Throwable {
		return new MockTransactionalGovernance();
	}

	/**
	 * Mock transactional {@link Governance}.
	 */
	private class MockTransactionalGovernance implements
			Governance<MockTransaction, None> {

		/**
		 * {@link MockTransaction} instances.
		 */
		private final List<MockTransaction> transactions = new LinkedList<MockTransaction>();

		/*
		 * ====================== Governance =======================
		 */

		@Override
		public void governManagedObject(MockTransaction extensionInterface,
				GovernanceContext<None> context) throws Exception {
			extensionInterface.begin();
			this.transactions.add(extensionInterface);
		}

		@Override
		public void enforceGovernance(GovernanceContext<None> context)
				throws Exception {
			for (MockTransaction transaction : this.transactions) {
				transaction.commit();
			}
			this.transactions.clear();
		}

		@Override
		public void disregardGovernance(GovernanceContext<None> context)
				throws Exception {
			for (MockTransaction transaction : this.transactions) {
				transaction.rollback();
			}
			this.transactions.clear();
		}
	}

}