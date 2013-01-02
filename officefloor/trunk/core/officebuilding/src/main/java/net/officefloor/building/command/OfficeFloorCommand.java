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
package net.officefloor.building.command;

import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Command for an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommand {

	/**
	 * Obtains a description of this {@link OfficeFloorCommand}.
	 * 
	 * @return Description of this {@link OfficeFloorCommand}.
	 */
	String getDescription();

	/**
	 * Obtains the possible {@link OfficeFloorCommandParameter} instances for
	 * this {@link OfficeFloorCommand}.
	 * 
	 * @return Possible {@link OfficeFloorCommandParameter} instances.
	 */
	OfficeFloorCommandParameter[] getParameters();

	/**
	 * <p>
	 * Initialises this command's environment.
	 * <p>
	 * This allows to register additional class path entries for the
	 * {@link ManagedProcess} to be created by this command. It also allows the
	 * additional class path entries to be decorated by the
	 * {@link OfficeFloorDecorator} instances.
	 * 
	 * @param context
	 *            {@link OfficeFloorCommandContext}.
	 * @throws Exception
	 *             If fails to initialise this command.
	 */
	void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception;

	/**
	 * Creates the {@link ManagedProcess} to undertake the command.
	 * 
	 * @param environment
	 *            {@link OfficeFloorCommandEnvironment}.
	 * @return {@link ManagedProcess} to undertake the command.
	 * @throws Exception
	 *             If fails to create the {@link ManagedProcess}.
	 */
	ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception;

}