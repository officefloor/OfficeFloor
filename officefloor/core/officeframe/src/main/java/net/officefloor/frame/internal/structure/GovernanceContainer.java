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
 * Container managing the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceContainer<I, F extends Enum<F>> {

	/**
	 * Obtains the index of this {@link Governance} registered within the
	 * {@link ProcessState}.
	 * 
	 * @return Index of this {@link Governance} registered within the
	 *         {@link ProcessState}.
	 */
	int getProcessRegisteredIndex();

	/**
	 * Indicates if this {@link Governance} is active.
	 * 
	 * @return <code>true</code> if this {@link Governance} is active.
	 */
	boolean isActive();

	/**
	 * Activates the {@link Governance}.
	 * 
	 * @return {@link FunctionState} to activate the {@link Governance}.
	 */
	FunctionState activateGovernance();

	/**
	 * Creates the {@link ActiveGovernance} to enable activation of
	 * {@link Governance} of the {@link ManagedObject}.
	 * 
	 * @param extensionInterface
	 *            Appropriate extension interface of the {@link ManagedObject}
	 *            to allow {@link Governance} over it.
	 * @param managedobjectContainer
	 *            {@link ManagedObjectContainer} of the {@link ManagedObject}.
	 * @return {@link ActiveGovernance} for the {@link ManagedObject} of the
	 *         {@link ManagedObjectContainer}.
	 */
	ActiveGovernance<I, F> createActiveGovernance(I extensionInterface, ManagedObjectContainer managedobjectContainer);

	/**
	 * Enforces the {@link Governance}.
	 * 
	 * @return {@link FunctionState} to enforce the {@link Governance}.
	 */
	FunctionState enforceGovernance();

	/**
	 * Disregards the {@link Governance}.
	 * 
	 * @return {@link FunctionState} to disregard the {@link Governance}.
	 */
	FunctionState disregardGovernance();

}