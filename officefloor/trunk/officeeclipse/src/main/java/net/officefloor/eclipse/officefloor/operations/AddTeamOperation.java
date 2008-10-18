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
package net.officefloor.eclipse.officefloor.operations;

import org.eclipse.core.resources.IProject;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.TeamCreateDialog;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.TeamModel;

/**
 * Adds a {@link TeamModel} to the {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class AddTeamOperation extends AbstractOperation<OfficeFloorEditPart> {

	/**
	 * Initiate.
	 */
	public AddTeamOperation() {
		super("Add team", OfficeFloorEditPart.class);
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
		final OfficeFloorEditPart editPart = context.getEditPart();

		// Create the Team
		TeamModel team = null;
		try {
			AbstractOfficeFloorEditor<?, ?> editor = editPart.getEditor();
			IProject project = ProjectConfigurationContext.getProject(editor
					.getEditorInput());
			TeamCreateDialog dialog = new TeamCreateDialog(editor.getSite()
					.getShell(), project);
			team = dialog.createTeam();
		} catch (Exception ex) {
			editPart.messageError(ex);
		}

		// Ensure created
		if (team == null) {
			return;
		}

		// Set location
		context.positionModel(team);

		// Make changes
		final TeamModel newTeam = team;
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().addTeam(newTeam);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().removeTeam(newTeam);
			}
		});
	}

}
