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
package net.officefloor.eclipse.room.operations;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractSingleOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.room.editparts.SubRoomEditPart;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.room.RoomLoader;

/**
 * Refreshes the {@link SubRoomModel}.
 * 
 * @author Daniel
 */
public class RefreshSubRoomOperation extends
		AbstractSingleOperation<SubRoomEditPart> {

	/**
	 * Initiate.
	 */
	public RefreshSubRoomOperation() {
		super("Refresh sub room", SubRoomEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.action.AbstractSingleOperation#createCommand
	 * (net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart)
	 */
	@Override
	protected OfficeFloorCommand createCommand(final SubRoomEditPart editPart) {
		return new OfficeFloorCommand() {

			@Override
			public void doCommand() {
				try {

					// Create the Project class loader
					ProjectClassLoader projectClassLoader = ProjectClassLoader
							.create(editPart.getEditor());

					// Create the room loader
					RoomLoader roomLoader = new RoomLoader();

					// Load the sub room
					roomLoader.loadSubRoom(editPart.getCastedModel(),
							projectClassLoader.getConfigurationContext());

				} catch (Throwable ex) {

					// TODO implement, provide message error (or error)
					// (extend Command to provide method invoked from execute to
					// throw exception and handle by message box and possibly
					// eclipse error)
					System.err.println("Failed refreshing the sub room");
					ex.printStackTrace();
					throw new UnsupportedOperationException(
							"TODO provide Exception to createCommand of "
									+ Operation.class.getName());
				}
			}

			@Override
			protected void undoCommand() {
				// TODO Implement
				throw new UnsupportedOperationException(
						"TODO implement OfficeFloorCommand.undoCommand");
			}
		};
	}

}
