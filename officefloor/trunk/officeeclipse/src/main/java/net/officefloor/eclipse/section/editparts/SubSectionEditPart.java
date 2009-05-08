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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.section.SubSectionFigureContext;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SubSectionModel}.
 * 
 * @author Daniel
 */
public class SubSectionEditPart
		extends
		AbstractOfficeFloorEditPart<SubSectionModel, SubSectionEvent, OfficeFloorFigure>
		implements SubSectionFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getSubSectionInputs());
		childModels.addAll(this.getCastedModel().getSubSectionOutputs());
		childModels.addAll(this.getCastedModel().getSubSectionObjects());
	}

	@Override
	protected Class<SubSectionEvent> getPropertyChangeEventType() {
		return SubSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(SubSectionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_SUB_SECTION_INPUT:
		case REMOVE_SUB_SECTION_INPUT:
		case ADD_SUB_SECTION_OUTPUT:
		case REMOVE_SUB_SECTION_OUTPUT:
		case ADD_SUB_SECTION_OBJECT:
		case REMOVE_SUB_SECTION_OBJECT:
			SubSectionEditPart.this.refreshChildren();
			break;
		}
	}

	/*
	 * ================= SubRoomFigureContext =======================
	 */

	@Override
	public String getSubSectionName() {
		return this.getCastedModel().getSubSectionName();
	}

}