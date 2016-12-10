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
package net.officefloor.eclipse.desk.operations;

import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.eclipse.desk.editparts.WorkTaskEditPart;
import net.officefloor.eclipse.util.ModelUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

import org.eclipse.gef.EditPart;

/**
 * Creates a {@link TaskModel} from a {@link WorkTaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class CreateTaskFromWorkTaskOperation extends
		AbstractDeskChangeOperation<WorkTaskEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public CreateTaskFromWorkTaskOperation(DeskChanges deskChanges) {
		super("Add as task", WorkTaskEditPart.class, deskChanges);
	}

	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the work task to create task
		WorkTaskEditPart editPart = context.getEditPart();
		WorkTaskModel workTask = editPart.getCastedModel();

		// Obtain the work type
		EditPart workEditPart = editPart.getParent();
		WorkModel work = (WorkModel) workEditPart.getModel();

		// Obtain the work type
		WorkType<?> workType = ModelUtil
				.getWorkType(work, editPart.getEditor());
		if (workType == null) {
			return null; // must have work type
		}

		// Obtain the task type
		TaskType<?, ?, ?> taskType = null;
		String taskName = workTask.getWorkTaskName();
		for (TaskType<?, ?, ?> task : workType.getTaskTypes()) {
			if (taskName.equals(task.getTaskName())) {
				taskType = task;
			}
		}
		if (taskType == null) {
			editPart.messageError("Task " + taskName
					+ " is not on work.\n\nPlease conform work.");
			return null; // must have task type
		}

		// Create the change to add the task
		Change<TaskModel> change = changes.addTask(workTask.getWorkTaskName(),
				workTask, taskType);

		// Position the task
		TaskModel task = change.getTarget();
		context.positionModel(task);
		task.setX(task.getX() + 100); // position task to right of work task

		// Return the change
		return change;
	}

}