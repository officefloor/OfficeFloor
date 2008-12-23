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
package net.officefloor.eclipse.office.operations;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.editparts.DeskEditPart;
import net.officefloor.eclipse.office.editparts.FlowItemEditPart;
import net.officefloor.eclipse.office.editparts.RoomEditPart;
import net.officefloor.eclipse.room.RoomEditor;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.room.RoomModel;

/**
 * {@link Operation} to open the top level {@link RoomModel}.
 * 
 * @author Daniel
 */
public class OpenRoomOperation<E extends AbstractOfficeFloorEditPart<?, ?>>
		extends AbstractOperation<E> {

	/**
	 * Creates a {@link OpenRoomOperation} for a {@link RoomEditPart}.
	 * 
	 * @return {@link OpenRoomOperation}.
	 */
	public static OpenRoomOperation<RoomEditPart> createFromRoom() {
		return new OpenRoomOperation<RoomEditPart>(RoomEditPart.class);
	}

	/**
	 * Creates a {@link OpenRoomOperation} for a {@link DeskEditPart}.
	 * 
	 * @return {@link OpenRoomOperation}.
	 */
	public static OpenRoomOperation<DeskEditPart> createFromDesk() {
		return new OpenRoomOperation<DeskEditPart>(DeskEditPart.class);
	}

	/**
	 * Creates a {@link OpenRoomOperation} for a {@link FlowItemEditPart}.
	 * 
	 * @return {@link OpenRoomOperation}.
	 */
	public static OpenRoomOperation<FlowItemEditPart> createFromFlowItem() {
		return new OpenRoomOperation<FlowItemEditPart>(FlowItemEditPart.class);
	}

	/**
	 * Force construction via static creation methods.
	 * 
	 * @param editPartClass
	 *            Class of the {@link AbstractOfficeFloorEditPart}.
	 */
	private OpenRoomOperation(Class<E> editPartClass) {
		super("Open room", editPartClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the office
		final OfficeModel office = (OfficeModel) context.getEditPart()
				.getEditor().getRootEditPart().getModel();

		// Obtain the top level room of the office
		final OfficeRoomModel room = office.getRoom();

		// Ensure have top level room
		if (room == null) {
			return;
		}

		// Open the room
		context.getEditPart().openClasspathFile(room.getId(),
				RoomEditor.EDITOR_ID);

		// Do not register command as nothing to undo on an open
	}

}
