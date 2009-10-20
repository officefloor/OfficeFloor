/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigureContext;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionInputModel.OfficeSectionInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSectionInputModel}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionInputEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSectionInputModel, OfficeSectionInputEvent, OfficeFloorFigure>
		implements OfficeSectionInputFigureContext {

	/*
	 * ================= AbstractOfficeFloorEditPart ===========================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSectionInputFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeSectionOutputs());
		models
				.addAll(this.getCastedModel()
						.getOfficeManagedObjectSourceFlows());
		models.addAll(this.getCastedModel().getOfficeEscalations());
	}

	@Override
	protected Class<OfficeSectionInputEvent> getPropertyChangeEventType() {
		return OfficeSectionInputEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSectionInputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_SECTION_OUTPUT:
		case REMOVE_OFFICE_SECTION_OUTPUT:
		case ADD_OFFICE_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_OFFICE_MANAGED_OBJECT_SOURCE_FLOW:
		case ADD_OFFICE_ESCALATION:
		case REMOVE_OFFICE_ESCALATION:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ==================== OfficeSectionInputFigureContext ====================
	 */

	@Override
	public String getOfficeSectionInputName() {
		return this.getCastedModel().getOfficeSectionInputName();
	}

}