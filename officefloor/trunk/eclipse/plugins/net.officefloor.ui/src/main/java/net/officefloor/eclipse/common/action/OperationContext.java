/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

/**
 * Context for executing an {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OperationContext {

	/**
	 * Obtains the {@link EditPart} instances for the {@link Operation}.
	 * 
	 * @return {@link EditPart} instances.
	 */
	EditPart[] getEditParts();

	/**
	 * Obtains the location for the {@link Operation}.
	 * 
	 * @return Location for the {@link Operation}.
	 */
	Point getLocation();

	/**
	 * Executes the {@link OfficeFloorCommand}.
	 * 
	 * @param command
	 *            {@link OfficeFloorCommand}.
	 */
	void execute(OfficeFloorCommand command);

}
