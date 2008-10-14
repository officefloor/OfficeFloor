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

import net.officefloor.desk.TaskToFlowItemSynchroniser;
import net.officefloor.eclipse.common.action.AbstractSingleOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskTaskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskWorkEditPart;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.work.TaskModel;

/**
 * Creates a {@link FlowItemModel} from a {@link DeskTaskModel}.
 * 
 * @author Daniel
 */
public class CreateFlowItemFromDeskTaskOperation extends
		AbstractSingleOperation<DeskTaskEditPart> {

	/**
	 * Initiate.
	 */
	public CreateFlowItemFromDeskTaskOperation() {
		super("Add as flow item", DeskTaskEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.action.AbstractSingleOperation#createCommand
	 * (net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart)
	 */
	@Override
	protected OfficeFloorCommand createCommand(DeskTaskEditPart editPart) {

		// Obtain the work
		DeskWorkEditPart workEditPart = (DeskWorkEditPart) editPart.getParent();
		DeskWorkModel work = workEditPart.getCastedModel();

		// Obtain the desk
		// Note parent is work listing then desk
		DeskEditPart deskEditPart = (DeskEditPart) workEditPart.getParent()
				.getParent();
		final DeskModel desk = deskEditPart.getCastedModel();

		// Create the flow item for this task
		final DeskTaskModel task = editPart.getCastedModel();
		final FlowItemModel flowItem = new FlowItemModel(task.getName(), false,
				work.getId(), task.getName(), task.getTask(), null, null, null,
				null, null, null, null, null, null);
		flowItem.setId(deskEditPart.getUniqueFlowItemId(flowItem));
		flowItem.setX(300);
		flowItem.setY(100);

		// Obtain the task model
		TaskModel<?, ?> taskModel = task.getTask();
		if (taskModel == null) {
			editPart.messageWarning("Can not obtain "
					+ TaskModel.class.getSimpleName() + " for synchronising");
		} else {
			// Ensure synchronised to the task
			try {
				TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task
						.getTask(), flowItem);
			} catch (Exception ex) {
				editPart.messageError(ex);
			}
		}

		// Link the flow item with the task
		final DeskTaskToFlowItemModel conn = new DeskTaskToFlowItemModel(
				flowItem, task);

		// Make the change
		return new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Add the flow item to the desk
				desk.addFlowItem(flowItem);

				// Connect the flow
				conn.connect();
			}

			@Override
			protected void undoCommand() {
				// Disconnect the flow
				conn.remove();

				// Remove the flow item from the desk
				desk.removeFlowItem(flowItem);
			}
		};
	}

}
