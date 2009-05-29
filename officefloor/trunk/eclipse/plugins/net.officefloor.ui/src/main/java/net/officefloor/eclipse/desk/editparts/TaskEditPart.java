/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.desk.TaskFigure;
import net.officefloor.eclipse.skin.desk.TaskFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.TaskModel.TaskEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskEditPart extends
		AbstractOfficeFloorEditPart<TaskModel, TaskEvent, TaskFigure> implements
		TaskFigureContext {

	@Override
	protected TaskFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTaskFlows());
		childModels.addAll(this.getCastedModel().getTaskEscalations());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Add task to next task
		TaskToNextTaskModel nextTask = this.getCastedModel().getNextTask();
		if (nextTask != null) {
			models.add(nextTask);
		}

		// Add task to next external flow
		TaskToNextExternalFlowModel nextExternalFlow = this.getCastedModel()
				.getNextExternalFlow();
		if (nextExternalFlow != null) {
			models.add(nextExternalFlow);
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Add work that this is initial task
		EclipseUtil.addToList(models, this.getCastedModel()
				.getInitialTaskForWork());

		// Add work task
		EclipseUtil.addToList(models, this.getCastedModel().getWorkTask());

		// Add task inputs, handled escalations, previous tasks
		models.addAll(this.getCastedModel().getTaskFlowInputs());
		models.addAll(this.getCastedModel().getTaskEscalationInputs());
		models.addAll(this.getCastedModel().getPreviousTasks());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<TaskModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<DeskChanges, TaskModel>() {
			@Override
			public String getInitialValue() {
				return TaskEditPart.this.getCastedModel().getTaskName();
			}

			@Override
			public IFigure getLocationFigure() {
				return TaskEditPart.this.getOfficeFloorFigure()
						.getTaskNameFigure();
			}

			@Override
			public Change<TaskModel> createChange(DeskChanges changes,
					TaskModel target, String newValue) {
				return changes.renameTask(target, newValue);
			}
		});
	}

	@Override
	protected Class<TaskEvent> getPropertyChangeEventType() {
		return TaskEvent.class;
	}

	@Override
	protected void handlePropertyChange(TaskEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_TASK_NAME:
			this.getOfficeFloorFigure().setTaskName(
					this.getCastedModel().getTaskName());
			break;
		case CHANGE_IS_PUBLIC:
			// Ensure display is public
			this.getOfficeFloorFigure().setIsPublic(
					this.getCastedModel().getIsPublic());
			break;
		case CHANGE_NEXT_TASK:
		case CHANGE_NEXT_EXTERNAL_FLOW:
			this.refreshSourceConnections();
			break;
		case CHANGE_INITIAL_TASK_FOR_WORK:
		case CHANGE_WORK_TASK:
		case ADD_TASK_FLOW_INPUT:
		case REMOVE_TASK_FLOW_INPUT:
		case ADD_TASK_ESCALATION_INPUT:
		case REMOVE_TASK_ESCALATION_INPUT:
		case ADD_PREVIOUS_TASK:
		case REMOVE_PREVIOUS_TASK:
			this.refreshTargetConnections();
			break;
		case ADD_TASK_FLOW:
		case REMOVE_TASK_FLOW:
		case ADD_TASK_ESCALATION:
		case REMOVE_TASK_ESCALATION:
			this.refreshChildren();
			break;
		}
	}

	/*
	 * ======================= FlowItemFigureContext ========================
	 */

	@Override
	public String getTaskName() {
		return this.getCastedModel().getTaskName();
	}

	@Override
	public boolean isPublic() {
		return this.getCastedModel().getIsPublic();
	}

	@Override
	public void setIsPublic(final boolean isPublic) {

		// Store current state
		final boolean currentIsPublic = this.getCastedModel().getIsPublic();

		// Make change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				TaskEditPart.this.getCastedModel().setIsPublic(isPublic);
			}

			@Override
			protected void undoCommand() {
				TaskEditPart.this.getCastedModel().setIsPublic(currentIsPublic);
			}
		});
	}

}