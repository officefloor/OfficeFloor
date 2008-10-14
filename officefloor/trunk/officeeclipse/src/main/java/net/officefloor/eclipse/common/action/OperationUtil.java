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

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

import org.eclipse.gef.commands.Command;

/**
 * Utility class to aid working with an {@link Operation}.
 * 
 * @author Daniel
 */
public class OperationUtil {

	/**
	 * Executes the {@link OfficeFloorCommand} instances created from the
	 * {@link Operation}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param operation
	 *            {@link Operation}.
	 * @param selectedEditParts
	 *            Selected {@link AbstractOfficeFloorEditPart} instances.
	 *            Defaults to the editPart if none provided.
	 */
	public static <R extends Model, M extends Model, F extends OfficeFloorFigure> void execute(
			AbstractOfficeFloorEditPart<M, F> editPart, Operation operation,
			AbstractOfficeFloorEditPart<?, ?>... selectedEditParts) {

		// Use the current edit part as selection if none selected
		if ((selectedEditParts == null) || (selectedEditParts.length == 0)) {
			selectedEditParts = new AbstractOfficeFloorEditPart<?, ?>[] { editPart };
		}

		// Create the commands
		OfficeFloorCommand[] commands = operation
				.createCommands(selectedEditParts);
		if ((commands == null) || (commands.length == 0)) {
			// No commands so nothing to execute
			return;
		}

		// Chain the commands together
		Command command = commands[0];
		for (int i = 1; i < commands.length; i++) {
			command = command.chain(commands[i]);
		}

		// Execute the commands
		editPart.getViewer().getEditDomain().getCommandStack().execute(command);
	}

	/**
	 * All access via static methods.
	 */
	private OperationUtil() {
	}
}
