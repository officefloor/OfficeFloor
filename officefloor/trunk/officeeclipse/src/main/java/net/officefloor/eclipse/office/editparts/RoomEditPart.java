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

import org.eclipse.draw2d.IFigure;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.office.OfficeRoomModel.OfficeRoomEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.OfficeRoomModel}.
 * 
 * @author Daniel
 */
public class RoomEditPart extends AbstractOfficeFloorEditPart<OfficeRoomModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeRoomEvent>(OfficeRoomEvent
				.values()) {
			@Override
			protected void handlePropertyChange(OfficeRoomEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_DESK:
				case REMOVE_DESK:
				case ADD_SUB_ROOM:
				case REMOVE_SUB_ROOM:
					RoomEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(this.getCastedModel().getId()));
		figure.addChildContainerFigure();
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getSubRooms());
		childModels.addAll(this.getCastedModel().getDesks());
	}

}
