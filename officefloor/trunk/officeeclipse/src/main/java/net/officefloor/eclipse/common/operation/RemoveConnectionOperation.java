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
package net.officefloor.eclipse.common.operation;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.model.ConnectionModel;

/**
 * {@link Operation} to remove a {@link ConnectionModel}.
 * 
 * @author Daniel
 */
// TODO remove to be done by OfficeFloorLayoutEditPolicy.
@Deprecated
@SuppressWarnings("unchecked")
public class RemoveConnectionOperation extends
		AbstractOperation<OfficeFloorConnectionEditPart> {

	/**
	 * Initiate.
	 */
	public RemoveConnectionOperation() {
		super("Remove connection", OfficeFloorConnectionEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the connection
		final ConnectionModel connection = context.getEditPart()
				.getCastedModel();

		// Remove the connection
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				connection.remove();
			}

			@Override
			protected void undoCommand() {
				connection.connect();
			}
		});
	}

}
