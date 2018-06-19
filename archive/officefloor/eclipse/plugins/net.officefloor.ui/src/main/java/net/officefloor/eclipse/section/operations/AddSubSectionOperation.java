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
package net.officefloor.eclipse.section.operations;

import net.officefloor.eclipse.section.editparts.SectionEditPart;
import net.officefloor.eclipse.wizard.sectionsource.SectionInstance;
import net.officefloor.eclipse.wizard.sectionsource.SectionSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Adds a {@link SubSectionModel} to the {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddSubSectionOperation extends
		AbstractSectionChangeOperation<SectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public AddSubSectionOperation(SectionChanges sectionChanges) {
		super("Add sub section", SectionEditPart.class, sectionChanges);
	}

	/*
	 * ================== AbstractSectionChangeOperation ============
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the section instance with the section type
		SectionInstance section = SectionSourceWizard.loadSectionType(
				context.getEditPart(), null, false);
		if (section == null) {
			return null; // must have section
		}

		// Obtain the add sub section change
		Change<SubSectionModel> change = changes.addSubSection(
				section.getSectionName(), section.getSectionSourceClassName(),
				section.getSectionLocation(), section.getPropertylist(),
				section.getSectionType());

		// Position the section
		context.positionModel(change.getTarget());

		// Return change to add sub section
		return change;
	}

}