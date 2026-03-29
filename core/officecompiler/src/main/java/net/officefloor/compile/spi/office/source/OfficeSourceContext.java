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

package net.officefloor.compile.spi.office.source;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * <p>
	 * Obtains the location of the {@link Office}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the {@link OfficeSource}.
	 * 
	 * @return Location of the {@link Office}.
	 */
	String getOfficeLocation();

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * <p>
	 * Loads the {@link OfficeSectionType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link OfficeSection} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param sectionName            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName Name of the implementing {@link SectionSource}
	 *                               class.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param properties             {@link PropertyList} to configure the
	 *                               {@link OfficeSection}.
	 * @return {@link OfficeSectionType} or <code>null</code> if fails to load the
	 *         {@link OfficeSectionType}.
	 */
	OfficeSectionType loadOfficeSectionType(String sectionName, String sectionSourceClassName, String sectionLocation,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link OfficeSectionType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link OfficeSection} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param sectionName     Name of the {@link OfficeSection}.
	 * @param sectionSource   {@link SectionSource} instance.
	 * @param sectionLocation Location of the {@link OfficeSection}.
	 * @param properties      {@link PropertyList} to configure the
	 *                        {@link OfficeSection}.
	 * @return {@link OfficeSectionType} or <code>null</code> if fails to load the
	 *         {@link OfficeSectionType}.
	 */
	OfficeSectionType loadOfficeSectionType(String sectionName, SectionSource sectionSource, String sectionLocation,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link ManagedObjectType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedObject} to allow reflective configuration by the
	 * {@link OfficeSource}.
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
	 * {@link OfficeSource}.
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
	 * {@link OfficeSource}.
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
	 * Loads the {@link AdministrationType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link Administration} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param administrationName            Name of {@link Administration}.
	 * @param administrationSourceClassName Name of the implementing
	 *                                      {@link AdministrationSource} class. May
	 *                                      also be an alias.
	 * @param properties                    {@link PropertyList} to configure the
	 *                                      {@link AdministrationSource}.
	 * @return {@link AdministrationType} or <code>null</code> if fails to load the
	 *         {@link AdministrationType}.
	 */
	AdministrationType<?, ?, ?> loadAdministrationType(String administrationName, String administrationSourceClassName,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link AdministrationType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link Administration} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param administrationName   Name of {@link Administration}.
	 * @param administrationSource {@link AdministrationSource} instance.
	 * @param properties           {@link PropertyList} to configure the
	 *                             {@link AdministrationSource}.
	 * @return {@link AdministrationType} or <code>null</code> if fails to load the
	 *         {@link AdministrationType}.
	 */
	AdministrationType<?, ?, ?> loadAdministrationType(String administrationName,
			AdministrationSource<?, ?, ?> administrationSource, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link GovernanceType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Governance}
	 * to allow reflective configuration by the {@link OfficeSource}.
	 * 
	 * @param governanceName            Name of {@link Governance}.
	 * @param governanceSourceClassName Name of the implementing
	 *                                  {@link GovernanceSource} class. May also be
	 *                                  an alias.
	 * @param properties                {@link PropertyList} for configuring the
	 *                                  {@link GovernanceSource}.
	 * @return {@link GovernanceType} or <code>null</code> if fails to load the
	 *         {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType(String governanceName, String governanceSourceClassName,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link GovernanceType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Governance}
	 * to allow reflective configuration by the {@link OfficeSource}.
	 * 
	 * @param governanceName   Name of {@link Governance}.
	 * @param governanceSource {@link GovernanceSource} instance.
	 * @param properties       {@link PropertyList} for configuring the
	 *                         {@link GovernanceSource}.
	 * @return {@link GovernanceType} or <code>null</code> if fails to load the
	 *         {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType(String governanceName, GovernanceSource<?, ?> governanceSource,
			PropertyList properties);

}
