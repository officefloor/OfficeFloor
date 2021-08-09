/*-
 * #%L
 * Spring Data Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
