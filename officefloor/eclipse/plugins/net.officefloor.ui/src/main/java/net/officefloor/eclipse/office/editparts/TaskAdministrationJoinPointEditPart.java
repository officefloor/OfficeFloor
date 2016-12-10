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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.models.AbstractTaskAdministrationJoinPointModel;
import net.officefloor.eclipse.office.models.TaskAdministrationJoinPointEvent;
import net.officefloor.eclipse.skin.OfficeFloorFigure;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link AbstractTaskAdministrationJoinPointModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskAdministrationJoinPointEditPart
		extends
		AbstractOfficeFloorEditPart<AbstractTaskAdministrationJoinPointModel, TaskAdministrationJoinPointEvent, OfficeFloorFigure> {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createTaskAdministrationJoinPointFigure();
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		if (this.getCastedModel().isPreRatherThanPost()) {
			models.addAll(this.getCastedModel().getTask().getPreDuties());
		} else {
			models.addAll(this.getCastedModel().getTask().getPostDuties());
		}
	}

	@Override
	protected Class<TaskAdministrationJoinPointEvent> getPropertyChangeEventType() {
		return TaskAdministrationJoinPointEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			TaskAdministrationJoinPointEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DUTIES:
			TaskAdministrationJoinPointEditPart.this.refreshSourceConnections();
			break;
		}
	}

}