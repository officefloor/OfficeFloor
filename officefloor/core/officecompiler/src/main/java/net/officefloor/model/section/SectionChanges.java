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
package net.officefloor.model.section;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link SectionModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface SectionChanges {

	/**
	 * Value for {@link ManagedObjectScope#PROCESS} on
	 * {@link SectionManagedObjectModel} instances.
	 */
	String PROCESS_MANAGED_OBJECT_SCOPE = ManagedObjectScope.PROCESS.name();

	/**
	 * Value for {@link ManagedObjectScope#THREAD} on
	 * {@link SectionManagedObjectModel} instances.
	 */
	String THREAD_MANAGED_OBJECT_SCOPE = ManagedObjectScope.THREAD.name();

	/**
	 * Value for {@link ManagedObjectScope#WORK} on
	 * {@link SectionManagedObjectModel} instances.
	 */
	String WORK_MANAGED_OBJECT_SCOPE = ManagedObjectScope.WORK.name();

	/**
	 * Adds a {@link SubSectionModel} to the {@link SectionModel}.
	 *
	 * @param subSectionName
	 *            Name of the {@link SubSectionModel}.
	 * @param sectionSourceClassName
	 *            Name of the {@link SectionSource} class.
	 * @param sectionLocation
	 *            Location of the {@link SubSectionModel}.
	 * @param properties
	 *            {@link PropertyList} for the {@link SectionSource}.
	 * @param sectionType
	 *            {@link SectionType}.
	 * @return {@link Change} to add the {@link SubSectionModel}.
	 */
	Change<SubSectionModel> addSubSection(String subSectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType);

	/**
	 * Removes the {@link SubSectionModel} from the {@link SectionModel}.
	 *
	 * @param subSection
	 *            {@link SubSectionModel} to remove.
	 * @return {@link Change} to remove the {@link SubSectionModel}.
	 */
	Change<SubSectionModel> removeSubSection(SubSectionModel subSection);

	/**
	 * Renames the {@link SubSectionModel} to the new name.
	 *
	 * @param subSection
	 *            {@link SubSectionModel} to rename.
	 * @param newSubSectionName
	 *            New name for the {@link SubSectionModel}.
	 * @return {@link Change} to rename the {@link SubSectionModel}.
	 */
	Change<SubSectionModel> renameSubSection(SubSectionModel subSection,
			String newSubSectionName);

	/**
	 * Sets the {@link SubSectionInputModel} public/private.
	 *
	 * @param isPublic
	 *            Flag indicating if public/private.
	 * @param publicName
	 *            Public name if setting public. Ignored if setting private.
	 * @param input
	 *            {@link SubSectionInputModel} to set public/private.
	 * @return {@link SubSectionInputModel} to set public/private.
	 */
	Change<SubSectionInputModel> setSubSectionInputPublic(boolean isPublic,
			String publicName, SubSectionInputModel input);

	/**
	 * Adds an {@link ExternalFlowModel} to the {@link SectionModel}.
	 *
	 * @param externalFlowName
	 *            Name of the {@link ExternalFlowModel}.
	 * @param argumentType
	 *            Argument type for the {@link ExternalFlowModel}.
	 * @return {@link Change} to add the {@link ExternalFlowModel}.
	 */
	Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType);

	/**
	 * Removes the {@link ExternalFlowModel} from the {@link SectionModel}.
	 *
	 * @param externalFlow
	 *            {@link ExternalFlowModel} to remove.
	 * @return {@link Change} to remove the {@link ExternalFlowModel}.
	 */
	Change<ExternalFlowModel> removeExternalFlow(ExternalFlowModel externalFlow);

	/**
	 * Renames the {@link ExternalFlowModel}.
	 *
	 * @param externalFlow
	 *            {@link ExternalFlowModel} to rename.
	 * @param newExternalFlowName
	 *            New name for the {@link ExternalFlowModel}.
	 * @return {@link Change} to rename the {@link ExternalFlowModel}.
	 */
	Change<ExternalFlowModel> renameExternalFlow(
			ExternalFlowModel externalFlow, String newExternalFlowName);

	/**
	 * Adds the {@link ExternalManagedObjectModel} to the {@link SectionModel}.
	 *
	 * @param externalManagedObjectName
	 *            Name of the {@link ExternalManagedObjectModel}.
	 * @param objectType
	 *            Object type for the {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String objectType);

	/**
	 * Removes the {@link ExternalManagedObjectModel} from the
	 * {@link SectionModel}.
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
	 * Adds an {@link SectionManagedObjectSourceModel} to {@link SectionModel}.
	 *
	 * @param managedObjectSourceName
	 *            Name of the {@link SectionManagedObjectSourceModel}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param timeout
	 *            Timeout of the {@link ManagedObject}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link SectionManagedObjectSourceModel}
	 *         .
	 */
	Change<SectionManagedObjectSourceModel> addSectionManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			long timeout, ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link SectionManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link SectionManagedObjectSourceModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectSourceModel}.
	 */
	Change<SectionManagedObjectSourceModel> removeSectionManagedObjectSource(
			SectionManagedObjectSourceModel managedObjectSource);

	/**
	 * Renames the {@link SectionManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link SectionManagedObjectSourceModel} to rename.
	 * @param newManagedObjectSourceName
	 *            New name for the {@link SectionManagedObjectSourceModel}.
	 * @return {@link Change} to rename the
	 *         {@link SectionManagedObjectSourceModel}.
	 */
	Change<SectionManagedObjectSourceModel> renameSectionManagedObjectSource(
			SectionManagedObjectSourceModel managedObjectSource,
			String newManagedObjectSourceName);

	/**
	 * Adds an {@link SectionManagedObjectModel} for an
	 * {@link SectionManagedObjectSourceModel} to the {@link SectionModel}.
	 *
	 * @param managedObjectName
	 *            Name of the {@link SectionManagedObjectModel}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the
	 *            {@link SectionManagedObjectModel}.
	 * @param managedObjectSource
	 *            {@link SectionManagedObjectSourceModel}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link SectionManagedObjectModel}.
	 */
	Change<SectionManagedObjectModel> addSectionManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			SectionManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link SectionManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link SectionManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link SectionManagedObjectModel}.
	 */
	Change<SectionManagedObjectModel> removeSectionManagedObject(
			SectionManagedObjectModel managedObject);

	/**
	 * Renames the {@link SectionManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link SectionManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link SectionManagedObjectModel}.
	 * @return {@link Change} to rename the {@link SectionManagedObjectModel}.
	 */
	Change<SectionManagedObjectModel> renameSectionManagedObject(
			SectionManagedObjectModel managedObject, String newManagedObjectName);

	/**
	 * Scopes the {@link SectionManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link SectionManagedObjectModel} to scope.
	 * @param newManagedObjectScope
	 *            New {@link ManagedObjectScope} for the
	 *            {@link SectionManagedObjectModel}.
	 * @return {@link Change} to scope {@link SectionManagedObjectModel}.
	 */
	Change<SectionManagedObjectModel> rescopeSectionManagedObject(
			SectionManagedObjectModel managedObject,
			ManagedObjectScope newManagedObjectScope);

	/**
	 * Links the {@link SubSectionInputModel} to the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param subSectionObject
	 *            {@link SubSectionObjectModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add
	 *         {@link SubSectionObjectToExternalManagedObjectModel}.
	 */
	Change<SubSectionObjectToExternalManagedObjectModel> linkSubSectionObjectToExternalManagedObject(
			SubSectionObjectModel subSectionObject,
			ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link SubSectionObjectToExternalManagedObjectModel}.
	 *
	 * @param subSectionObjectToExternalManagedObject
	 *            {@link SubSectionObjectToExternalManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link SubSectionObjectToExternalManagedObjectModel}.
	 */
	Change<SubSectionObjectToExternalManagedObjectModel> removeSubSectionObjectToExternalManagedObject(
			SubSectionObjectToExternalManagedObjectModel subSectionObjectToExternalManagedObject);

	/**
	 * Links the {@link SubSectionObjectModel} to the
	 * {@link SectionManagedObjectModel}.
	 *
	 * @param subSectionObject
	 *            {@link SubSectionObjectModel}.
	 * @param managedObject
	 *            {@link SectionManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link SubSectionObjectToSectionManagedObjectModel}.
	 */
	Change<SubSectionObjectToSectionManagedObjectModel> linkSubSectionObjectToSectionManagedObject(
			SubSectionObjectModel subSectionObject,
			SectionManagedObjectModel managedObject);

	/**
	 * Removes the {@link SubSectionObjectToSectionManagedObjectModel}.
	 *
	 * @param subSectionObjectToManagedObject
	 *            {@link SubSectionObjectToSectionManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link SubSectionObjectToSectionManagedObjectModel}.
	 */
	Change<SubSectionObjectToSectionManagedObjectModel> removeSubSectionObjectToSectionManagedObject(
			SubSectionObjectToSectionManagedObjectModel subSectionObjectToManagedObject);

	/**
	 * Links the {@link SubSectionOutputModel} to the
	 * {@link SubSectionInputModel}.
	 *
	 * @param subSectionOutput
	 *            {@link SubSectionOutputModel}.
	 * @param subSectionInput
	 *            {@link SubSectionInputModel}.
	 * @return {@link Change} to add
	 *         {@link SubSectionOutputToSubSectionInputModel}.
	 */
	Change<SubSectionOutputToSubSectionInputModel> linkSubSectionOutputToSubSectionInput(
			SubSectionOutputModel subSectionOutput,
			SubSectionInputModel subSectionInput);

	/**
	 * Removes the {@link SubSectionOutputToSubSectionInputModel}.
	 *
	 * @param subSectionOutputToSubSectionInput
	 *            {@link SubSectionOutputToSubSectionInputModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link SubSectionOutputToSubSectionInputModel}.
	 */
	Change<SubSectionOutputToSubSectionInputModel> removeSubSectionOutputToSubSectionInput(
			SubSectionOutputToSubSectionInputModel subSectionOutputToSubSectionInput);

	/**
	 * Links the {@link SubSectionOutputModel} to the {@link ExternalFlowModel}.
	 *
	 * @param subSectionOutput
	 *            {@link SubSectionOutputModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @return {@link Change} to add the
	 *         {@link SubSectionOutputToExternalFlowModel}.
	 */
	Change<SubSectionOutputToExternalFlowModel> linkSubSectionOutputToExternalFlow(
			SubSectionOutputModel subSectionOutput,
			ExternalFlowModel externalFlow);

	/**
	 * Removes the {@link SubSectionOutputToExternalFlowModel}.
	 *
	 * @param subSectionOutputToExternalFlow
	 *            {@link SubSectionOutputToExternalFlowModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link SubSectionOutputToExternalFlowModel}.
	 */
	Change<SubSectionOutputToExternalFlowModel> removeSubSectionOutputToExternalFlow(
			SubSectionOutputToExternalFlowModel subSectionOutputToExternalFlow);

	/**
	 * Links the {@link SectionManagedObjectSourceFlowModel} to the
	 * {@link ExternalFlowModel}.
	 *
	 * @param managedObjectSourceFlow
	 *            {@link SectionManagedObjectSourceFlowModel}.
	 * @param subSectionInput
	 *            {@link SubSectionInputModel}.
	 * @return {@link Change} to add the
	 *         {@link SectionManagedObjectSourceFlowToSubSectionInputModel}.
	 */
	Change<SectionManagedObjectSourceFlowToSubSectionInputModel> linkSectionManagedObjectSourceFlowToSubSectionInput(
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow,
			SubSectionInputModel subSectionInput);

	/**
	 * Removes the {@link SectionManagedObjectSourceFlowToSubSectionInputModel}.
	 *
	 * @param managedObjectSourceFlowToSubSectionInput
	 *            {@link SectionManagedObjectSourceFlowToSubSectionInputModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectSourceFlowToSubSectionInputModel}.
	 */
	Change<SectionManagedObjectSourceFlowToSubSectionInputModel> removeSectionManagedObjectSourceFlowToSubSectionInput(
			SectionManagedObjectSourceFlowToSubSectionInputModel managedObjectSourceFlowToSubSectionInput);

	/**
	 * Links the {@link SectionManagedObjectSourceFlowModel} to the
	 * {@link ExternalFlowModel}.
	 *
	 * @param managedObjectSourceFlow
	 *            {@link SectionManagedObjectSourceFlowModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @return {@link Change} to add the
	 *         {@link SectionManagedObjectSourceFlowToExternalFlowModel}.
	 */
	Change<SectionManagedObjectSourceFlowToExternalFlowModel> linkSectionManagedObjectSourceFlowToExternalFlow(
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow,
			ExternalFlowModel externalFlow);

	/**
	 * Removes the {@link SectionManagedObjectSourceFlowToExternalFlowModel}.
	 *
	 * @param managedObjectSourceFlowToExternalFlow
	 *            {@link SectionManagedObjectSourceFlowToExternalFlowModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectSourceFlowToExternalFlowModel}.
	 */
	Change<SectionManagedObjectSourceFlowToExternalFlowModel> removeSectionManagedObjectSourceFlowToExternalFlow(
			SectionManagedObjectSourceFlowToExternalFlowModel managedObjectSourceFlowToExternalFlow);

	/**
	 * Links the {@link SectionManagedObjectDependencyModel} to the
	 * {@link SectionManagedObjectModel}.
	 *
	 * @param dependency
	 *            {@link SectionManagedObjectDependencyModel}.
	 * @param managedObject
	 *            {@link SectionManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link SectionManagedObjectDependencyToSectionManagedObjectModel}
	 *         .
	 */
	Change<SectionManagedObjectDependencyToSectionManagedObjectModel> linkSectionManagedObjectDependencyToSectionManagedObject(
			SectionManagedObjectDependencyModel dependency,
			SectionManagedObjectModel managedObject);

	/**
	 * Removes the
	 * {@link SectionManagedObjectDependencyToSectionManagedObjectModel}.
	 *
	 * @param dependencyToManagedObject
	 *            {@link SectionManagedObjectDependencyToSectionManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectDependencyToSectionManagedObjectModel}
	 *         .
	 */
	Change<SectionManagedObjectDependencyToSectionManagedObjectModel> removeSectionManagedObjectDependencyToSectionManagedObject(
			SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToManagedObject);

	/**
	 * Links the {@link SectionManagedObjectDependencyModel} to the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param dependency
	 *            {@link SectionManagedObjectDependencyModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link SectionManagedObjectDependencyToExternalManagedObjectModel}
	 *         .
	 */
	Change<SectionManagedObjectDependencyToExternalManagedObjectModel> linkSectionManagedObjectDependencyToExternalManagedObject(
			SectionManagedObjectDependencyModel dependency,
			ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the
	 * {@link SectionManagedObjectDependencyToExternalManagedObjectModel}.
	 *
	 * @param dependencyToExternalManagedObject
	 *            {@link SectionManagedObjectDependencyToExternalManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectDependencyToExternalManagedObjectModel}
	 *         .
	 */
	Change<SectionManagedObjectDependencyToExternalManagedObjectModel> removeSectionManagedObjectDependencyToExternalManagedObject(
			SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExternalManagedObject);

}