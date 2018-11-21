/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;

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
	 * Obtains the {@link OfficeSupplierThreadLocal}.
	 * 
	 * @param qualifier Qualifier of the required {@link ManagedObject}. May be
	 *                  <code>null</code> to match only on type.
	 * @param type      Type of object required for the
	 *                  {@link OfficeSupplierThreadLocal}.
	 * @return {@link OfficeSupplierThreadLocal}.
	 */
	OfficeSupplierThreadLocal getOfficeSupplierThreadLocal(String qualifier, String type);

	/**
	 * Obtains the {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName Name of the {@link OfficeManagedObjectSource}.
	 * @param qualifier               Qualifier on the object type. May be
	 *                                <code>null</code> to match only on type.
	 * @param type                    Type of object required from the
	 *                                {@link OfficeSupplier}.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource getOfficeManagedObjectSource(String managedObjectSourceName, String qualifier,
			String type);

}