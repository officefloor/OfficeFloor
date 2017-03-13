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
package net.officefloor.model.desk;

import java.util.Map;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link DeskModel}.
 *
 * @author Daniel Sagenschneider
 */
@Deprecated // Move functions to Section and deprecate Desk
public interface DeskChanges {

	/**
	 * Value for {@link ManagedObjectScope#PROCESS} on
	 * {@link DeskManagedObjectModel} instances.
	 */
	String PROCESS_MANAGED_OBJECT_SCOPE = ManagedObjectScope.PROCESS.name();

	/**
	 * Value for {@link ManagedObjectScope#THREAD} on
	 * {@link DeskManagedObjectModel} instances.
	 */
	String THREAD_MANAGED_OBJECT_SCOPE = ManagedObjectScope.THREAD.name();

	/**
	 * Value for {@link ManagedObjectScope#FUNCTION} on
	 * {@link DeskManagedObjectModel} instances.
	 */
	String FUNCTION_MANAGED_OBJECT_SCOPE = ManagedObjectScope.FUNCTION.name();

	/**
	 * Adds a {@link FunctionNamespaceModel} to the {@link DeskModel}.
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
	 *            Listing of {@link WorkTaskModel} names to be loaded. Empty
	 *            list results in loading all {@link WorkTaskModel} instances
	 *            for the {@link FunctionNamespaceType}.
	 * @return {@link Change} to add the {@link WorkModel}.
	 */
	Change<FunctionNamespaceModel> addFunctionNamespace(String functionNamspaceName,
			String functionNamspaceSourceClassName, PropertyList properties, FunctionNamespaceType functionNamspaceType,
			String... managedFunctionNames);

	/**
	 * Removes a {@link FunctionNamespaceModel} from the {@link DeskModel}.
	 *
	 * @param functionNamespaceModel
	 *            {@link FunctionNamespaceModel} to be removed.
	 * @return {@link Change} to remove the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> removeFunctionNamespace(FunctionNamespaceModel functionNamspaceModel);

	/**
	 * Renames the {@link FunctionNamespaceModel}.
	 *
	 * @param functionNamspaceModel
	 *            {@link FunctionNamespaceModel} to rename.
	 * @param newFunctionNamespaceName
	 *            New name for the {@link FunctionNamespaceModel}.
	 * @return {@link Change} to rename the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> renameFunctionNamespace(FunctionNamespaceModel functionNamspaceModel,
			String newFunctionNamespaceName);

	/**
	 * Refactors the {@link FunctionNamespaceModel}.
	 *
	 * @param functionNamspaceModel
	 *            {@link FunctionNamespaceModel} to refactor.
	 * @param functionNamespaceName
	 *            New name for the {@link FunctionNamespaceModel}.
	 * @param managedFunctionSourceClassName
	 *            New {@link ManagedFunctionSource} class name for the
	 *            {@link FunctionNamespaceModel}.
	 * @param properties
	 *            New {@link PropertyList} for the
	 *            {@link FunctionNamespaceModel}.
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
	 *            Listing of {@link ManagedFunctionModel} names to be loaded.
	 *            Empty list results in loading all {@link ManagedFunctionModel}
	 *            instances for the {@link FunctionNamespaceType}.
	 * @return {@link Change} to refactor the {@link FunctionNamespaceModel}.
	 */
	Change<FunctionNamespaceModel> refactorFunctionNamespace(FunctionNamespaceModel functionNamespaceModel,
			String functionNamespaceName, String managedFunctionSourceClassName, PropertyList properties,
			FunctionNamespaceType functionNamespaceType, Map<String, String> managedFunctionNameMapping,
			Map<String, Map<String, String>> managedFunctionToObjectNameMapping,
			Map<String, Map<String, String>> functionToFlowNameMapping,
			Map<String, Map<String, String>> functionToEscalationTypeMapping, String... managedFunctionNames);

	/**
	 * Adds the {@link ManagedFunctionType} as a {@link ManagedFunctionModel} to
	 * the {@link FunctionNamspaceModel}.
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
	 *            {@link ManagdedFunctionModel} to be removed.
	 * @return {@link Change} to remove the {@link ManagedFunctionModel} from
	 *         the {@link FunctionNamespaceModel}.
	 */
	Change<ManagedFunctionModel> removeManagedFunction(FunctionNamespaceModel functionNamespaceModel,
			ManagedFunctionModel managedFunctionModel);

	/**
	 * Adds a {@link ManagedFunctionType} as a {@link FunctionModel} to the
	 * {@link DeskModel}.
	 *
	 * @param <M>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link Flow} type keys.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param managedFunctionModel
	 *            {@link ManagedFunctionModel} for the
	 *            {@link ManagedFunctionType}.
	 * @param managedFunctionType
	 *            {@link ManagedFunctionType} for the {@link FunctionModel}.
	 * @return {@link Change} to add the {@link ManagedFunctionType} to the
	 *         {@link DeskModel}.
	 */
	<M extends Enum<M>, F extends Enum<F>> Change<FunctionModel> addFunction(String functionName,
			ManagedFunctionModel managedFunctionModel, ManagedFunctionType<M, F> managedFunctionType);

	/**
	 * Removes the {@link FunctionModel} from the {@link DeskModel}.
	 *
	 * @param functionModel
	 *            {@link FunctionModel} to be removed.
	 * @return {@link Change} to remove the {@link FunctionModel} from the
	 *         {@link DeskModel}.
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
	 * Specifies a {@link ManagedFunctionObjectModel} as a parameter or an
	 * object.
	 *
	 * @param isParameter
	 *            <code>true</code> for the {@link ManagedFunctionObjectModel}
	 *            to be a parameter. <code>false</code> to be a dependency
	 *            object.
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
	 *            <code>false</code> for the {@link FunctionModel} to be
	 *            private.
	 * @param functionModel
	 *            {@link FunctionModel} to set public/private.
	 * @return {@link Change} to set the {@link FunctionModel} public/private.
	 */
	Change<FunctionModel> setFunctionAsPublic(boolean isPublic, FunctionModel functionModel);

	/**
	 * Adds an {@link ExternalFlowModel} to the {@link DeskModel}.
	 *
	 * @param externalFlowName
	 *            Name of the {@link ExternalFlowModel}.
	 * @param argumentType
	 *            Argument type for the {@link ExternalFlowModel}.
	 * @return {@link Change} to add the {@link ExternalFlowModel}.
	 */
	Change<ExternalFlowModel> addExternalFlow(String externalFlowName, String argumentType);

	/**
	 * Removes an {@link ExternalFlowModel} from the {@link DeskModel}.
	 *
	 * @param externalFlow
	 *            {@link ExternalFlowModel} for removal from the
	 *            {@link DeskModel}.
	 * @return {@link Change} to remove the {@link ExternalFlowModel} from the
	 *         {@link DeskModel}.
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
	 * Adds an {@link ExternalManagedObjectModel} to the {@link DeskModel}.
	 *
	 * @param externalManagedObjectName
	 *            Name of the {@link ExternalManagedObjectModel}.
	 * @param objectType
	 *            Object type for the {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the {@link ExternalManagedObjectModel}.
	 */
	Change<ExternalManagedObjectModel> addExternalManagedObject(String externalManagedObjectName, String objectType);

	/**
	 * Removes an {@link ExternalManagedObjectModel} from the {@link DeskModel}.
	 *
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel} to remove from the
	 *            {@link DeskModel}.
	 * @return {@link Change} to remove the {@link ExternalManagedObjectModel}
	 *         from the {@link DeskModel}.
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
	 * Adds an {@link DeskManagedObjectSourceModel} to {@link DeskModel}.
	 *
	 * @param managedObjectSourceName
	 *            Name of the {@link DeskManagedObjectSourceModel}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param timeout
	 *            Timeout of the {@link ManagedObject}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link DeskManagedObjectSourceModel} .
	 */
	Change<DeskManagedObjectSourceModel> addDeskManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties, long timeout,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link DeskManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link DeskManagedObjectSourceModel} to remove.
	 * @return {@link Change} to remove the {@link DeskManagedObjectSourceModel}
	 *         .
	 */
	Change<DeskManagedObjectSourceModel> removeDeskManagedObjectSource(
			DeskManagedObjectSourceModel managedObjectSource);

	/**
	 * Renames the {@link DeskManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link DeskManagedObjectSourceModel} to rename.
	 * @param newManagedObjectSourceName
	 *            New name for the {@link DeskManagedObjectSourceModel}.
	 * @return {@link Change} to rename the {@link DeskManagedObjectSourceModel}
	 *         .
	 */
	Change<DeskManagedObjectSourceModel> renameDeskManagedObjectSource(DeskManagedObjectSourceModel managedObjectSource,
			String newManagedObjectSourceName);

	/**
	 * Adds an {@link DeskManagedObjectModel} for an
	 * {@link DeskManagedObjectSourceModel} to the {@link DeskModel}.
	 *
	 * @param managedObjectName
	 *            Name of the {@link DeskManagedObjectModel}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the
	 *            {@link DeskManagedObjectModel}.
	 * @param managedObjectSource
	 *            {@link DeskManagedObjectSourceModel}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link DeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectModel> addDeskManagedObject(String managedObjectName, ManagedObjectScope managedObjectScope,
			DeskManagedObjectSourceModel managedObjectSource, ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link DeskManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link DeskManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link DeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectModel> removeDeskManagedObject(DeskManagedObjectModel managedObject);

	/**
	 * Renames the {@link DeskManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link DeskManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link DeskManagedObjectModel}.
	 * @return {@link Change} to rename the {@link DeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectModel> renameDeskManagedObject(DeskManagedObjectModel managedObject,
			String newManagedObjectName);

	/**
	 * Scopes the {@link DeskManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link DeskManagedObjectModel} to scope.
	 * @param newManagedObjectScope
	 *            New {@link ManagedObjectScope} for the
	 *            {@link DeskManagedObjectModel}.
	 * @return {@link Change} to scope {@link DeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectModel> rescopeDeskManagedObject(DeskManagedObjectModel managedObject,
			ManagedObjectScope newManagedObjectScope);

	/**
	 * Links the {@link ManagedFunctionObjectModel} to be the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param managedFunctionObject
	 *            {@link WorkTaskObjectModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add a
	 *         {@link ManagedFunctionObjectToExternalManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToExternalManagedObjectModel> linkManagedFunctionObjectToExternalManagedObject(
			ManagedFunctionObjectModel managedFunctionTaskObject, ExternalManagedObjectModel externalManagedObject);

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
	 * {@link DeskManagedObjectModel}.
	 *
	 * @param managedFunctionObject
	 *            {@link ManagedFunctionObjectModel}.
	 * @param managedObject
	 *            {@link DeskManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link ManagedFunctionObjectToDeskManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToDeskManagedObjectModel> linkManagedFunctionObjectToDeskManagedObject(
			ManagedFunctionObjectModel managedFunctionTaskObject, DeskManagedObjectModel managedObject);

	/**
	 * Removes the {@link ManagedFunctionObjectToDeskManagedObjectModel}.
	 *
	 * @param managedFunctionObjectToManagedObject
	 *            {@link ManagedFunctionObjectToDeskManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link ManagedFunctionObjectToDeskManagedObjectModel}.
	 */
	Change<ManagedFunctionObjectToDeskManagedObjectModel> removeManagedFunctionObjectToDeskManagedObject(
			ManagedFunctionObjectToDeskManagedObjectModel managedFunctionObjectToManagedObject);

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
	Change<FunctionFlowToFunctionModel> removeFunctionFlowToFunction(FunctionFlowToFunctionModel taskFlowToTask);

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
	 * Links {@link FunctionEscalationModel} to the {@link FunctionModel}.
	 *
	 * @param functionEscalation
	 *            {@link FunctionEscalationModel}.
	 * @param function
	 *            {@link FunctionModel}.
	 * @return {@link Change} to add a
	 *         {@link FunctionEscalationToFunctionModel}.
	 */
	Change<FunctionEscalationToFunctionModel> linkFunctionEscalationToFunction(
			FunctionEscalationModel functionEscalation, FunctionModel function);

	/**
	 * Removes the {@link FunctionEscalationToFunctionModel}.
	 *
	 * @param functionEscalationToFunction
	 *            {@link FunctionEscalationToFunctionModel} to remove.
	 * @return {@link Change} to remove
	 *         {@link FunctionEscalationToFunctionModel}.
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
	 * @return {@link Change} to add
	 *         {@link FunctionEscalationToExternalFlowModel}.
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
	 * Links the {@link DeskManagedObjectSourceFlowModel} to the
	 * {@link FunctionModel}.
	 *
	 * @param managedObjectSourceFlow
	 *            {@link DeskManagedObjectSourceFlowModel}.
	 * @param function
	 *            {@link FunctionModel}.
	 * @return {@link Change} to add the
	 *         {@link DeskManagedObjectSourceFlowToFunctionModel}.
	 */
	Change<DeskManagedObjectSourceFlowToFunctionModel> linkDeskManagedObjectSourceFlowToFunction(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow, FunctionModel function);

	/**
	 * Removes the {@link DeskManagedObjectSourceFlowToFunctionModel}.
	 *
	 * @param managedObjectSourceFlowToFunction
	 *            {@link DeskManagedObjectSourceFlowToFunctionModel} to be
	 *            removed.
	 * @return {@link Change} to remove the
	 *         {@link DeskManagedObjectSourceFlowToFunctionModel}.
	 */
	Change<DeskManagedObjectSourceFlowToFunctionModel> removeDeskManagedObjectSourceFlowToFunction(
			DeskManagedObjectSourceFlowToFunctionModel managedObjectSourceFlowToFunction);

	/**
	 * Links the {@link DeskManagedObjectSourceFlowModel} to the
	 * {@link ExternalFlowModel}.
	 *
	 * @param managedObjectSourceFlow
	 *            {@link DeskManagedObjectSourceFlowModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @return {@link Change} to add the
	 *         {@link DeskManagedObjectSourceFlowToExternalFlowModel}.
	 */
	Change<DeskManagedObjectSourceFlowToExternalFlowModel> linkDeskManagedObjectSourceFlowToExternalFlow(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow, ExternalFlowModel externalFlow);

	/**
	 * Removes the {@link DeskManagedObjectSourceFlowToExternalFlowModel}.
	 *
	 * @param managedObjectSourceFlowToExternalFlow
	 *            {@link DeskManagedObjectSourceFlowToExternalFlowModel} to be
	 *            removed.
	 * @return {@link Change} to remove the
	 *         {@link DeskManagedObjectSourceFlowToExternalFlowModel}.
	 */
	Change<DeskManagedObjectSourceFlowToExternalFlowModel> removeDeskManagedObjectSourceFlowToExternalFlow(
			DeskManagedObjectSourceFlowToExternalFlowModel managedObjectSourceFlowToExternalFlow);

	/**
	 * Links the {@link DeskManagedObjectDependencyModel} to the
	 * {@link DeskManagedObjectModel}.
	 *
	 * @param dependency
	 *            {@link DeskManagedObjectDependencyModel}.
	 * @param managedObject
	 *            {@link DeskManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link DeskManagedObjectDependencyToDeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectDependencyToDeskManagedObjectModel> linkDeskManagedObjectDependencyToDeskManagedObject(
			DeskManagedObjectDependencyModel dependency, DeskManagedObjectModel managedObject);

	/**
	 * Removes the {@link DeskManagedObjectDependencyToDeskManagedObjectModel}.
	 *
	 * @param dependencyToManagedObject
	 *            {@link DeskManagedObjectDependencyToDeskManagedObjectModel} to
	 *            be removed.
	 * @return {@link Change} to remove the
	 *         {@link DeskManagedObjectDependencyToDeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectDependencyToDeskManagedObjectModel> removeDeskManagedObjectDependencyToDeskManagedObject(
			DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToManagedObject);

	/**
	 * Links the {@link DeskManagedObjectDependencyModel} to the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param dependency
	 *            {@link DeskManagedObjectDependencyModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link DeskManagedObjectDependencyToExternalManagedObjectModel}.
	 */
	Change<DeskManagedObjectDependencyToExternalManagedObjectModel> linkDeskManagedObjectDependencyToExternalManagedObject(
			DeskManagedObjectDependencyModel dependency, ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the
	 * {@link DeskManagedObjectDependencyToExternalManagedObjectModel}.
	 *
	 * @param dependencyToExternalManagedObject
	 *            {@link DeskManagedObjectDependencyToExternalManagedObjectModel}
	 *            to be removed.
	 * @return {@link Change} to remove the
	 *         {@link DeskManagedObjectDependencyToExternalManagedObjectModel}.
	 */
	Change<DeskManagedObjectDependencyToExternalManagedObjectModel> removeDeskManagedObjectDependencyToExternalManagedObject(
			DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExternalManagedObject);

}