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

import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.eclipse.wizard.sectionsource.SectionInstance;
import net.officefloor.eclipse.wizard.sectionsource.SectionSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionModel;

/**
 * Adds an {@link OfficeSectionModel} to the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class AddOfficeSectionOperation extends
		AbstractOfficeChangeOperation<OfficeEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeChanges
	 *            {@link OfficeChanges}.
	 */
	public AddOfficeSectionOperation(OfficeChanges officeChanges) {
		super("Add section", OfficeEditPart.class, officeChanges);
	}

	/*
	 * ================ AbstractOfficeChangeOperation =================
	 */
	@Override
	protected Change<?> getChange(OfficeChanges changes, Context context) {

		// Obtain the section instance with the office section
		SectionInstance section = SectionSourceWizard.loadOfficeSection(context
				.getEditPart(), null);
		if (section == null) {
			return null; // must have section
		}

		// Obtain the add office section change
		Change<OfficeSectionModel> change = changes.addOfficeSection(section
				.getSectionSourceClassName(), section.getSectionLocation(),
				section.getPropertylist(), section.getOfficeSection());

		// Position the office section
		context.positionModel(change.getTarget());

		// Return change to add office section
		return change;
	}

}