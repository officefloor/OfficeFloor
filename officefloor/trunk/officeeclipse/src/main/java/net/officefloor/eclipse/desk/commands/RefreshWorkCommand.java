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
package net.officefloor.eclipse.desk.commands;

import net.officefloor.desk.DeskLoader;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractSingleCommandFactory;
import net.officefloor.eclipse.common.action.CommandFactory;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskWorkModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.Action;

/**
 * Refreshes the {@link DeskWorkModel}.
 * 
 * @author Daniel
 */
public class RefreshWorkCommand extends
		AbstractSingleCommandFactory<DeskWorkModel, DeskModel> {

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
	public RefreshWorkCommand(String actionText, IProject project) {
		super(actionText, DeskWorkModel.class);
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.AbstractSingleCommandFactory#createCommand(net.officefloor.model.Model,
	 *      net.officefloor.model.Model)
	 */
	@Override
	protected Command createCommand(final DeskWorkModel model,
			DeskModel rootModel) {
		return new Command() {
			@Override
			public void execute() {
				// Create the Project class loader
				ProjectClassLoader projectClassLoader = ProjectClassLoader
						.create(RefreshWorkCommand.this.project);

				// Create the desk loader
				DeskLoader deskLoader = new DeskLoader(projectClassLoader);

				// Load the work (which does the synchronising)
				try {
					deskLoader.loadWork(model, projectClassLoader
							.getConfigurationContext());
				} catch (Throwable ex) {

					// TODO implement, provide message error (or error)
					// (extend Command to provide method invoked from execute to
					// throw exception and handle by message box and possibly
					// eclipse error)
					System.err.println("Failed refreshing the work");
					ex.printStackTrace();
					throw new UnsupportedOperationException(
							"TODO provide Exception to createCommand of "
									+ CommandFactory.class.getName());
				}
			}
		};
	}

}
