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
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowModel.DeskManagedObjectSourceFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeskManagedObjectSourceFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskManagedObjectSourceFlowEditPart
		extends
		AbstractOfficeFloorEditPart<DeskManagedObjectSourceFlowModel, DeskManagedObjectSourceFlowEvent, DeskManagedObjectSourceFlowFigure>
		implements DeskManagedObjectSourceFlowFigureContext {

	@Override
	protected DeskManagedObjectSourceFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createDeskManagedObjectSourceFlowFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getTask());
		EclipseUtil.addToList(models, this.getCastedModel().getExternalFlow());
	}

	@Override
	protected Class<DeskManagedObjectSourceFlowEvent> getPropertyChangeEventType() {
		return DeskManagedObjectSourceFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			DeskManagedObjectSourceFlowEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_TASK:
		case CHANGE_EXTERNAL_FLOW:
			DeskManagedObjectSourceFlowEditPart.this.refreshSourceConnections();
			break;

		case CHANGE_DESK_MANAGED_OBJECT_SOURCE_FLOW_NAME:
			this.getOfficeFloorFigure().setDeskManagedObjectSourceFlowName(
					this.getCastedModel().getDeskManagedObjectSourceFlowName());
			break;

		case CHANGE_ARGUMENT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ============== DeskManagedObjectSourceFlowFigureContext ==============
	 */

	@Override
	public String getDeskManagedObjectSourceFlowName() {
		return this.getCastedModel().getDeskManagedObjectSourceFlowName();
	}

}