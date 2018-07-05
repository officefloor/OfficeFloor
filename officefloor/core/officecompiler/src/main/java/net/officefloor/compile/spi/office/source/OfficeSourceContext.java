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
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            Name of the implementing {@link SectionSource} class.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList} to configure the {@link OfficeSection}.
	 * @return {@link OfficeSectionType} or <code>null</code> if fails to load
	 *         the {@link OfficeSectionType}.
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
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSource
	 *            {@link SectionSource} instance.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList} to configure the {@link OfficeSection}.
	 * @return {@link OfficeSectionType} or <code>null</code> if fails to load
	 *         the {@link OfficeSectionType}.
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
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Name of the implementing {@link ManagedObjectSource} class.
	 *            May also be an alias.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} or <code>null</code> if fails to load
	 *         the {@link ManagedObjectType}.
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
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} or <code>null</code> if fails to load
	 *         the {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link AdministrationType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link Administration} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param administrationSourceClassName
	 *            Name of the implementing {@link AdministrationSource} class.
	 *            May also be an alias.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link AdministrationSource}.
	 * @return {@link AdministrationType} or <code>null</code> if fails to load
	 *         the {@link AdministrationType}.
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
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param administrationSource
	 *            {@link AdministrationSource} instance.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link AdministrationSource}.
	 * @return {@link AdministrationType} or <code>null</code> if fails to load
	 *         the {@link AdministrationType}.
	 */
	AdministrationType<?, ?, ?> loadAdministrationType(String administrationName,
			AdministrationSource<?, ?, ?> administrationSource, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link GovernanceType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link Governance} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceClassName
	 *            Name of the implementing {@link GovernanceSource} class. May
	 *            also be an alias.
	 * @param properties
	 *            {@link PropertyList} for configuring the
	 *            {@link GovernanceSource}.
	 * @return {@link GovernanceType} or <code>null</code> if fails to load the
	 *         {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType(String governanceName, String governanceSourceClassName,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link GovernanceType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link Governance} to allow reflective configuration by the
	 * {@link OfficeSource}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSource
	 *            {@link GovernanceSource} instance.
	 * @param properties
	 *            {@link PropertyList} for configuring the
	 *            {@link GovernanceSource}.
	 * @return {@link GovernanceType} or <code>null</code> if fails to load the
	 *         {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType(String governanceName, GovernanceSource<?, ?> governanceSource,
			PropertyList properties);

}