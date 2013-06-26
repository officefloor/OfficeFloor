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
package net.officefloor.eclipse.woof.operations;

import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.sectionsource.SectionInstance;
import net.officefloor.eclipse.wizard.sectionsource.SectionSourceWizard;
import net.officefloor.eclipse.woof.editparts.WoofSectionEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofSectionOutputModel;

/**
 * {@link Operation} to refactor a {@link WoofSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorSectionOperation extends
		AbstractWoofChangeOperation<WoofSectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public RefactorSectionOperation(WoofChanges woofChanges) {
		super("Refactor section", WoofSectionEditPart.class, woofChanges);
	}

	/*
	 * ==================== AbstractWoofChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the section to refactor
		WoofSectionModel section = context.getEditPart().getCastedModel();

		// Create the existing section to refactor
		OfficeSectionModel existing = new OfficeSectionModel(
				section.getWoofSectionName(),
				section.getSectionSourceClassName(),
				section.getSectionLocation());
		for (PropertyModel property : section.getProperties()) {
			existing.addProperty(new net.officefloor.model.office.PropertyModel(
					property.getName(), property.getValue()));
		}
		for (WoofSectionInputModel input : section.getInputs()) {
			existing.addOfficeSectionInput(new OfficeSectionInputModel(input
					.getWoofSectionInputName(), input.getParameterType()));
		}
		for (WoofSectionOutputModel output : section.getOutputs()) {
			existing.addOfficeSectionOutput(new OfficeSectionOutputModel(output
					.getWoofSectionOutputName(), output.getArgumentType(),
					false));
		}

		// Obtain the refactored section instance
		SectionInstance instance = SectionSourceWizard.getSectionInstance(true,
				context.getEditPart(), new SectionInstance(existing), true);
		if (instance == null) {
			return null; // must have section
		}

		// Obtain section details
		String sectionName = instance.getSectionName();
		String sectionSourceClassName = instance.getSectionSourceClassName();
		String sectionLocation = instance.getSectionLocation();
		PropertyList properties = instance.getPropertylist();
		SectionType sectionType = instance.getSectionType();
		Map<String, String> inputNameMapping = instance.getInputNameMapping();
		Map<String, String> outputNameMapping = instance.getOutputNameMapping();

		// Create change to add section
		Change<WoofSectionModel> change = changes.refactorSection(section,
				sectionName, sectionSourceClassName, sectionLocation,
				properties, sectionType, inputNameMapping, outputNameMapping);

		// Position section
		context.positionModel(change.getTarget());

		// Return change to add the section
		return change;
	}

}