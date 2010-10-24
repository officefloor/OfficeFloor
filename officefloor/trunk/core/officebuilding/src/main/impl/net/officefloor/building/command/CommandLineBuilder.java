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

/**
 * Builds a command line.
 * 
 * @author Daniel Sagenschneider
 */
public class CommandLineBuilder {

	/**
	 * Command line.
	 */
	private final List<String> commandLine = new LinkedList<String>();

	/**
	 * Adds an argument to the command line.
	 * 
	 * @param name
	 *            Argument name.
	 * @param value
	 *            Argument value.
	 */
	public void addArgument(String name, String value) {
		this.commandLine.add("--" + name);
		this.commandLine.add(value);
	}

	/**
	 * Adds a command to the command line.
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