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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.Action;

import net.officefloor.model.Model;

/**
 * {@link CommandFactory} that aggregates {@link CommandFactory} instances.
 * 
 * @author Daniel
 */
public class AggregateCommandFactory<R extends Model> implements
		CommandFactory<R> {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * {@link CommandFactory} instances.
	 */
	private final CommandFactory<R>[] commandFactories;

	/**
	 * Handled {@link Model} types.
	 */
	private final Class<? extends Model>[] modelTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param commandFactories
	 *            Listing of {@link CommandFactory} instances.
	 */
	@SuppressWarnings("unchecked")
	public AggregateCommandFactory(String actionText,
			CommandFactory<R>... commandFactories) {
		this.actionText = actionText;
		this.commandFactories = commandFactories;

		// Create the set of model types that are handled
		Set<Class<? extends Model>> modelTypeSet = new HashSet<Class<? extends Model>>();
		for (CommandFactory<R> commandFactory : this.commandFactories) {
			modelTypeSet.addAll(Arrays.asList(commandFactory.getModelTypes()));
		}
		this.modelTypes = modelTypeSet.toArray(new Class[0]);
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
	 * @see net.officefloor.eclipse.common.action.CommandFactory#createCommands(net.officefloor.model.Model[],
	 *      net.officefloor.model.Model)
	 */
	@Override
	public Command[] createCommands(Model[] models, R rootModel) {
		// Create the appropriate commands
		List<Command> commands = new LinkedList<Command>();
		for (CommandFactory<R> commandFactory : this.commandFactories) {
			for (Model model : models) {

				// Determine if command factory handles the model
				boolean isHandle = false;
				for (Class<? extends Model> handledModelType : commandFactory
						.getModelTypes()) {
					if (handledModelType.isAssignableFrom(model.getClass())) {
						isHandle = true;
					}
				}

				// Add the commands if command factory handles
				if (isHandle) {
					commands.addAll(Arrays.asList(commandFactory
							.createCommands(new Model[] { model }, rootModel)));
				}
			}
		}

		// Return the created commands
		return commands.toArray(new Command[0]);
	}
}
