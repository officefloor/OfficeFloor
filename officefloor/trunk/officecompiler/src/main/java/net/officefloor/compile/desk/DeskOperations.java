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
package net.officefloor.compile.desk;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * Changes that can be made to a {@link DeskModel}.
 * 
 * @author Daniel
 */
public interface DeskOperations {

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
	final String ASYNCHRONOUS_LINK = FlowInstigationStrategyEnum.ASYNCHRONOUS
			.name();

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

	<W extends Work> Change<WorkModel> conformWork(WorkModel workModel,
			WorkType<W> workType);

	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			WorkModel workModel, TaskType<W, D, F> taskType);

	Change<WorkTaskModel> removeWorkTask(WorkModel workModel,
			WorkTaskModel taskModel);

	Change<WorkTaskModel> setObjectAsParameter(boolean isParameter,
			String objectName, WorkTaskModel workTaskModel);

	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<TaskModel> addTask(
			String taskName, WorkTaskModel workTaskModel,
			TaskType<W, D, F> taskType);

	Change<TaskModel> removeTask(TaskModel taskModel);

	Change<TaskModel> renameTask(TaskModel taskModel, String newTaskName);

	Change<TaskModel> setTaskAsPublic(boolean isPublic, TaskModel taskModel);

	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> conformTask(
			WorkTaskModel taskModel, TaskType<W, D, F> taskType);

	Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType);

	Change<ExternalFlowModel> removeExternalFlow(ExternalFlowModel externalFlow);

	Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String argumentType);

	Change<ExternalManagedObjectModel> removeExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject);

}