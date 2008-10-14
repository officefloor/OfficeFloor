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

import net.officefloor.desk.DeskLoader;
import net.officefloor.desk.TaskToFlowItemSynchroniser;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractSingleOperation;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.desk.editparts.DeskWorkEditPart;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.work.TaskModel;

/**
 * Refreshes the {@link DeskWorkModel}.
 * 
 * @author Daniel
 */
public class RefreshWorkOperation extends
		AbstractSingleOperation<DeskWorkEditPart> {

	/**
	 * Initiate.
	 */
	public RefreshWorkOperation() {
		super("Refresh Work", DeskWorkEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.action.AbstractSingleOperation#createCommand
	 * (net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart)
	 */
	@Override
	protected OfficeFloorCommand createCommand(final DeskWorkEditPart editPart) {

		// Make changes
		return new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				try {
					// Create the Project class loader
					ProjectClassLoader projectClassLoader = ProjectClassLoader
							.create(editPart.getEditor());

					// Create the desk loader
					DeskLoader deskLoader = new DeskLoader(projectClassLoader);

					// Obtain the desk work model
					DeskWorkModel deskWorkModel = editPart.getCastedModel();

					// Load the work (which does the synchronising)
					deskLoader.loadWork(deskWorkModel, projectClassLoader
							.getConfigurationContext());

					// Synchronise the tasks onto flow items
					for (DeskTaskModel deskTaskModel : deskWorkModel.getTasks()) {

						// Obtain the task model
						TaskModel<?, ?> taskModel = deskTaskModel.getTask();
						if (taskModel == null) {
							// No task model so can not synchronise
							continue;
						}

						// Synchronise the flow items
						for (DeskTaskToFlowItemModel taskToFlow : deskTaskModel
								.getFlowItems()) {
							FlowItemModel flowItem = taskToFlow.getFlowItem();
							TaskToFlowItemSynchroniser
									.synchroniseTaskOntoFlowItem(taskModel,
											flowItem);
						}
					}

				} catch (Throwable ex) {

					// TODO implement, provide message error (or error)
					// (extend Command to provide method invoked from execute to
					// throw exception and handle by message box and possibly
					// eclipse error)
					System.err.println("Failed refreshing the work");
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
