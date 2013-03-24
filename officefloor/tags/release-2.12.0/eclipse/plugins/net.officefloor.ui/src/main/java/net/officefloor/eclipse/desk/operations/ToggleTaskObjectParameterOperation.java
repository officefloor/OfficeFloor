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
package net.officefloor.eclipse.desk.operations;

import net.officefloor.eclipse.desk.editparts.WorkTaskObjectEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * Toggles whether the {@link DeskTaskObjectModel} is a parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class ToggleTaskObjectParameterOperation extends
		AbstractDeskChangeOperation<WorkTaskObjectEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public ToggleTaskObjectParameterOperation(DeskChanges deskChanges) {
		super("Toggle as parameter", WorkTaskObjectEditPart.class, deskChanges);
	}

	/*
	 * ============ AbstractDeskChangeOperation =========================
	 */
	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the work task object
		WorkTaskObjectModel object = context.getEditPart().getCastedModel();

		// Return change to toggle as parameter
		return changes.setObjectAsParameter(!object.getIsParameter(), object);
	}

}