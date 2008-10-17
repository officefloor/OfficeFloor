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
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.desk.ExternalManagedObjectModel;

/**
 * {@link Operation} to add the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class AddExternalManagedObjectOperation extends
		AbstractOperation<DeskEditPart> {

	/**
	 * Initiate.
	 */
	public AddExternalManagedObjectOperation() {
		super("Add managed object", DeskEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the desk edit part
		final DeskEditPart editPart = context.getEditPart();

		// Create the populated External Managed Object
		final ExternalManagedObjectModel mo = new ExternalManagedObjectModel();
		BeanDialog dialog = editPart.createBeanDialog(mo, "Object Type", "X",
				"Y");
		if (!dialog.populate()) {
			// Not created so do not provide command
			return;
		}

		// Set location
		context.positionModel(mo);

		// Add the external managed object
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().addExternalManagedObject(mo);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().removeExternalManagedObject(mo);
			}
		});
	}

}
