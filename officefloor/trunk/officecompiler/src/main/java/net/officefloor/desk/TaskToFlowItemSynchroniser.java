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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.work.TaskEscalationModel;
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
	 * @throws Exception
	 *             If fail to synchronise.
	 */
	public static void synchroniseTaskOntoFlowItem(TaskModel<?, ?> task,
			FlowItemModel flowItem) throws Exception {

		// Ensure the flow outputs have unique names
		Set<String> flowOutputNames = new HashSet<String>();
		for (TaskFlowModel<?> flow : task.getFlows()) {

			// Obtain the id of the flow item output
			String flowItemOutputId = DeskLoader.getFlowItemOutputId(flow);

			// Ensure not already registered
			if (flowOutputNames.contains(flowItemOutputId)) {
				throw new Exception("Flow output '" + flowItemOutputId
						+ "' is not unique on task " + task.getTaskName());
			}

			// Add the id
			flowOutputNames.add(flowItemOutputId);
		}

		// Specify the Task onto the Flow Item
		flowItem.setTask(task);

		// Create the map of existing flow item outputs by id
		Map<String, FlowItemOutputModel> existingOutputs = new HashMap<String, FlowItemOutputModel>();
		for (FlowItemOutputModel output : flowItem.getOutputs()) {
			existingOutputs.put(output.getId(), output);
		}

		// Merge the flows
		for (TaskFlowModel<?> flow : task.getFlows()) {

			// Obtain the Id of the flow item output
			String flowItemOutputId = DeskLoader.getFlowItemOutputId(flow);

			// Determine if already existing on flow item
			if (existingOutputs.containsKey(flowItemOutputId)) {
				// Remove from existing, so not remove later
				existingOutputs.remove(flowItemOutputId);

				// No further changes for flow item output
				continue;
			}

			// Create a new output
			flowItem.addOutput(new FlowItemOutputModel(flowItemOutputId, flow,
					null, null));
		}

		// Remove any additional flows
		for (FlowItemOutputModel output : existingOutputs.values()) {

			// Remove connections for output
			output.removeConnections();

			// Remove the output
			flowItem.removeOutput(output);
		}

		// Create the map of existing flow item escalations by type
		Map<String, FlowItemEscalationModel> existingEscalations = new HashMap<String, FlowItemEscalationModel>();
		for (FlowItemEscalationModel escalation : flowItem.getEscalations()) {
			existingEscalations.put(escalation.getEscalationType(), escalation);
		}

		// Merge the escalations
		for (TaskEscalationModel escalation : task.getEscalations()) {

			// Obtain the type of the flow item escalation
			String escalationType = escalation.getEscalationType();

			// Determine if already existing on flow item
			if (existingEscalations.containsKey(escalationType)) {
				// Remove from existing, so not remove later
				existingEscalations.remove(escalationType);

				// No further changes for flow item escalation
				continue;
			}

			// Create a new escalation
			flowItem.addEscalation(new FlowItemEscalationModel(escalationType,
					escalation, null));
		}

		// Remove any additional escalations
		for (FlowItemEscalationModel escalation : existingEscalations.values()) {

			// Remove connections for escalation
			escalation.removeConnections();

			// Remove the escalation
			flowItem.removeEscalation(escalation);
		}
	}

	/**
	 * All access via static methods.
	 */
	private TaskToFlowItemSynchroniser() {
	}
}
