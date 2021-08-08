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

package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * <p>
	 * Obtains the location of the {@link OfficeFloor}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the {@link OfficeFloorSource}.
	 * 
	 * @return Location of the {@link OfficeFloor}.
	 */
	String getOfficeFloorLocation();

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * <p>
	 * Loads the {@link ManagedObjectType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedObject} to allow reflective configuration by the
	 * {@link OfficeFloorSource}.
	 * 
	 * @param managedObjectSourceName      Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClassName Name of the implementing
	 *                                     {@link ManagedObjectSource} class. May
	 *                                     also be an alias.
	 * @param properties                   {@link PropertyList} to configure the
	 *                                     {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} or <code>null</code> if fails to load the
	 *         {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName, String managedObjectSourceClassName,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link ManagedObjectType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedObject} to allow reflective configuration by the
	 * {@link OfficeFloorSource}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource} instance.
	 * @param properties              {@link PropertyList} to configure the
	 *                                {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} or <code>null</code> if fails to load the
	 *         {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link InitialSupplierType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link SupplierSource} to allow reflective configuration by the
	 * {@link OfficeFloorSource}.
	 * 
	 * @param supplierName            Name of the {@link SupplierSource}.
	 * @param supplierSourceClassName Name of the implementing
	 *                                {@link SupplierSource} class. May also be an
	 *                                alias.
	 * @param properties              {@link PropertyList} to configure the
	 *                                {@link SupplierSource}.
	 * @return {@link InitialSupplierType} or <code>null</code> if fails to load the
	 *         {@link InitialSupplierType}.
	 */
	InitialSupplierType loadSupplierType(String supplierName, String supplierSourceClassName, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link InitialSupplierType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link SupplierSource} to allow reflective configuration by the
	 * {@link OfficeFloorSource}.
	 * 
	 * @param supplierName   Name of the {@link SupplierSource}.
	 * @param supplierSource {@link SupplierSource}.
	 * @param properties     {@link PropertyList} to configure the
	 *                       {@link SupplierSource}.
	 * @return {@link InitialSupplierType} or <code>null</code> if fails to load the
	 *         {@link InitialSupplierType}.
	 */
	InitialSupplierType loadSupplierType(String supplierName, SupplierSource supplierSource, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link OfficeType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Office} to
	 * allow reflective configuration by the {@link OfficeFloorSource}.
	 * 
	 * @param officeName            Name of the {@link Office}.
	 * @param officeSourceClassName Name of the implementing {@link OfficeSource}
	 *                              class. May also be an alias.
	 * @param location              Location of the {@link Office}.
	 * @param properties            {@link PropertyList} to configure the
	 *                              {@link OfficeSource}.
	 * @return {@link OfficeType} or <code>null</code> if fails to load the
	 *         {@link OfficeType}.
	 */
	OfficeType loadOfficeType(String officeName, String officeSourceClassName, String location,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link OfficeType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Office} to
	 * allow reflective configuration by the {@link OfficeFloorSource}.
	 * 
	 * @param officeName   Name of the {@link Office}.
	 * @param officeSource {@link OfficeSource} instance.
	 * @param location     Location of the {@link Office}.
	 * @param properties   {@link PropertyList} to configure the
	 *                     {@link OfficeSource}.
	 * @return {@link OfficeType} or <code>null</code> if fails to load the
	 *         {@link OfficeType}.
	 */
	OfficeType loadOfficeType(String officeName, OfficeSource officeSource, String location, PropertyList properties);

}
