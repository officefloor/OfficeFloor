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

import net.officefloor.eclipse.woof.editparts.WoofGovernanceEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofModel;

/**
 * Deletes a {@link WoofGovernanceModel} from the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteGovernanceOperation extends AbstractWoofChangeOperation<WoofGovernanceEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public DeleteGovernanceOperation(WoofChanges woofChanges) {
		super("Delete governance", WoofGovernanceEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the governance area to remove
		WoofGovernanceModel governance = context.getEditPart().getCastedModel();

		// Create the change
		Change<WoofGovernanceModel> change = changes.removeGovernance(governance);

		// Return the change
		return change;
	}

}