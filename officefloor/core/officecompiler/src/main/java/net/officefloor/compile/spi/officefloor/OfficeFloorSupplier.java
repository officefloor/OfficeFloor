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
package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Supplies {@link OfficeFloorManagedObjectSource} instances within the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSupplier extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeFloorSupplier}.
	 * 
	 * @return Name of this {@link OfficeFloorSupplier}.
	 */
	String getOfficeFloorSupplierName();

	/**
	 * Adds an {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSource}.
	 * @param type
	 *            Type of object required from the {@link OfficeFloorSupplier}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName, String type);

	/**
	 * Adds an {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSource}.
	 * @param type
	 *            Type of object required from the {@link OfficeFloorSupplier}.
	 * @param qualifier
	 *            Qualifier on the object type.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName, String type,
			String qualifier);

}