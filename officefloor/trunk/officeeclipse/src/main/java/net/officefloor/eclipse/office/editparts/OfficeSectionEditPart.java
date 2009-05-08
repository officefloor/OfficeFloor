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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionFigureContext;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionModel.OfficeSectionEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSectionModel}.
 * 
 * @author Daniel
 */
public class OfficeSectionEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSectionModel, OfficeSectionEvent, OfficeFloorFigure>
		implements OfficeSectionFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOfficeSectionInputs());
		childModels.addAll(this.getCastedModel().getOfficeSectionOutputs());
		childModels.addAll(this.getCastedModel().getOfficeSectionObjects());
		childModels.addAll(this.getCastedModel()
				.getOfficeSectionResponsibilities());
	}

	@Override
	protected Class<OfficeSectionEvent> getPropertyChangeEventType() {
		return OfficeSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSectionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_SECTION_INPUT:
		case REMOVE_OFFICE_SECTION_INPUT:
		case ADD_OFFICE_SECTION_OUTPUT:
		case REMOVE_OFFICE_SECTION_OUTPUT:
		case ADD_OFFICE_SECTION_OBJECT:
		case REMOVE_OFFICE_SECTION_OBJECT:
		case ADD_OFFICE_SECTION_RESPONSIBILITY:
		case REMOVE_OFFICE_SECTION_RESPONSIBILITY:
			OfficeSectionEditPart.this.refreshChildren();
			break;
		}
	}

	/*
	 * ================== RoomFigureContext ===============================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.getCastedModel().getOfficeSectionName();
	}

}