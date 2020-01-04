/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

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
	 * Obtains the {@link OfficeFloorSupplierThreadLocal}.
	 * 
	 * @param qualifier Qualifier of the required {@link ManagedObject}. May be
	 *                  <code>null</code> to match only on type.
	 * @param type      Type of object required for the
	 *                  {@link OfficeFloorSupplierThreadLocal}.
	 * @return {@link OfficeFloorSupplierThreadLocal}.
	 */
	OfficeFloorSupplierThreadLocal getOfficeFloorSupplierThreadLocal(String qualifier, String type);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName Name of the
	 *                                {@link OfficeFloorManagedObjectSource}.
	 * @param qualifier               Qualifier on the object type. May be
	 *                                <code>null</code> to match only on type.
	 * @param type                    Type of object required from the
	 *                                {@link OfficeFloorSupplier}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource getOfficeFloorManagedObjectSource(String managedObjectSourceName, String qualifier,
			String type);

}
