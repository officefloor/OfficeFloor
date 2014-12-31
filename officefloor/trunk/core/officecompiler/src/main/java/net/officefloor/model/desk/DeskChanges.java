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

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link DeskModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface DeskChanges {

	/**
	 * Value for {@link FlowInstigationStrategyEnum#SEQUENTIAL} on the
	 * {@link DeskModel} {@link ConnectionModel} instances.
	 */
	String SEQUENTIAL_LINK = FlowInstigationStrategyEnum.SEQUENTIAL.name();

	/**
	 * Value for {@link FlowInstigationStrategyEnum#PARALLEL} on the
	 * {@link DeskModel} {@link ConnectionModel} instances.
	 */
	String PARALLEL_LINK = FlowInstigationStrategyEnum.PARALLEL.name();

	/**
	 * Value for {@link FlowInstigationStrategyEnum#ASYNCHRONOUS} on the
	 * {@link DeskModel} {@link ConnectionModel} instances.
	 */
	String ASYNCHRONOUS_LINK = FlowInstigationStrategyEnum.ASYNCHRONOUS.name();

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
	 * Value for {@link ManagedObjectScope#WORK} on
	 * {@link DeskManagedObjectModel} instances.
	 */
	String WORK_MANAGED_OBJECT_SCOPE = ManagedObjectScope.WORK.name();

	/**
	 * Adds a {@link WorkModel} to the {@link DeskModel}.
	 *
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workSourceClassName
	 *            Fully qualified name of the {@link WorkSource}.
	 * @param properties
	 *            {@link PropertyList} to configure the {@link WorkSource}.
	 * @param workType
	 *            {@link WorkType} from the {@link WorkSource}.
	 * @param taskNames
	 *            Listing of {@link WorkTaskModel} names to be loaded. Empty
	 *            list results in loading all {@link WorkTaskModel} instances
	 *            for the {@link WorkType}.
	 * @return {@link Change} to add the {@link WorkModel}.
	 */
	<W extends Work> Change<WorkModel> addWork(String workName,
			String workSourceClassName, PropertyList properties,
			WorkType<W> workType, String... taskNames);

	/**
	 * Removes a {@link WorkModel} from the {@link DeskModel}.
	 *
	 * @param workModel
	 *            {@link WorkModel} to be removed.
	 * @return {@link Change} to remove the {@link WorkModel}.
	 */
	Change<WorkModel> removeWork(WorkModel workModel);

	/**
	 * Renames the {@link WorkModel}.
	 *
	 * @param workModel
	 *            {@link WorkModel} to rename.
	 * @param newWorkName
	 *            New name for the {@link WorkModel}.
	 * @return {@link Change} to rename the {@link WorkModel}.
	 */
	Change<WorkModel> renameWork(WorkModel workModel, String newWorkName);

	/**
	 * Refactors the {@link WorkModel}.
	 *
	 * @param workModel
	 *            {@link WorkModel} to refactor.
	 * @param workName
	 *            New name for the {@link WorkModel}.
	 * @param workSourceClassName
	 *            New {@link WorkSource} class name for the {@link WorkModel}.
	 * @param properties
	 *            New {@link PropertyList} for the {@link WorkModel}.
	 * @param workType
	 *            {@link WorkType} that the {@link WorkModel} is being
	 *            refactored to.
	 * @param workTaskNameMapping
	 *            Mapping of the {@link TaskType} name to the
	 *            {@link WorkTaskModel} name.
	 * @param workTaskToObjectNameMapping
	 *            Mapping of the {@link WorkTaskModel} name to the
	 *            {@link TaskObjectType} name to the {@link WorkTaskObjectModel}
	 *            name.
	 * @param taskToFlowNameMapping
	 *            Mapping of the {@link TaskModel} name to the
	 *            {@link TaskFlowType} name to the {@link TaskFlowModel} name.
	 * @param taskToEscalationTypeMapping
	 *            Mapping of the {@link TaskModel} name to the
	 *            {@link TaskEscalationType} type to the
	 *            {@link TaskEscalationModel} type.
	 * @param taskNames
	 *            Listing of {@link WorkTaskModel} names to be loaded. Empty
	 *            list results in loading all {@link WorkTaskModel} instances
	 *            for the {@link WorkType}.
	 * @return {@link Change} to refactor the {@link WorkModel}.
	 */
	<W extends Work> Change<WorkModel> refactorWork(WorkModel workModel,
			String workName, String workSourceClassName,
			PropertyList properties, WorkType<W> workType,
			Map<String, String> workTaskNameMapping,
			Map<String, Map<String, String>> workTaskToObjectNameMapping,
			Map<String, Map<String, String>> taskToFlowNameMapping,
			Map<String, Map<String, String>> taskToEscalationTypeMapping,
			String... taskNames);

	/**
	 * Adds the {@link TaskType} as a {@link WorkTaskModel} to the
	 * {@link WorkModel}.
	 *
	 * @param workModel
	 *            {@link WorkModel} to have the {@link TaskType} added.
	 * @param taskType
	 *            {@link TaskType} to be added to the {@link WorkModel}.
	 * @return {@link Change} to add the {@link TaskType} to the
	 *         {@link WorkModel}.
	 */
	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			WorkModel workModel, TaskType<W, D, F> taskType);

	/**
	 * Removes the {@link WorkTaskModel} from the {@link WorkModel}.
	 *
	 * @param workModel
	 *            {@link WorkModel} to have the {@link WorkTaskModel} removed.
	 * @param taskModel
	 *            {@link WorkTaskModel} to be removed.
	 * @return {@link Change} to remove the {@link WorkTaskModel} from the
	 *         {@link WorkModel}.
	 */
	Change<WorkTaskModel> removeWorkTask(WorkModel workModel,
			WorkTaskModel taskModel);

	/**
	 * Adds a {@link TaskType} as a {@link TaskModel} to the {@link DeskModel}.
	 *
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param workTaskModel
	 *            {@link WorkTaskModel} for the {@link TaskType}.
	 * @param taskType
	 *            {@link TaskType} for the {@link TaskModel}.
	 * @return {@link Change} to add the {@link TaskType} to the
	 *         {@link DeskModel}.
	 */
	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<TaskModel> addTask(
			String taskName, WorkTaskModel workTaskModel,
			TaskType<W, D, F> taskType);

	/**
	 * Removes the {@link TaskModel} from the {@link DeskModel}.
	 *
	 * @param taskModel
	 *            {@link TaskModel} to be removed.
	 * @return {@link Change} to remove the {@link TaskModel} from the
	 *         {@link DeskModel}.
	 */
	Change<TaskModel> removeTask(TaskModel taskModel);

	/**
	 * Renames the {@link TaskModel}.
	 *
	 * @param taskModel
	 *            {@link TaskModel} to be renamed.
	 * @param newTaskName
	 *            New name for the {@link TaskModel}.
	 * @return {@link Change} to rename the {@link TaskModel}.
	 */
	Change<TaskModel> renameTask(TaskModel taskModel, String newTaskName);

	/**
	 * Specifies a {@link WorkTaskObjectModel} as a parameter or an object.
	 *
	 * @param isParameter
	 *            <code>true</code> for the {@link WorkTaskObjectModel} to be a
	 *            parameter. <code>false</code> to be a dependency object.
	 * @param taskObjectModel
	 *            {@link WorkTaskObjectModel} to set as a parameter or object.
	 * @return {@link Change} to set the {@link WorkTaskObjectModel} as a
	 *         parameter or object.
	 */
	Change<WorkTaskObjectModel> setObjectAsParameter(boolean isParameter,
			WorkTaskObjectModel taskObjectModel);

	/**
	 * Specifies a {@link TaskModel} as public/private.
	 *
	 * @param isPublic
	 *            <code>true</code> for the {@link TaskModel} to be public.
	 *            <code>false</code> for the {@link TaskModel} to be private.
	 * @param taskModel
	 *            {@link TaskModel} to set public/private.
	 * @return {@link Change} to set the {@link TaskModel} public/private.
	 */
	Change<TaskModel> setTaskAsPublic(boolean isPublic, TaskModel taskModel);

	/**
	 * Adds an {@link ExternalFlowModel} to the {@link DeskModel}.
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
	Change<ExternalFlowModel> renameExternalFlow(
			ExternalFlowModel externalFlow, String newExternalFlowName);

	/**
	 * Adds an {@link ExternalManagedObjectModel} to the {@link DeskModel}.
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
	 * Removes an {@link ExternalManagedObjectModel} from the {@link DeskModel}.
	 *
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel} to remove from the
	 *            {@link DeskModel}.
	 * @return {@link Change} to remove the {@link ExternalManagedObjectModel}
	 *         from the {@link DeskModel}.
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
	Change<DeskManagedObjectSourceModel> addDeskManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			long timeout, ManagedObjectType<?> managedObjectType);

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
	Change<DeskManagedObjectSourceModel> renameDeskManagedObjectSource(
			DeskManagedObjectSourceModel managedObjectSource,
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
	Change<DeskManagedObjectModel> addDeskManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			DeskManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType);

	/**
	 * Removes the {@link DeskManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link DeskManagedObjectModel} to remove.
	 * @return {@link Change} to remove the {@link DeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectModel> removeDeskManagedObject(
			DeskManagedObjectModel managedObject);

	/**
	 * Renames the {@link DeskManagedObjectModel}.
	 *
	 * @param managedObject
	 *            {@link DeskManagedObjectModel} to rename.
	 * @param newManagedObjectName
	 *            New name for the {@link DeskManagedObjectModel}.
	 * @return {@link Change} to rename the {@link DeskManagedObjectModel}.
	 */
	Change<DeskManagedObjectModel> renameDeskManagedObject(
			DeskManagedObjectModel managedObject, String newManagedObjectName);

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
	Change<DeskManagedObjectModel> rescopeDeskManagedObject(
			DeskManagedObjectModel managedObject,
			ManagedObjectScope newManagedObjectScope);

	/**
	 * Links the {@link WorkTaskObjectModel} to be the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param workTaskObject
	 *            {@link WorkTaskObjectModel}.
	 * @param externalManagedObject
	 *            {@link ExternalManagedObjectModel}.
	 * @return {@link Change} to add a
	 *         {@link WorkTaskObjectToExternalManagedObjectModel}.
	 */
	Change<WorkTaskObjectToExternalManagedObjectModel> linkWorkTaskObjectToExternalManagedObject(
			WorkTaskObjectModel workTaskObject,
			ExternalManagedObjectModel externalManagedObject);

	/**
	 * Removes the {@link WorkTaskObjectToExternalManagedObjectModel}.
	 *
	 * @param objectToExternalManagedObject
	 *            {@link WorkTaskObjectToExternalManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link WorkTaskObjectToExternalManagedObjectModel}.
	 */
	Change<WorkTaskObjectToExternalManagedObjectModel> removeWorkTaskObjectToExternalManagedObject(
			WorkTaskObjectToExternalManagedObjectModel objectToExternalManagedObject);

	/**
	 * Links the {@link WorkTaskObjectModel} to the
	 * {@link DeskManagedObjectModel}.
	 *
	 * @param workTaskObject
	 *            {@link WorkTaskObjectModel}.
	 * @param managedObject
	 *            {@link DeskManagedObjectModel}.
	 * @return {@link Change} to add the
	 *         {@link WorkTaskObjectToDeskManagedObjectModel}.
	 */
	Change<WorkTaskObjectToDeskManagedObjectModel> linkWorkTaskObjectToDeskManagedObject(
			WorkTaskObjectModel workTaskObject,
			DeskManagedObjectModel managedObject);

	/**
	 * Removes the {@link WorkTaskObjectToDeskManagedObjectModel}.
	 *
	 * @param workTaskObjectToManagedObject
	 *            {@link WorkTaskObjectToDeskManagedObjectModel} to remove.
	 * @return {@link Change} to remove the
	 *         {@link WorkTaskObjectToDeskManagedObjectModel}.
	 */
	Change<WorkTaskObjectToDeskManagedObjectModel> removeWorkTaskObjectToDeskManagedObject(
			WorkTaskObjectToDeskManagedObjectModel workTaskObjectToManagedObject);

	/**
	 * Links the {@link TaskFlowModel} to the {@link TaskModel}.
	 *
	 * @param taskFlow
	 *            {@link TaskFlowModel}.
	 * @param task
	 *            {@link TaskModel}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @return {@link Change} to add a {@link TaskFlowToTaskModel}.
	 */
	Change<TaskFlowToTaskModel> linkTaskFlowToTask(TaskFlowModel taskFlow,
			TaskModel task, FlowInstigationStrategyEnum instigationStrategy);

	/**
	 * Removes the {@link TaskFlowToTaskModel}.
	 *
	 * @param taskFlowToTask
	 *            {@link TaskFlowToTaskModel} to remove.
	 * @return {@link Change} to remove {@link TaskFlowToTaskModel}.
	 */
	Change<TaskFlowToTaskModel> removeTaskFlowToTask(
			TaskFlowToTaskModel taskFlowToTask);

	/**
	 * Links the {@link TaskFlowModel} to the {@link ExternalFlowModel}.
	 *
	 * @param taskFlow
	 *            {@link TaskFlowModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @return {@link Change} to add a {@link TaskFlowToExternalFlowModel}.
	 */
	Change<TaskFlowToExternalFlowModel> linkTaskFlowToExternalFlow(
			TaskFlowModel taskFlow, ExternalFlowModel externalFlow,
			FlowInstigationStrategyEnum instigationStrategy);

	/**
	 * Removes the {@link TaskFlowToExternalFlowModel}.
	 *
	 * @param taskFlowToExternalFlow
	 *            {@link TaskFlowToExternalFlowModel} to remove.
	 * @return {@link Change} to remove {@link TaskFlowToExternalFlowModel}.
	 */
	Change<TaskFlowToExternalFlowModel> removeTaskFlowToExternalFlow(
			TaskFlowToExternalFlowModel taskFlowToExternalFlow);

	/**
	 * Links {@link TaskModel} to next {@link TaskModel}.
	 *
	 * @param task
	 *            {@link TaskModel}.
	 * @param nextTask
	 *            Next {@link TaskModel}.
	 * @return {@link Change} to add a {@link TaskToNextTaskModel}.
	 */
	Change<TaskToNextTaskModel> linkTaskToNextTask(TaskModel task,
			TaskModel nextTask);

	/**
	 * Removes the {@link TaskToNextTaskModel}.
	 *
	 * @param taskToNextTask
	 *            {@link TaskToNextTaskModel} to remove.
	 * @return {@link Change} to remove {@link TaskToNextTaskModel}.
	 */
	Change<TaskToNextTaskModel> removeTaskToNextTask(
			TaskToNextTaskModel taskToNextTask);

	/**
	 * Links {@link TaskModel} to next {@link ExternalFlowModel}.
	 *
	 * @param task
	 *            {@link TaskModel}.
	 * @param nextExternalFlow
	 *            Next {@link ExternalFlowModel}.
	 * @return {@link Change} to add a {@link TaskToNextExternalFlowModel}.
	 */
	Change<TaskToNextExternalFlowModel> linkTaskToNextExternalFlow(
			TaskModel task, ExternalFlowModel nextExternalFlow);

	/**
	 * Removes the {@link TaskToNextExternalFlowModel}.
	 *
	 * @param taskToNextExternalFlow
	 *            {@link TaskToNextExternalFlowModel} to remove.
	 * @return {@link Change} to remove {@link TaskToNextExternalFlowModel}.
	 */
	Change<TaskToNextExternalFlowModel> removeTaskToNextExternalFlow(
			TaskToNextExternalFlowModel taskToNextExternalFlow);

	/**
	 * Links {@link TaskEscalationModel} to the {@link TaskModel}.
	 *
	 * @param taskEscalation
	 *            {@link TaskEscalationModel}.
	 * @param task
	 *            {@link TaskModel}.
	 * @return {@link Change} to add a {@link TaskEscalationToTaskModel}.
	 */
	Change<TaskEscalationToTaskModel> linkTaskEscalationToTask(
			TaskEscalationModel taskEscalation, TaskModel task);

	/**
	 * Removes the {@link TaskEscalationToTaskModel}.
	 *
	 * @param taskEscalationToTask
	 *            {@link TaskEscalationToTaskModel} to remove.
	 * @return {@link Change} to remove {@link TaskEscalationToTaskModel}.
	 */
	Change<TaskEscalationToTaskModel> removeTaskEscalationToTask(
			TaskEscalationToTaskModel taskEscalationToTask);

	/**
	 * Links {@link TaskEscalationModel} to the {@link ExternalFlowModel}.
	 *
	 * @param taskEscalation
	 *            {@link TaskEscalationModel}.
	 * @param externalFlow
	 *            {@link ExternalFlowModel}.
	 * @return {@link Change} to add {@link TaskEscalationToExternalFlowModel}.
	 */
	Change<TaskEscalationToExternalFlowModel> linkTaskEscalationToExternalFlow(
			TaskEscalationModel taskEscalation, ExternalFlowModel externalFlow);

	/**
	 * Removes the {@link TaskEscalationToExternalFlowModel}.
	 *
	 * @param taskEscalationToExternalFlow
	 *            {@link TaskEscalationToExternalFlowModel} to remove.
	 * @return {@link Change} to remove
	 *         {@link TaskEscalationToExternalFlowModel}.
	 */
	Change<TaskEscalationToExternalFlowModel> removeTaskEscalationToExternalFlow(
			TaskEscalationToExternalFlowModel taskEscalationToExternalFlow);

	/**
	 * Links the {@link WorkModel} to its initial {@link TaskModel}.
	 *
	 * @param work
	 *            {@link WorkModel}.
	 * @param initialTask
	 *            Initial {@link TaskModel}.
	 * @return {@link Change} to add a {@link WorkToInitialTaskModel}.
	 */
	Change<WorkToInitialTaskModel> linkWorkToInitialTask(WorkModel work,
			TaskModel initialTask);

	/**
	 * Removes the {@link WorkToInitialTaskModel}.
	 *
	 * @param workToInitialTask
	 *            {@link WorkToInitialTaskModel} to remove.
	 * @return {@link Change} to remove {@link WorkToInitialTaskModel}.
	 */
	Change<WorkToInitialTaskModel> removeWorkToInitialTask(
			WorkToInitialTaskModel workToInitialTask);

	/**
	 * Links the {@link DeskManagedObjectSourceFlowModel} to the
	 * {@link TaskModel}.
	 *
	 * @param managedObjectSourceFlow
	 *            {@link DeskManagedObjectSourceFlowModel}.
	 * @param task
	 *            {@link TaskModel}.
	 * @return {@link Change} to add the
	 *         {@link DeskManagedObjectSourceFlowToTaskModel}.
	 */
	Change<DeskManagedObjectSourceFlowToTaskModel> linkDeskManagedObjectSourceFlowToTask(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow,
			TaskModel task);

	/**
	 * Removes the {@link DeskManagedObjectSourceFlowToTaskModel}.
	 *
	 * @param managedObjectSourceFlowToTask
	 *            {@link DeskManagedObjectSourceFlowToTaskModel} to be removed.
	 * @return {@link Change} to remove the
	 *         {@link DeskManagedObjectSourceFlowToTaskModel}.
	 */
	Change<DeskManagedObjectSourceFlowToTaskModel> removeDeskManagedObjectSourceFlowToTask(
			DeskManagedObjectSourceFlowToTaskModel managedObjectSourceFlowToTask);

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
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow,
			ExternalFlowModel externalFlow);

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
			DeskManagedObjectDependencyModel dependency,
			DeskManagedObjectModel managedObject);

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
			DeskManagedObjectDependencyModel dependency,
			ExternalManagedObjectModel externalManagedObject);

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