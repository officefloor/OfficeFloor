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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

/**
 * Moves a figure.
 * 
 * @author Daniel
 */
public class MovePositionalModelCommand extends Command {

	/**
	 * Edit part to be moved.
	 */
	protected final AbstractOfficeFloorEditPart editPart;

	/**
	 * New position for the figure.
	 */
	protected final Point newLocation;

	/**
	 * Current (old) position for the edit part.
	 */
	protected final Point oldLocation;

	/**
	 * Initiate to move a position model.
	 * 
	 * @param editPart
	 *            Edit part to move.
	 * @param location
	 *            Location to move the edit part.
	 */
	public MovePositionalModelCommand(AbstractOfficeFloorEditPart editPart,
			Point location) {
		super("Move");

		// Store state
		this.editPart = editPart;
		this.newLocation = new Point(location);
		this.oldLocation = new Point(editPart.getFigure().getBounds()
				.getLocation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// Move to new location
		this.move(this.newLocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		// Move back to old location
		this.move(this.oldLocation);
	}

	/**
	 * Moves the edit part.
	 * 
	 * @param constraints
	 *            Constraints for the edit part.
	 */
	protected void move(Point location) {
		// Specify location for edit part
		this.editPart.setLocation(location);
	}

}
