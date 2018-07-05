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
package net.officefloor.model.office;

import java.util.Map;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeChanges {

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
	 * Value for {@link ManagedObjectScope#FUNCTION} on
	 * {@link OfficeManagedObjectModel} instances.
	 */
	String FUNCTION_MANAGED_OBJECT_SCOPE = ManagedObjectScope.FUNCTION.name();

	/**
	 * Adds an {@link OfficeSectionModel} to the {@link OfficeModel}.
	 * 
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param officeSectionType
	 *            {@link OfficeSectionType}.
	 * @return {@link Change} to add the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> addOfficeSection(String sectionSourceClassName, String sectionLocation,
			PropertyList properties, OfficeSectionType officeSectionType);

	/**
	 * Removes the {@link OfficeSectionModel}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> removeOfficeSection(OfficeSectionModel officeSection);

	/**
	 * Renames the {@link OfficeSectionModel}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel} to rename.
	 * @param newOfficeSectionName
	 *            New {@link OfficeSectionModel} name.
	 * @return {@link Change} to rename the {@link OfficeSectionModel}.
	 */
	Change<OfficeSectionModel> renameOfficeSection(OfficeSectionModel officeSection, String newOfficeSectionName);

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
	 * @param officeSectionType
	 *            {@link OfficeSectionType} that the {@link OfficeSectionModel} is
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
	Change<OfficeSectionModel> refactorOfficeSection(OfficeSectionModel sectionModel, String sectionName,
			String sectionSourceClassName, String sectionLocation, PropertyList properties,
			OfficeSectionType officeSectionType, Map<String, String> inputNameMapping,
			Map<String, String> outputNameMapping, Map<String, String> objectNameMapping);

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
	 * Adds {@link TypeQualificationModel} to the {@link OfficeTeamModel}.
	 * 
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @param qualifier
	 *            Qualfiier. May be <code>null</code>.
	 * @param type
	 *            Type.
	 * @return {@link Change} to add the {@link TypeQualificationModel}.
	 */
	Change<TypeQualificationModel> addOfficeTeamTypeQualification(OfficeTeamModel officeTeam, String qualifier,
			String type);

	/**
	 * Removes the {@link TypeQualificationModel} from the {@link OfficeTeamModel}.
	 * 
	 * @param typeQualification
	 *            {@link TypeQualificationModel}.
	 * @return {@link Change} to remove the {@link TypeQualificationModel}.
	 */
	Change<TypeQualificationModel> removeOfficeTeamTypeQualification(TypeQualificationModel typeQualification);

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
	Change<OfficeTeamModel> renameOfficeTeam(OfficeTeamModel officeTeam, String newOfficeTeamName);

	/**
	 * Adds an {@link ExternalManagedObjectModel} to the {@link OfficeModel}.
	 * 
	 * @param externalManagedObjectName
	 *            Name of the {@link ExternalManagedObjectModel}.
	 * @param objectType
	 *            Object type.
	 * @return {@link Change} to add the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> addExternalManagedObject(String externalManagedObjectName, String objectType);

	/**
	 * Removes the {@link ExternalManagedObjectModel}.
	 * 
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> removeExternalManagedObject(ExternalManagedObjectModel externalManagedObject);

	/**
	 * Renames the {@link ExternalManagedObjectModel}.
	 * 
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel} to rename.
	 * @param newExternalManagedObjectName
	 *            New name for the {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to rename the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> renameExternalManagedObject(ExternalManagedObjectModel externalManagedObject,
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
	Change<OfficeManagedObjectSourceModel> addOfficeManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties, long timeout,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link OfficeManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeManagedObjectSourceModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeManagedObjectSourceModel}.
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
	 * @return {@link Change} to rename the {@link OfficeManagedObjectSourceModel}.
	 */
	Change<OfficeManagedObjectSourceModel> renameOfficeManagedObjectSource(
			OfficeManagedObjectSourceModel managedObjectSource, String newManagedObjectSourceName);

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
	Change<OfficeManagedObjectModel> addOfficeManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope, OfficeManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link OfficeManagedObjectModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectModel> removeOfficeManagedObject(OfficeManagedObjectModel managedObject);

	/**
	 * Renames the {@link OfficeManagedObjectModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link OfficeManagedObjectModel}.
	 * @return {@link Change} to rename the {@link OfficeManagedObjectModel}.
	 */
	Change<OfficeManagedObjectModel> renameOfficeManagedObject(OfficeManagedObjectModel managedObject,
			String newManagedObjectName);

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
	Change<OfficeManagedObjectModel> rescopeOfficeManagedObject(OfficeManagedObjectModel managedObject,
			ManagedObjectScope newManagedObjectScope);

	/**
	 * Adds {@link TypeQualificationModel} to the {@link OfficeManagedObjectModel}.
	 * 
	 * @param officeManagedObject
	 *            {@link OfficeManagedObjectModel}.
	 * @param qualifier
	 *            Qualifier. May be <code>null</code>.
	 * @param type
	 *            Type.
	 * @return {@link Change} to add the {@link TypeQualificationModel}.
	 */
	Change<TypeQualificationModel> addOfficeManagedObjectTypeQualification(OfficeManagedObjectModel officeManagedObject,
			String qualifier, String type);

	/**
	 * Removes the {@link TypeQualificationModel} from the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param typeQualification
	 *            {@link TypeQualificationModel}.
	 * @return {@link Change} to remove the {@link TypeQualificationModel}.
	 */
	Change<TypeQualificationModel> removeOfficeManagedObjectTypeQualification(TypeQualificationModel typeQualification);

	/**
	 * Adds an {@link AdministrationModel} to the {@link OfficeModel}.
	 * 
	 * @param administrationName
	 *            Name of the {@link AdministrationModel}.
	 * @param administrationSourceClassName
	 *            Class name of the {@link AdministrationSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param administrationType
	 *            {@link AdministrationType}.
	 * @return {@link Change} to add the {@link AdministrationModel}.
	 */
	Change<AdministrationModel> addAdministration(String administrationName, String administrationSourceClassName,
			PropertyList properties, AdministrationType<?, ?, ?> administrationType);

	/**
	 * Removes the {@link AdministrationModel}.
	 * 
	 * @param administration
	 *            {@link AdministrationModel} to remove.
	 * @return {@link Change} to remove the {@link AdministrationModel}.
	 */
	Change<AdministrationModel> removeAdministration(AdministrationModel administration);

	/**
	 * Renames the {@link AdministrationModel}.
	 * 
	 * @param administration
	 *            {@link AdministrationModel}.
	 * @param newAdministrationName
	 *            New name for the {@link AdministrationModel}.
	 * @return {@link Change} to rename the {@link AdministrationModel}.
	 */
	Change<AdministrationModel> renameAdministration(AdministrationModel administration, String newAdministrationName);

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
	Change<OfficeEscalationModel> removeOfficeEscalation(OfficeEscalationModel officeEscalation);

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
			OfficeSectionObjectModel officeSectionObject, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link OfficeSectionObjectToExternalManagedObjectModel}.
	 * 
	 * @param officeSectionObjectToExternalManagedObject
	 *            {@link OfficeSectionObjectToExternalManagedObjectModel} to remove.
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
			OfficeSectionObjectModel officeSectionObject, OfficeManagedObjectModel officeManagedObject);

	/**
	 * Removes the {@link OfficeSectionObjectToOfficeManagedObjectModel}.
	 * 
	 * @param officeSectionObjectToOfficeManagedObject
	 *            {@link OfficeSectionObjectToOfficeManagedObjectModel} to remove.
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
			OfficeManagedObjectDependencyModel dependency, OfficeManagedObjectModel managedObject);

	/**
	 * Removes the {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel}.
	 * 
	 * @param officeManagedObjectDependencyToOfficeManagedObject
	 *            {@link OfficeManagedObjectDependencyToOfficeManagedObjectModel} to
	 *            remove.
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
	 *         {@link OfficeManagedObjectDependencyToExternalManagedObjectModel} .
	 */
	Change<OfficeManagedObjectDependencyToExternalManagedObjectModel> linkOfficeManagedObjectDependencyToExternalManagedObject(
			OfficeManagedObjectDependencyModel dependency, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the
	 * {@link OfficeManagedObjectDependencyToExternalManagedObjectModel}.
	 * 
	 * @param officeManagedObjectDependencyToExternalManagedObject
	 *            {@link OfficeManagedObjectDependencyToExternalManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectDependencyToExternalManagedObjectModel} .
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
			OfficeManagedObjectSourceFlowModel managedObjectSourceFlow, OfficeSectionInputModel officeSectionInput);

	/**
	 * Removes the the
	 * {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel}.
	 * 
	 * @param managedObjectSourceFlowToOfficeSectionInput
	 *            {@link OfficeManagedObjectSourceFlowToOfficeSectionInputModel} to
	 *            remove.
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
			OfficeSectionOutputModel officeSectionOutput, OfficeSectionInputModel officeSectionInput);

	/**
	 * Removes the {@link OfficeSectionOutputToOfficeSectionInputModel}.
	 * 
	 * @param officeSectionOutputToOfficeSectionInput
	 *            {@link OfficeSectionOutputToOfficeSectionInputModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeSectionOutputToOfficeSectionInputModel}.
	 */
	Change<OfficeSectionOutputToOfficeSectionInputModel> removeOfficeSectionOutputToOfficeSectionInput(
			OfficeSectionOutputToOfficeSectionInputModel officeSectionOutputToOfficeSectionInput);

	/**
	 * Links the {@link OfficeManagedObjectSourceTeamModel} to the
	 * {@link OfficeTeamModel}.
	 * 
	 * @param mosTeam
	 *            {@link OfficeManagedObjectSourceTeamModel}.
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @return {@link Change} to add the {@link OfficeManagedObjectSourceTeamModel}.
	 */
	Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> linkOfficeManagedObjectSourceTeamToOfficeTeam(
			OfficeManagedObjectSourceTeamModel mosTeam, OfficeTeamModel officeTeam);

	/**
	 * Removes the {@link OfficeManagedObjectSourceTeamToOfficeTeamModel}.
	 * 
	 * @param officeManagedObjectSourceTeamToOfficeTeam
	 *            {@link OfficeManagedObjectSourceTeamToOfficeTeamModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectSourceTeamToOfficeTeamModel}.
	 */
	Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> removeOfficeManagedObjectSourceTeamToOfficeTeam(
			OfficeManagedObjectSourceTeamToOfficeTeamModel officeManagedObjectSourceTeamToOfficeTeam);

	/**
	 * Links the {@link AdministrationModel} to the {@link OfficeTeamModel}.
	 * 
	 * @param administration
	 *            {@link AdministrationModel}.
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @return {@link Change} to add the {@link AdministrationToOfficeTeamModel}.
	 */
	Change<AdministrationToOfficeTeamModel> linkAdministrationToOfficeTeam(AdministrationModel administration,
			OfficeTeamModel officeTeam);

	/**
	 * Removes the {@link AdministrationToOfficeTeamModel}.
	 * 
	 * @param administrationToOfficeTeam
	 *            {@link AdministrationToOfficeTeamModel} to remove.
	 * @return {@link Change} to remove the {@link AdministrationToOfficeTeamModel}.
	 */
	Change<AdministrationToOfficeTeamModel> removeAdministrationToOfficeTeam(
			AdministrationToOfficeTeamModel administrationToOfficeTeam);

	/**
	 * Links the {@link AdministrationModel} to administer the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param administration
	 *            {@link AdministrationModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link AdministrationToExternalManagedObjectModel}.
	 */
	Change<AdministrationToExternalManagedObjectModel> linkAdministrationToExternalManagedObject(
			AdministrationModel administration, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link AdministrationToExternalManagedObjectModel}.
	 * 
	 * @param externalManagedObjectToAdministration
	 *            {@link AdministrationToExternalManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link AdministrationToExternalManagedObjectModel}.
	 */
	Change<AdministrationToExternalManagedObjectModel> removeAdministrationToExternalManagedObject(
			AdministrationToExternalManagedObjectModel externalManagedObjectToAdministration);

	/**
	 * Links the {@link AdministrationModel} to administer the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param managedObject
	 *            {@link OfficeManagedObjectModel}.
	 * @param administration
	 *            {@link AdministrationModel}.
	 * @return {@link Change} to add the
	 *         {@link AdministrationToOfficeManagedObjectModel}.
	 */
	Change<AdministrationToOfficeManagedObjectModel> linkAdministrationToOfficeManagedObject(
			AdministrationModel administration, OfficeManagedObjectModel managedObject);

	/**
	 * Removes the {@link AdministrationToOfficeManagedObjectModel}.
	 * 
	 * @param managedObjectToAdministration
	 *            {@link AdministrationToOfficeManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link AdministrationToOfficeManagedObjectModel}.
	 */
	Change<AdministrationToOfficeManagedObjectModel> removeAdministrationToOfficeManagedObject(
			AdministrationToOfficeManagedObjectModel managedObjectToAdministration);

	/**
	 * Links the {@link OfficeManagedObjectModel} to its pre-load
	 * {@link AdministrationModel}.
	 * 
	 * @param officeManagedObject
	 *            {@link OfficeManagedObjectModel}.
	 * @param administration
	 *            Pre-load {@link AdministrationModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeManagedObjectToPreLoadAdministrationModel}.
	 */
	Change<OfficeManagedObjectToPreLoadAdministrationModel> linkOfficeManagedObjectToPreLoadAdministration(
			OfficeManagedObjectModel officeManagedObject, AdministrationModel administration);

	/**
	 * Removes the {@link OfficeManagedObjectToPreLoadAdministrationModel}.
	 * 
	 * @param managedObjectToPreloadAdmin
	 *            {@link OfficeManagedObjectToPreLoadAdministrationModel}.
	 * @return {@link Change} to remove the
	 *         {@link OfficeManagedObjectToPreLoadAdministrationModel}.
	 */
	Change<OfficeManagedObjectToPreLoadAdministrationModel> removeOfficeManagedObjectToPreLoadAdministration(
			OfficeManagedObjectToPreLoadAdministrationModel managedObjectToPreloadAdmin);

	/**
	 * Links the {@link ExternalManagedObjectModel} to its pre-load
	 * {@link AdministrationModel}.
	 * 
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @param administration
	 *            Pre-load {@link AdministrationModel}.
	 * @return {@link Change} to add the
	 *         {@link ExternalManagedObjectToPreLoadAdministrationModel}.
	 */
	Change<ExternalManagedObjectToPreLoadAdministrationModel> linkExternalManagedObjectToPreLoadAdministration(
			ExternalManagedObjectModel externalManagedObject, AdministrationModel administration);

	/**
	 * Removes the {@link ExternalManagedObjectToPreLoadAdministrationModel}.
	 * 
	 * @param managedObjectToPreloadAdmin
	 *            {@link ExternalManagedObjectToPreLoadAdministrationModel}.
	 * @return {@link Change} to remove the
	 *         {@link ExternalManagedObjectToPreLoadAdministrationModel}.
	 */
	Change<ExternalManagedObjectToPreLoadAdministrationModel> removeExternalManagedObjectToPreLoadAdministration(
			ExternalManagedObjectToPreLoadAdministrationModel managedObjectToPreloadAdmin);

	/**
	 * Links the {@link OfficeFunctionModel} to the {@link OfficeTeamModel}.
	 * 
	 * @param officeFunction
	 *            {@link OfficeFunctionModel}.
	 * @param officeTeam
	 *            {@link OfficeTeamModel}.
	 * @return {@link Change} to add the {@link OfficeFunctionToOfficeTeamModel}.
	 */
	Change<OfficeFunctionToOfficeTeamModel> linkOfficeFunctionToOfficeTeam(OfficeFunctionModel officeFunction,
			OfficeTeamModel officeTeam);

	/**
	 * Removes the {@link OfficeFunctionToOfficeTeamModel}.
	 * 
	 * @param officeFunctionToOfficeTeam
	 *            {@link OfficeFunctionToOfficeTeamModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeFunctionToOfficeTeamModel}.
	 */
	Change<OfficeFunctionToOfficeTeamModel> removeOfficeFunctionToOfficeTeam(
			OfficeFunctionToOfficeTeamModel officeFunctionToOfficeTeam);

	/**
	 * Links the {@link OfficeFunctionModel} to the {@link AdministrationModel} for
	 * pre-administration.
	 * 
	 * @param officeSectionModel
	 *            {@link OfficeSectionModel} containing the
	 *            {@link OfficeFunctionModel}.
	 * @param officeFunctionType
	 *            {@link OfficeSectionFunction} of the {@link OfficeSection} to
	 *            ensure an {@link OfficeFunctionModel} exists for it.
	 * @param administration
	 *            {@link AdministrationModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFunctionToPreAdministrationModel}.
	 */
	Change<OfficeFunctionToPreAdministrationModel> linkOfficeFunctionToPreAdministration(
			OfficeSectionModel officeSectionModel, OfficeFunctionType officeFunctionType,
			AdministrationModel administration);

	/**
	 * Removes the {@link OfficeFunctionToPreAdministrationModel}.
	 * 
	 * @param officeFunctionToPreAdministration
	 *            {@link OfficeFunctionToPreAdministrationModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFunctionToPreAdministrationModel}.
	 */
	Change<OfficeFunctionToPreAdministrationModel> removeOfficeFunctionToPreAdministration(
			OfficeFunctionToPreAdministrationModel officeFunctionToPreAdministration);

	/**
	 * Links the {@link OfficeFunctionModel} to the {@link AdministrationModel} for
	 * post-administration.
	 * 
	 * @param officeSectionModel
	 *            {@link OfficeSectionModel} containing the
	 *            {@link OfficeFunctionModel}.
	 * @param officeFunctionType
	 *            {@link OfficeSectionFunction} of the {@link OfficeSection} to
	 *            ensure an {@link OfficeFunctionModel} exists for it.
	 * @param administration
	 *            {@link AdministrationModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFunctionToPostAdministrationModel}.
	 */
	Change<OfficeFunctionToPostAdministrationModel> linkOfficeFunctionToPostAdministration(
			OfficeSectionModel officeSectionModel, OfficeFunctionType officeFunctionType,
			AdministrationModel administration);

	/**
	 * Removes the {@link OfficeFunctionToPostAdministrationModel}.
	 * 
	 * @param officeFunctionToAdministration
	 *            {@link OfficeFunctionToPostAdministrationModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFunctionToPostAdministrationModel}.
	 */
	Change<OfficeFunctionToPostAdministrationModel> removeOfficeFunctionToPostAdministration(
			OfficeFunctionToPostAdministrationModel officeFunctionToAdministration);

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
			OfficeEscalationModel escalation, OfficeSectionInputModel sectionInput);

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
	Change<OfficeStartToOfficeSectionInputModel> linkOfficeStartToOfficeSectionInput(OfficeStartModel start,
			OfficeSectionInputModel sectionInput);

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