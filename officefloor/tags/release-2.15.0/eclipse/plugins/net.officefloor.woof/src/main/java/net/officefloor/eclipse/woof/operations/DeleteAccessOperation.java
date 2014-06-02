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

import net.officefloor.eclipse.woof.editparts.WoofAccessEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofModel;

/**
 * Deletes a {@link WoofAccessModel} from the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteAccessOperation extends
		AbstractWoofChangeOperation<WoofAccessEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public DeleteAccessOperation(WoofChanges woofChanges) {
		super("Delete access", WoofAccessEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the access to remove
		WoofAccessModel access = context.getEditPart().getCastedModel();

		// Create the change
		Change<WoofAccessModel> change = changes.removeAccess(access);

		// Return the change
		return change;
	}

}