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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.desk.editparts.DeskWorkEditPart;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.FlowItemModel;

/**
 * {@link Operation} to remove a {@link DeskWorkModel}.
 * 
 * @author Daniel
 */
public class RemoveWorkOperation extends AbstractOperation<DeskWorkEditPart> {

	/**
	 * Initiate.
	 */
	public RemoveWorkOperation() {
		super("Remove work", DeskWorkEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the desk
		final DeskModel desk = (DeskModel) context.getEditPart().getParent()
				.getModel();

		// Obtain the desk work to remove
		final DeskWorkModel work = context.getEditPart().getCastedModel();

		// Create the list of flow items for the work
		List<FlowItemModel> flowItemList = new LinkedList<FlowItemModel>();
		for (DeskTaskModel task : new ArrayList<DeskTaskModel>(work.getTasks())) {
			for (DeskTaskToFlowItemModel taskToFlowItem : new ArrayList<DeskTaskToFlowItemModel>(
					task.getFlowItems())) {
				FlowItemModel flowItem = taskToFlowItem.getFlowItem();
				if (flowItem != null) {
					flowItemList.add(flowItem);
				}
			}
		}
		final FlowItemModel[] flowItems = flowItemList
				.toArray(new FlowItemModel[0]);

		// Make the change
		context.execute(new OfficeFloorCommand() {

			private List<RemoveConnectionsAction<FlowItemModel>> flowItemConnections;

			private RemoveConnectionsAction<DeskWorkModel> workConnections;

			@Override
			protected void doCommand() {

				// Remove the flow item and their connections
				this.flowItemConnections = new LinkedList<RemoveConnectionsAction<FlowItemModel>>();
				for (FlowItemModel flowItem : flowItems) {
					this.flowItemConnections.add(flowItem.removeConnections());
					desk.removeFlowItem(flowItem);
				}

				// Remove the work and its connections
				this.workConnections = work.removeConnections();
				desk.removeWork(work);
			}

			@Override
			protected void undoCommand() {

				// Add the work and its connections
				desk.addWork(work);
				this.workConnections.reconnect();

				// Add the flow items
				for (FlowItemModel flowItem : flowItems) {
					desk.addFlowItem(flowItem);
				}

				// Add the flow item connections
				for (RemoveConnectionsAction<FlowItemModel> connections : this.flowItemConnections) {
					connections.reconnect();
				}
			}
		});
	}

}
