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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.classpath.ClassPathFactory;
import net.officefloor.building.classpath.ClassPathFactoryImpl;
import net.officefloor.building.classpath.RemoteRepository;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.OfficeFloorCommandParseException;
import net.officefloor.building.command.OfficeFloorCommandParser;
import net.officefloor.building.command.OfficeFloorCommandParserImpl;
import net.officefloor.building.command.OfficeFloorNoCommandsException;
import net.officefloor.building.command.RemoteRepositoryUrlsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.RemoteRepositoryUrlsOfficeFloorCommandParameterImpl;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.execute.OfficeFloorExecutionUnit;
import net.officefloor.building.execute.OfficeFloorExecutionUnitCreateException;
import net.officefloor.building.execute.OfficeFloorExecutionUnitFactory;
import net.officefloor.building.execute.OfficeFloorExecutionUnitFactoryImpl;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessException;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.ProcessStartListener;

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
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * {@link OfficeFloorDecorator} instances.
	 */
	private final OfficeFloorDecorator[] decorators;

	/**
	 * {@link HelpOfficeFloorCommandFactory}.
	 */
	private final HelpOfficeFloorCommandFactory helpCommandFactory;

	/**
	 * Initiate.
	 * 
	 * @param scriptName
	 *            Name of script invoking this {@link OfficeFloorConsole}.
	 * @param commandFactories
	 *            {@link OfficeFloorCommandFactory} instances.
	 * @param environment
	 *            Environment {@link Properties}.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 */
	public OfficeFloorConsoleImpl(String scriptName,
			OfficeFloorCommandFactory[] commandFactories,
			Properties environment, OfficeFloorDecorator[] decorators) {
		this.scriptName = scriptName;
		this.environment = environment;
		this.decorators = decorators;

		// Create the OfficeFloor command parser
		if (commandFactories.length == 1) {
			// Single command so wrap to provide help options
			OfficeFloorCommandFactory commandWithHelp = new HelpOfficeFloorCommandFactory(
					commandFactories[0]);
			this.parser = new OfficeFloorCommandParserImpl(commandWithHelp);
			this.commandFactories = new OfficeFloorCommandFactory[] { commandWithHelp };
			this.helpCommandFactory = null; // no help as always a command

		} else {
			// Multiple commands so include help
			this.helpCommandFactory = new HelpOfficeFloorCommandFactory(null);

			// Multiple commands (+1 to include help command)
			this.commandFactories = new OfficeFloorCommandFactory[commandFactories.length + 1];
			System.arraycopy(commandFactories, 0, this.commandFactories, 0,
					commandFactories.length);
			this.commandFactories[commandFactories.length] = this.helpCommandFactory;
			this.parser = new OfficeFloorCommandParserImpl(
					this.commandFactories);
		}
	}

	/**
	 * Write the {@link Exception}.
	 * 
	 * @param err
	 *            Error stream to write {@link Exception}.
	 * @param ex
	 *            {@link Exception}.
	 */
	private void writeErr(PrintStream err, Exception ex) {

		// Write the error
		err.print("ERROR: ");
		err.println(ex.getMessage());

		// Output all the causes
		Throwable error = ex;
		Throwable cause = ex.getCause();
		while ((cause != null) && (error != cause)) {

			// Provide cause
			err.print("    Caused by ");
			err.print(cause.getMessage());
			err.print(" [");
			err.print(cause.getClass().getSimpleName());
			err.println("]");

			// Set up for next iteration
			error = cause;
			cause = cause.getCause();
		}
	}

	/*
	 * ===================== OfficeFloorConsole =======================
	 */

	@Override
	public boolean run(PrintStream out, PrintStream err,
			ProcessStartListener startListener,
			ProcessCompletionListener completionListener, String... arguments) {

		// Parse the commands to execute
		OfficeFloorCommand[] commands;
		try {
			commands = this.parser.parseCommands(arguments);

		} catch (OfficeFloorNoCommandsException ex) {
			// Provide help message as no command
			OfficeFloorCommand helpCommand = this.helpCommandFactory
					.createCommand();
			commands = new OfficeFloorCommand[] { helpCommand };

		} catch (OfficeFloorCommandParseException ex) {
			// Failed to parse commands
			this.writeErr(err, ex);
			return false;
		}

		// Execute each command
		for (OfficeFloorCommand command : commands) {

			// Obtain the parameters
			OfficeFloorCommandParameter[] parameters = command.getParameters();

			// Obtain remote repositories (from properties and environment)
			List<RemoteRepository> remoteRepositories = new LinkedList<RemoteRepository>();
			for (OfficeFloorCommandParameter parameter : parameters) {
				if (parameter instanceof RemoteRepositoryUrlsOfficeFloorCommandParameter) {
					// Have remote repository URLs parameter so use
					RemoteRepositoryUrlsOfficeFloorCommandParameter remoteRepositoryUrlsParameter = (RemoteRepositoryUrlsOfficeFloorCommandParameter) parameter;
					for (String remoteRepositoryUrl : remoteRepositoryUrlsParameter
							.getRemoteRepositoryUrls()) {
						remoteRepositories.add(new RemoteRepository(
								remoteRepositoryUrl));
					}
				}
			}
			for (String remoteRepositoryUrl : RemoteRepositoryUrlsOfficeFloorCommandParameterImpl
					.getRemoteRepositoryUrls(this.environment)) {
				remoteRepositories
						.add(new RemoteRepository(remoteRepositoryUrl));
			}

			// Create the class path factory
			ClassPathFactory classPathFactory;
			try {
				classPathFactory = new ClassPathFactoryImpl(
						null,
						remoteRepositories
								.toArray(new RemoteRepository[remoteRepositories
										.size()]));
			} catch (Exception ex) {
				// Failed to create execution unit for command
				this.writeErr(err, ex);
				return false;
			}

			// Create the OfficeFloor execution unit factory
			OfficeFloorExecutionUnitFactory executionUnitFactory = new OfficeFloorExecutionUnitFactoryImpl(
					classPathFactory, this.environment, this.decorators);

			// Create execution unit for command
			OfficeFloorExecutionUnit executionUnit;
			try {
				executionUnit = executionUnitFactory
						.createExecutionUnit(command);
			} catch (OfficeFloorExecutionUnitCreateException ex) {
				// Failed to create execution unit for command
				this.writeErr(err, ex);
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

			// Enrich configuration with completion listener
			ProcessConfiguration configuration = executionUnit
					.getProcessConfiguration();
			configuration.setProcessStartListener(startListener);
			configuration.setProcessCompletionListener(completionListener);

			// Execute the managed process for command
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
				this.writeErr(err, ex);
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