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

package net.officefloor.model.section;

import java.util.Map;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
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
	 * Value for {@link ManagedObjectScope#FUNCTION} on
	 * {@link SectionManagedObjectModel} instances.
	 */
	String FUNCTION_MANAGED_OBJECT_SCOPE = ManagedObjectScope.FUNCTION.name();

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
	Change<SubSectionModel> addSubSection(String subSectionName, String sectionSourceClassName, String sectionLocation,
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
	Change<SubSectionModel> renameSubSection(SubSectionModel subSection, String newSubSectionName);

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
	Change<SubSectionInputModel> setSubSectionInputPublic(boolean isPublic, String publicName,
			SubSectionInputModel input);

	/**
	 * Adds a {@link FunctionNamespaceModel} to the {@link SectionModel}.
	 *
	 * @param functionNamspaceName
	 *            Name of the {@link ManagedFunctionSource}.
	 * @param functionNamspaceSourceClassName
	 *            Fully qualified name of the {@link ManagedFunctionSource}.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link ManagedFunctionSource}.
	 * @param functionNamspaceType
	 *            {@link FunctionNamespaceType} from the
	 *            {@link ManagedFunctionSource}.
	 * @param managedFunctionNames
	 *            Listing of {@link ManagedFunctionModel} names to be loaded. Empty
	 *            list results in loading all {@link ManagedFunctionModel} instances
	 *            for the {@link FunctionNamespaceType}.
	 * @return {@link Change} to add the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> addFunctionNamespace(String functionNamspaceName,
			String functionNamspaceSourceClassName, PropertyList properties, FunctionNamespaceType functionNamspaceType,
			String... managedFunctionNames);

	/**
	 * Removes a {@link FunctionNamespaceModel} from the {@link SectionModel}.
	 *
	 * @param functionNamespaceModel
	 *            {@link FunctionNamespaceModel} to be removed.
	 * @return {@link Change} to remove the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> removeFunctionNamespace(FunctionNamespaceModel functionNamespaceModel);

	/**
	 * Renames the {@link FunctionNamespaceModel}.
	 *
	 * @param functionNamespaceModel
	 *            {@link FunctionNamespaceModel} to rename.
	 * @param newFunctionNamespaceName
	 *            New name for the {@link FunctionNamespaceModel}.
	 * @return {@link Change} to rename the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> renameFunctionNamespace(FunctionNamespaceModel functionNamespaceModel,
			String newFunctionNamespaceName);

	/**
	 * Refactors the {@link FunctionNamespaceModel}.
	 *
	 * @param functionNamespaceModel
	 *            {@link FunctionNamespaceModel} to refactor.
	 * @param functionNamespaceName
	 *            New name for the {@link FunctionNamespaceModel}.
	 * @param managedFunctionSourceClassName
	 *            New {@link ManagedFunctionSource} class name for the
	 *            {@link FunctionNamespaceModel}.
	 * @param properties
	 *            New {@link PropertyList} for the {@link FunctionNamespaceModel}.
	 * @param functionNamespaceType
	 *            {@link FunctionNamespaceType} that the
	 *            {@link FunctionNamespaceModel} is being refactored to.
	 * @param managedFunctionNameMapping
	 *            Mapping of the {@link ManagedFunctionType} name to the
	 *            {@link ManagedFunctionModel} name.
	 * @param managedFunctionToObjectNameMapping
	 *            Mapping of the {@link ManagedFunctionModel} name to the
	 *            {@link ManagedFunctionObjectType} name to the
	 *            {@link ManagedFunctionObjectModel} name.
	 * @param functionToFlowNameMapping
	 *            Mapping of the {@link FunctionModel} name to the
	 *            {@link ManagedFunctionFlowType} name to the
	 *            {@link FunctionFlowModel} name.
	 * @param functionToEscalationTypeMapping
	 *            Mapping of the {@link FunctionModel} name to the
	 *            {@link ManagedFunctionEscalationType} type to the
	 *            {@link FunctionEscalationModel} type.
	 * @param managedFunctionNames
	 *            Listing of {@link ManagedFunctionModel} names to be loaded. Empty
	 *            list results in loading all {@link ManagedFunctionModel} instances
	 *            for the {@link FunctionNamespaceType}.
	 * @return {@link Change} to refactor the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> refactorFunctionNamespace(FunctionNamespaceModel functionNamespaceModel,
			String functionNamespaceName, String managedFunctionSourceClassName, PropertyList properties,
			FunctionNamespaceType functionNamespaceType, Map<String, String> managedFunctionNameMapping,
			Map<String, Map<String, String>> managedFunctionToObjectNameMapping,
			Map<String, Map<String, String>> functionToFlowNameMapping,
			Map<String, Map<String, String>> functionToEscalationTypeMapping, String... managedFunctionNames);

	/**
	 * Adds the {@link ManagedFunctionType} as a {@link ManagedFunctionModel} to the
	 * {@link FunctionNamespaceModel}.
	 *
	 * @param <M>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link Flow} type keys.
	 * @param functionNamespaceModel
	 *            {@link FunctionNamespaceModel} to have the
	 *            {@link ManagedFunctionType} added.
	 * @param managedFunctionType
	 *            {@link ManagedFunctionType} to be added to the
	 *            {@link FunctionNamespaceModel}.
	 * @return {@link Change} to add the {@link ManagedFunctionType} to the
	 *         {@link FunctionNamespaceModel}.
	 */
	<M extends Enum<M>, F extends Enum<F>> Change<ManagedFunctionModel> addManagedFunction(
			FunctionNamespaceModel functionNamespaceModel, ManagedFunctionType<M, F> managedFunctionType);

	/**
	 * Removes the {@link ManagedFunctionModel} from the
	 * {@link FunctionNamespaceModel}.
	 *
	 * @param functionNamespaceModel
	 *            {@link FunctionNamespaceModel} to have the
	 *            {@link ManagedFunctionModel} removed.
	 * @param managedFunctionModel
	 *            {@link ManagedFunctionModel} to be removed.
	 * @return {@link Change} to remove the {@link ManagedFunctionModel} from the
	 *         {@link FunctionNamespaceModel}.
	 */
	Change<ManagedFunctionModel> removeManagedFunction(FunctionNamespaceModel functionNamespaceModel,
			ManagedFunctionModel managedFunctionModel);

	/**
	 * Adds a {@link ManagedFunctionType} as a {@link FunctionModel} to the
	 * {@link SectionModel}.
	 *
	 * @param <M>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link Flow} type keys.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param managedFunctionModel
	 *            {@link ManagedFunctionModel} for the {@link ManagedFunctionType}.
	 * @param managedFunctionType
	 *            {@link ManagedFunctionType} for the {@link FunctionModel}.
	 * @return {@link Change} to add the {@link ManagedFunctionType} to the
	 *         {@link SectionModel}.
	 */
	<M extends Enum<M>, F extends Enum<F>> Change<FunctionModel> addFunction(String functionName,
			ManagedFunctionModel managedFunctionModel, ManagedFunctionType<M, F> managedFunctionType);

	/**
	 * Removes the {@link FunctionModel} from the {@link SectionModel}.
	 *
	 * @param functionModel
	 *            {@link FunctionModel} to be removed.
	 * @return {@link Change} to remove the {@link FunctionModel} from the
	 *         {@link SectionModel}.
	 */
	Change<FunctionModel> removeFunction(FunctionModel functionModel);

	/**
	 * Renames the {@link FunctionModel}.
	 *
	 * @param functionModel
	 *            {@link FunctionModel} to be renamed.
	 * @param newFunctionName
	 *            New name for the {@link FunctionModel}.
	 * @return {@link Change} to rename the {@link FunctionModel}.
	 */
	Change<FunctionModel> renameFunction(FunctionModel functionModel, String newFunctionName);

	/**
	 * Specifies a {@link ManagedFunctionObjectModel} as a parameter or an object.
	 *
	 * @param isParameter
	 *            <code>true</code> for the {@link ManagedFunctionObjectModel} to be
	 *            a parameter. <code>false</code> to be a dependency object.
	 * @param managedFunctionObjectModel
	 *            {@link ManagedFunctionObjectModel} to set as a parameter or
	 *            object.
	 * @return {@link Change} to set the {@link ManagedFunctionObjectModel} as a
	 *         parameter or object.
	 */
	Change<ManagedFunctionObjectModel> setObjectAsParameter(boolean isParameter,
			ManagedFunctionObjectModel managedFunctionObjectModel);

	/**
	 * Specifies a {@link FunctionModel} as public/private.
	 *
	 * @param isPublic
	 *            <code>true</code> for the {@link FunctionModel} to be public.
	 *            <code>false</code> for the {@link FunctionModel} to be private.
	 * @param functionModel
	 *            {@link FunctionModel} to set public/private.
	 * @return {@link Change} to set the {@link FunctionModel} public/private.
	 */
	Change<FunctionModel> setFunctionAsPublic(boolean isPublic, FunctionModel functionModel);

	/**
	 * Adds an {@link ExternalFlowModel} to the {@link SectionModel}.
	 *
	 * @param externalFlowName
	 *            Name of the {@link ExternalFlowModel}.
	 * @param argumentType
	 *            Argument type for the {@link ExternalFlowModel}.
	 * @return {@link Change} to add the {@link ExternalFlowModel}.
	 */
	Change<ExternalFlowModel> addExternalFlow(String externalFlowName, String argumentType);

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
	Change<ExternalFlowModel> renameExternalFlow(ExternalFlowModel externalFlow, String newExternalFlowName);

	/**
	 * Adds the {@link ExternalManagedObjectModel} to the {@link SectionModel}.
	 *
	 * @param externalManagedObjectName
	 *            Name of the {@link ExternalManagedObjectModel}.
	 * @param objectType
	 *            Object type for the {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> addExternalManagedObject(String externalManagedObjectName, String objectType);

	/**
	 * Removes the {@link ExternalManagedObjectModel} from the {@link SectionModel}.
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
	 * @return {@link Change} to add the {@link SectionManagedObjectSourceModel} .
	 */
	Change<SectionManagedObjectSourceModel> addSectionManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties, long timeout,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link SectionManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link SectionManagedObjectSourceModel} to remove.
	 * @return {@link Change} to remove the {@link SectionManagedObjectSourceModel}.
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
	 * @return {@link Change} to rename the {@link SectionManagedObjectSourceModel}.
	 */
	Change<SectionManagedObjectSourceModel> renameSectionManagedObjectSource(
			SectionManagedObjectSourceModel managedObjectSource, String newManagedObjectSourceName);

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
	Change<SectionManagedObjectModel> addSectionManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope, SectionManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link SectionManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link SectionManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link SectionManagedObjectModel}.
	 */
	Change<SectionManagedObjectModel> removeSectionManagedObject(SectionManagedObjectModel managedObject);

	/**
	 * Renames the {@link SectionManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link SectionManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link SectionManagedObjectModel}.
	 * @return {@link Change} to rename the {@link SectionManagedObjectModel}.
	 */
	Change<SectionManagedObjectModel> renameSectionManagedObject(SectionManagedObjectModel managedObject,
			String newManagedObjectName);

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
	Change<SectionManagedObjectModel> rescopeSectionManagedObject(SectionManagedObjectModel managedObject,
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
			SubSectionObjectModel subSectionObject, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link SubSectionObjectToExternalManagedObjectModel}.
	 *
	 * @param subSectionObjectToExternalManagedObject
	 *            {@link SubSectionObjectToExternalManagedObjectModel} to remove.
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
			SubSectionObjectModel subSectionObject, SectionManagedObjectModel managedObject);

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
	 * Links the {@link ManagedFunctionObjectModel} to be the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param managedFunctionObject
	 *            {@link ManagedFunctionObjectModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add a
	 *         {@link ManagedFunctionObjectToExternalManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToExternalManagedObjectModel> linkManagedFunctionObjectToExternalManagedObject(
			ManagedFunctionObjectModel managedFunctionObject, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link ManagedFunctionObjectToExternalManagedObjectModel}.
	 *
	 * @param objectToExternalManagedObject
	 *            {@link ManagedFunctionObjectToExternalManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link ManagedFunctionObjectToExternalManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToExternalManagedObjectModel> removeManagedFunctionObjectToExternalManagedObject(
			ManagedFunctionObjectToExternalManagedObjectModel objectToExternalManagedObject);

	/**
	 * Links the {@link ManagedFunctionObjectModel} to the
	 * {@link SectionManagedObjectModel}.
	 *
	 * @param managedFunctionObject
	 *            {@link ManagedFunctionObjectModel}.
	 * @param managedObject
	 *            {@link SectionManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link ManagedFunctionObjectToSectionManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToSectionManagedObjectModel> linkManagedFunctionObjectToSectionManagedObject(
			ManagedFunctionObjectModel managedFunctionObject, SectionManagedObjectModel managedObject);

	/**
	 * Removes the {@link ManagedFunctionObjectToSectionManagedObjectModel}.
	 *
	 * @param managedFunctionObjectToManagedObject
	 *            {@link ManagedFunctionObjectToSectionManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link ManagedFunctionObjectToSectionManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToSectionManagedObjectModel> removeManagedFunctionObjectToSectionManagedObject(
			ManagedFunctionObjectToSectionManagedObjectModel managedFunctionObjectToManagedObject);

	/**
	 * Links the {@link SubSectionOutputModel} to the {@link SubSectionInputModel}.
	 *
	 * @param subSectionOutput
	 *            {@link SubSectionOutputModel}.
	 * @param subSectionInput
	 *            {@link SubSectionInputModel}.
	 * @return {@link Change} to add {@link SubSectionOutputToSubSectionInputModel}.
	 */
	Change<SubSectionOutputToSubSectionInputModel> linkSubSectionOutputToSubSectionInput(
			SubSectionOutputModel subSectionOutput, SubSectionInputModel subSectionInput);

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
			SubSectionOutputModel subSectionOutput, ExternalFlowModel externalFlow);

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
	 * Links the {@link SubSectionOutputModel} to the {@link FunctionModel}.
	 *
	 * @param subSectionOutput
	 *            {@link FunctionModel}.
	 * @param function
	 *            {@link FunctionModel}.
	 * @return {@link Change} to add the {@link SubSectionOutputToFunctionModel}.
	 */
	Change<SubSectionOutputToFunctionModel> linkSubSectionOutputToFunction(SubSectionOutputModel subSectionOutput,
			FunctionModel function);

	/**
	 * Removes the {@link SubSectionOutputToFunctionModel}.
	 *
	 * @param subSectionOutputToFunction
	 *            {@link SubSectionOutputToFunctionModel} to remove.
	 * @return {@link Change} to remove the {@link SubSectionOutputToFunctionModel}.
	 */
	Change<SubSectionOutputToFunctionModel> removeSubSectionOutputToFunction(
			SubSectionOutputToFunctionModel subSectionOutputToFunction);

	/**
	 * Links the {@link FunctionFlowModel} to the {@link FunctionModel}.
	 *
	 * @param functionFlow
	 *            {@link FunctionFlowModel}.
	 * @param function
	 *            {@link FunctionModel}.
	 * @param isSpawnThreadState
	 *            Indicates if to spawn a {@link ThreadState}.
	 * @return {@link Change} to add a {@link FunctionFlowToFunctionModel}.
	 */
	Change<FunctionFlowToFunctionModel> linkFunctionFlowToFunction(FunctionFlowModel functionFlow,
			FunctionModel function, boolean isSpawnThreadState);

	/**
	 * Removes the {@link FunctionFlowToFunctionModel}.
	 *
	 * @param functionFlowToFunction
	 *            {@link FunctionFlowToFunctionModel} to remove.
	 * @return {@link Change} to remove {@link FunctionFlowToFunctionModel}.
	 */
	Change<FunctionFlowToFunctionModel> removeFunctionFlowToFunction(
			FunctionFlowToFunctionModel functionFlowToFunction);

	/**
	 * Links the {@link FunctionFlowModel} to the {@link ExternalFlowModel}.
	 *
	 * @param functionFlow
	 *            {@link FunctionFlowModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @param isSpawnThreadState
	 *            Indicates if to spawn a {@link ThreadState}.
	 * @return {@link Change} to add a {@link FunctionFlowToExternalFlowModel}.
	 */
	Change<FunctionFlowToExternalFlowModel> linkFunctionFlowToExternalFlow(FunctionFlowModel functionFlow,
			ExternalFlowModel externalFlow, boolean isSpawnThreadState);

	/**
	 * Removes the {@link FunctionFlowToExternalFlowModel}.
	 *
	 * @param functionFlowToExternalFlow
	 *            {@link FunctionFlowToExternalFlowModel} to remove.
	 * @return {@link Change} to remove {@link FunctionFlowToExternalFlowModel}.
	 */
	Change<FunctionFlowToExternalFlowModel> removeFunctionFlowToExternalFlow(
			FunctionFlowToExternalFlowModel functionFlowToExternalFlow);

	/**
	 * Links the {@link FunctionFlowModel} to the {@link SubSectionInputModel}.
	 *
	 * @param functionFlow
	 *            {@link FunctionFlowModel}.
	 * @param subSectionInput
	 *            {@link SubSectionInputModel}.
	 * @param isSpawnThreadState
	 *            Indicates if to spawn a {@link ThreadState}.
	 * @return {@link Change} to add a {@link FunctionFlowToSubSectionInputModel}.
	 */
	Change<FunctionFlowToSubSectionInputModel> linkFunctionFlowToSubSectionInput(FunctionFlowModel functionFlow,
			SubSectionInputModel subSectionInput, boolean isSpawnThreadState);

	/**
	 * Removes the {@link FunctionFlowToSubSectionInputModel}.
	 *
	 * @param functionFlowToSubSectionInput
	 *            {@link FunctionFlowToSubSectionInputModel} to remove.
	 * @return {@link Change} to remove {@link FunctionFlowToSubSectionInputModel}.
	 */
	Change<FunctionFlowToSubSectionInputModel> removeFunctionFlowToSubSectionInput(
			FunctionFlowToSubSectionInputModel functionFlowToSubSectionInput);

	/**
	 * Links {@link FunctionModel} to next {@link FunctionModel}.
	 *
	 * @param function
	 *            {@link FunctionModel}.
	 * @param nextFunction
	 *            Next {@link FunctionModel}.
	 * @return {@link Change} to add a {@link FunctionToNextFunctionModel}.
	 */
	Change<FunctionToNextFunctionModel> linkFunctionToNextFunction(FunctionModel function, FunctionModel nextFunction);

	/**
	 * Removes the {@link FunctionToNextFunctionModel}.
	 *
	 * @param functionToNextFunction
	 *            {@link FunctionToNextFunctionModel} to remove.
	 * @return {@link Change} to remove {@link FunctionToNextFunctionModel}.
	 */
	Change<FunctionToNextFunctionModel> removeFunctionToNextFunction(
			FunctionToNextFunctionModel functionToNextFunction);

	/**
	 * Links {@link FunctionModel} to next {@link ExternalFlowModel}.
	 *
	 * @param function
	 *            {@link FunctionModel}.
	 * @param nextExternalFlow
	 *            Next {@link ExternalFlowModel}.
	 * @return {@link Change} to add a {@link FunctionToNextExternalFlowModel}.
	 */
	Change<FunctionToNextExternalFlowModel> linkFunctionToNextExternalFlow(FunctionModel function,
			ExternalFlowModel nextExternalFlow);

	/**
	 * Removes the {@link FunctionToNextExternalFlowModel}.
	 *
	 * @param functionToNextExternalFlow
	 *            {@link FunctionToNextExternalFlowModel} to remove.
	 * @return {@link Change} to remove {@link FunctionToNextExternalFlowModel}.
	 */
	Change<FunctionToNextExternalFlowModel> removeFunctionToNextExternalFlow(
			FunctionToNextExternalFlowModel functionToNextExternalFlow);

	/**
	 * Links {@link FunctionModel} to next {@link SubSectionInputModel}.
	 *
	 * @param function
	 *            {@link FunctionModel}.
	 * @param nextSubSectionInput
	 *            Next {@link SubSectionInputModel}.
	 * @return {@link Change} to add a {@link FunctionToNextSubSectionInputModel}.
	 */
	Change<FunctionToNextSubSectionInputModel> linkFunctionToNextSubSectionInput(FunctionModel function,
			SubSectionInputModel nextSubSectionInput);

	/**
	 * Removes the {@link FunctionToNextSubSectionInputModel}.
	 *
	 * @param functionToNextSubSectionInput
	 *            {@link FunctionToNextSubSectionInputModel} to remove.
	 * @return {@link Change} to remove {@link FunctionToNextSubSectionInputModel}.
	 */
	Change<FunctionToNextSubSectionInputModel> removeFunctionToNextSubSectionInput(
			FunctionToNextSubSectionInputModel functionToNextSubSectionInput);

	/**
	 * Links {@link FunctionEscalationModel} to the {@link FunctionModel}.
	 *
	 * @param functionEscalation
	 *            {@link FunctionEscalationModel}.
	 * @param function
	 *            {@link FunctionModel}.
	 * @return {@link Change} to add a {@link FunctionEscalationToFunctionModel}.
	 */
	Change<FunctionEscalationToFunctionModel> linkFunctionEscalationToFunction(
			FunctionEscalationModel functionEscalation, FunctionModel function);

	/**
	 * Removes the {@link FunctionEscalationToFunctionModel}.
	 *
	 * @param functionEscalationToFunction
	 *            {@link FunctionEscalationToFunctionModel} to remove.
	 * @return {@link Change} to remove {@link FunctionEscalationToFunctionModel}.
	 */
	Change<FunctionEscalationToFunctionModel> removeFunctionEscalationToFunction(
			FunctionEscalationToFunctionModel functionEscalationToFunction);

	/**
	 * Links {@link FunctionEscalationModel} to the {@link ExternalFlowModel}.
	 *
	 * @param functionEscalation
	 *            {@link FunctionEscalationModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @return {@link Change} to add {@link FunctionEscalationToExternalFlowModel}.
	 */
	Change<FunctionEscalationToExternalFlowModel> linkFunctionEscalationToExternalFlow(
			FunctionEscalationModel functionEscalation, ExternalFlowModel externalFlow);

	/**
	 * Removes the {@link FunctionEscalationToExternalFlowModel}.
	 *
	 * @param functionEscalationToExternalFlow
	 *            {@link FunctionEscalationToExternalFlowModel} to remove.
	 * @return {@link Change} to remove
	 *         {@link FunctionEscalationToExternalFlowModel}.
	 */
	Change<FunctionEscalationToExternalFlowModel> removeFunctionEscalationToExternalFlow(
			FunctionEscalationToExternalFlowModel functionEscalationToExternalFlow);

	/**
	 * Links {@link FunctionEscalationModel} to the {@link SubSectionInputModel}.
	 *
	 * @param functionEscalation
	 *            {@link FunctionEscalationModel}.
	 * @param subSectionInput
	 *            {@link SubSectionInputModel}.
	 * @return {@link Change} to add
	 *         {@link FunctionEscalationToSubSectionInputModel}.
	 */
	Change<FunctionEscalationToSubSectionInputModel> linkFunctionEscalationToSubSectionInput(
			FunctionEscalationModel functionEscalation, SubSectionInputModel subSectionInput);

	/**
	 * Removes the {@link FunctionEscalationToSubSectionInputModel}.
	 *
	 * @param functionEscalationToSubSectionInput
	 *            {@link FunctionEscalationToSubSectionInputModel} to remove.
	 * @return {@link Change} to remove
	 *         {@link FunctionEscalationToSubSectionInputModel}.
	 */
	Change<FunctionEscalationToSubSectionInputModel> removeFunctionEscalationToSubSectionInput(
			FunctionEscalationToSubSectionInputModel functionEscalationToSubSectionInput);

	/**
	 * Links the {@link SectionManagedObjectSourceFlowModel} to the
	 * {@link FunctionModel}.
	 *
	 * @param managedObjectSourceFlow
	 *            {@link SectionManagedObjectSourceFlowModel}.
	 * @param function
	 *            {@link FunctionModel}.
	 * @return {@link Change} to add the
	 *         {@link SectionManagedObjectSourceFlowToFunctionModel}.
	 */
	Change<SectionManagedObjectSourceFlowToFunctionModel> linkSectionManagedObjectSourceFlowToFunction(
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow, FunctionModel function);

	/**
	 * Removes the {@link SectionManagedObjectSourceFlowToFunctionModel}.
	 *
	 * @param managedObjectSourceFlowToFunction
	 *            {@link SectionManagedObjectSourceFlowToFunctionModel} to be
	 *            removed.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectSourceFlowToFunctionModel}.
	 */
	Change<SectionManagedObjectSourceFlowToFunctionModel> removeSectionManagedObjectSourceFlowToFunction(
			SectionManagedObjectSourceFlowToFunctionModel managedObjectSourceFlowToFunction);

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
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow, SubSectionInputModel subSectionInput);

	/**
	 * Removes the {@link SectionManagedObjectSourceFlowToSubSectionInputModel}.
	 *
	 * @param managedObjectSourceFlowToSubSectionInput
	 *            {@link SectionManagedObjectSourceFlowToSubSectionInputModel} to
	 *            remove.
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
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow, ExternalFlowModel externalFlow);

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
	 *         {@link SectionManagedObjectDependencyToSectionManagedObjectModel} .
	 */
	Change<SectionManagedObjectDependencyToSectionManagedObjectModel> linkSectionManagedObjectDependencyToSectionManagedObject(
			SectionManagedObjectDependencyModel dependency, SectionManagedObjectModel managedObject);

	/**
	 * Removes the
	 * {@link SectionManagedObjectDependencyToSectionManagedObjectModel}.
	 *
	 * @param dependencyToManagedObject
	 *            {@link SectionManagedObjectDependencyToSectionManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectDependencyToSectionManagedObjectModel} .
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
	 *         {@link SectionManagedObjectDependencyToExternalManagedObjectModel} .
	 */
	Change<SectionManagedObjectDependencyToExternalManagedObjectModel> linkSectionManagedObjectDependencyToExternalManagedObject(
			SectionManagedObjectDependencyModel dependency, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the
	 * {@link SectionManagedObjectDependencyToExternalManagedObjectModel}.
	 *
	 * @param dependencyToExternalManagedObject
	 *            {@link SectionManagedObjectDependencyToExternalManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link SectionManagedObjectDependencyToExternalManagedObjectModel} .
	 */
	Change<SectionManagedObjectDependencyToExternalManagedObjectModel> removeSectionManagedObjectDependencyToExternalManagedObject(
			SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExternalManagedObject);

}
