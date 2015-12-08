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
package net.officefloor.eclipse.officefloor.operations;

import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.eclipse.wizard.officesource.OfficeInstance;
import net.officefloor.eclipse.wizard.officesource.OfficeSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * Adds an {@link DeployedOfficeModel} to a {@link OfficeFloorModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddDeployedOfficeOperation extends
		AbstractOfficeFloorChangeOperation<OfficeFloorEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeFloorChanges
	 *            {@link OfficeFloorChanges}.
	 */
	public AddDeployedOfficeOperation(OfficeFloorChanges officeFloorChanges) {
		super("Add office", OfficeFloorEditPart.class, officeFloorChanges);
	}

	/*
	 * ================ AbstractOfficeFloorChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(OfficeFloorChanges changes, Context context) {

		// Obtain the office instance
		OfficeInstance office = OfficeSourceWizard.getOfficeInstance(
				context.getEditPart(), null);
		if (office == null) {
			return null; // must have office
		}

		// Create change to add the office
		Change<DeployedOfficeModel> change = changes.addDeployedOffice(
				office.getOfficeName(), office.getOfficeSourceClassName(),
				office.getOfficeLocation(), office.getPropertylist(),
				office.getOfficeType());

		// Position the office
		context.positionModel(change.getTarget());

		// Return the change to add the office
		return change;
	}

}