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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.TaskFlowFigureContext;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskFlowModel.TaskFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TaskFlowModel}.
 * 
 * @author Daniel
 */
public class TaskFlowEditPart
		extends
		AbstractOfficeFloorEditPart<TaskFlowModel, TaskFlowEvent, OfficeFloorFigure>
		implements TaskFlowFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskFlowFigure(this);
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
	protected Class<TaskFlowEvent> getPropertyChangeEventType() {
		return TaskFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(TaskFlowEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_TASK:
		case CHANGE_EXTERNAL_FLOW:
			TaskFlowEditPart.this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ======================= FlowItemOutputFigureContext ================
	 */

	@Override
	public String getTaskFlowName() {
		return this.getCastedModel().getFlowName();
	}

}