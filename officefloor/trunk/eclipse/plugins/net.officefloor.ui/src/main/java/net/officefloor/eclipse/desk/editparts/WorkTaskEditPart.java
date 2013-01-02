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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.OperationUtil;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.desk.operations.CreateTaskFromWorkTaskOperation;
import net.officefloor.eclipse.skin.desk.WorkTaskFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskFigureContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskModel.WorkTaskEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WorkTaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkTaskEditPart
		extends
		AbstractOfficeFloorEditPart<WorkTaskModel, WorkTaskEvent, WorkTaskFigure>
		implements WorkTaskFigureContext {

	/**
	 * Obtains the {@link Work} that contains the {@link WorkTaskModel}.
	 * 
	 * @param workTask
	 *            {@link WorkTaskModel}.
	 * @param desk
	 *            {@link DeskModel} containing the {@link WorkTaskModel}.
	 * @return {@link WorkModel} or <code>null</code> if not contained by a
	 *         {@link WorkModel}.
	 */
	public static WorkModel getWork(WorkTaskModel workTask, DeskModel desk) {
		// Ensure have work task
		if (workTask == null) {
			return null;
		}

		// Obtain the containing work
		for (WorkModel work : desk.getWorks()) {
			for (WorkTaskModel check : work.getWorkTasks()) {
				if (workTask == check) {
					// Found work task, so use the containing work
					return work;
				}
			}
		}

		// As here no containing work
		return null;
	}

	@Override
	protected WorkTaskFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createWorkTaskFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTaskObjects());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getTasks());
	}

	@Override
	protected Class<WorkTaskEvent> getPropertyChangeEventType() {
		return WorkTaskEvent.class;
	}

	@Override
	protected void handlePropertyChange(WorkTaskEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_TASK:
		case REMOVE_TASK:
			WorkTaskEditPart.this.refreshSourceConnections();
			break;

		case ADD_TASK_OBJECT:
		case REMOVE_TASK_OBJECT:
			WorkTaskEditPart.this.refreshChildren();
			break;

		case CHANGE_WORK_TASK_NAME:
			this.getOfficeFloorFigure().setWorkTaskName(this.getWorkTaskName());
			break;
		}
	}

	/*
	 * ============ DeskTaskFigureContext ============
	 */

	@Override
	public String getWorkTaskName() {
		return this.getCastedModel().getWorkTaskName();
	}

	@Override
	public void createAsNewTask() {

		// Obtain the desk changes
		DeskChanges deskChanges = (DeskChanges) this.getEditor()
				.getModelChanges();

		// Execute operation to add as task (to right of this work task)
		OperationUtil.execute(new CreateTaskFromWorkTaskOperation(deskChanges),
				this.getCastedModel().getX() + 100, this.getCastedModel()
						.getY(), this);
	}

}