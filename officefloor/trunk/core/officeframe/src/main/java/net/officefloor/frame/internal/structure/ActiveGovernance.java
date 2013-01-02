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

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Identifies active {@link Governance} of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActiveGovernance<I, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the index by which the {@link ManagedObject} has registered this
	 * {@link ActiveGovernance}.
	 * <p>
	 * This allows the {@link ManagedObject} to quickly find the
	 * {@link ActiveGovernance} for unregistering it.
	 * 
	 * @return Index by which the {@link ManagedObject} has registered this
	 *         {@link ActiveGovernance}.
	 */
	int getManagedObjectRegisteredIndex();

	/**
	 * Indicates if the {@link Governance} is still active.
	 * 
	 * @return <code>true</code> if the {@link Governance} is still active.
	 */
	boolean isActive();

	/**
	 * Creates the {@link GovernanceActivity} to provide {@link Governance} on
	 * the {@link ManagedObject}.
	 * 
	 * @return {@link GovernanceActivity} to provide {@link Governance} on the
	 *         {@link ManagedObject}.
	 */
	GovernanceActivity<I, F> createGovernActivity();

}