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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link OfficeFloorCommandParser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCommandParserImpl implements OfficeFloorCommandParser {

	/**
	 * Single {@link OfficeFloorCommandFactory}.
	 */
	private final OfficeFloorCommandFactory singleFactory;

	/**
	 * {@link OfficeFloorCommandFactory} instances by their command name.
	 */
	private final Map<String, OfficeFloorCommandFactory> factories;

	/**
	 * Initiate for the single command.
	 * 
	 * @param factory
	 *            {@link OfficeFloorCommandFactory}.
	 */
	public OfficeFloorCommandParserImpl(OfficeFloorCommandFactory factory) {
		this.singleFactory = factory;
		this.factories = null;
	}

	/**
	 * Initiate for allowing multiple {@link OfficeFloorCommand} instances to be
	 * executed.
	 * 
	 * @param factories
	 *            {@link OfficeFloorCommandFactory} instances.
	 */
	public OfficeFloorCommandParserImpl(OfficeFloorCommandFactory[] factories) {

		// Not single command
		this.singleFactory = null;

		// Load the factories by their name
		this.factories = new HashMap<String, OfficeFloorCommandFactory>(
				factories.length);
		for (OfficeFloorCommandFactory factory : factories) {
			this.factories.put(factory.getCommandName(), factory);
		}
	}

	/*
	 * ====================== OfficeFloorCommandParser ===================
	 */

	@Override
	public OfficeFloorCommand[] parseCommands(String[] arguments)
			throws OfficeFloorCommandParseException {

		final String SHORTENED_PREFIX = "-";
		final String DESCRIPTIVE_PREFIX = "--";

		// Strip out blank arguments along with trimming values
		List<String> argList = new ArrayList<String>(arguments.length);
		for (String argument : arguments) {
			argument = (argument == null ? "" : argument.trim());
			if (argument.length() > 0) {
				argList.add(argument);
			}
		}
		arguments = argList.toArray(new String[argList.size()]);

		// Find the starting command (pattern: [option value]* command*)
		int commandIndex = -1;
		FOUND_COMMAND: for (int i = 0; i < arguments.length; i += 2) {
			String option = arguments[i];
			if (!option.startsWith(SHORTENED_PREFIX)) {
				// Found first command
				commandIndex = i;
				break FOUND_COMMAND;
			}
		}

		// Create the listing of commands to be populated
		List<CommandStruct> commands = new ArrayList<CommandStruct>(
				arguments.length - commandIndex);
		if (this.singleFactory != null) {
			// Single command (and ensure single command)
			if (commandIndex >= 0) {
				throw new OfficeFloorCommandParseException(
						"Must not provide command");
			}

			// Flag to load all options
			commandIndex = arguments.length;

			// Load the single command
			commands.add(new CommandStruct(this.singleFactory.createCommand()));

		} else {
			// Multiple commands (and ensure command specified)
			if (commandIndex < 0) {
				throw new OfficeFloorCommandParseException(
						"Must specify a command");
			}

			// Load the multiple commands
			for (int i = commandIndex; i < arguments.length; i++) {
				String commandName = arguments[i];

				// Obtain the command factory
				OfficeFloorCommandFactory factory = this.factories
						.get(commandName);
				if (factory == null) {
					throw new OfficeFloorCommandParseException(
							"Unknown command '" + commandName + "'");
				}

				// Register the command
				commands.add(new CommandStruct(factory.createCommand()));
			}
		}

		// Register the parameters for each command
		for (CommandStruct command : commands) {
			for (OfficeFloorCommandParameter parameter : command.command
					.getParameters()) {
				command.parametersByName.put(parameter.getName(), parameter);
				String shortName = parameter.getShortName();
				if (shortName != null) {
					command.parametersByShortName.put(shortName, parameter);
				}
			}
		}

		// Populate the commands
		for (int i = 0; i < commandIndex; i += 2) {
			String optionName = arguments[i];
			String value = arguments[i + 1];

			// Handle loading
			boolean isValueLoaded = false;
			if (optionName.startsWith(DESCRIPTIVE_PREFIX)) {
				// Descriptive name
				String name = optionName.substring(DESCRIPTIVE_PREFIX.length());

				// Load by descriptive name
				for (CommandStruct command : commands) {
					OfficeFloorCommandParameter parameter = command.parametersByName
							.get(name);
					if (parameter != null) {
						parameter.addValue(value);
						isValueLoaded = true;
					}
				}

			} else {
				// Shortened name
				String name = optionName.substring(SHORTENED_PREFIX.length());

				// Load by short name
				for (CommandStruct command : commands) {
					OfficeFloorCommandParameter parameter = command.parametersByShortName
							.get(name);
					if (parameter != null) {
						parameter.addValue(value);
						isValueLoaded = true;
					}
				}
			}

			// Ensure value loaded (otherwise invalid option)
			if (!isValueLoaded) {
				throw new OfficeFloorCommandParseException("Unknown option '"
						+ optionName + "'");
			}
		}

		// Return the populated commands
		OfficeFloorCommand[] officeFloorCommands = new OfficeFloorCommand[commands
				.size()];
		for (int i = 0; i < officeFloorCommands.length; i++) {
			officeFloorCommands[i] = commands.get(i).command;
		}
		return officeFloorCommands;
	}

	/**
	 * Command structure.
	 */
	private static class CommandStruct {

		/**
		 * {@link OfficeFloorCommand}.
		 */
		public final OfficeFloorCommand command;

		/**
		 * {@link OfficeFloorCommandArgument} by name.
		 */
		public final Map<String, OfficeFloorCommandParameter> parametersByName = new HashMap<String, OfficeFloorCommandParameter>();

		/**
		 * {@link OfficeFloorCommandArgument} by short name.
		 */
		public final Map<String, OfficeFloorCommandParameter> parametersByShortName = new HashMap<String, OfficeFloorCommandParameter>();

		/**
		 * Initiate.
		 * 
		 * @param command
		 *            {@link OfficeFloorCommand}.
		 */
		public CommandStruct(OfficeFloorCommand command) {
			this.command = command;
		}
	}

}