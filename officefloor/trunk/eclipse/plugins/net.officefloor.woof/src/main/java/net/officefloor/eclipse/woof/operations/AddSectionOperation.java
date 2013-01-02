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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.sectionsource.SectionInstance;
import net.officefloor.eclipse.wizard.sectionsource.SectionSourceWizard;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofSectionModel;

/**
 * {@link Operation} to add a {@link WoofSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddSectionOperation extends
		AbstractWoofChangeOperation<WoofEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public AddSectionOperation(WoofChanges woofChanges) {
		super("Add section", WoofEditPart.class, woofChanges);
	}

	/*
	 * ==================== AbstractWoofChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the section instance
		SectionInstance instance = SectionSourceWizard.getSectionInstance(true,
				context.getEditPart(), null, true);
		if (instance == null) {
			return null; // must have section
		}

		// Obtain section details
		String sectionName = instance.getSectionName();
		String sectionSourceClassName = instance.getSectionSourceClassName();
		String sectionLocation = instance.getSectionLocation();
		PropertyList properties = instance.getPropertylist();
		SectionType sectionType = instance.getSectionType();

		// URI's provided after section creation
		Map<String, String> inputToUri = new HashMap<String, String>();

		// Create change to add section
		Change<WoofSectionModel> change = changes.addSection(sectionName,
				sectionSourceClassName, sectionLocation, properties,
				sectionType, inputToUri);

		// Position section
		context.positionModel(change.getTarget());

		// Return change to add the section
		return change;
	}

}