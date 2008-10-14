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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;

/**
 * Abstract {@link Operation} that creates a {@link OfficeFloorCommand} per
 * {@link EditPart}.
 * 
 * @author Daniel
 */
public abstract class AbstractMultipleOperation implements Operation {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * Handled {@link EditPart} types.
	 */
	private final Class<? extends EditPart>[] editPartTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param editPartTypes
	 *            {@link EditPart} types being handled.
	 */
	public AbstractMultipleOperation(String actionText,
			Class<? extends EditPart>... editPartTypes) {
		this.actionText = actionText;
		this.editPartTypes = editPartTypes;
	}

	/*
	 * ======================= Operation ============================
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
		// By default create the command for each model
		List<OfficeFloorCommand> list = new LinkedList<OfficeFloorCommand>();
		for (AbstractOfficeFloorEditPart<?, ?> editPart : editParts) {
			OfficeFloorCommand command = this.createCommand(editPart);
			if (command != null) {
				list.add(command);
			}
		}
		OfficeFloorCommand[] commands = list.toArray(new OfficeFloorCommand[0]);

		// Return the commands
		return commands;
	}

	/**
	 * Creates a {@link OfficeFloorCommand} for the {@link EditPart} passed in.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to create the
	 *            {@link OfficeFloorCommand}.
	 * @return {@link OfficeFloorCommand} or <code>null</code> if nothing to
	 *         execute.
	 */
	protected abstract OfficeFloorCommand createCommand(
			AbstractOfficeFloorEditPart<?, ?> editPart);

}
