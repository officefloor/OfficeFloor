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
package net.officefloor.eclipse.common.commands;

import org.eclipse.gef.commands.Command;

/**
 * Office Floor {@link Command}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFloorCommand extends Command {

	/**
	 * Default constructor to default details from class type.
	 */
	public OfficeFloorCommand() {
		this.setLabel(this.getClass().getSimpleName());
		this.setDebugLabel(this.getLabel());
	}

	/**
	 * Initiate.
	 * 
	 * @param label
	 *            Label for the command.
	 */
	public OfficeFloorCommand(String label) {
		super(label);
		this.setDebugLabel(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public final void execute() {
		this.doCommand();
	}

	/**
	 * Provides implementation of executing the {@link Command}.
	 */
	protected abstract void doCommand();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public final void undo() {
		this.undoCommand();
	}

	/**
	 * Provides implementation of undoing the {@link Command}.
	 */
	protected abstract void undoCommand();

}
