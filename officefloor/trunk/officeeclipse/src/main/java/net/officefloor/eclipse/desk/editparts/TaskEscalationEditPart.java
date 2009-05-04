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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskEscalationModel.TaskEscalationEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link TaskEscalationModel}.
 * 
 * @author Daniel
 */
public class TaskEscalationEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<TaskEscalationModel, OfficeFloorFigure>
		implements TaskEscalationFigureContext {

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<TaskEscalationEvent>(
				TaskEscalationEvent.values()) {
			@Override
			protected void handlePropertyChange(TaskEscalationEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_TASK:
				case CHANGE_EXTERNAL_FLOW:
					TaskEscalationEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskEscalationFigure(this);
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				if (target instanceof TaskModel) {
					// Create the task connection
					TaskEscalationToTaskModel conn = new TaskEscalationToTaskModel();
					conn.setEscalation((TaskEscalationModel) source);
					conn.setTask((TaskModel) target);
					conn.connect();
					return conn;

				} else if (target instanceof ExternalFlowModel) {
					// Create the external flow connection
					TaskEscalationToExternalFlowModel conn = new TaskEscalationToExternalFlowModel();
					conn.setTaskEscalation((TaskEscalationModel) source);
					conn.setExternalFlow((ExternalFlowModel) target);
					conn.connect();
					return conn;

				} else {
					// Unknown type
					throw new OfficeFloorPluginFailure("Unknown target model "
							+ target.getClass().getName());
				}
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(TaskModel.class);
		types.add(ExternalFlowModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Task
		TaskEscalationToTaskModel task = this.getCastedModel().getTask();
		if (task != null) {
			models.add(task);
		}

		// External flow
		TaskEscalationToExternalFlowModel extFlow = this.getCastedModel()
				.getExternalFlow();
		if (extFlow != null) {
			models.add(extFlow);
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	/*
	 * ================== TaskEscalationFigureContext ================
	 */

	@Override
	public String getEscalationType() {
		return this.getCastedModel().getEscalationType();
	}

}
