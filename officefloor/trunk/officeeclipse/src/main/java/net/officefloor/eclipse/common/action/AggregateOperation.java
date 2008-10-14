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

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;

/**
 * {@link Operation} that aggregates {@link Operation} instances.
 * 
 * @author Daniel
 */
public class AggregateOperation implements Operation {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * {@link Operation} instances.
	 */
	private final Operation[] operations;

	/**
	 * Handled {@link EditPart} types.
	 */
	private final Class<? extends EditPart>[] editPartTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param operations
	 *            Listing of {@link Operation} instances.
	 */
	@SuppressWarnings("unchecked")
	public AggregateOperation(String actionText, Operation... operations) {
		this.actionText = actionText;
		this.operations = operations;

		// Create the set of edit part types that are handled
		Set<Class<? extends EditPart>> editPartTypeSet = new HashSet<Class<? extends EditPart>>();
		for (Operation operation : this.operations) {
			editPartTypeSet.addAll(Arrays.asList(operation.getEditPartTypes()));
		}
		this.editPartTypes = editPartTypeSet.toArray(new Class[0]);
	}

	/*
	 * ======================= Operation ===============================
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
	 * @see net.officefloor.eclipse.common.action.Operation#getEditPartTypes()
	 */
	@Override
	public Class<? extends EditPart>[] getEditPartTypes() {
		return this.editPartTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.Operation#createCommands(net.
	 * officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart<?,?>[])
	 */
	@Override
	public OfficeFloorCommand[] createCommands(
			AbstractOfficeFloorEditPart<?, ?>[] editParts) {
		// Create the appropriate commands
		List<OfficeFloorCommand> commands = new LinkedList<OfficeFloorCommand>();
		for (Operation operation : this.operations) {
			for (AbstractOfficeFloorEditPart<?, ?> editPart : editParts) {

				// Determine if command factory handles the model
				boolean isHandle = false;
				for (Class<? extends EditPart> handledEditPartType : operation
						.getEditPartTypes()) {
					if (handledEditPartType
							.isAssignableFrom(handledEditPartType.getClass())) {
						isHandle = true;
					}
				}

				// Add the commands if command factory handles
				if (isHandle) {
					commands
							.addAll(Arrays
									.asList(operation
											.createCommands(new AbstractOfficeFloorEditPart<?, ?>[] { editPart })));
				}
			}
		}

		// Return the created commands
		return commands.toArray(new OfficeFloorCommand[0]);
	}

}
