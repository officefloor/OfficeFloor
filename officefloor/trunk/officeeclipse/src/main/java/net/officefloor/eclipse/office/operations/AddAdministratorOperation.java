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
package net.officefloor.eclipse.office.operations;

import org.eclipse.core.resources.IProject;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.AdministratorSourceCreateDialog;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.OfficeModel;

/**
 * Adds an {@link AdministratorModel} to the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class AddAdministratorOperation extends
		AbstractOperation<OfficeEditPart> {

	/**
	 * Initiate.
	 */
	public AddAdministratorOperation() {
		super("Add administrator", OfficeEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the edit part
		final OfficeEditPart editPart = context.getEditPart();

		// Create the Administrator Source
		AdministratorModel administrator = null;
		try {
			AbstractOfficeFloorEditor<?, ?> editor = editPart.getEditor();
			IProject project = ProjectConfigurationContext.getProject(editor
					.getEditorInput());
			AdministratorSourceCreateDialog dialog = new AdministratorSourceCreateDialog(
					editor.getSite().getShell(), project);
			administrator = dialog.createAdministratorSource();
		} catch (Throwable ex) {
			editPart.messageError(ex);
		}

		// Ensure created
		if (administrator == null) {
			return;
		}

		// Set location
		context.positionModel(administrator);

		// Make the change
		final AdministratorModel newAdministrator = administrator;
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().addAdministrator(newAdministrator);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().removeAdministrator(newAdministrator);
			}
		});
	}

}
