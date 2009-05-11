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
package net.officefloor.model.impl.office;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.PropertyModel;

/**
 * {@link OfficeChanges} implementation.
 * 
 * @author Daniel
 */
public class OfficeChangesImpl implements OfficeChanges {

	/**
	 * {@link OfficeModel} to change.
	 */
	private final OfficeModel office;

	/**
	 * Initiate.
	 * 
	 * @param office
	 *            {@link OfficeModel} to change.
	 */
	public OfficeChangesImpl(OfficeModel office) {
		this.office = office;
	}

	/*
	 * =================== OfficeChanges =========================
	 */

	@Override
	public Change<OfficeSectionModel> addOfficeSection(
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, OfficeSection officeSection) {

		// TODO test this method (addOfficeSection)

		// Create the office section model
		String sectionName = officeSection.getOfficeSectionName();
		final OfficeSectionModel section = new OfficeSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);
		for (Property property : properties) {
			section.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Add the inputs
		for (OfficeSectionInput input : officeSection.getOfficeSectionInputs()) {
			section.addOfficeSectionInput(new OfficeSectionInputModel(input
					.getOfficeSectionInputName(), input.getParameterType()));
		}

		// Add the outputs
		for (OfficeSectionOutput output : officeSection
				.getOfficeSectionOutputs()) {
			section.addOfficeSectionOutput(new OfficeSectionOutputModel(output
					.getOfficeSectionOutputName(), output.getArgumentType(),
					output.isEscalationOnly()));
		}

		// Add the objects
		for (OfficeSectionObject object : officeSection
				.getOfficeSectionObjects()) {
			section.addOfficeSectionObject(new OfficeSectionObjectModel(object
					.getOfficeSectionObjectName(), object.getObjectType()));
		}

		// Return the change to add the section
		return new AbstractChange<OfficeSectionModel>(section, "Add section") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeSection(section);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeOfficeSection(section);
			}
		};
	}

}