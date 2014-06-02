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
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputModel.OfficeSectionOutputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionOutputEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSectionOutputModel, OfficeSectionOutputEvent, OfficeSectionOutputFigure>
		implements OfficeSectionOutputFigureContext {

	/*
	 * =================== AbstractOfficeFloorEditPart ========================
	 */

	@Override
	protected OfficeSectionOutputFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSectionOutputFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeSectionInput());
	}

	@Override
	protected Class<OfficeSectionOutputEvent> getPropertyChangeEventType() {
		return OfficeSectionOutputEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSectionOutputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_SECTION_OUTPUT_NAME:
			this.getOfficeFloorFigure().setOfficeSectionOutputName(
					this.getOfficeSectionOutputName());
			break;

		case CHANGE_OFFICE_SECTION_INPUT:
			this.refreshSourceConnections();
			break;

		case CHANGE_ARGUMENT_TYPE:
		case CHANGE_ESCALATION_ONLY:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== OfficeSectionOutputFigureContext ====================
	 */

	@Override
	public String getOfficeSectionOutputName() {
		return this.getCastedModel().getOfficeSectionOutputName();
	}

}