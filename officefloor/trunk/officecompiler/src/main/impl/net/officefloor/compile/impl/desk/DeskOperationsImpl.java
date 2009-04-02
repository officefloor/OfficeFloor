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
package net.officefloor.compile.impl.desk;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.desk.DeskOperations;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * {@link DeskOperations} implementation.
 * 
 * @author Daniel
 */
public class DeskOperationsImpl implements DeskOperations {

	/**
	 * {@link DeskModel}.
	 */
	private final DeskModel desk;

	/**
	 * Initiate.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 */
	public DeskOperationsImpl(DeskModel desk) {
		this.desk = desk;
	}

	/*
	 * ==================== DeskOperations =================================
	 */

	@Override
	public <W extends Work> Change<WorkModel> addWork(String workName,
			WorkType<W> workType, String... taskNames) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addWork");
	}

	@Override
	public Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addExternalFlow");
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String argumentType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addExternalManagedObject");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<TaskModel> addTask(
			String taskName, WorkTaskModel workTaskModel,
			TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addTask");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			WorkModel workModel, TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.addWorkTask");
	}

	@Override
	public <W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> conformTask(
			WorkTaskModel taskModel, TaskType<W, D, F> taskType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.conformTask");
	}

	@Override
	public <W extends Work> Change<WorkModel> conformWork(WorkModel workModel,
			WorkType<W> workType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.conformWork");
	}

	@Override
	public Change<ExternalFlowModel> removeExternalFlow(
			ExternalFlowModel externalFlow) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeExternalFlow");
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeExternalManagedObject");
	}

	@Override
	public Change<TaskModel> removeTask(TaskModel taskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeTask");
	}

	@Override
	public Change<WorkModel> removeWork(WorkModel workModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeWork");
	}

	@Override
	public Change<WorkTaskModel> removeWorkTask(WorkModel workModel,
			WorkTaskModel taskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.removeWorkTask");
	}

	@Override
	public Change<TaskModel> renameTask(TaskModel taskModel, String newTaskName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.renameTask");
	}

	@Override
	public Change<WorkModel> renameWork(WorkModel workModel, String newWorkName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.renameWork");
	}

	@Override
	public Change<WorkTaskModel> setObjectAsParameter(boolean isParameter,
			String objectName, WorkTaskModel workTaskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.setObjectAsParameter");
	}

	@Override
	public Change<TaskModel> setTaskAsPublic(boolean isPublic,
			TaskModel taskModel) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement DeskOperations.setTaskAsPublic");
	}

}