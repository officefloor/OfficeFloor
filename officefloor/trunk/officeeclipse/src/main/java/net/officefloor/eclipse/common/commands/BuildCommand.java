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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.gef.commands.Command;

/**
 * {@link org.eclipse.gef.commands.Command} to build.
 * 
 * @author Daniel
 */
public class BuildCommand extends Command {

	/**
	 * Items to be built.
	 */
	protected final List<?> items;

	/**
	 * {@link IProject} items reside within.
	 */
	protected final IProject project;

	/**
	 * Initiate with items to build.
	 * 
	 * @param items
	 *            Items to build.
	 * @param project
	 *            {@link IProject} containing the items to build.
	 */
	public BuildCommand(List<?> items, IProject project) {
		super("Build");

		// Store state
		this.items = items;
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	public boolean canUndo() {
		// Can not be undone
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// Build the items
		// OfficeFloorBuilder.getInstance().build(this.items,
		// new ProjectConfigurationContext(this.project));

		// TODO implement
		throw new UnsupportedOperationException("TODO implement 'execute'");
	}

}
