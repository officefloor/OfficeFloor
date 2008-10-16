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

import org.eclipse.draw2d.geometry.Point;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;

/**
 * Adds an {@link ExternalFlowModel} to the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class AddExternalFlowOperation extends AbstractOperation<DeskEditPart> {

	/**
	 * Initiate.
	 */
	public AddExternalFlowOperation() {
		super("Add external flow", DeskEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(final Context context) {

		// Create the populated External Flow
		final ExternalFlowModel flow = new ExternalFlowModel();
		BeanDialog dialog = context.getEditPart().createBeanDialog(flow, "X",
				"Y");
		if (!dialog.populate()) {
			// Not created
			return;
		}

		// Set location
		Point location = context.getLocation();
		flow.setX(location.x);
		flow.setY(location.y);

		// Make the change
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				context.getEditPart().getCastedModel().addExternalFlow(flow);
			}

			@Override
			protected void undoCommand() {
				context.getEditPart().getCastedModel().removeExternalFlow(flow);
			}
		});
	}

}
