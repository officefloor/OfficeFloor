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

import net.officefloor.eclipse.common.editparts.RemovableEditPart;

import org.eclipse.gef.commands.Command;

/**
 * Deletes an {@link org.eclipse.gef.EditPart} (and subsequently its containd
 * model).
 * 
 * @author Daniel
 */
public class DeleteEditPartCommand extends Command {

	/**
	 * Edit part to delete.
	 */
	protected final RemovableEditPart editPart;

	/**
	 * Initiate with {@link org.eclipse.gef.EditPart} to delete.
	 * 
	 * @param editPart
	 *            {@link org.eclipse.gef.EditPart} to delete.
	 */
	public DeleteEditPartCommand(RemovableEditPart editPart) {
		// Store state
		this.editPart = editPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// Delete the edit part
		this.editPart.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		// Undelete the edit part
		this.editPart.undelete();
	}

}
