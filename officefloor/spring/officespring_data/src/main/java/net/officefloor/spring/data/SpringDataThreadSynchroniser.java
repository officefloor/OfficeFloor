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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import net.officefloor.frame.api.thread.ThreadSynchroniser;

/**
 * Spring Data {@link ThreadSynchroniser}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataThreadSynchroniser implements ThreadSynchroniser {

	/**
	 * Resources bound to the {@link Thread}.
	 */
	private Map<Object, Object> resourceMap;

	/**
	 * Synchronizations.
	 */
	private List<TransactionSynchronization> synchronizations;

	/**
	 * Current transaction name.
	 */
	private String currentTransactionName;

	/**
	 * Current transaction read-only.
	 */
	private boolean currentTransactionReadOnly;

	/**
	 * Current transaction isolation level.
	 */
	private Integer currentTransactionIsolationLevel;

	/**
	 * Actual transaction active.
	 */
	private boolean actualTransactionActive;

	/*
	 * ==================== ThreadSynchroniser =========================
	 */

	@Override
	public void suspendThread() {

		// Capture and clear resources
		this.resourceMap = new HashMap<>(TransactionSynchronizationManager.getResourceMap());
		for (Object key : this.resourceMap.keySet()) {
			TransactionSynchronizationManager.unbindResource(key);
		}

		// Capture and clear state
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			this.synchronizations = TransactionSynchronizationManager.getSynchronizations();
		} else {
			this.synchronizations = null;
		}
		this.currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
		this.currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		this.currentTransactionIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
		this.actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
		TransactionSynchronizationManager.clear();
	}

	@Override
	public void resumeThread() {

		// Load resources
		for (Object key : this.resourceMap.keySet()) {
			TransactionSynchronizationManager.bindResource(key, this.resourceMap.get(key));
		}

		// Load possible synchronizations
		if (this.synchronizations != null) {
			TransactionSynchronizationManager.initSynchronization();
			for (TransactionSynchronization synchronization : this.synchronizations) {
				TransactionSynchronizationManager.registerSynchronization(synchronization);
			}
		}

		// Load remaining state
		TransactionSynchronizationManager.setCurrentTransactionName(this.currentTransactionName);
		TransactionSynchronizationManager.setCurrentTransactionReadOnly(this.currentTransactionReadOnly);
		TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(this.currentTransactionIsolationLevel);
		TransactionSynchronizationManager.setActualTransactionActive(this.actualTransactionActive);
	}

}
