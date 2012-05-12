/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.eclipse.woof.editparts.WoofExceptionEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofModel;

/**
 * Deletes a {@link WoofExceptionModel} to the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteExceptionOperation extends
		AbstractWoofChangeOperation<WoofExceptionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public DeleteExceptionOperation(WoofChanges woofChanges) {
		super("Delete exception", WoofExceptionEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the exception to remove
		WoofExceptionModel exception = context.getEditPart().getCastedModel();

		// Create the change
		Change<WoofExceptionModel> change = changes.removeException(exception);

		// Return the change
		return change;
	}

}