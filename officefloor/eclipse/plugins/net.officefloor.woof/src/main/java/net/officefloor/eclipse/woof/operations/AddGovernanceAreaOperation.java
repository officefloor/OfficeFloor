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
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;

/**
 * Adds a {@link WoofGovernanceAreaModel} to the {@link WoofGovernanceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddGovernanceAreaOperation extends AbstractWoofChangeOperation<WoofGovernanceEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public AddGovernanceAreaOperation(WoofChanges woofChanges) {
		super("Add governance area", WoofGovernanceEditPart.class, woofChanges);
	}

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the governance
		WoofGovernanceModel governance = context.getEditPart().getCastedModel();

		// Create change to add the governance area
		Change<WoofGovernanceAreaModel> change = changes.addGovernanceArea(governance, 100, 100);

		// Position area just offset to governance location
		WoofGovernanceAreaModel area = change.getTarget();
		area.setX(governance.getX() + 20);
		area.setY(governance.getY() + 20);

		// Return change to add governance area
		return change;
	}

}