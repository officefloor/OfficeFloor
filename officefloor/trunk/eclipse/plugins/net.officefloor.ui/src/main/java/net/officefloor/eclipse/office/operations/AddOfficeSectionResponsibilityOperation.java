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

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.office.editparts.OfficeSectionEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;

/**
 * {@link Operation} to add an {@link OfficeSectionResponsibilityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddOfficeSectionResponsibilityOperation extends
		AbstractOfficeChangeOperation<OfficeSectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeChanges
	 *            {@link OfficeChanges}.
	 */
	public AddOfficeSectionResponsibilityOperation(OfficeChanges officeChanges) {
		super("Add responsibility", OfficeSectionEditPart.class, officeChanges);
	}

	/*
	 * =================== AbstractOfficeChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(OfficeChanges changes, Context context) {

		// Obtain the office section
		OfficeSectionEditPart editPart = context.getEditPart();
		OfficeSectionModel section = editPart.getCastedModel();

		// Create the populated responsibility
		final OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel();
		BeanDialog dialog = editPart.createBeanDialog(responsibility,
				"Office Team", "X", "Y");
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Create and return change to add the responsibility
		return changes.addOfficeSectionResponsibility(section, responsibility
				.getOfficeSectionResponsibilityName());
	}

}