/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * Meta-data for the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadMetaData {

	/**
	 * Obtains the {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to the {@link ThreadState}.
	 * 
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject} instances
	 *         bound to the {@link ThreadState}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link GovernanceMetaData} of the possible {@link Governance}
	 * within this {@link ThreadState}.
	 * 
	 * @return {@link GovernanceMetaData} instances.
	 */
	GovernanceMetaData<?, ?>[] getGovernanceMetaData();

	/**
	 * <p>
	 * Obtains the maximum {@link FunctionState} chain length for this
	 * {@link ThreadState}.
	 * <p>
	 * Once the {@link FunctionState} chain has reached this length, it will be
	 * broken. (spawned in another {@link Thread}). This avoids
	 * {@link StackOverflowError} issues in {@link FunctionState} chain being too
	 * large.
	 * 
	 * @return Maximum {@link FunctionState} chain length for this
	 *         {@link ThreadState}.
	 */
	int getMaximumFunctionChainLength();

	/**
	 * Obtains the {@link ThreadSynchroniserFactory} instances.
	 * 
	 * @return {@link ThreadSynchroniserFactory} instances.
	 */
	ThreadSynchroniserFactory[] getThreadSynchronisers();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link Office}.
	 * 
	 * @return {@link EscalationProcedure} for the {@link Office}.
	 */
	EscalationProcedure getOfficeEscalationProcedure();

	/**
	 * Obtains the catch all {@link EscalationFlow} for the {@link OfficeFloor}.
	 * 
	 * @return Catch all {@link EscalationFlow} for the {@link OfficeFloor}.
	 */
	EscalationFlow getOfficeFloorEscalation();

}
