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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.desk.operations.RemoveFlowItemOperation;
import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.desk.TaskModel.TaskEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link TaskModel}.
 * 
 * @author Daniel
 */
// TODO rename to TaskEditPart
public class FlowItemEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<TaskModel, FlowItemFigure>
		implements RemovableEditPart, FlowItemFigureContext {

	@Override
	protected FlowItemFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createFlowItemFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTaskFlows());
		childModels.addAll(this.getCastedModel().getTaskEscalations());
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(TaskModel.class);
		types.add(ExternalFlowModel.class);
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				ConnectionModel returnConn;
				if (target instanceof TaskModel) {
					// Create the connection to next task
					TaskToNextTaskModel conn = new TaskToNextTaskModel();
					conn.setPreviousTask((TaskModel) source);
					conn.setNextTask((TaskModel) target);
					conn.connect();
					returnConn = conn;

				} else if (target instanceof ExternalFlowModel) {
					// Create the connection to external flow
					TaskToNextExternalFlowModel conn = new TaskToNextExternalFlowModel();
					conn.setPreviousTask((TaskModel) source);
					conn.setNextExternalFlow((ExternalFlowModel) target);
					conn.connect();
					returnConn = conn;

				} else {
					// Unknown target
					throw new OfficeFloorPluginFailure("Unknown target type "
							+ target.getClass().getName());
				}

				// Return the connection
				return returnConn;
			}
		};
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
		WorkToInitialTaskModel work = this.getCastedModel()
				.getInitialTaskForWork();
		if (work != null) {
			models.add(work);
		}

		// Add work task
		WorkTaskToTaskModel task = this.getCastedModel().getWorkTask();
		if (task != null) {
			models.add(task);
		}

		// Add task inputs
		models.addAll(this.getCastedModel().getTaskFlowInputs());

		// Add handled escalations
		models.addAll(this.getCastedModel().getTaskEscalationInputs());

		// Add previous tasks
		models.addAll(this.getCastedModel().getPreviousTasks());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<TaskEvent>(TaskEvent.values()) {
			protected void handlePropertyChange(TaskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_IS_PUBLIC:
					// Ensure display is public
					FlowItemEditPart.this.getOfficeFloorFigure().setIsPublic(
							FlowItemEditPart.this.getCastedModel()
									.getIsPublic());
					break;
				case CHANGE_NEXT_TASK:
				case CHANGE_NEXT_EXTERNAL_FLOW:
					FlowItemEditPart.this.refreshSourceConnections();
					break;
				case CHANGE_INITIAL_TASK_FOR_WORK:
				case CHANGE_WORK_TASK:
				case ADD_TASK_FLOW_INPUT:
				case REMOVE_TASK_FLOW_INPUT:
				case ADD_TASK_ESCALATION_INPUT:
				case REMOVE_TASK_ESCALATION_INPUT:
				case ADD_PREVIOUS_TASK:
				case REMOVE_PREVIOUS_TASK:
					FlowItemEditPart.this.refreshTargetConnections();
					break;
				case ADD_TASK_FLOW:
				case REMOVE_TASK_FLOW:
				case ADD_TASK_ESCALATION:
				case REMOVE_TASK_ESCALATION:
					FlowItemEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveFlowItemOperation();
	}

	/*
	 * ======================= FlowItemFigureContext ========================
	 */

	// TODO rename to getTaskName
	@Override
	public String getFlowItemName() {
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
				FlowItemEditPart.this.getCastedModel().setIsPublic(isPublic);
			}

			@Override
			protected void undoCommand() {
				FlowItemEditPart.this.getCastedModel().setIsPublic(
						currentIsPublic);
			}
		});
	}

}