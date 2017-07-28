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

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.models.AbstractFunctionAdministrationJoinPointModel;
import net.officefloor.eclipse.office.models.FunctionAdministrationJoinPointEvent;
import net.officefloor.eclipse.skin.OfficeFloorFigure;

/**
 * {@link EditPart} for {@link AbstractFunctionAdministrationJoinPointModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionAdministrationJoinPointEditPart extends
		AbstractOfficeFloorEditPart<AbstractFunctionAdministrationJoinPointModel, FunctionAdministrationJoinPointEvent, OfficeFloorFigure> {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory().createTaskAdministrationJoinPointFigure();
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		if (this.getCastedModel().isPreRatherThanPost()) {
			models.addAll(this.getCastedModel().getFunction().getPreAdministrations());
		} else {
			models.addAll(this.getCastedModel().getFunction().getPostAdministrations());
		}
	}

	@Override
	protected Class<FunctionAdministrationJoinPointEvent> getPropertyChangeEventType() {
		return FunctionAdministrationJoinPointEvent.class;
	}

	@Override
	protected void handlePropertyChange(FunctionAdministrationJoinPointEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_ADMINISTRATION:
			FunctionAdministrationJoinPointEditPart.this.refreshSourceConnections();
			break;
		}
	}

}