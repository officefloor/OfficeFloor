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
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigureContext;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionModel.OfficeSubSectionEvent;

/**
 * {@link EditPart} for the {@link OfficeSubSectionModel}.
 * 
 * @author Daniel
 */
public class OfficeSubSectionEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSubSectionModel, OfficeSubSectionEvent, OfficeFloorFigure>
		implements OfficeSubSectionFigureContext {

	/*
	 * ==================== AbstractOfficeFloorEditPart ===================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSubSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOfficeTasks());
		childModels.addAll(this.getCastedModel().getOfficeSubSections());
	}

	@Override
	protected Class<OfficeSubSectionEvent> getPropertyChangeEventType() {
		return OfficeSubSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSubSectionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_TASK:
		case REMOVE_OFFICE_TASK:
		case ADD_OFFICE_SUB_SECTION:
		case REMOVE_OFFICE_SUB_SECTION:
			this.refreshChildren();
			break;
		}
	}

}