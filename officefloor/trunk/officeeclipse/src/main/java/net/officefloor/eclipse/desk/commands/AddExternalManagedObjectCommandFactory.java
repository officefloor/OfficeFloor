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
package net.officefloor.eclipse.desk.commands;

import net.officefloor.eclipse.common.action.AbstractSingleCommandFactory;
import net.officefloor.eclipse.common.action.CommandFactory;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;

/**
 * {@link CommandFactory} to add the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class AddExternalManagedObjectCommandFactory extends
		AbstractSingleCommandFactory<DeskModel, DeskModel> {

	/**
	 * {@link DeskEditPart}.
	 */
	private final DeskEditPart deskEditPart;

	/**
	 * Initiate.
	 * 
	 * @param deskEditPart
	 *            {@link DeskEditPart}.
	 */
	public AddExternalManagedObjectCommandFactory(DeskEditPart deskEditPart) {
		super("Add managed object", DeskModel.class);
		this.deskEditPart = deskEditPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractSingleCommandFactory#
	 * createCommand(net.officefloor.model.Model, net.officefloor.model.Model)
	 */
	@Override
	protected OfficeFloorCommand createCommand(final DeskModel model,
			DeskModel rootModel) {

		// Create the populated External Managed Object
		final ExternalManagedObjectModel mo = new ExternalManagedObjectModel();
		BeanDialog dialog = AddExternalManagedObjectCommandFactory.this.deskEditPart
				.createBeanDialog(mo, "Object Type", "X", "Y");
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Add the external managed object
		return new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				model.addExternalManagedObject(mo);
			}

			@Override
			protected void undoCommand() {
				model.removeExternalManagedObject(mo);
			}
		};
	}

}
