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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.DeskFigureContext;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeDeskModel.OfficeDeskEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.OfficeDeskModel}.
 * 
 * @author Daniel
 */
public class DeskEditPart extends
		AbstractOfficeFloorEditPart<OfficeDeskModel, OfficeFloorFigure>
		implements DeskFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeDeskEvent>(OfficeDeskEvent
				.values()) {
			@Override
			protected void handlePropertyChange(OfficeDeskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_FLOW_ITEM:
				case REMOVE_FLOW_ITEM:
					DeskEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createDeskFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getFlowItems());
	}

	/*
	 * ================= DeskFigureContext ========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.office.DeskFigureContext#getDeskName()
	 */
	@Override
	public String getDeskName() {
		return this.getCastedModel().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.office.DeskFigureContext#getRoomNames()
	 */
	@Override
	public String[] getRoomNames() {

		// Obtain the parent rooms
		Deque<String> roomNames = new LinkedList<String>();
		RoomEditPart room = (RoomEditPart) this.getParent();
		while (room != null) {
			roomNames.push(room.getRoomName());

			// Obtain the parent
			EditPart parentEditPart = room.getParent();
			if (parentEditPart instanceof RoomEditPart) {
				// Another parent room
				room = (RoomEditPart) parentEditPart;
			} else {
				// No further parent room (likely now office)
				room = null;
			}
		}

		// Ignore the top level room
		if (roomNames.size() > 0) {
			roomNames.pop();
		}

		// Create the parent rooms list
		String[] names = new String[roomNames.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = roomNames.pop();
		}

		// Return the room names
		return names;
	}

}
