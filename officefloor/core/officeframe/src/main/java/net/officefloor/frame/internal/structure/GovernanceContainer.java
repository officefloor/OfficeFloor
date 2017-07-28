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
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Container managing the {@link Governance}.
 * <p>
 * {@link Governance} may only reside on the single {@link ThreadState}
 * requiring the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceContainer<E> {

	/**
	 * Indicates if {@link Governance} within this {@link GovernanceContainer}
	 * is active.
	 * 
	 * @return <code>true</code> if the {@link Governance} is active.
	 */
	boolean isGovernanceActive();

	/**
	 * Registers the {@link ManagedObject} for {@link Governance}.
	 * 
	 * @param managedObjectExtension
	 *            Extension of the {@link ManagedObject} to enable
	 *            {@link Governance}.
	 * @param managedObjectContainer
	 *            {@link ManagedObjectContainer} for the {@link ManagedObject}.
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} for the {@link ManagedObject}.
	 * @param managedFunctionContainer
	 *            {@link ManagedFunctionContainer} to enable access to
	 *            {@link ManagedFunctionContainer} bound dependencies.
	 * @return {@link RegisteredGovernance}.
	 */
	<O extends Enum<O>> RegisteredGovernance registerManagedObject(E managedObjectExtension,
			ManagedObjectContainer managedObjectContainer, ManagedObjectMetaData<O> managedObjectMetaData,
			ManagedFunctionContainer managedFunctionContainer);

	/**
	 * Activates the {@link Governance}. This will co-ordinate the
	 * {@link Governance} over the {@link ManagedObject} instances.
	 * 
	 * @return {@link FunctionState} to activate the {@link Governance}.
	 */
	FunctionState activateGovernance();

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

	/**
	 * Deactivates the {@link Governance}. This will release the
	 * {@link ManagedObject} instances from {@link Governance}.
	 * 
	 * @return Deactivate the {@link Governance}.
	 */
	FunctionState deactivateGovernance();

}