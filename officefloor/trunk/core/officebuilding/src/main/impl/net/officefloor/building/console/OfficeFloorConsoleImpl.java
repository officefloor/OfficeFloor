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
package net.officefloor.building.console;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParseException;
import net.officefloor.building.command.OfficeFloorCommandParser;
import net.officefloor.building.command.OfficeFloorCommandParserImpl;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.execute.OfficeFloorExecutionUnit;
import net.officefloor.building.execute.OfficeFloorExecutionUnitCreateException;
import net.officefloor.building.execute.OfficeFloorExecutionUnitFactory;
import net.officefloor.building.execute.OfficeFloorExecutionUnitFactoryImpl;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessException;
import net.officefloor.building.process.ProcessManager;

/**
 * {@link OfficeFloorConsole} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorConsoleImpl implements OfficeFloorConsole {

	/**
	 * Name of script invoking this {@link OfficeFloorConsole}.
	 */
	private final String scriptName;

	/**
	 * {@link OfficeFloorCommandFactory} instances.
	 */
	private final OfficeFloorCommandFactory[] commandFactories;

	/**
	 * {@link OfficeFloorCommandParser}.
	 */
	private final OfficeFloorCommandParser parser;

	/**
	 * {@link OfficeFloorExecutionUnitFactory}.
	 */
	private final OfficeFloorExecutionUnitFactory executionUnitFactory;

	/**
	 * Initiate.
	 * 
	 * @param scriptName
	 *            Name of script invoking this {@link OfficeFloorConsole}.
	 * @param commandFactories
	 *            {@link OfficeFloorCommandFactory} instances.
	 * @param localRepositoryDirectory
	 *            Local repository directory.
	 * @param remoteRepositoryUrls
	 *            Remote repository URLs.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 */
	public OfficeFloorConsoleImpl(String scriptName,
			OfficeFloorCommandFactory[] commandFactories,
			File localRepositoryDirectory, String[] remoteRepositoryUrls,
			OfficeFloorDecorator[] decorators) {
		this.scriptName = scriptName;

		// Create the OfficeFloor command parser
		if (commandFactories.length == 1) {
			// Single command so wrap to provide help options
			OfficeFloorCommandFactory commandWithHelp = new HelpOfficeFloorCommandFactory(
					commandFactories[0]);
			this.parser = new OfficeFloorCommandParserImpl(commandWithHelp);
			this.commandFactories = new OfficeFloorCommandFactory[] { commandWithHelp };
		} else {
			// Multiple commands (+1 to include help command)
			this.commandFactories = new OfficeFloorCommandFactory[commandFactories.length + 1];
			System.arraycopy(commandFactories, 0, this.commandFactories, 0,
					commandFactories.length);
			this.commandFactories[commandFactories.length] = new HelpOfficeFloorCommandFactory(
					null);
			this.parser = new OfficeFloorCommandParserImpl(
					this.commandFactories);
		}

		// Create the OfficeFloor execution unit factory
		this.executionUnitFactory = new OfficeFloorExecutionUnitFactoryImpl(
				localRepositoryDirectory, remoteRepositoryUrls, decorators);
	}

	/*
	 * ===================== OfficeFloorConsole =======================
	 */

	@Override
	public boolean run(PrintStream out, PrintStream err, String... arguments) {

		// Parse the commands to execute
		OfficeFloorCommand[] commands;
		try {
			commands = this.parser.parseCommands(arguments);
		} catch (OfficeFloorCommandParseException ex) {
			// Failed to parse commands
			err.println(ex.getMessage());
			return false;
		}

		// Execute each command
		for (OfficeFloorCommand command : commands) {

			// Create execution unit for command
			OfficeFloorExecutionUnit executionUnit;
			try {
				executionUnit = this.executionUnitFactory
						.createExecutionUnit(command);
			} catch (OfficeFloorExecutionUnitCreateException ex) {
				// Failed to create execution unit for command
				err.println(ex.getMessage());
				return false;
			}

			// Determine if help
			ManagedProcess managedProcess = executionUnit.getManagedProcess();
			if (managedProcess instanceof HelpManagedProcess) {
				// Help command so write help message
				HelpManagedProcess help = (HelpManagedProcess) managedProcess;
				help.writeHelp(out, this.scriptName, this.commandFactories);
				continue; // help message written
			}

			// Execute the managed process for command
			ProcessConfiguration configuration = executionUnit
					.getProcessConfiguration();
			try {
				if (executionUnit.isSpawnProcess()) {
					// Requires to be run within a spawned process
					ProcessManager.startProcess(managedProcess, configuration);
				} else {
					// Run locally
					ProcessManager.runProcess(managedProcess, configuration);
				}
			} catch (ProcessException ex) {
				// Failed to execute within process
				err.println(ex.getMessage());
				return false;
			}
		}

		// Successfully run all commands
		return true;
	}

	@Override
	public void start(Reader in, PrintStream out, PrintStream err,
			String... prefixArguments) {
		// TODO implement OfficeFloorConsole.start
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorConsole.start");
	}

}