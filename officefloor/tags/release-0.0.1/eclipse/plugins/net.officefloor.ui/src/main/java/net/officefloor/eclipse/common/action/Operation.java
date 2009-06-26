/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.common.action;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.IAction;

/**
 * Operation done on {@link EditPart} instances to create resulting
 * {@link OfficeFloorCommand} to make changes.
 * 
 * @author Daniel Sagenschneider
 */
public interface Operation {

	/**
	 * Obtains the text to use for the {@link IAction}.
	 * 
	 * @return Text to use for the {@link IAction}.
	 */
	String getActionText();

	/**
	 * Indicates if this {@link Operation} is applicable for the input
	 * {@link EditPart} instances.
	 * 
	 * @param editParts
	 *            {@link EditPart} instances.
	 * @return <code>true</code> if this {@link Operation} is applicable for the
	 *         input {@link EditPart} instances.
	 */
	boolean isApplicable(EditPart[] editParts);

	/**
	 * Performs this {@link Operation}.
	 * 
	 * @param context
	 *            {@link OperationContext}.
	 */
	void perform(OperationContext context);

}