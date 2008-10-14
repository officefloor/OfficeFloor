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

import org.eclipse.core.resources.IProject;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.AbstractSingleOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.DeskWorkCreateDialog;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskWorkModel;

/**
 * Adds a {@link DeskWorkModel} to the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class AddWorkOperation extends AbstractSingleOperation<DeskEditPart> {

	/**
	 * Initiate.
	 */
	public AddWorkOperation() {
		super("Add work", DeskEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.action.AbstractSingleOperation#createCommand
	 * (net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart)
	 */
	@Override
	protected OfficeFloorCommand createCommand(final DeskEditPart editPart) {

		// Create the work
		DeskWorkModel deskWork = null;
		try {
			AbstractOfficeFloorEditor<?, ?> editor = editPart.getEditor();
			IProject project = ProjectConfigurationContext.getProject(editor
					.getEditorInput());
			deskWork = new DeskWorkCreateDialog(editor.getSite().getShell(),
					project).createDeskWork();

		} catch (Exception ex) {
			editPart.messageError(ex);
		}

		// Ensure have the work
		if (deskWork == null) {
			return null;
		}

		// Add the work
		final DeskWorkModel work = deskWork;
		return new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().addWork(work);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().removeWork(work);
			}
		};
	}

}
