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

import net.officefloor.eclipse.woof.editparts.WoofSectionEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofSectionModel;

/**
 * Deletes a {@link WoofSectionModel} from the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteSectionOperation extends
		AbstractWoofChangeOperation<WoofSectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public DeleteSectionOperation(WoofChanges woofChanges) {
		super("Delete section", WoofSectionEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the section to remove
		WoofSectionModel section = context.getEditPart().getCastedModel();

		// Create the change
		Change<WoofSectionModel> change = changes.removeSection(section);

		// Return the change
		return change;
	}

}