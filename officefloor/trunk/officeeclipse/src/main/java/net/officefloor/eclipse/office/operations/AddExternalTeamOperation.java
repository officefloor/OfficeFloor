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

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.model.office.OfficeModel;

/**
 * Adds an {@link ExternalTeamModel} to the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class AddExternalTeamOperation extends AbstractOperation<OfficeEditPart> {

	/**
	 * Initiate.
	 */
	public AddExternalTeamOperation() {
		super("Add team", OfficeEditPart.class);
	}

	@Override
	protected void perform(Context context) {

//		// Obtain the edit part
//		final OfficeEditPart editPart = context.getEditPart();
//
//		// Add the team
//		final ExternalTeamModel team = new ExternalTeamModel();
//		BeanDialog dialog = editPart.createBeanDialog(team, "X", "Y");
//		if (!dialog.populate()) {
//			// Not created
//			return;
//		}
//		
//		// Set location
//		context.positionModel(team);
//
//		// Make changes
//		context.execute(new OfficeFloorCommand() {
//
//			@Override
//			protected void doCommand() {
//				editPart.getCastedModel().addExternalTeam(team);
//			}
//
//			@Override
//			protected void undoCommand() {
//				editPart.getCastedModel().removeExternalTeam(team);
//			}
//		});
	}

}