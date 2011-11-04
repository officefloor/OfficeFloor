/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
public interface GovernanceContainer<I> {

	/**
	 * Indicates if this {@link Governance} is active.
	 * 
	 * @return <code>true</code> if this {@link Governance} is active.
	 */
	boolean isActive();

	/**
	 * Activates the {@link Governance}.
	 * 
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void activateGovernance(ContainerContext context);

	/**
	 * Creates the {@link ActiveGovernance} to enable activation of
	 * {@link Governance} of the {@link ManagedObject}.
	 * 
	 * @param extensionInterface
	 *            Appropriate extension interface of the {@link ManagedObject}
	 *            to allow {@link Governance} over it.
	 * @param managedobjectContainer
	 *            {@link ManagedObjectContainer} of the {@link ManagedObject}.
	 * @return {@link ActiveGovernance}.
	 * @throws Exception
	 *             If fails to govern the {@link ManagedObject}.
	 */
	ActiveGovernance createActiveGovernance(I extensionInterface,
			ManagedObjectContainer managedobjectContainer);

	/**
	 * Enforces the {@link Governance}.
	 * 
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void enforceGovernance(ContainerContext context);

	/**
	 * Disregards the {@link Governance}.
	 * 
	 * @param context
	 *            {@link ContainerContext}.
	 */
	void disregardGovernance(ContainerContext context);

}