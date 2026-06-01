/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.spring.starter.rest.data;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Spring Data transaction {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataTransactionGovernance implements Governance<Repository<?, ?>, None> {

	/**
	 * {@link TransactionDefinition}.
	 */
	private final TransactionDefinition transactionDefinition;

	/**
	 * {@link ApplicationContext}.
	 */
	private final ApplicationContext applicationContext;

	/**
	 * {@link PlatformTransactionManager}.
	 */
	private PlatformTransactionManager transactionManager = null;

	/**
	 * {@link TransactionStatus}.
	 */
	private TransactionStatus transaction = null;

	/**
	 * Instantiate.
	 *
	 * @param transactionDefinition {@link TransactionDefinition}.
	 * @param applicationContext {@link ApplicationContext}.
	 */
	public SpringDataTransactionGovernance(TransactionDefinition transactionDefinition,
										   ApplicationContext applicationContext) {
		this.transactionDefinition = transactionDefinition;
		this.applicationContext = applicationContext;
	}

	/*
	 * ================== Governance ========================
	 */

	@Override
	public void governManagedObject(Repository<?, ?> repository, GovernanceContext<None> context)
			throws Throwable {

		// Start the transaction (on first repository)
		if (this.transactionManager == null) {
			this.transactionManager = applicationContext.getBeanProvider(PlatformTransactionManager.class).getObject();
			this.transaction = this.transactionManager.getTransaction(this.transactionDefinition);
		}
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
