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

import java.util.List;

import net.officefloor.desk.TaskToFlowItemSynchroniser;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.eclipse.desk.DeskUtil;
import net.officefloor.eclipse.desk.WorkLoaderInstance;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskTaskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskWorkEditPart;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.geometry.Point;

/**
 * Creates a {@link FlowItemModel} from a {@link DeskTaskModel}.
 * 
 * @author Daniel
 */
public class CreateFlowItemFromDeskTaskOperation extends
		AbstractOperation<DeskTaskEditPart> {

	/**
	 * Initiate.
	 */
	public CreateFlowItemFromDeskTaskOperation() {
		super("Add as flow item", DeskTaskEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the desk task edit part
		final DeskTaskEditPart taskEditPart = context.getEditPart();
		final DeskTaskModel task = taskEditPart.getCastedModel();

		// Obtain the work
		DeskWorkEditPart workEditPart = (DeskWorkEditPart) taskEditPart
				.getParent();
		DeskWorkModel work = workEditPart.getCastedModel();

		// Obtain the desk
		DeskEditPart deskEditPart = (DeskEditPart) workEditPart.getParent();
		final DeskModel desk = deskEditPart.getCastedModel();

		// Obtain the project
		IProject project = FileConfigurationItem.getProject(taskEditPart);

		// Obtain the work loader instance
		String workLoaderClassName = work.getLoader();
		if ((workLoaderClassName == null)
				|| (workLoaderClassName.trim().length() == 0)) {
			// Must have work loader specified
			taskEditPart.messageError("No loader specified for work "
					+ work.getId());
			return;
		}
		WorkLoaderInstance workLoaderInstance = DeskUtil
				.createWorkLoaderInstance(workLoaderClassName, project);
		if (workLoaderInstance == null) {
			taskEditPart
					.messageError("Can not load work to obtain details of the flow item");
			return;
		}

		// Obtain the work
		List<PropertyModel> properties = work.getProperties();
		WorkModel<?> workModel;
		try {
			workModel = workLoaderInstance.createWorkModel(properties);
		} catch (Exception ex) {
			// Failed to obtain work
			taskEditPart.messageError(ex);
			return;
		}

		// Find the corresponding task model
		String taskName = task.getName();
		TaskModel<?, ?> taskModel = null;
		for (TaskModel<?, ?> possibleTask : workModel.getTasks()) {
			if (taskName.equals(possibleTask.getTaskName())) {
				taskModel = possibleTask;
			}
		}

		// Ensure have the corresponding task
		if (taskModel == null) {
			taskEditPart.messageError("No task on work by name " + taskName
					+ ".  Likely work requires to be refreshed.");
			return;

		}

		// Create the flow item for this task
		final FlowItemModel flowItem = new FlowItemModel(task.getName(), false,
				work.getId(), task.getName(), taskModel, null, null, null,
				null, null, null, null, null, null);
		flowItem.setId(deskEditPart.getUniqueFlowItemId(flowItem));

		// Ensure flow item synchronised to the task
		try {
			TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(taskModel,
					flowItem);
		} catch (Exception ex) {
			// Failed to synchronise
			taskEditPart.messageError(ex);
			return;
		}

		// Position the flow item
		Point location = context.getLocation();
		flowItem.setX(location.x + 100);
		flowItem.setY(location.y);

		// Link the flow item with the task
		final DeskTaskToFlowItemModel conn = new DeskTaskToFlowItemModel(
				flowItem, task);

		// Make the change
		context.execute(new OfficeFloorCommand() {

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
		});
	}

}
