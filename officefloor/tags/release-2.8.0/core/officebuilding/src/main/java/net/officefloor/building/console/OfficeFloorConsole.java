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
package net.officefloor.building.console;

import java.io.PrintStream;
import java.io.Reader;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessStartListener;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Console for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorConsole {

	/**
	 * Help {@link OfficeFloorCommand} name.
	 */
	String COMMAND_HELP = "help";

	/**
	 * Exit {@link OfficeFloorCommand} name.
	 */
	String COMMAND_EXIT = "exit";

	/**
	 * This is a blocking method that starts reading {@link OfficeFloorCommand}
	 * instances from the console in and executing them.
	 * 
	 * @param in
	 *            Console in.
	 * @param out
	 *            Console out.
	 * @param err
	 *            Console err.
	 * @param prefixArguments
	 *            Arguments that are prefixed to every run of arguments.
	 */
	void start(Reader in, PrintStream out, PrintStream err,
			String... prefixArguments);

	/**
	 * Runs the {@link OfficeFloorCommand} for the arguments.
	 * 
	 * @param out
	 *            Console out.
	 * @param err
	 *            Console err.
	 * @param startListener
	 *            {@link ProcessStartListener}. May be <code>null</code>.
	 * @param completionListener
	 *            {@link ProcessCompletionListener}. May be <code>null</code>.
	 * @param arguments
	 *            Arguments for the {@link OfficeFloorCommand} instances.
	 * @return <code>true</code> if successfully run/started
	 *         {@link OfficeFloorCommand}.
	 */
	boolean run(PrintStream out, PrintStream err,
			ProcessStartListener startListener,
			ProcessCompletionListener completionListener, String... arguments);

}