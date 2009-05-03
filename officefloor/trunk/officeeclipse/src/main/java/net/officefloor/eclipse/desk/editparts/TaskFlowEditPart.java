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
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.TaskFlowFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskFlowModel.TaskFlowEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link TaskFlowModel}.
 * 
 * @author Daniel
 */
public class TaskFlowEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<TaskFlowModel, OfficeFloorFigure>
		implements TaskFlowFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskFlowFigure(this);
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				// TODO Handle always only connected to one type

				// Obtain the type of link
				String linkType = (String) request.getNewObject();

				if (target instanceof TaskModel) {
					// Create the task connection
					TaskFlowToTaskModel conn = new TaskFlowToTaskModel();
					conn.setTaskFlow((TaskFlowModel) source);
					conn.setTask((TaskModel) target);
					conn.setLinkType(linkType);
					conn.connect();
					return conn;

				} else if (target instanceof ExternalFlowModel) {
					// Create the external flow connection
					TaskFlowToExternalFlowModel conn = new TaskFlowToExternalFlowModel();
					conn.setTaskFlow((TaskFlowModel) source);
					conn.setExternalFlow((ExternalFlowModel) target);
					conn.setLinkType(linkType);
					conn.connect();
					return conn;

				} else {
					throw new OfficeFloorPluginFailure(
							"Unknown connection target type "
									+ target.getClass().getName()
									+ " for "
									+ TaskFlowEditPart.this.getClass()
											.getName());
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
		TaskFlowToTaskModel flowTask = this.getCastedModel().getTask();
		if (flowTask != null) {
			models.add(flowTask);
		}

		// External flow
		TaskFlowToExternalFlowModel extFlow = this.getCastedModel()
				.getExternalFlow();
		if (extFlow != null) {
			models.add(extFlow);
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<TaskFlowEvent>(TaskFlowEvent
				.values()) {
			protected void handlePropertyChange(TaskFlowEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_TASK:
				case CHANGE_EXTERNAL_FLOW:
					TaskFlowEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	/*
	 * ======================= FlowItemOutputFigureContext ================
	 */

	@Override
	public String getTaskFlowName() {
		return this.getCastedModel().getFlowName();
	}

}
