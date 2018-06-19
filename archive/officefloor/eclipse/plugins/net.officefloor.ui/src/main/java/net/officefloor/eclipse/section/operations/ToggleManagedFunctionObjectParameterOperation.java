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

import net.officefloor.eclipse.section.editparts.ManagedFunctionObjectEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.SectionChanges;

/**
 * Toggles whether the {@link ManagedFunctionObjectModel} is a parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class ToggleManagedFunctionObjectParameterOperation
		extends AbstractSectionChangeOperation<ManagedFunctionObjectEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public ToggleManagedFunctionObjectParameterOperation(SectionChanges sectionChanges) {
		super("Toggle as parameter", ManagedFunctionObjectEditPart.class, sectionChanges);
	}

	/*
	 * ============ AbstractDeskChangeOperation =========================
	 */
	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the managed function object
		ManagedFunctionObjectModel object = context.getEditPart().getCastedModel();

		// Return change to toggle as parameter
		return changes.setObjectAsParameter(!object.getIsParameter(), object);
	}

}