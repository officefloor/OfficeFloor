/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.office;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link OfficeModel}.
 * 
 * @author Daniel
 */
public interface OfficeChanges {

	/**
	 * Adds an {@link OfficeSectionModel} to the {@link OfficeModel}.
	 * 
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param officeSection
	 *            {@link OfficeSection}.
	 * @return {@link Change} to add the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> addOfficeSection(String sectionSourceClassName,
			String sectionLocation, PropertyList properties,
			OfficeSection officeSection);

	/**
	 * Removes the {@link OfficeSectionModel}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> removeOfficeSection(
			OfficeSectionModel officeSection);

	/**
	 * Renames the {@link OfficeSectionModel}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} to rename.
	 * @param newOfficeSectionName
	 *            New {@link OfficeSectionModel} name.
	 * @return {@link Change} to rename the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> renameOfficeSection(
			OfficeSectionModel officeSection, String newOfficeSectionName);

	/**
	 * Adds an {@link OfficeTeamModel} to the {@link OfficeModel}.
	 * 
	 * @param teamName
	 *            Name of the {@link OfficeTeamModel}.
	 * @return {@link Change} to add the {@link OfficeTeamModel}.
	 */
	Change<OfficeTeamModel> addOfficeTeam(String teamName);

	/**
	 * Removes the {@link OfficeTeamModel}.
	 * 
	 * @param officeTeam
	 *            {@link OfficeTeamModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeTeamModel}.
	 */
	Change<OfficeTeamModel> removeOfficeTeam(OfficeTeamModel officeTeam);

	/**
	 * Renames the {@link OfficeTeamModel}.
	 * 
	 * @param officeTeam
	 *            {@link OfficeTeamModel} to rename.
	 * @param newOfficeTeamName
	 *            New name for the {@link OfficeTeamModel}.
	 * @return {@link Change} to rename the {@link OfficeTeamModel}.
	 */
	Change<OfficeTeamModel> renameOfficeTeam(OfficeTeamModel officeTeam,
			String newOfficeTeamName);

	/**
	 * Adds an {@link ExternalManagedObjectModel} to the {@link OfficeModel}.
	 * 
	 * @param externalManagedObjectName
	 *            Name of the {@link ExternalManagedObjectModel}.
	 * @param objectType
	 *            Object type.
	 * @return {@link Change} to add the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String objectType);

	/**
	 * Removes the {@link ExternalManagedObjectModel}.
	 * 
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> removeExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject);

	/**
	 * Renames the {@link ExternalManagedObjectModel}.
	 * 
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel} to rename.
	 * @param newExternalManagedObjectName
	 *            New name for the {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to rename the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> renameExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject,
			String newExternalManagedObjectName);

	/**
	 * Adds an {@link AdministratorModel} to the {@link OfficeModel}.
	 * 
	 * @param administratorName
	 *            Name of the {@link AdministratorModel}.
	 * @param administratorSourceClassName
	 *            Class name of the {@link AdministratorSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param administratorType
	 *            {@link AdministratorType}.
	 * @return {@link Change} to add the {@link AdministratorModel}.
	 */
	Change<AdministratorModel> addAdministrator(String administratorName,
			String administratorSourceClassName, PropertyList properties,
			AdministratorType<?, ?> administratorType);

	/**
	 * Removes the {@link AdministratorModel}.
	 * 
	 * @param administrator
	 *            {@link AdministratorModel} to remove.
	 * @return {@link Change} to remove the {@link AdministratorModel}.
	 */
	Change<AdministratorModel> removeAdministrator(
			AdministratorModel administrator);

	/**
	 * Renames the {@link AdministratorModel}.
	 * 
	 * @param administrator
	 *            {@link AdministratorModel}.
	 * @param newAdministratorName
	 *            New name for the {@link AdministratorModel}.
	 * @return {@link Change} to rename the {@link AdministratorModel}.
	 */
	Change<AdministratorModel> renameAdministrator(
			AdministratorModel administrator, String newAdministratorName);

}