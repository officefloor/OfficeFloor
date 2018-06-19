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
import net.officefloor.eclipse.skin.office.OfficeEscalationFigure;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationModel.OfficeEscalationEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeEscalationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeEscalationEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeEscalationModel, OfficeEscalationEvent, OfficeEscalationFigure>
		implements OfficeEscalationFigureContext {

	/*
	 * ==================== AbstractOfficeFloorEditPart =======================
	 */

	@Override
	protected OfficeEscalationFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeEscalationFigure(this);
	}

	@Override
	protected Class<OfficeEscalationEvent> getPropertyChangeEventType() {
		return OfficeEscalationEvent.class;
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeSectionInput());
	}

	@Override
	protected void handlePropertyChange(OfficeEscalationEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_ESCALATION_TYPE:
			this.getOfficeFloorFigure().setOfficeEscalationTypeName(
					this.getOfficeEscalationTypeName());
			break;

		case CHANGE_OFFICE_SECTION_INPUT:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ================== OfficeEscalationFigureContext ========================
	 */

	@Override
	public String getOfficeEscalationTypeName() {
		return this.getCastedModel().getEscalationType();
	}

}