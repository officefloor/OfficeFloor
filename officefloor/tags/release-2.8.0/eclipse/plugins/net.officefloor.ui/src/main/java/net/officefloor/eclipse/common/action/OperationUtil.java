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
package net.officefloor.eclipse.common.action;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;

/**
 * Utility class to aid working with an {@link Operation}.
 * 
 * @author Daniel Sagenschneider
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
	 *            First selected {@link EditPart} instance to ensure at least
	 *            one.
	 * @param remainingSelectedEditParts
	 *            Remaining {@link EditPart} instances.
	 */
	public static void execute(Operation operation, int x, int y,
			EditPart firstSelectedEditPart,
			EditPart... remainingSelectedEditParts) {

		// Create the listing of selected edit parts
		EditPart[] selectedEditParts = new EditPart[1 + remainingSelectedEditParts.length];
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
	 * <p>
	 * Obtains the {@link Command} from the {@link Operation}.
	 * <p>
	 * Note should the {@link Operation} provide more than one {@link Command},
	 * only the last {@link Command} is returned.
	 * 
	 * @param operation
	 *            {@link Operation}.
	 * @param x
	 *            X of location.
	 * @param y
	 *            Y of location.
	 * @param editPart
	 *            {@link EditPart} to perform the {@link Operation} on.
	 * @return {@link Command} or <code>null</code> if {@link Operation} does
	 *         not provide one.
	 */
	public static Command getCommand(Operation operation, int x, int y,
			EditPart editPart) {

		// Create a command stack to capture the command
		final Command[] commandHolder = new Command[1];
		CommandStack commandStack = new CommandStack() {
			@Override
			public void execute(Command command) {
				// Capture the command
				commandHolder[0] = command;
			}
		};

		// Run the operation to obtain the command
		new OperationAction(commandStack, operation,
				new EditPart[] { editPart }, new Point(x, y)).run();

		// Return the captured command
		return commandHolder[0];
	}

	/**
	 * All access via static methods.
	 */
	private OperationUtil() {
	}
}
