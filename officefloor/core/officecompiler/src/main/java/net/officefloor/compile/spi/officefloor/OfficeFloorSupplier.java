/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
