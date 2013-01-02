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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.desk.TaskFlowFigure;
import net.officefloor.eclipse.skin.desk.TaskFlowFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowModel.TaskFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TaskFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskFlowEditPart
		extends
		AbstractOfficeFloorEditPart<TaskFlowModel, TaskFlowEvent, TaskFlowFigure>
		implements TaskFlowFigureContext {

	@Override
	protected TaskFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskFlowFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getTask());
		EclipseUtil.addToList(models, this.getCastedModel().getExternalFlow());
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

		case CHANGE_FLOW_NAME:
			this.getOfficeFloorFigure().setTaskFlowName(this.getTaskFlowName());
			break;

		case CHANGE_ARGUMENT_TYPE:
		case CHANGE_KEY:
			// Non visual change
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