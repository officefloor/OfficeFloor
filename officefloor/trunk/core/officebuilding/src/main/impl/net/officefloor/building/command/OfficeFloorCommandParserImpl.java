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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link OfficeFloorCommandParser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCommandParserImpl implements OfficeFloorCommandParser {

	/**
	 * Prefix of the option for the {@link OfficeFloorCommandParameter}.
	 */
	public static final String OPTION_PREFIX = "--";

	/**
	 * Prefix of the short option for the {@link OfficeFloorCommandParameter}.
	 */
	public static final String OPTION_SHORT_PREFIX = "-";

	/**
	 * Single {@link OfficeFloorCommandFactory}.
	 */
	private final OfficeFloorCommandFactory singleFactory;

	/**
	 * {@link OfficeFloorCommandFactory} instances by their command name.
	 */
	private final Map<String, OfficeFloorCommandFactory> factories;

	/**
	 * Map indicating whether a parameter name requires a value.
	 */
	private final Map<String, Boolean> parameterNameToRequireValue = new HashMap<String, Boolean>();

	/**
	 * Map indicating whether a parameter short name requires a value.
	 */
	private final Map<String, Boolean> parameterShortNameToRequireValue = new HashMap<String, Boolean>();

	/**
	 * Initiate for the single command.
	 * 
	 * @param factory
	 *            {@link OfficeFloorCommandFactory}.
	 */
	public OfficeFloorCommandParserImpl(OfficeFloorCommandFactory factory) {
		this.singleFactory = factory;
		this.factories = null;
		this.populateIfParameterRequiresValue(factory);
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
		this.populateIfParameterRequiresValue(factories);
	}

	/**
	 * Populates whether the {@link OfficeFloorCommandParameter} instances
	 * require a value.
	 * 
	 * @param factories
	 *            {@link OfficeFloorCommandFactory} instances.
	 */
	private void populateIfParameterRequiresValue(
			OfficeFloorCommandFactory... factories) {
		for (OfficeFloorCommandFactory factory : factories) {

			// Each time command is same, so create and use to populate
			OfficeFloorCommand command = factory.createCommand();

			// Populate parameter details
			for (OfficeFloorCommandParameter parameter : command
					.getParameters()) {
				String name = parameter.getName();
				String shortName = parameter.getShortName();
				boolean isRequireValue = parameter.isRequireValue();

				// Specify name
				Boolean isNameRequireValue = this.parameterNameToRequireValue
						.get(name);
				if ((isNameRequireValue != null)
						&& (isRequireValue != isNameRequireValue.booleanValue())) {
					// Conflict between same parameter requiring value
					throw new IllegalStateException("Conflict in parameter '"
							+ name + "' requiring value");
				}
				this.parameterNameToRequireValue.put(name,
						Boolean.valueOf(isRequireValue));

				// Specify short name (if available)
				if (shortName != null) {
					Boolean isShortNameRequireValue = this.parameterShortNameToRequireValue
							.get(shortName);
					if ((isShortNameRequireValue != null)
							&& (isRequireValue != isShortNameRequireValue
									.booleanValue())) {
						// Conflict between same parameter requiring value
						throw new IllegalStateException(
								"Conflict in parameter '" + shortName
										+ "' requiring value");
					}
					this.parameterShortNameToRequireValue.put(shortName,
							Boolean.valueOf(isRequireValue));
				}
			}
		}
	}

	/*
	 * ====================== OfficeFloorCommandParser ===================
	 */

	@Override
	public OfficeFloorCommand[] parseCommands(String[] arguments)
			throws OfficeFloorCommandParseException {

		// Strip out blank arguments along with trimming values
		List<String> argList = new ArrayList<String>(arguments.length);
		for (String argument : arguments) {
			argument = (argument == null ? "" : argument.trim());
			if (argument.length() > 0) {
				argList.add(argument);
			}
		}
		arguments = argList.toArray(new String[argList.size()]);

		// Find the starting command (pattern: [option [value]]* command*)
		int commandIndex = -1;
		FOUND_COMMAND: for (int i = (arguments.length - 1); i >= 0; i--) {
			String argument = arguments[i];

			// Determine if option
			if (argument.startsWith(OPTION_SHORT_PREFIX)) {
				// Found last option, so determine if requires value
				Boolean isRequireValue = this.isRequireValue(argument);
				if (isRequireValue == null) {
					// Argument may be parameter value so try one more argument.
					// Example being JVM option: --jvm-option -Done=a
					if (i > 0) {
						// Attempt to obtain previous parameter
						String previousArgument = arguments[i - 1];
						isRequireValue = this.isRequireValue(previousArgument);
						if (isRequireValue != null) {
							// Determine if previous parameter requires value
							if (!isRequireValue.booleanValue()) {
								// Option does not require value
								throw new OfficeFloorCommandParseException(
										"Unknown option " + argument);
							}

							// Index already at value so do not require value
							isRequireValue = Boolean.FALSE;
						}
					}
				}
				if (isRequireValue == null) {
					// Option is unknown
					throw new OfficeFloorCommandParseException(
							"Unknown option '" + argument + "'");
				}

				// Specify command index (+1 ignore option, [+1 required value])
				commandIndex = i + 1 + (isRequireValue.booleanValue() ? 1 : 0);
				if (commandIndex >= arguments.length) {
					// Index after end of arguments so no commands
					commandIndex = -1;
				}
				break FOUND_COMMAND;

			} else if (i == 0) {
				// First argument is a command
				commandIndex = 0;
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
				throw new OfficeFloorNoCommandsException(
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
		int parameterIndex = 0;
		while (parameterIndex < commandIndex) {

			// Obtain the option name
			String optionName = arguments[parameterIndex];

			// Obtain the listing of parameters to load
			List<OfficeFloorCommandParameter> parameters = new LinkedList<OfficeFloorCommandParameter>();
			if (optionName.startsWith(OPTION_PREFIX)) {
				// Descriptive name
				String name = optionName.substring(OPTION_PREFIX.length());

				// Load by descriptive name
				for (CommandStruct command : commands) {
					OfficeFloorCommandParameter parameter = command.parametersByName
							.get(name);
					if (parameter != null) {
						parameters.add(parameter);
					}
				}

			} else {
				// Shortened name
				String name = optionName
						.substring(OPTION_SHORT_PREFIX.length());

				// Load by short name
				for (CommandStruct command : commands) {
					OfficeFloorCommandParameter parameter = command.parametersByShortName
							.get(name);
					if (parameter != null) {
						parameters.add(parameter);
					}
				}
			}

			// Ensure parameter for flag/option (otherwise invalid option)
			if (parameters.size() == 0) {
				throw new OfficeFloorCommandParseException("Unknown option '"
						+ optionName + "'");
			}

			// Load the parameter values
			boolean isValueRequired = false;
			for (OfficeFloorCommandParameter parameter : parameters) {

				// Obtain the value
				String value = null;
				if (parameter.isRequireValue()) {
					isValueRequired = true;

					// Ensure value on command line
					int valueIndex = parameterIndex + 1;
					if (valueIndex >= commandIndex) {
						throw new OfficeFloorCommandParseException(
								"No value for option '" + optionName + "'");
					}

					// Obtain the value
					value = arguments[valueIndex];
				}

				// Load the value
				parameter.addValue(value);
			}

			// Increment for next option
			parameterIndex++; // move past option
			if (isValueRequired) {
				parameterIndex++; // move past value
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
	 * Returns whether the parameter for the argument requires a value.
	 * 
	 * @param argument
	 *            Argument to determine if requires value.
	 * @return <code>true</code> if argument requires a value. Also
	 *         <code>null</code> if unknown parameter.
	 */
	private Boolean isRequireValue(String argument) {
		Boolean isRequireValue;
		if (argument.startsWith(OPTION_PREFIX)) {
			// Name option
			String optionName = argument.substring(OPTION_PREFIX.length());
			isRequireValue = this.parameterNameToRequireValue.get(optionName);
		} else {
			// Short name option
			String optionShortName = argument.substring(OPTION_SHORT_PREFIX
					.length());
			isRequireValue = this.parameterShortNameToRequireValue
					.get(optionShortName);
		}
		return isRequireValue;
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