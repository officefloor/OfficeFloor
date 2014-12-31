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
package net.officefloor.model.office;

import java.util.Map;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeChanges {

	/**
	 * Value for {@link AdministratorScope#PROCESS} on
	 * {@link AdministratorModel} instances.
	 */
	String PROCESS_ADMINISTRATOR_SCOPE = AdministratorScope.PROCESS.name();

	/**
	 * Value for {@link AdministratorScope#THREAD} on {@link AdministratorModel}
	 * instances.
	 */
	String THREAD_ADMINISTRATOR_SCOPE = AdministratorScope.THREAD.name();

	/**
	 * Value for {@link AdministratorScope#WORK} on {@link AdministratorModel}
	 * instances.
	 */
	String WORK_ADMINISTRATOR_SCOPE = AdministratorScope.WORK.name();

	/**
	 * Value for {@link ManagedObjectScope#PROCESS} on
	 * {@link OfficeManagedObjectModel} instances.
	 */
	String PROCESS_MANAGED_OBJECT_SCOPE = ManagedObjectScope.PROCESS.name();

	/**
	 * Value for {@link ManagedObjectScope#THREAD} on
	 * {@link OfficeManagedObjectModel} instances.
	 */
	String THREAD_MANAGED_OBJECT_SCOPE = ManagedObjectScope.THREAD.name();

	/**
	 * Value for {@link ManagedObjectScope#WORK} on
	 * {@link OfficeManagedObjectModel} instances.
	 */
	String WORK_MANAGED_OBJECT_SCOPE = ManagedObjectScope.WORK.name();

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
	 * Refactors the {@link OfficeSectionModel}.
	 * 
	 * @param sectionModel
	 *            {@link OfficeSectionModel} to refactor.
	 * @param sectionName
	 *            Name for the {@link OfficeSectionModel}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name for the
	 *            {@link OfficeSectionModel}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param officeSection
	 *            {@link OfficeSection} that the {@link OfficeSectionModel} is
	 *            being refactored to.
	 * @param inputNameMapping
	 *            Mapping of the {@link OfficeSectionInput} name to the
	 *            {@link OfficeSectionInputModel} name.
	 * @param outputNameMapping
	 *            Mapping of the {@link OfficeSectionOutput} name to the
	 *            {@link OfficeSectionOutputModel} name.
	 * @param objectNameMapping
	 *            Mapping of the {@link OfficeSectionObject} name to the
	 *            {@link OfficeSectionObjectModel} name.
	 * @return {@link Change} to refactor the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> refactorOfficeSection(
			OfficeSectionModel sectionModel, String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, OfficeSection officeSection,
			Map<String, String> inputNameMapping,
			Map<String, String> outputNameMapping,
			Map<String, String> objectNameMapping);

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
	 * Adds an {@link OfficeStartModel} to the {@link OfficeModel}.
	 * 
	 * @return {@link Change} to add the {@link OfficeStartModel}.
	 */
	Change<OfficeStartModel> addOfficeStart();

	/**
	 * Removes the {@link OfficeStartModel}.
	 * 
	 * @param officeStart
	 *            {@link OfficeStartModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeStartModel}.
	 */
	Change<OfficeStartModel> removeOfficeStart(OfficeStartModel officeStart);

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
	 * Adds an {@link OfficeManagedObjectSourceModel} to {@link OfficeModel}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSourceModel}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param timeout
	 *            Timeout for the {@link ManagedObject}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link OfficeManagedObjectSourceModel}.
	 */
	Change<OfficeManagedObjectSourceModel> addOfficeManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			long timeout, ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link OfficeManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeManagedObjectSourceModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectSourceModel}.
	 */
	Change<OfficeManagedObjectSourceModel> removeOfficeManagedObjectSource(
			OfficeManagedObjectSourceModel managedObjectSource);

	/**
	 * Renames the {@link OfficeManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeManagedObjectSourceModel} to rename.
	 * @param newManagedObjectSourceName
	 *            New name for the {@link OfficeManagedObjectSourceModel}.
	 * @return {@link Change} to rename the
	 *         {@link OfficeManagedObjectSourceModel}.
	 */
	Change<OfficeManagedObjectSourceModel> renameOfficeManagedObjectSource(
			OfficeManagedObjectSourceModel managedObjectSource,
			String newManagedObjectSourceName);

	/**
	 * Adds an {@link OfficeManagedObjectModel} for an
	 * {@link OfficeManagedObjectSourceModel} to the {@link OfficeModel}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObjectModel}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the
	 *            {@link OfficeManagedObjectModel}.
	 * @param managedObjectSource
	 *            {@link OfficeManagedObjectSourceModel}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link OfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectModel> addOfficeManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			OfficeManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link OfficeManagedObjectModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectModel> removeOfficeManagedObject(
			OfficeManagedObjectModel managedObject);

	/**
	 * Renames the {@link OfficeManagedObjectModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link OfficeManagedObjectModel}.
	 * @return {@link Change} to rename the {@link OfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectModel> renameOfficeManagedObject(
			OfficeManagedObjectModel managedObject, String newManagedObjectName);

	/**
	 * Scopes the {@link OfficeManagedObjectModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel} to scope.
	 * @param newManagedObjectScope
	 *            New {@link ManagedObjectScope} for the
	 *            {@link OfficeManagedObjectModel}.
	 * @return {@link Change} to scope {@link OfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectModel> rescopeOfficeManagedObject(
			OfficeManagedObjectModel managedObject,
			ManagedObjectScope newManagedObjectScope);

	/**
	 * Adds an {@link AdministratorModel} to the {@link OfficeModel}.
	 * 
	 * @param administratorName
	 *            Name of the {@link AdministratorModel}.
	 * @param administratorSourceClassName
	 *            Class name of the {@link AdministratorSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param administratorScope
	 *            {@link AdministratorScope} for the {@link AdministratorModel}.
	 * @param administratorType
	 *            {@link AdministratorType}.
	 * @return {@link Change} to add the {@link AdministratorModel}.
	 */
	Change<AdministratorModel> addAdministrator(String administratorName,
			String administratorSourceClassName, PropertyList properties,
			AdministratorScope administratorScope,
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

	/**
	 * Adds an {@link OfficeEscalationModel} to the {@link OfficeModel}.
	 * 
	 * @param escalationType
	 *            Type of {@link Escalation}.
	 * @return {@link Change} to add the {@link OfficeEscalationModel}.
	 */
	Change<OfficeEscalationModel> addOfficeEscalation(String escalationType);

	/**
	 * Removes the {@link OfficeEscalationModel}.
	 * 
	 * @param officeEscalation
	 *            {@link OfficeEscalationModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeEscalationModel}.
	 */
	Change<OfficeEscalationModel> removeOfficeEscalation(
			OfficeEscalationModel officeEscalation);

	/**
	 * Adds a {@link OfficeSectionResponsibilityModel} to the
	 * {@link OfficeSectionModel}.
	 * 
	 * @param section
	 *            {@link OfficeSectionModel} to receive the added
	 *            {@link OfficeSectionResponsibilityModel}.
	 * @param officeSectionResponsibilityName
	 *            Name of the {@link OfficeSectionResponsibilityModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeSectionResponsibilityModel}.
	 */
	Change<OfficeSectionResponsibilityModel> addOfficeSectionResponsibility(
			OfficeSectionModel section, String officeSectionResponsibilityName);

	/**
	 * Removes the {@link OfficeSectionResponsibilityModel}.
	 * 
	 * @param officeSectionResponsibility
	 *            {@link OfficeSectionResponsibilityModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeSectionResponsibilityModel}.
	 */
	Change<OfficeSectionResponsibilityModel> removeOfficeSectionResponsibility(
			OfficeSectionResponsibilityModel officeSectionResponsibility);

	/**
	 * Rename the {@link OfficeSectionResponsibilityModel}.
	 * 
	 * @param officeSectionResponsibility
	 *            {@link OfficeSectionResponsibilityModel} to rename.
	 * @param newOfficeSectionResponsibilityName
	 *            New name for the {@link OfficeSectionResponsibilityModel}.
	 * @return {@link Change} to rename the
	 *         {@link OfficeSectionResponsibilityModel}.
	 */
	Change<OfficeSectionResponsibilityModel> renameOfficeSectionResponsibility(
			OfficeSectionResponsibilityModel officeSectionResponsibility,
			String newOfficeSectionResponsibilityName);

	/**
	 * Links the {@link OfficeSectionObjectModel} to the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param officeSectionObject
	 *            {@link OfficeSectionObjectModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeSectionObjectToExternalManagedObjectModel}.
	 */
	Change<OfficeSectionObjectToExternalManagedObjectModel> linkOfficeSectionObjectToExternalManagedObject(
			OfficeSectionObjectModel officeSectionObject,
			ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link OfficeSectionObjectToExternalManagedObjectModel}.
	 * 
	 * @param officeSectionObjectToExternalManagedObject
	 *            {@link OfficeSectionObjectToExternalManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeSectionObjectToExternalManagedObjectModel}.
	 */
	Change<OfficeSectionObjectToExternalManagedObjectModel> removeOfficeSectionObjectToExternalManagedObject(
			OfficeSectionObjectToExternalManagedObjectModel officeSectionObjectToExternalManagedObject);

	/**
	 * Links the {@link OfficeSectionObjectModel} to the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param officeSectionObject
	 *            {@link OfficeSectionObjectModel}.
	 * @param officeManagedObject
	 *            {@link OfficeManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeSectionObjectToOfficeManagedObjectModel}.
	 */
	Change<OfficeSectionObjectToOfficeManagedObjectModel> linkOfficeSectionObjectToOfficeManagedObject(
			OfficeSectionObjectModel officeSectionObject,
			OfficeManagedObjectModel officeManagedObject);

	/**
	 * Removes the {@link OfficeSectionObjectToOfficeManagedObjectModel}.
	 * 
	 * @param officeSectionObjectToOfficeManagedObject
	 *            {@link OfficeSectionObjectToOfficeManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeSectionObjectToOfficeManagedObjectModel}.
	 */
	Change<OfficeSectionObjectToOfficeManagedObjectModel> removeOfficeSectionObjectToOfficeManagedObject(
			OfficeSectionObjectToOfficeManagedObjectModel officeSectionObjectToOfficeManagedObject);

	/**
	 * Links the {@link OfficeManagedObjectDependencyModel} to the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param dependency
	 *            {@link OfficeManagedObjectDependencyModel}.
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectDependencyToOfficeManagedObjectModel> linkOfficeManagedObjectDependencyToOfficeManagedObject(
			OfficeManagedObjectDependencyModel dependency,
			OfficeManagedObjectModel managedObject);

	/**
	 * Removes the
	 * {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel}.
	 * 
	 * @param officeManagedObjectDependencyToOfficeManagedObject
	 *            {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectDependencyToOfficeManagedObjectModel> removeOfficeManagedObjectDependencyToOfficeManagedObject(
			OfficeManagedObjectDependencyToOfficeManagedObjectModel officeManagedObjectDependencyToOfficeManagedObject);

	/**
	 * Links the {@link OfficeManagedObjectDependencyModel} to the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param dependency
	 *            {@link OfficeManagedObjectDependencyModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectDependencyToExternalManagedObjectModel}
	 *         .
	 */
	Change<OfficeManagedObjectDependencyToExternalManagedObjectModel> linkOfficeManagedObjectDependencyToExternalManagedObject(
			OfficeManagedObjectDependencyModel dependency,
			ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the
	 * {@link OfficeManagedObjectDependencyToExternalManagedObjectModel}.
	 * 
	 * @param officeManagedObjectDependencyToExternalManagedObject
	 *            {@link OfficeManagedObjectDependencyToExternalManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectDependencyToExternalManagedObjectModel}
	 *         .
	 */
	Change<OfficeManagedObjectDependencyToExternalManagedObjectModel> removeOfficeManagedObjectDependencyToExternalManagedObject(
			OfficeManagedObjectDependencyToExternalManagedObjectModel officeManagedObjectDependencyToExternalManagedObject);

	/**
	 * Links the {@link OfficeManagedObjectSourceFlowModel} to the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param managedObjectSourceFlow
	 *            {@link OfficeManagedObjectSourceFlowModel}.
	 * @param officeSectionInput
	 *            {@link OfficeSectionInputModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel}.
	 */
	Change<OfficeManagedObjectSourceFlowToOfficeSectionInputModel> linkOfficeManagedObjectSourceFlowToOfficeSectionInput(
			OfficeManagedObjectSourceFlowModel managedObjectSourceFlow,
			OfficeSectionInputModel officeSectionInput);

	/**
	 * Removes the the
	 * {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel}.
	 * 
	 * @param managedObjectSourceFlowToOfficeSectionInput
	 *            {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel}.
	 */
	Change<OfficeManagedObjectSourceFlowToOfficeSectionInputModel> removeOfficeManagedObjectSourceFlowToOfficeSectionInput(
			OfficeManagedObjectSourceFlowToOfficeSectionInputModel managedObjectSourceFlowToOfficeSectionInput);

	/**
	 * Links the {@link OfficeSectionOutputModel} to the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param officeSectionOutput
	 *            {@link OfficeSectionOutputModel}.
	 * @param officeSectionInput
	 *            {@link OfficeSectionInputModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeSectionOutputToOfficeSectionInputModel}.
	 */
	Change<OfficeSectionOutputToOfficeSectionInputModel> linkOfficeSectionOutputToOfficeSectionInput(
			OfficeSectionOutputModel officeSectionOutput,
			OfficeSectionInputModel officeSectionInput);

	/**
	 * Removes the {@link OfficeSectionOutputToOfficeSectionInputModel}.
	 * 
	 * @param officeSectionOutputToOfficeSectionInput
	 *            {@link OfficeSectionOutputToOfficeSectionInputModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeSectionOutputToOfficeSectionInputModel}.
	 */
	Change<OfficeSectionOutputToOfficeSectionInputModel> removeOfficeSectionOutputToOfficeSectionInput(
			OfficeSectionOutputToOfficeSectionInputModel officeSectionOutputToOfficeSectionInput);

	/**
	 * Links the {@link OfficeSectionResponsibilityModel} to the
	 * {@link OfficeTeamModel}.
	 * 
	 * @param officeSectionResponsibility
	 *            {@link OfficeSectionResponsibilityModel}.
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeSectionResponsibilityToOfficeTeamModel}.
	 */
	Change<OfficeSectionResponsibilityToOfficeTeamModel> linkOfficeSectionResponsibilityToOfficeTeam(
			OfficeSectionResponsibilityModel officeSectionResponsibility,
			OfficeTeamModel officeTeam);

	/**
	 * Removes the {@link OfficeSectionResponsibilityToOfficeTeamModel}.
	 * 
	 * @param officeSectionResponsibilityToOfficeTeam
	 *            {@link OfficeSectionResponsibilityToOfficeTeamModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeSectionResponsibilityToOfficeTeamModel}.
	 */
	Change<OfficeSectionResponsibilityToOfficeTeamModel> removeOfficeSectionResponsibilityToOfficeTeam(
			OfficeSectionResponsibilityToOfficeTeamModel officeSectionResponsibilityToOfficeTeam);

	/**
	 * Links the {@link OfficeManagedObjectSourceTeamModel} to the
	 * {@link OfficeTeamModel}.
	 * 
	 * @param mosTeam
	 *            {@link OfficeManagedObjectSourceTeamModel}.
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectSourceTeamModel}.
	 */
	Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> linkOfficeManagedObjectSourceTeamToOfficeTeam(
			OfficeManagedObjectSourceTeamModel mosTeam,
			OfficeTeamModel officeTeam);

	/**
	 * Removes the {@link OfficeManagedObjectSourceTeamToOfficeTeamModel}.
	 * 
	 * @param officeManagedObjectSourceTeamToOfficeTeam
	 *            {@link OfficeManagedObjectSourceTeamToOfficeTeamModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectSourceTeamToOfficeTeamModel}.
	 */
	Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> removeOfficeManagedObjectSourceTeamToOfficeTeam(
			OfficeManagedObjectSourceTeamToOfficeTeamModel officeManagedObjectSourceTeamToOfficeTeam);

	/**
	 * Links the {@link AdministratorModel} to the {@link OfficeTeamModel}.
	 * 
	 * @param administrator
	 *            {@link AdministratorModel}.
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @return {@link Change} to add the {@link AdministratorToOfficeTeamModel}.
	 */
	Change<AdministratorToOfficeTeamModel> linkAdministratorToOfficeTeam(
			AdministratorModel administrator, OfficeTeamModel officeTeam);

	/**
	 * Removes the {@link AdministratorToOfficeTeamModel}.
	 * 
	 * @param administratorToOfficeTeam
	 *            {@link AdministratorToOfficeTeamModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link AdministratorToOfficeTeamModel}.
	 */
	Change<AdministratorToOfficeTeamModel> removeAdministratorToOfficeTeam(
			AdministratorToOfficeTeamModel administratorToOfficeTeam);

	/**
	 * Links the {@link ExternalManagedObjectModel} to the
	 * {@link AdministratorModel}.
	 * 
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @param administrator
	 *            {@link AdministratorModel}.
	 * @return {@link Change} to add the
	 *         {@link ExternalManagedObjectToAdministratorModel}.
	 */
	Change<ExternalManagedObjectToAdministratorModel> linkExternalManagedObjectToAdministrator(
			ExternalManagedObjectModel externalManagedObject,
			AdministratorModel administrator);

	/**
	 * Removes the {@link ExternalManagedObjectToAdministratorModel}.
	 * 
	 * @param externalManagedObjectToAdministrator
	 *            {@link ExternalManagedObjectToAdministratorModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link ExternalManagedObjectToAdministratorModel}.
	 */
	Change<ExternalManagedObjectToAdministratorModel> removeExternalManagedObjectToAdministrator(
			ExternalManagedObjectToAdministratorModel externalManagedObjectToAdministrator);

	/**
	 * Links the {@link OfficeManagedObjectModel} to the
	 * {@link AdministratorModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel}.
	 * @param administrator
	 *            {@link AdministratorModel}.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectToAdministratorModel}.
	 */
	Change<OfficeManagedObjectToAdministratorModel> linkOfficeManagedObjectToAdministrator(
			OfficeManagedObjectModel managedObject,
			AdministratorModel administrator);

	/**
	 * Removes the {@link OfficeManagedObjectToAdministratorModel}.
	 * 
	 * @param managedObjectToAdministrator
	 *            {@link OfficeManagedObjectToAdministratorModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectToAdministratorModel}.
	 */
	Change<OfficeManagedObjectToAdministratorModel> removeOfficeManagedObjectToAdministrator(
			OfficeManagedObjectToAdministratorModel managedObjectToAdministrator);

	/**
	 * Links the {@link OfficeTaskModel} to the {@link Duty} for
	 * pre-administration.
	 * 
	 * @param officeTask
	 *            {@link OfficeTask} of the {@link OfficeSection} to ensure an
	 *            {@link OfficeTaskModel} exists for it.
	 * @param duty
	 *            {@link DutyModel}.
	 * @param officeSectionModel
	 *            {@link OfficeSectionModel} to ensure {@link OfficeTaskModel}
	 *            exists on.
	 * @param officeSection
	 *            {@link OfficeSection} for the {@link OfficeSectionModel}.
	 * @return {@link Change} to add the {@link OfficeTaskToPreDutyModel}.
	 */
	Change<OfficeTaskToPreDutyModel> linkOfficeTaskToPreDuty(
			OfficeTask officeTask, DutyModel duty,
			OfficeSectionModel officeSectionModel, OfficeSection officeSection);

	/**
	 * Removes the {@link OfficeTaskToPreDutyModel}.
	 * 
	 * @param officeTaskToPreDuty
	 *            {@link OfficeTaskToPreDutyModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeTaskToPreDutyModel}.
	 */
	Change<OfficeTaskToPreDutyModel> removeOfficeTaskToPreDuty(
			OfficeTaskToPreDutyModel officeTaskToPreDuty);

	/**
	 * Links the {@link OfficeTaskModel} to the {@link Duty} for
	 * post-administration.
	 * 
	 * @param officeTask
	 *            {@link OfficeTask} of the {@link OfficeSection} to ensure an
	 *            {@link OfficeTaskModel} exists for it.
	 * @param duty
	 *            {@link DutyModel}.
	 * @param officeSectionModel
	 *            {@link OfficeSectionModel} to ensure {@link OfficeTaskModel}
	 *            exists on.
	 * @param officeSection
	 *            {@link OfficeSection} for the {@link OfficeSectionModel}.
	 * @return {@link Change} to add the {@link OfficeTaskToPostDutyModel}.
	 */
	Change<OfficeTaskToPostDutyModel> linkOfficeTaskToPostDuty(
			OfficeTask officeTask, DutyModel duty,
			OfficeSectionModel officeSectionModel, OfficeSection officeSection);

	/**
	 * Removes the {@link OfficeTaskToPostDutyModel}.
	 * 
	 * @param officeTaskToPostDuty
	 *            {@link OfficeTaskToPostDutyModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeTaskToPostDutyModel}.
	 */
	Change<OfficeTaskToPostDutyModel> removeOfficeTaskToPostDuty(
			OfficeTaskToPostDutyModel officeTaskToPostDuty);

	/**
	 * Links the {@link OfficeEscalationModel} to the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param escalation
	 *            {@link OfficeEscalationModel}.
	 * @param sectionInput
	 *            {@link OfficeSectionInputModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeEscalationToOfficeSectionInputModel}.
	 */
	Change<OfficeEscalationToOfficeSectionInputModel> linkOfficeEscalationToOfficeSectionInput(
			OfficeEscalationModel escalation,
			OfficeSectionInputModel sectionInput);

	/**
	 * Removes the {@link OfficeEscalationToOfficeSectionInputModel}.
	 * 
	 * @param escalationToSectionInput
	 *            {@link OfficeEscalationToOfficeSectionInputModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeEscalationToOfficeSectionInputModel}.
	 */
	Change<OfficeEscalationToOfficeSectionInputModel> removeOfficeEscalationToOfficeSectionInput(
			OfficeEscalationToOfficeSectionInputModel escalationToSectionInput);

	/**
	 * Links the {@link OfficeStartModel} to {@link OfficeSectionInputModel}.
	 * 
	 * @param start
	 *            {@link OfficeStartModel}.
	 * @param sectionInput
	 *            {@link OfficeSectionInputModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeStartToOfficeSectionInputModel}.
	 */
	Change<OfficeStartToOfficeSectionInputModel> linkOfficeStartToOfficeSectionInput(
			OfficeStartModel start, OfficeSectionInputModel sectionInput);

	/**
	 * Removes the {@link OfficeStartToOfficeSectionInputModel}.
	 * 
	 * @param startToSectionInput
	 *            {@link OfficeStartToOfficeSectionInputModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeStartToOfficeSectionInputModel}.
	 */
	Change<OfficeStartToOfficeSectionInputModel> removeOfficeStartToOfficeSectionInput(
			OfficeStartToOfficeSectionInputModel startToSectionInput);

}