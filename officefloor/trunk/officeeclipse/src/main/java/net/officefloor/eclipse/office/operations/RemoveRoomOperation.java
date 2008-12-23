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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.editparts.DeskEditPart;
import net.officefloor.eclipse.office.editparts.FlowItemEditPart;
import net.officefloor.eclipse.office.editparts.RoomEditPart;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;

/**
 * {@link Operation} to remove the top level {@link OfficeRoomModel}.
 * 
 * @author Daniel
 */
public class RemoveRoomOperation<E extends AbstractOfficeFloorEditPart<?, ?>>
		extends AbstractOperation<E> {

	/**
	 * Creates a {@link RemoveRoomOperation} for a {@link RoomEditPart}.
	 * 
	 * @return {@link RemoveRoomOperation}.
	 */
	public static RemoveRoomOperation<RoomEditPart> createFromRoom() {
		return new RemoveRoomOperation<RoomEditPart>(RoomEditPart.class);
	}

	/**
	 * Creates a {@link RemoveRoomOperation} for a {@link DeskEditPart}.
	 * 
	 * @return {@link RemoveRoomOperation}.
	 */
	public static RemoveRoomOperation<DeskEditPart> createFromDesk() {
		return new RemoveRoomOperation<DeskEditPart>(DeskEditPart.class);
	}

	/**
	 * Creates a {@link RemoveRoomOperation} for a {@link FlowItemEditPart}.
	 * 
	 * @return {@link RemoveRoomOperation}.
	 */
	public static RemoveRoomOperation<FlowItemEditPart> createFromFlowItem() {
		return new RemoveRoomOperation<FlowItemEditPart>(FlowItemEditPart.class);
	}

	/**
	 * Force construction via static creation methods.
	 * 
	 * @param editPartClass
	 *            Class of the {@link AbstractOfficeFloorEditPart}.
	 */
	private RemoveRoomOperation(Class<E> editPartClass) {
		super("Remove room", editPartClass);
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

		// Top level therefore remove the room (and managed objects)
		context.execute(new OfficeFloorCommand() {

			private RemoveConnectionsAction<OfficeRoomModel> roomConnections;

			private List<RemoveConnectionsAction<ExternalManagedObjectModel>> moConnections;

			@Override
			protected void doCommand() {

				// Remove the room
				this.roomConnections = room.removeConnections();
				office.setRoom(null);

				// Remove the managed objects
				this.moConnections = new LinkedList<RemoveConnectionsAction<ExternalManagedObjectModel>>();
				for (ExternalManagedObjectModel mo : new ArrayList<ExternalManagedObjectModel>(
						office.getExternalManagedObjects())) {
					this.moConnections.add(mo.removeConnections());
					office.removeExternalManagedObject(mo);
				}

			}

			@Override
			protected void undoCommand() {

				// Re-add the managed objects
				for (RemoveConnectionsAction<ExternalManagedObjectModel> mo : this.moConnections) {
					office.addExternalManagedObject(mo.getModel());
					mo.reconnect();
				}

				// Re-add the room
				office.setRoom(this.roomConnections.getModel());
				this.roomConnections.reconnect();
			}
		});
	}

}
