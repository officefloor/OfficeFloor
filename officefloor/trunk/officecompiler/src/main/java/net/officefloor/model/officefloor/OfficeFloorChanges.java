/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.model.officefloor;

import java.util.Map;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to an {@link OfficeFloorModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorChanges {

	/**
	 * Seperator between Section name and Input Name.
	 */
	char SECTION_INPUT_SEPARATOR = ':';

	/**
	 * Value for {@link ManagedObjectScope#PROCESS} on
	 * {@link OfficeFloorManagedObjectModel} instances.
	 */
	String PROCESS_MANAGED_OBJECT_SCOPE = ManagedObjectScope.PROCESS.name();

	/**
	 * Value for {@link ManagedObjectScope#THREAD} on
	 * {@link OfficeFloorManagedObjectModel} instances.
	 */
	String THREAD_MANAGED_OBJECT_SCOPE = ManagedObjectScope.THREAD.name();

	/**
	 * Value for {@link ManagedObjectScope#WORK} on
	 * {@link OfficeFloorManagedObjectModel} instances.
	 */
	String WORK_MANAGED_OBJECT_SCOPE = ManagedObjectScope.WORK.name();

	/**
	 * Adds a {@link OfficeFloorTeamModel} to the {@link OfficeFloorModel}.
	 *
	 * @param teamName
	 *            Name of the {@link OfficeFloorTeamModel}.
	 * @param teamSourceClassName
	 *            Class name for the {@link TeamSource}.
	 * @param propertylist
	 *            {@link PropertyList}.
	 * @param teamType
	 *            {@link TeamType}.
	 * @return {@link Change} to add the {@link OfficeFloorTeamModel}.
	 */
	Change<OfficeFloorTeamModel> addOfficeFloorTeam(String teamName,
			String teamSourceClassName, PropertyList propertylist,
			TeamType teamType);

	/**
	 * Removes the {@link OfficeFloorTeamModel}.
	 *
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeamModel} to remove.
	 * @return {@link Change} to remove the {@link OfficeFloorTeamModel}.
	 */
	Change<OfficeFloorTeamModel> removeOfficeFloorTeam(
			OfficeFloorTeamModel officeFloorTeam);

	/**
	 * Renames the {@link OfficeFloorTeamModel}.
	 *
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeamModel} to rename.
	 * @param newOfficeFloorTeamName
	 *            New name for the {@link OfficeFloorTeamModel}.
	 * @return {@link Change} to rename the {@link OfficeFloorTeamModel}.
	 */
	Change<OfficeFloorTeamModel> renameOfficeFloorTeam(
			OfficeFloorTeamModel officeFloorTeam, String newOfficeFloorTeamName);

	/**
	 * Adds a {@link DeployedOfficeModel} to the {@link OfficeFloorModel}.
	 *
	 * @param officeName
	 *            Name of the {@link DeployedOfficeModel}.
	 * @param officeSourceClassName
	 *            Class name of the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param officeType
	 *            {@link OfficeType}.
	 * @return {@link Change} to add the {@link DeployedOfficeModel}.
	 */
	Change<DeployedOfficeModel> addDeployedOffice(String officeName,
			String officeSourceClassName, String officeLocation,
			PropertyList propertyList, OfficeType officeType);

	/**
	 * Removes the {@link DeployedOfficeModel}.
	 *
	 * @param deployedOffice
	 *            {@link DeployedOfficeModel} to remove.
	 * @return {@link Change} to remove the {@link DeployedOfficeModel}.
	 */
	Change<DeployedOfficeModel> removeDeployedOffice(
			DeployedOfficeModel deployedOffice);

	/**
	 * Renames the {@link DeployedOfficeModel}.
	 *
	 * @param deployedOffice
	 *            {@link DeployedOfficeModel} to rename.
	 * @param newDeployedOfficeName
	 *            New name for the {@link DeployedOfficeModel}.
	 * @return {@link Change} to rename the {@link DeployedOfficeModel}.
	 */
	Change<DeployedOfficeModel> renameDeployedOffice(
			DeployedOfficeModel deployedOffice, String newDeployedOfficeName);

	/**
	 * Refactors the {@link DeployedOfficeModel}.
	 *
	 * @param deployedOffice
	 *            {@link DeployedOfficeModel} to refactor.
	 * @param officeName
	 *            Name for the {@link DeployedOfficeModel}.
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name for the
	 *            {@link DeployedOfficeModel}.
	 * @param officeLocation
	 *            Location of the {@link DeployedOfficeModel}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param officeType
	 *            {@link OfficeType} to refactor the {@link DeployedOfficeModel}
	 *            to.
	 * @param objectNameMapping
	 *            Mapping of {@link OfficeManagedObjectType} name to
	 *            {@link DeployedOfficeObjectModel} name.
	 * @param inputNameMapping
	 *            Mapping of {@link OfficeInputType} name to
	 *            {@link DeployedOfficeInputModel} name.
	 * @param teamNameMapping
	 *            Mapping of {@link OfficeTeamType} name to
	 *            {@link DeployedOfficeTeamModel} name.
	 * @return {@link Change} to refactor the {@link DeployedOfficeModel}.
	 */
	Change<DeployedOfficeModel> refactorDeployedOffice(
			DeployedOfficeModel deployedOffice, String officeName,
			String officeSourceClassName, String officeLocation,
			PropertyList properties, OfficeType officeType,
			Map<String, String> objectNameMapping,
			Map<String, String> inputNameMapping,
			Map<String, String> teamNameMapping);

	/**
	 * Adds an {@link OfficeFloorManagedObjectSourceModel} to the
	 * {@link OfficeFloorModel}.
	 *
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSourceModel}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param timeout
	 *            Timeout of the {@link ManagedObject}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFloorManagedObjectSourceModel}.
	 */
	Change<OfficeFloorManagedObjectSourceModel> addOfficeFloorManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			long timeout, ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link OfficeFloorManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectSourceModel}.
	 */
	Change<OfficeFloorManagedObjectSourceModel> removeOfficeFloorManagedObjectSource(
			OfficeFloorManagedObjectSourceModel managedObjectSource);

	/**
	 * Renames the {@link OfficeFloorManagedObjectSourceModel}.
	 *
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel} to rename.
	 * @param newManagedObjectSourceName
	 *            New name for the {@link OfficeFloorManagedObjectSourceModel}.
	 * @return {@link Change} to rename the
	 *         {@link OfficeFloorManagedObjectSourceModel}.
	 */
	Change<OfficeFloorManagedObjectSourceModel> renameOfficeFloorManagedObjectSource(
			OfficeFloorManagedObjectSourceModel managedObjectSource,
			String newManagedObjectSourceName);

	/**
	 * Adds an {@link OfficeFloorManagedObjectModel} for an
	 * {@link OfficeFloorManagedObjectSourceModel} to the
	 * {@link OfficeFloorModel}.
	 *
	 * @param managedObjectName
	 *            Name of the {@link OfficeFloorManagedObjectModel}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the
	 *            {@link OfficeFloorManagedObjectModel}.
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @return {@link Change} to add the {@link OfficeFloorManagedObjectModel}.
	 */
	Change<OfficeFloorManagedObjectModel> addOfficeFloorManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			OfficeFloorManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link OfficeFloorManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link OfficeFloorManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectModel}.
	 */
	Change<OfficeFloorManagedObjectModel> removeOfficeFloorManagedObject(
			OfficeFloorManagedObjectModel managedObject);

	/**
	 * Renames the {@link OfficeFloorManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link OfficeFloorManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link OfficeFloorManagedObjectModel}.
	 * @return {@link Change} to rename the
	 *         {@link OfficeFloorManagedObjectModel}.
	 */
	Change<OfficeFloorManagedObjectModel> renameOfficeFloorManagedObject(
			OfficeFloorManagedObjectModel managedObject,
			String newManagedObjectName);

	/**
	 * Scopes the {@link OfficeFloorManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link OfficeFloorManagedObjectModel} to scope.
	 * @param newManagedObjectScope
	 *            New {@link ManagedObjectScope} for the
	 *            {@link OfficeFloorManagedObjectModel}.
	 * @return {@link Change} to scope {@link OfficeFloorManagedObjectModel}.
	 */
	Change<OfficeFloorManagedObjectModel> rescopeOfficeFloorManagedObject(
			OfficeFloorManagedObjectModel managedObject,
			ManagedObjectScope newManagedObjectScope);

	/**
	 * Links the {@link OfficeFloorManagedObjectSourceModel} to the
	 * {@link DeployedOfficeModel}.
	 *
	 * @param officeFloorManagedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel}.
	 * @param deployedOffice
	 *            {@link DeployedOfficeModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFloorManagedObjectSourceToDeployedOfficeModel}.
	 */
	Change<OfficeFloorManagedObjectSourceToDeployedOfficeModel> linkOfficeFloorManagedObjectSourceToDeployedOffice(
			OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource,
			DeployedOfficeModel deployedOffice);

	/**
	 * Removes the {@link OfficeFloorManagedObjectSourceToDeployedOfficeModel}.
	 *
	 * @param officeFloorManagedObjectSourceToDeployedOffice
	 *            {@link OfficeFloorManagedObjectSourceToDeployedOfficeModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectSourceToDeployedOfficeModel}.
	 */
	Change<OfficeFloorManagedObjectSourceToDeployedOfficeModel> removeOfficeFloorManagedObjectSourceToDeployedOffice(
			OfficeFloorManagedObjectSourceToDeployedOfficeModel officeFloorManagedObjectSourceToDeployedOffice);

	/**
	 * Adds the {@link OfficeFloorInputManagedObjectModel}.
	 *
	 * @param inputManagedObjectName
	 *            Name of the {@link OfficeFloorInputManagedObjectModel}.
	 * @param objectType
	 *            Object type of the {@link OfficeFloorInputManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFloorInputManagedObjectModel}.
	 */
	Change<OfficeFloorInputManagedObjectModel> addOfficeFloorInputManagedObject(
			String inputManagedObjectName, String objectType);

	/**
	 * Renames the {@link OfficeFloorInputManagedObjectModel}.
	 *
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObjectModel} to be renamed.
	 * @param newInputManagedObjectName
	 *            New name for the {@link OfficeFloorInputManagedObjectModel}.
	 * @return {@link Change} to rename the
	 *         {@link OfficeFloorInputManagedObjectModel}.
	 */
	Change<OfficeFloorInputManagedObjectModel> renameOfficeFloorInputManagedObject(
			OfficeFloorInputManagedObjectModel inputManagedObject,
			String newInputManagedObjectName);

	/**
	 * Removes the {@link OfficeFloorInputManagedObjectModel}.
	 *
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorInputManagedObjectModel}.
	 */
	Change<OfficeFloorInputManagedObjectModel> removeOfficeFloorInputManagedObject(
			OfficeFloorInputManagedObjectModel inputManagedObject);

	/**
	 * Links the {@link OfficeFloorManagedObjectSourceModel} to the
	 * {@link OfficeFloorInputManagedObjectModel}.
	 *
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel}.
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObjectModel}.
	 * @return {@link Change} to link the
	 *         {@link OfficeFloorManagedObjectSourceModel} to the
	 *         {@link OfficeFloorInputManagedObjectModel}.
	 */
	Change<OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel> linkOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject(
			OfficeFloorManagedObjectSourceModel managedObjectSource,
			OfficeFloorInputManagedObjectModel inputManagedObject);

	/**
	 * Removes the
	 * {@link OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel}
	 * .
	 *
	 * @param managedObjectSourceToInputManagedObject
	 *            {@link OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel}
	 *         .
	 */
	Change<OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel> removeOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject(
			OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel managedObjectSourceToInputManagedObject);

	/**
	 * Links the {@link OfficeFloorInputManagedObjectModel} to its bound
	 * {@link OfficeFloorManagedObjectSourceModel}.
	 *
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObjectModel}.
	 * @param boundManagedObjectSource
	 *            Bound {@link OfficeFloorManagedObjectSourceModel}.
	 * @return {@link Change} to link the
	 *         {@link OfficeFloorInputManagedObjectModel} to its bound
	 *         {@link OfficeFloorManagedObjectSourceModel}.
	 */
	Change<OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel> linkOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSource(
			OfficeFloorInputManagedObjectModel inputManagedObject,
			OfficeFloorManagedObjectSourceModel boundManagedObjectSource);

	/**
	 * Removes the
	 * {@link OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel}
	 * .
	 *
	 * @param inputManagedObjectToBoundManagedObjectSource
	 *            {@link OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel}
	 *            to be removed.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel}
	 *         .
	 */
	Change<OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel> removeOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSource(
			OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel inputManagedObjectToBoundManagedObjectSource);

	/**
	 * Links the {@link OfficeFloorManagedObjectSourceFlowModel} to the
	 * {@link DeployedOfficeInputModel}.
	 *
	 * @param officeFloorManagedObjectSourceFlow
	 *            {@link OfficeFloorManagedObjectSourceFlowModel}.
	 * @param deployedOfficeInput
	 *            {@link DeployedOfficeInputModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel}
	 */
	Change<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel> linkOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(
			OfficeFloorManagedObjectSourceFlowModel officeFloorManagedObjectSourceFlow,
			DeployedOfficeInputModel deployedOfficeInput);

	/**
	 * Removes the
	 * {@link OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel}.
	 *
	 * @param officeFloorManagedObjectSourceFlow
	 *            {@link OfficeFloorManagedObjectSourceFlowModel}.
	 * @param deployedOfficeInput
	 *            {@link DeployedOfficeInputModel}.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel}
	 */
	Change<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel> removeOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(
			OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel officeFloorManagedObjectSourceFlowToDeployedOfficeInput);

	/**
	 * Links the {@link OfficeFloorManagedObjectSourceTeamModel} to the
	 * {@link OfficeFloorTeamModel}.
	 *
	 * @param officeFloorManagedObjectSourceTeam
	 *            {@link OfficeFloorManagedObjectSourceTeamModel}.
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeamModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel}.
	 */
	Change<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel> linkOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(
			OfficeFloorManagedObjectSourceTeamModel officeFloorManagedObjectSourceTeam,
			OfficeFloorTeamModel officeFloorTeam);

	/**
	 * Removes the
	 * {@link OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel}.
	 *
	 * @param officeFloorManagedObjectSourceTeamToOfficeFloorTeam
	 *            {@link OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel}.
	 */
	Change<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel> removeOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(
			OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel officeFloorManagedObjectSourceTeamToOfficeFloorTeam);

	/**
	 * Links the {@link OfficeFloorManagedObjectDependencyModel} to the
	 * {@link OfficeFloorManagedObjectModel}.
	 *
	 * @param officeFloorManagedObjectDependency
	 *            {@link OfficeFloorManagedObjectDependencyModel}.
	 * @param officeFloorManagedObject
	 *            {@link OfficeFloorManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel}
	 */
	Change<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel> linkOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(
			OfficeFloorManagedObjectDependencyModel officeFloorManagedObjectDependency,
			OfficeFloorManagedObjectModel officeFloorManagedObject);

	/**
	 * Removes the
	 * {@link OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel}
	 *
	 * @param officeFloorManagedObjectDependencyToOfficeFloorManagedObject
	 *            {@link OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel}
	 *            to remove.
	 * @return {@link Change} to remove the
	 *         {@link OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel}
	 */
	Change<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel> removeOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(
			OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel officeFloorManagedObjectDependencyToOfficeFloorManagedObject);

	/**
	 * Links the {@link DeployedOfficeObjectModel} to the
	 * {@link OfficeFloorManagedObjectModel}.
	 *
	 * @param deployedOfficeObject
	 *            {@link DeployedOfficeObjectModel}.
	 * @param officeFloorManagedObject
	 *            {@link OfficeFloorManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link DeployedOfficeObjectToOfficeFloorManagedObjectModel}.
	 */
	Change<DeployedOfficeObjectToOfficeFloorManagedObjectModel> linkDeployedOfficeObjectToOfficeFloorManagedObject(
			DeployedOfficeObjectModel deployedOfficeObject,
			OfficeFloorManagedObjectModel officeFloorManagedObject);

	/**
	 * Removes the {@link DeployedOfficeObjectToOfficeFloorManagedObjectModel}.
	 *
	 * @param deployedOfficeObjectToOfficeFloorManagedObject
	 *            {@link DeployedOfficeObjectToOfficeFloorManagedObjectModel} to
	 *            remove.
	 * @return {@link Change} to remove the
	 *         {@link DeployedOfficeObjectToOfficeFloorManagedObjectModel}.
	 */
	Change<DeployedOfficeObjectToOfficeFloorManagedObjectModel> removeDeployedOfficeObjectToOfficeFloorManagedObject(
			DeployedOfficeObjectToOfficeFloorManagedObjectModel deployedOfficeObjectToOfficeFloorManagedObject);

	/**
	 * Links the {@link DeployedOfficeObjectModel} to the
	 * {@link OfficeFloorInputManagedObjectModel}.
	 *
	 * @param deployedOfficeObject
	 *            {@link DeployedOfficeObjectModel}.
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link DeployedOfficeObjectToOfficeFloorInputManagedObjectModel}.
	 */
	Change<DeployedOfficeObjectToOfficeFloorInputManagedObjectModel> linkDeployedOfficeObjectToOfficeFloorInputManagedObject(
			DeployedOfficeObjectModel deployedOfficeObject,
			OfficeFloorInputManagedObjectModel inputManagedObject);

	/**
	 * Removes the
	 * {@link DeployedOfficeObjectToOfficeFloorInputManagedObjectModel}.
	 *
	 * @param deployedOfficeObjectToInputManagedObject
	 *            {@link DeployedOfficeObjectToOfficeFloorInputManagedObjectModel}
	 *            to be removed.
	 * @return {@link Change} to remove the
	 *         {@link DeployedOfficeObjectToOfficeFloorInputManagedObjectModel}.
	 */
	Change<DeployedOfficeObjectToOfficeFloorInputManagedObjectModel> removeDeployedOfficeObjectToOfficeFloorInputManagedObject(
			DeployedOfficeObjectToOfficeFloorInputManagedObjectModel deployedOfficeObjectToInputManagedObject);

	/**
	 * Links the {@link DeployedOfficeTeamModel} to the
	 * {@link OfficeFloorTeamModel}.
	 *
	 * @param deployedOfficeTeam
	 *            {@link DeployedOfficeTeamModel}.
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeamModel}.
	 * @return {@link Change} to add the
	 *         {@link DeployedOfficeTeamToOfficeFloorTeamModel}.
	 */
	Change<DeployedOfficeTeamToOfficeFloorTeamModel> linkDeployedOfficeTeamToOfficeFloorTeam(
			DeployedOfficeTeamModel deployedOfficeTeam,
			OfficeFloorTeamModel officeFloorTeam);

	/**
	 * Removes the {@link DeployedOfficeTeamToOfficeFloorTeamModel}.
	 *
	 * @param deployedOfficeTeamToOfficeFloorTeam
	 *            {@link DeployedOfficeTeamToOfficeFloorTeamModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link DeployedOfficeTeamToOfficeFloorTeamModel}.
	 */
	Change<DeployedOfficeTeamToOfficeFloorTeamModel> removeDeployedOfficeTeamToOfficeFloorTeam(
			DeployedOfficeTeamToOfficeFloorTeamModel deployedOfficeTeamToOfficeFloorTeam);

}