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
 * Abstract {@link CommandFactory} for a single {@link Model} type.
 * 
 * @author Daniel
 */
public abstract class AbstractSingleCommandFactory<M extends Model, R extends Model>
		implements CommandFactory<R> {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * Handled {@link Model} type. Length always one.
	 */
	private final Class<M>[] modelTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param modelTypes
	 *            {@link Model} type being handled.
	 */
	@SuppressWarnings("unchecked")
	public AbstractSingleCommandFactory(String actionText, Class<M> modelType) {
		this.actionText = actionText;
		this.modelTypes = new Class[] { modelType };
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
	public Class<M>[] getModelTypes() {
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
	@SuppressWarnings("unchecked")
	public OfficeFloorCommand[] createCommands(Model[] models, R rootModel) {
		// By Default create the command for each model
		OfficeFloorCommand[] commands = new OfficeFloorCommand[models.length];
		for (int i = 0; i < models.length; i++) {
			M model = (M) models[i];
			commands[i] = this.createCommand(model, rootModel);
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
	protected abstract OfficeFloorCommand createCommand(M model, R rootModel);

}
