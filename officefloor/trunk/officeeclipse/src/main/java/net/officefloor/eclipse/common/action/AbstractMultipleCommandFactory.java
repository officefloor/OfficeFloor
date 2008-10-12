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
import org.eclipse.jface.action.Action;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.model.Model;

/**
 * Abstract {@link CommandFactory}.
 * 
 * @author Daniel
 */
public abstract class AbstractMultipleCommandFactory<R extends Model>
		implements CommandFactory<R> {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * Handled {@link Model} types.
	 */
	private final Class<? extends Model>[] modelTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param modelTypes
	 *            {@link Model} types being handled.
	 */
	public AbstractMultipleCommandFactory(String actionText,
			Class<? extends Model>... modelTypes) {
		this.actionText = actionText;
		this.modelTypes = modelTypes;
	}

	/*
	 * =====================================================================
	 * CommandFactory
	 * =====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.CommandFactory#getActionText()
	 */
	@Override
	public String getActionText() {
		return this.actionText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.CommandFactory#getModelTypes()
	 */
	@Override
	public Class<? extends Model>[] getModelTypes() {
		return this.modelTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.action.CommandFactory#createCommands(net
	 * .officefloor.model.Model[], net.officefloor.model.Model)
	 */
	@Override
	public OfficeFloorCommand[] createCommands(Model[] models, R rootModel) {
		// By default create the command for each model
		OfficeFloorCommand[] commands = new OfficeFloorCommand[models.length];
		for (int i = 0; i < models.length; i++) {
			commands[i] = this.createCommand(models[i], rootModel);
		}

		// Return the commands
		return commands;
	}

	/**
	 * Creates a {@link OfficeFloorCommand} for the {@link Model} passed in.
	 * 
	 * @param model
	 *            {@link Model} to create the {@link Command}.
	 * @param rootModel
	 *            Root {@link Model}.
	 * @return {@link OfficeFloorCommand}.
	 */
	protected abstract OfficeFloorCommand createCommand(Model model, R rootModel);

}
