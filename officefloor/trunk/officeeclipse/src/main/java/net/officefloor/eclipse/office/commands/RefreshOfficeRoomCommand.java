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
package net.officefloor.eclipse.office.commands;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractSingleCommandFactory;
import net.officefloor.eclipse.common.action.CommandFactory;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.office.OfficeLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;

/**
 * Refreshes the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class RefreshOfficeRoomCommand extends
		AbstractSingleCommandFactory<OfficeRoomModel, OfficeModel> {

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param project
	 *            {@link IProject}.
	 */
	public RefreshOfficeRoomCommand(String actionText, IProject project) {
		super(actionText, OfficeRoomModel.class);
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractSingleCommandFactory#
	 * createCommand(net.officefloor.model.Model, net.officefloor.model.Model)
	 */
	@Override
	protected OfficeFloorCommand createCommand(final OfficeRoomModel model,
			OfficeModel rootModel) {
		return new OfficeFloorCommand() {

			@Override
			public void doCommand() {
				try {

					// Create the Project class loader
					ProjectClassLoader projectClassLoader = ProjectClassLoader
							.create(RefreshOfficeRoomCommand.this.project);

					// Create the office loader
					OfficeLoader officeLoader = new OfficeLoader();

					// Load the office room
					officeLoader.loadOfficeRoom(model, projectClassLoader
							.getConfigurationContext(), projectClassLoader);

				} catch (Throwable ex) {

					// TODO implement, provide message error (or error)
					// (extend Command to provide method invoked from execute to
					// throw exception and handle by message box and possibly
					// eclipse error)
					System.err.println("Failed refreshing the office room");
					ex.printStackTrace();
					throw new UnsupportedOperationException(
							"TODO provide Exception to createCommand of "
									+ CommandFactory.class.getName());
				}
			}

			@Override
			protected void undoCommand() {
				// TODO Implement
				throw new UnsupportedOperationException(
						"TODO implement OfficeFloorCommand.undoCommand");
			}
		};
	}

}
