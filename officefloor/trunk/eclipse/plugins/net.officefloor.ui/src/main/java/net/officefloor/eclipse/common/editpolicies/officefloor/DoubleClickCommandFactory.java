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
package net.officefloor.eclipse.common.editpolicies.officefloor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

/**
 * {@link Command} factory for a double click.
 * 
 * @author Daniel Sagenschneider
 */
public interface DoubleClickCommandFactory<M> {

	/**
	 * Creates {@link Command} for the double click.
	 * 
	 * @param model
	 *            Model being double clicked.
	 * @param editPart
	 *            {@link EditPart} being double clicked.
	 * @return {@link Command} or <code>null</code> if nothing to be done.
	 */
	Command createCommand(M model, EditPart editPart);

}