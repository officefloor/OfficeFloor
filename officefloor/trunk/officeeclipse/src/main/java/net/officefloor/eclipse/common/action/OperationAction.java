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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;

/**
 * {@link Action} that invokes the created {@link OfficeFloorCommand} instances.
 * 
 * @author Daniel
 */
public class OperationAction extends Action {

	/**
	 * {@link CommandStack} to execute the {@link OfficeFloorCommand} instances.
	 */
	private final CommandStack commandStack;

	/**
	 * {@link Operation} to create the {@link OfficeFloorCommand} instances.
	 */
	private final Operation operation;

	/**
	 * Selected {@link EditPart} instances.
	 */
	private final EditPart[] selectedEditParts;

	/**
	 * Location.
	 */
	private final Point location;

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
	 * @param location
	 *            Location.
	 */
	public OperationAction(CommandStack commandStack, Operation operation,
			EditPart[] selectedEditParts,
			Point location) {
		super(operation.getActionText());
		this.commandStack = commandStack;
		this.operation = operation;
		this.selectedEditParts = selectedEditParts;
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		// Create the operation context
		OperationContextImpl context = new OperationContextImpl();

		// Perform the operation
		this.operation.perform(context);

		// Obtain the commands
		OfficeFloorCommand[] commands = context.commands
				.toArray(new OfficeFloorCommand[0]);
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

	/**
	 * {@link OperationContext}.
	 */
	private class OperationContextImpl implements OperationContext {

		/**
		 * {@link OfficeFloorCommand} instances.
		 */
		public final List<OfficeFloorCommand> commands = new LinkedList<OfficeFloorCommand>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.eclipse.common.action.OperationContext#getEditParts()
		 */
		@Override
		public EditPart[] getEditParts() {
			return OperationAction.this.selectedEditParts;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.eclipse.common.action.OperationContext#getLocation()
		 */
		@Override
		public Point getLocation() {
			return OperationAction.this.location;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.eclipse.common.action.OperationContext#execute(net
		 * .officefloor.eclipse.common.commands.OfficeFloorCommand)
		 */
		@Override
		public void execute(OfficeFloorCommand command) {
			this.commands.add(command);
		}
	}

}
