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

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.office.editparts.RoomEditPart;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.office.OfficeLoader;

/**
 * Refreshes the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class RefreshOfficeRoomOperation extends AbstractOperation<RoomEditPart> {

	/**
	 * Initiate.
	 */
	public RefreshOfficeRoomOperation() {
		super("Refresh room", RoomEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(final Context context) {
		context.execute(new OfficeFloorCommand() {

			@Override
			public void doCommand() {
				try {

					// Create the Project class loader
					ProjectClassLoader projectClassLoader = ProjectClassLoader
							.create(context.getEditPart().getEditor());

					// Create the office loader
					OfficeLoader officeLoader = new OfficeLoader();

					// Load the office room
					officeLoader.loadOfficeRoom(context.getEditPart()
							.getCastedModel(), projectClassLoader
							.getConfigurationContext(), projectClassLoader);

				} catch (Throwable ex) {

					// TODO implement, provide message error (or error)
					// (extend Command to provide method invoked from execute to
					// throw exception and handle by message box and possibly
					// eclipse error)
					System.err.println("Failed refreshing the office room");
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
		});
	}

}
