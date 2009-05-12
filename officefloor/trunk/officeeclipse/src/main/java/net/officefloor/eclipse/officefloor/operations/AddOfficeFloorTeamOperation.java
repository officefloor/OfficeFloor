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

import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.eclipse.wizard.teamsource.TeamInstance;
import net.officefloor.eclipse.wizard.teamsource.TeamSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;

/**
 * Adds a {@link TeamModel} to the {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class AddOfficeFloorTeamOperation extends
		AbstractOfficeFloorChangeOperation<OfficeFloorEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeFloorChanges
	 *            {@link OfficeFloorChanges}.
	 */
	public AddOfficeFloorTeamOperation(OfficeFloorChanges officeFloorChanges) {
		super("Add team", OfficeFloorEditPart.class, officeFloorChanges);
	}

	/*
	 * =============== AbstractOfficeFloorChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(OfficeFloorChanges changes, Context context) {

		// Obtain the team instance
		TeamInstance team = TeamSourceWizard.getTeamInstance(context
				.getEditPart(), null);
		if (team == null) {
			return null; // must have team
		}

		// Create change to add the team
		Change<OfficeFloorTeamModel> change = changes.addOfficeFloorTeam(team
				.getTeamName(), team.getTeamSourceClassName(), team
				.getPropertylist(), team.getTeamType());

		// Position the team
		context.positionModel(change.getTarget());

		// Return change to add the team
		return change;
	}

}