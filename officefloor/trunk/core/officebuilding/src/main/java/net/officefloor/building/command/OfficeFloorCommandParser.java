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

/**
 * Parses the command line input to produce the {@link OfficeFloorCommand}
 * instances to be invoked.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandParser {

	/**
	 * Parses the {@link OfficeFloorCommand} instances from the arguments.
	 * 
	 * @param arguments
	 *            Arguments - typically the command line arguments.
	 * @return Listing of {@link OfficeFloorCommand} instances to execute.
	 * @throws OfficeFloorCommandParseException
	 *             If invalid command line.
	 */
	OfficeFloorCommand[] parseCommands(String[] arguments)
			throws OfficeFloorCommandParseException;

}