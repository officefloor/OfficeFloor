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
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel.OfficeManagedObjectSourceFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeManagedObjectSourceFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectSourceFlowEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeManagedObjectSourceFlowModel, OfficeManagedObjectSourceFlowEvent, OfficeManagedObjectSourceFlowFigure>
		implements OfficeManagedObjectSourceFlowFigureContext {

	@Override
	protected OfficeManagedObjectSourceFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeManagedObjectSourceFlowFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeSectionInput());
	}

	@Override
	protected Class<OfficeManagedObjectSourceFlowEvent> getPropertyChangeEventType() {
		return OfficeManagedObjectSourceFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeManagedObjectSourceFlowEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_MANAGED_OBJECT_SOURCE_FLOW_NAME:
			this.getOfficeFloorFigure().setOfficeManagedObjectSourceFlowName(
					this.getOfficeManagedObjectSourceFlowName());
			break;

		case CHANGE_OFFICE_SECTION_INPUT:
			OfficeManagedObjectSourceFlowEditPart.this
					.refreshSourceConnections();
			break;

		case CHANGE_ARGUMENT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ============== OfficeManagedObjectSourceFlowFigureContext ==============
	 */

	@Override
	public String getOfficeManagedObjectSourceFlowName() {
		return this.getCastedModel().getOfficeManagedObjectSourceFlowName();
	}

}