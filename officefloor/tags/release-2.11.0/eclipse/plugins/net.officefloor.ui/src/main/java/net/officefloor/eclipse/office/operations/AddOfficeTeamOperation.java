/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.office.operations;

import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeTeamModel;

/**
 * Adds an {@link ExternalTeamModel} to the {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddOfficeTeamOperation extends
		AbstractOfficeChangeOperation<OfficeEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeChanges
	 *            {@link OfficeChanges}.
	 */
	public AddOfficeTeamOperation(OfficeChanges officeChanges) {
		super("Add team", OfficeEditPart.class, officeChanges);
	}

	/*
	 * ============ AbstractOfficeChangeOperation =========================
	 */

	@Override
	protected Change<?> getChange(OfficeChanges changes, Context context) {

		// Obtain the office edit part
		final OfficeEditPart editPart = context.getEditPart();

		// Create the populated Office Team
		final OfficeTeamModel team = new OfficeTeamModel();
		BeanDialog dialog = editPart.createBeanDialog(team, "X", "Y");
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Add the office team
		Change<OfficeTeamModel> change = changes.addOfficeTeam(team
				.getOfficeTeamName());

		// Position the model
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}