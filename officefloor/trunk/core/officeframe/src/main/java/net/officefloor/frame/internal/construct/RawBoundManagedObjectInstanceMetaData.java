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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Provides the meta-data for a particular {@link ManagedObjectSource} that may
 * provide a {@link ManagedObject} for the containing
 * {@link RawBoundManagedObjectMetaData}.
 *
 * @author Daniel Sagenschneider
 */
public interface RawBoundManagedObjectInstanceMetaData<D extends Enum<D>> {

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaData} instances of the
	 * dependencies of this {@link ManagedObject}.
	 *
	 * @return {@link RawBoundManagedObjectMetaData} instances of the
	 *         dependencies of this {@link ManagedObject}.
	 */
	RawBoundManagedObjectMetaData[] getDependencies();

	/**
	 * Obtains the {@link RawManagedObjectMetaData}.
	 *
	 * @return {@link RawManagedObjectMetaData}.
	 */
	RawManagedObjectMetaData<D, ?> getRawManagedObjectMetaData();

	/**
	 * Obtains the {@link ManagedObjectMetaData}.
	 *
	 * @return {@link ManagedObjectMetaData}.
	 */
	ManagedObjectMetaData<D> getManagedObjectMetaData();

}