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
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigureContext;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskEscalationModel.TaskEscalationEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TaskEscalationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskEscalationEditPart
		extends
		AbstractOfficeFloorEditPart<TaskEscalationModel, TaskEscalationEvent, OfficeFloorFigure>
		implements TaskEscalationFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskEscalationFigure(this);
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
	protected Class<TaskEscalationEvent> getPropertyChangeEventType() {
		return TaskEscalationEvent.class;
	}

	@Override
	protected void handlePropertyChange(TaskEscalationEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_TASK:
		case CHANGE_EXTERNAL_FLOW:
			TaskEscalationEditPart.this.refreshSourceConnections();
			break;

		case CHANGE_ESCALATION_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== TaskEscalationFigureContext ================
	 */

	@Override
	public String getEscalationType() {
		return this.getCastedModel().getEscalationType();
	}

}