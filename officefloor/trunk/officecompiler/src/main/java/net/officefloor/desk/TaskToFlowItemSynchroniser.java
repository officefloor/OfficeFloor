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
package net.officefloor.desk;

import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;

/**
 * Synchronises a {@link net.officefloor.model.work.TaskModel} to a
 * {@link net.officefloor.model.desk.FlowItemModel}.
 * 
 * @author Daniel
 */
public class TaskToFlowItemSynchroniser {

	/**
	 * Synchronises the {@link TaskModel} onto the {@link FlowItemModel}.
	 * 
	 * @param task
	 *            {@link TaskModel}.
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 */
	public static void synchroniseTaskOntoFlowItem(TaskModel<?, ?> task,
			FlowItemModel flowItem) {

		// Specify the Task onto the Flow Item
		flowItem.setTask(task);
		
		// Obtain the listing of flow item outputs
		FlowItemOutputModel[] outputs = flowItem.getOutputs().toArray(
				new FlowItemOutputModel[0]);

		// Merge the flows
		int outputIndex = 0;
		for (TaskFlowModel flow : task.getFlows()) {

			// Obtain the Id of the flow item output
			String flowItemOutputId = DeskLoader.getFlowItemOutputId(flow);

			// Determine if load to existing output
			if (outputIndex < (outputs.length - 1)) {
				FlowItemOutputModel output = outputs[outputIndex++];
				output.setId(flowItemOutputId);
				output.setTaskFlow(flow);
				continue;
			}

			// Create a new output
			flowItem.addOutput(new FlowItemOutputModel(flowItemOutputId, flow,
					null, null));
		}

		// Remove any additional flows
		for (int i = outputIndex; i < outputs.length; i++) {
			flowItem.removeOutput(outputs[i]);
		}
	}

	/**
	 * All access via static methods.
	 */
	private TaskToFlowItemSynchroniser() {
	}
}
