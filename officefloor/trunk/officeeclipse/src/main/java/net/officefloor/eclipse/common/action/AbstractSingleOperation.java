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
import net.officefloor.model.Model;

import org.eclipse.jface.action.Action;

/**
 * Abstract {@link Operation} for a single {@link Model} type.
 * 
 * @author Daniel
 */
public abstract class AbstractSingleOperation<E extends AbstractOfficeFloorEditPart<?, ?>>
		implements Operation {

	/**
	 * Text for the {@link Action}.
	 */
	private final String actionText;

	/**
	 * Handled {@link AbstractOfficeFloorEditPart} type. Length always one.
	 */
	private final Class<E>[] editPartTypes;

	/**
	 * Initiate.
	 * 
	 * @param actionText
	 *            {@link Action} text.
	 * @param editPartType
	 *            {@link AbstractOfficeFloorEditPart} type being handled.
	 */
	@SuppressWarnings("unchecked")
	public AbstractSingleOperation(String actionText, Class<E> editPartType) {
		this.actionText = actionText;
		this.editPartTypes = new Class[] { editPartType };
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
	public Class<E>[] getEditPartTypes() {
		return this.editPartTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.Operation#createCommands(net.
	 * officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart<?,?>[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public OfficeFloorCommand[] createCommands(
			AbstractOfficeFloorEditPart<?, ?>[] editParts) {
		// By Default create the command for each model
		List<OfficeFloorCommand> list = new LinkedList<OfficeFloorCommand>();
		for (AbstractOfficeFloorEditPart<?, ?> editPart : editParts) {
			E officeFloorEditPart = (E) editPart;
			OfficeFloorCommand command = this
					.createCommand(officeFloorEditPart);
			if (command != null) {
				list.add(command);
			}
		}
		OfficeFloorCommand[] commands = list.toArray(new OfficeFloorCommand[0]);

		// Return the commands
		return commands;
	}

	/**
	 * Creates a {@link OfficeFloorCommand} for the {@link Model} passed in.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} selected.
	 * @return {@link OfficeFloorCommand}.
	 */
	protected abstract OfficeFloorCommand createCommand(E editPart);

}
