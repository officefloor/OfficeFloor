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

import net.officefloor.eclipse.woof.editparts.WoofGovernanceAreaEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofModel;

/**
 * Deletes a {@link WoofGovernanceAreaModel} from the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteGovernanceAreaOperation extends
		AbstractWoofChangeOperation<WoofGovernanceAreaEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public DeleteGovernanceAreaOperation(WoofChanges woofChanges) {
		super("Delete governance area", WoofGovernanceAreaEditPart.class,
				woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the governance area to remove
		WoofGovernanceAreaModel governanceArea = context.getEditPart()
				.getCastedModel();

		// Create the change
		Change<WoofGovernanceAreaModel> change = changes
				.removeGovernanceArea(governanceArea);

		// Return the change
		return change;
	}

}