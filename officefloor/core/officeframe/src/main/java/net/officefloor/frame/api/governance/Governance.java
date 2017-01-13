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
package net.officefloor.frame.api.governance;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Provides {@link Governance} over the {@link ManagedObject} instances.
 * <p>
 * The extension interface of the {@link ManagedObject} is used to provide the
 * {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Governance<E, F extends Enum<F>> {

	/**
	 * Registers the {@link ManagedObject} for {@link Governance}.
	 * 
	 * @param managedObjectExtension
	 *            Extension of the {@link ManagedObject} to enable
	 *            {@link Governance}.
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Throwable
	 *             If fails to govern the {@link ManagedObject}.
	 */
	void governManagedObject(E managedObjectExtension, GovernanceContext<F> context) throws Throwable;

	/**
	 * Enforces the {@link Governance} of the {@link ManagedObject} instances
	 * under {@link Governance}.
	 * 
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Throwable
	 *             If fails to enforce {@link Governance}.
	 */
	void enforceGovernance(GovernanceContext<F> context) throws Throwable;

	/**
	 * Disregard {@link Governance} of the {@link ManagedObject} instances.
	 * 
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Throwable
	 *             If fails to disregard {@link Governance}.
	 */
	void disregardGovernance(GovernanceContext<F> context) throws Throwable;

}