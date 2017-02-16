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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.function.Promise;

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
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 *         instances bound to the {@link ThreadState}.
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
	 * Obtains the maximum {@link Promise} chain length for this
	 * {@link ThreadState}.
	 * <p>
	 * Once the {@link Promise} chain has reached this length, it will be broken
	 * to be spawned in another {@link Thread}. This avoids
	 * {@link StackOverflowError} issues in {@link Promise} chain being too
	 * large.
	 * 
	 * @return Maximum {@link Promise} chain length for this
	 *         {@link ThreadState}.
	 */
	int getMaximumPromiseChainLength();

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