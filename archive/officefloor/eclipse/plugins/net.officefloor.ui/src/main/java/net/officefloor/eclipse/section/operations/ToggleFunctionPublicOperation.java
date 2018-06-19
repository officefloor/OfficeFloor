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

import net.officefloor.eclipse.section.editparts.FunctionEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.SectionChanges;

/**
 * Toggles {@link FunctionModel} being public.
 * 
 * @author Daniel Sagenschneider
 */
public class ToggleFunctionPublicOperation extends AbstractSectionChangeOperation<FunctionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public ToggleFunctionPublicOperation(SectionChanges sectionChanges) {
		super("Toggle public", FunctionEditPart.class, sectionChanges);
	}

	/*
	 * =============== AbstractDeskChangeOperation =======================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the function
		FunctionModel function = context.getEditPart().getCastedModel();

		// Return change to toggle public
		return changes.setFunctionAsPublic(!function.getIsPublic(), function);
	}

}