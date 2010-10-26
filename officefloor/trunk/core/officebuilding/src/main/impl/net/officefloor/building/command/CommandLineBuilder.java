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
package net.officefloor.building.command;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.command.officefloor.OpenOfficeFloorCommand;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Builds a command line.
 * 
 * @author Daniel Sagenschneider
 */
public class CommandLineBuilder {

	/**
	 * Prefix for an option of the {@link OfficeFloorCommandParameter}.
	 */
	private static final String OPTION_PREFIX = OfficeFloorCommandParserImpl.OPTION_PREFIX;

	/**
	 * Command line.
	 */
	private final List<String> commandLine = new LinkedList<String>();

	/**
	 * Adds an archive to the command line.
	 * 
	 * @param archiveLocation
	 *            Archive location.
	 */
	public void addArchive(String archiveLocation) {
		this.addOption(OpenOfficeFloorCommand.PARAMETER_ARCHIVE_LOCATION,
				archiveLocation);
	}

	/**
	 * Adds a class path entry to the command line.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 */
	public void addClassPathEntry(String classPathEntry) {
		this.addOption(OpenOfficeFloorCommand.PARAMETER_CLASS_PATH,
				classPathEntry);
	}

	/**
	 * Adds an {@link OfficeFloor} location to the command line.
	 * 
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 */
	public void addOfficeFloor(String officeFloorLocation) {
		this.addOption(OpenOfficeFloorCommand.PARAMETER_OFFICE_FLOOR_LOCATION,
				officeFloorLocation);
	}

	/**
	 * Adds an {@link OfficeFloorCommandParameter} to the command line.
	 * 
	 * @param parameterName
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addOption(String parameterName, String value) {
		this.commandLine.add(OPTION_PREFIX + parameterName);
		this.commandLine.add(value);
	}

	/**
	 * Adds an {@link OfficeFloorCommand} to the command line.
	 * 
	 * @param commandName
	 *            Name of the command.
	 */
	public void addCommand(String commandName) {
		this.commandLine.add(commandName);
	}

	/**
	 * Obtains the built command line.
	 * 
	 * @return Built command line.
	 */
	public String[] getCommandLine() {
		return this.commandLine.toArray(new String[this.commandLine.size()]);
	}

}