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
 * Provides abstract function to create a new model.
 * 
 * @author Daniel
 */
public abstract class CreateCommand<P, M> extends Command {

	/**
	 * Parent to receive the new model.
	 */
	protected P parent;

	/**
	 * New model to add to parent.
	 */
	protected M newModel;

	/**
	 * Specifies the parent.
	 * 
	 * @param parent
	 *            Parent.
	 */
	public void setParent(P parent) {
		this.parent = parent;
	}

	/**
	 * Specifies the new model to create on the parent.
	 * 
	 * @param newModel
	 *            New model.
	 */
	public void setNewModel(M newModel) {
		this.newModel = newModel;
	}

	/**
	 * Convenience method for the creation so that sub classes do no require
	 * specifying the constructor.
	 * 
	 * @param parent
	 *            Parent.
	 * @param newModel
	 *            New Model
	 * @return this.
	 */
	public CreateCommand<P, M> init(P parent, M newModel) {
		// Store state
		this.setParent(parent);
		this.setNewModel(newModel);

		// Return this instance
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		this.create(this.parent, this.newModel);
	}

	/**
	 * Creates the new Model on the parent.
	 * 
	 * @param parent
	 *            Parent.
	 * @param newModel
	 *            New model.
	 */
	protected abstract void create(P parent, M newModel);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		this.remove(this.parent, this.newModel);
	}

	/**
	 * Removes the new Model (child) from the parent.
	 * 
	 * @param parent
	 *            Parent.
	 * @param child
	 *            Child to remove.
	 */
	protected abstract void remove(P parent, M child);

}
