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
package net.officefloor.eclipse.desk.operations;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.desk.editparts.FlowItemEditPart;
import net.officefloor.model.desk.FlowItemModel;

/**
 * Toggles {@link FlowItemModel} being public.
 * 
 * @author Daniel
 */
public class ToggleFlowItemPublicOperation extends
		AbstractOperation<FlowItemEditPart> {

	/**
	 * Initiate.
	 */
	public ToggleFlowItemPublicOperation() {
		super("Toggle public", FlowItemEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the flow item
		final FlowItemModel flowItem = context.getEditPart().getCastedModel();

		// Obtain initial public state
		final boolean initialState = flowItem.getIsPublic();

		// Make change
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Toggle
				flowItem.setIsPublic(!initialState);
			}

			@Override
			protected void undoCommand() {
				// Set back to initial state
				flowItem.setIsPublic(initialState);
			}
		});
	}

}
