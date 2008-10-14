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
import net.officefloor.model.Model;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;

/**
 * {@link Action} that invokes the created {@link OfficeFloorCommand} instances.
 * 
 * @author Daniel
 */
public class OperationAction<R extends Model> extends Action {

	/**
	 * {@link CommandStack} to execute the {@link OfficeFloorCommand} instances.
	 */
	private final CommandStack commandStack;

	/**
	 * {@link Operation} to create the {@link OfficeFloorCommand} instances.
	 */
	private final Operation operation;

	/**
	 * Selected {@link Model} instances.
	 */
	private final AbstractOfficeFloorEditPart<?, ?>[] selectedEditParts;

	/**
	 * Initiate.
	 * 
	 * @param commandStack
	 *            {@link CommandStack}.
	 * @param operation
	 *            {@link Operation} to create the {@link OfficeFloorCommand}
	 *            instances.
	 * @param selectedEditParts
	 *            Selected {@link AbstractOfficeFloorEditPart} instances.
	 */
	public OperationAction(CommandStack commandStack, Operation operation,
			AbstractOfficeFloorEditPart<?, ?>[] selectedEditParts) {
		super(operation.getActionText());
		this.commandStack = commandStack;
		this.operation = operation;
		this.selectedEditParts = selectedEditParts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		// Obtain the commands
		OfficeFloorCommand[] commands = this.operation
				.createCommands(this.selectedEditParts);
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
		this.commandStack.execute(command);
	}

}
