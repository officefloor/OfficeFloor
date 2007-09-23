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
package net.officefloor.eclipse.common.commands;

import org.eclipse.gef.commands.Command;

/**
 * Command to assign
 * 
 * @author Daniel
 */
public abstract class AssignCommand<I, V> extends Command {

	/**
	 * Item to have the value assigned.
	 */
	protected final I item;

	/**
	 * Value to assign to the item.
	 */
	protected final V value;

	/**
	 * Initiate with Item to have the Value assigned.
	 * 
	 * @param item
	 *            Item to have value assigned.
	 * @param value
	 *            Value to assign to the item.
	 */
	public AssignCommand(I item, V value) {
		super("Assign");

		// Store state
		this.item = item;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		this.assign(this.item, this.value);
	}

	/**
	 * Assign the value to the item.
	 * 
	 * @param item
	 *            Item.
	 * @param value
	 *            Value.
	 */
	protected abstract void assign(I item, V value);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		this.unassign(this.item, this.value);
	}

	/**
	 * Unassigns the value from the item.
	 * 
	 * @param item
	 *            Item.
	 * @param value
	 *            Value.
	 */
	protected abstract void unassign(I item, V value);
}
