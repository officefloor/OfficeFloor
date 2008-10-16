/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.action;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.draw2d.geometry.Point;

/**
 * Utility class to aid working with an {@link Operation}.
 * 
 * @author Daniel
 */
public class OperationUtil {

	/**
	 * Performs the {@link Operation}.
	 * 
	 * @param operation
	 *            {@link Operation}.
	 * @param x
	 *            X of location.
	 * @param y
	 *            Y of location.
	 * @param firstSelectedEditPart
	 *            First selected {@link AbstractOfficeFloorEditPart} instance to
	 *            ensure at least one.
	 * @param remainingSelectedEditParts
	 *            Remaining {@link AbstractOfficeFloorEditPart} instances.
	 */
	public static void execute(Operation operation, int x, int y,
			AbstractOfficeFloorEditPart<?, ?> firstSelectedEditPart,
			AbstractOfficeFloorEditPart<?, ?>... remainingSelectedEditParts) {

		// Create the listing of selected edit parts
		AbstractOfficeFloorEditPart<?, ?>[] selectedEditParts = new AbstractOfficeFloorEditPart[1 + remainingSelectedEditParts.length];
		selectedEditParts[0] = firstSelectedEditPart;
		for (int i = 0; i < remainingSelectedEditParts.length; i++) {
			selectedEditParts[i + 1] = remainingSelectedEditParts[i];
		}

		// Execute the operation
		new OperationAction(firstSelectedEditPart.getViewer().getEditDomain()
				.getCommandStack(), operation, selectedEditParts, new Point(x,
				y)).run();
	}

	/**
	 * All access via static methods.
	 */
	private OperationUtil() {
	}
}
