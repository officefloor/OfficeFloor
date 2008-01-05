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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;

/**
 * {@link Action} that invokes the initiated {@link Command} instances.
 * 
 * @author Daniel
 */
public class CommandAction extends Action {

	/**
	 * {@link CommandStack}.
	 */
	private final CommandStack commandStack;

	/**
	 * Listing of {@link Command} instances to execute.
	 */
	private final Command[] commands;

	/**
	 * Initiate.
	 * 
	 * @param text
	 *            Text for this {@link Action}.
	 * @param commandStack
	 *            {@link CommandStack}.
	 * @param commands
	 *            {@link Command} instances to be executed.
	 */
	public CommandAction(String text, CommandStack commandStack,
			Command... commands) {
		super(text);
		this.commandStack = commandStack;
		this.commands = commands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		// Execute the commands
		for (Command command : this.commands) {
			this.commandStack.execute(command);
		}
	}

}
