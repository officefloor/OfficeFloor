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

import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.office.editparts.OfficeSectionEditPart;
import net.officefloor.eclipse.wizard.sectionsource.SectionInstance;
import net.officefloor.eclipse.wizard.sectionsource.SectionSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeSectionModel;

/**
 * {@link Operation} to refactor an {@link OfficeSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeSectionChangeOperation extends
		AbstractOfficeChangeOperation<OfficeSectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeChanges
	 *            {@link OfficeChanges}.
	 */
	public RefactorOfficeSectionChangeOperation(OfficeChanges officeChanges) {
		super("Refactor Section", OfficeSectionEditPart.class, officeChanges);
	}

	/*
	 * ================= AbstractOfficeChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(OfficeChanges changes, Context context) {

		// Obtain the office section
		OfficeSectionEditPart editPart = context.getEditPart();
		OfficeSectionModel officeSection = editPart.getCastedModel();

		// Obtain the refactored office section
		SectionInstance sectionInstance = SectionSourceWizard
				.loadOfficeSection(editPart,
						new SectionInstance(officeSection), false);
		if (sectionInstance == null) {
			return null; // section not being refactored
		}

		// Obtain the mappings
		Map<String, String> inputNameMapping = sectionInstance
				.getInputNameMapping();
		Map<String, String> outputNameMapping = sectionInstance
				.getOutputNameMapping();
		Map<String, String> objectNameMapping = sectionInstance
				.getObjectNameMapping();

		// Return change to refactor the office section
		return changes.refactorOfficeSection(officeSection,
				sectionInstance.getSectionName(),
				sectionInstance.getSectionSourceClassName(),
				sectionInstance.getSectionLocation(),
				sectionInstance.getPropertylist(),
				sectionInstance.getOfficeSection(), inputNameMapping,
				outputNameMapping, objectNameMapping);
	}

}