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
package net.officefloor.eclipse.conform.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.conform.figures.ConformModelFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.conform.ExistingModel;
import net.officefloor.model.conform.ExistingModel.ExistingEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExistingModel}.
 * 
 * @author Daniel
 */
public class ExistingModelEditPart
		extends
		AbstractOfficeFloorEditPart<ExistingModel, ExistingEvent, OfficeFloorFigure> {

	/*
	 * ============== AbstractOfficeFloorEditPart ==========================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return new ConformModelFigure();
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getExistingItems());
	}

	@Override
	protected Class<ExistingEvent> getPropertyChangeEventType() {
		return ExistingEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExistingEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_EXISTING_ITEM:
		case REMOVE_EXISTING_ITEM:
			this.refreshChildren();
			break;
		}
	}

}
