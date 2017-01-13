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

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * Meta-data of a bound {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface RawBoundManagedObjectMetaData {

	/**
	 * Obtains the name the {@link ManagedObject} is bound under.
	 *
	 * @return Name the {@link ManagedObject} is bound under.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectIndex}.
	 *
	 * @return {@link ManagedObjectIndex}.
	 */
	ManagedObjectIndex getManagedObjectIndex();

	/**
	 * Obtains the index of the default
	 * {@link RawBoundManagedObjectInstanceMetaData} for this
	 * {@link RawBoundManagedObjectMetaData}.
	 *
	 * @return Index of the default
	 *         {@link RawBoundManagedObjectInstanceMetaData} for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	int getDefaultInstanceIndex();

	/**
	 * Obtains the {@link RawBoundManagedObjectInstanceMetaData} instances for
	 * the {@link ManagedObjectSource} instances that may provide a
	 * {@link ManagedObject} for this {@link RawBoundManagedObjectMetaData}.
	 *
	 * @return {@link RawBoundManagedObjectMetaData} instances for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	RawBoundManagedObjectInstanceMetaData<?>[] getRawBoundManagedObjectInstanceMetaData();

}