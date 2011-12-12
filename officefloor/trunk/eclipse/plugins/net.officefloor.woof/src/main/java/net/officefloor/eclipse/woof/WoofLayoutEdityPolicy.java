/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.eclipse.woof;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.layout.MovePositionalModelCommand;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.woof.editparts.WoofGovernanceAreaEditPart;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for the {@link WoofEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLayoutEdityPolicy extends OfficeFloorLayoutEditPolicy {

	/*
	 * =================== LayoutEditPolicy ===================================
	 */

	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {

		// Obtain the bounds of the constraint
		Rectangle rectangle = (Rectangle) constraint;

		// Determine if resizing the Governance Area
		if (child instanceof WoofGovernanceAreaEditPart) {
			// Resize the governance area
			WoofGovernanceAreaEditPart governanceAreaEditPart = (WoofGovernanceAreaEditPart) child;
			return new ResizeWoofGovernanceAreaModelCommand(
					governanceAreaEditPart, rectangle);
		}

		// Obtain the edit part and its positional model
		AbstractOfficeFloorEditPart<?, ?, ?> editPart = (AbstractOfficeFloorEditPart<?, ?, ?>) child;

		// Return the move command
		return new MovePositionalModelCommand(editPart, rectangle.getLocation());
	}

}