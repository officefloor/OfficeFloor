/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;

/**
 * Supplies {@link OfficeManagedObjectSource} instances within the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSupplier extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeSupplier}.
	 * 
	 * @return Name of this {@link OfficeSupplier}.
	 */
	String getOfficeSupplierName();

	/**
	 * Adds an {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param type
	 *            Type of object required from the {@link OfficeSupplier}.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName, String type);

	/**
	 * Adds an {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param type
	 *            Type of object required from the {@link OfficeSupplier}.
	 * @param qualifier
	 *            Qualifier on the object type.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName, String type,
			String qualifier);

}