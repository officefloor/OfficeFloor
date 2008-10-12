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
package net.officefloor.eclipse.room.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.room.SubRoomFigureContext;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomModel.SubRoomEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.SubRoomModel}.
 * 
 * @author Daniel
 */
public class SubRoomEditPart extends
		AbstractOfficeFloorEditPart<SubRoomModel, OfficeFloorFigure> implements
		RemovableEditPart, SubRoomFigureContext {

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
		handlers.add(new PropertyChangeHandler<SubRoomEvent>(SubRoomEvent
				.values()) {
			@Override
			protected void handlePropertyChange(SubRoomEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_INPUT_FLOW:
				case REMOVE_INPUT_FLOW:
				case ADD_MANAGED_OBJECT:
				case REMOVE_MANAGED_OBJECT:
				case ADD_OUTPUT_FLOW:
				case REMOVE_OUTPUT_FLOW:
				case ADD_ESCALATION:
				case REMOVE_ESCALATION:
					SubRoomEditPart.this.refreshChildren();
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
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubRoomFigure(this);
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
		childModels.addAll(this.getCastedModel().getInputFlows());
		childModels.addAll(this.getCastedModel().getManagedObjects());
		childModels.addAll(this.getCastedModel().getOutputFlows());
		childModels.addAll(this.getCastedModel().getEscalations());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#delete()
	 */
	@Override
	public void delete() {
		// Disconnect and remove the sub room
		RemoveConnectionsAction<SubRoomModel> subRoom = this.getCastedModel()
				.removeConnections();
		RoomModel room = (RoomModel) this.getParent().getModel();
		room.removeSubRoom(subRoom.getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.RemovableEditPart#undelete()
	 */
	@Override
	public void undelete() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SubRoomEditPart.undelete");
	}

	/*
	 * ================= SubRoomFigureContext =======================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.SubRoomFigureContext#getSubRoomName()
	 */
	@Override
	public String getSubRoomName() {
		return this.getCastedModel().getId();
	}

}
