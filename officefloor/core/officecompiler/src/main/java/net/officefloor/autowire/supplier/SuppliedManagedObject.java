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
package net.officefloor.autowire.supplier;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Supplied {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObject<D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the {@link ManagedObjectType} of this
	 * {@link SuppliedManagedObject}.
	 * 
	 * @return {@link ManagedObjectType} of this {@link SuppliedManagedObject}.
	 */
	ManagedObjectType<D> getManagedObjectType();

	/**
	 * Obtains the {@link ManagedObjectSource} for this
	 * {@link SuppliedManagedObject}.
	 * 
	 * @return {@link ManagedObjectSource} for this
	 *         {@link SuppliedManagedObject}.
	 */
	ManagedObjectSource<D, F> getManagedObjectSource();

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link SuppliedManagedObject}.
	 * 
	 * @return {@link PropertyList} to configure the
	 *         {@link SuppliedManagedObject}.
	 */
	PropertyList getProperties();

	/**
	 * Obtains the timeout for the {@link ManagedObjectSource}.
	 * 
	 * @return Timeout for the {@link ManagedObjectSource}.
	 */
	long getTimeout();

	/**
	 * Obtains the {@link SuppliedManagedObjectTeam} instances.
	 * 
	 * @return {@link SuppliedManagedObjectTeam} instances.
	 */
	SuppliedManagedObjectTeam[] getSuppliedTeams();

}