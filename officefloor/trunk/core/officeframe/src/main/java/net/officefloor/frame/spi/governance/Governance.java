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
package net.officefloor.frame.spi.governance;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <p>
 * Provides {@link Governance} over the {@link ManagedObject} instances.
 * <p>
 * The extension interface of the {@link ManagedObject} is used to provide the
 * {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Governance<I> {

	/**
	 * Starts the {@link Governance} of the {@link ManagedObject} instances.
	 */
	void startGovernance();

	/**
	 * Registers the {@link ManagedObject} for {@link Governance}.
	 * 
	 * @param extensionInterface
	 *            Extension interface of the {@link ManagedObject}.
	 * @throws Exception
	 *             If fails to govern the {@link ManagedObject}.
	 */
	void governManagedObject(I extensionInterface) throws Exception;

	/**
	 * Applies the {@link Governance} to the {@link ManagedObject} instances
	 * under {@link Governance}.
	 */
	void applyGovernance();

	/**
	 * Stops {@link Governance} of the {@link ManagedObject} instances.
	 */
	void stopGovernance();

}