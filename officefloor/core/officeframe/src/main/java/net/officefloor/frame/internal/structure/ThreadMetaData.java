/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
