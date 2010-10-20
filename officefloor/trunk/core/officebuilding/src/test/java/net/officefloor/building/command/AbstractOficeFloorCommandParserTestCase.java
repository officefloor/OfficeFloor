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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract functionality for testing the {@link OfficeFloorCommandParser}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOficeFloorCommandParserTestCase extends
		OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCommandFactory} instances in order registered.
	 */
	private final List<OfficeFloorCommandFactory> factoryList = new LinkedList<OfficeFloorCommandFactory>();

	/**
	 * {@link OfficeFloorCommandFactory} instances by their command name.
	 */
	private final Map<String, OfficeFloorCommandFactory> factories = new HashMap<String, OfficeFloorCommandFactory>();

	/**
	 * {@link OfficeFloorCommand} instances by their command name.
	 */
	private final Map<String, OfficeFloorCommand> commands = new HashMap<String, OfficeFloorCommand>();

	/**
	 * {@link OfficeFloorCommandParameter} instances by command name then
	 * parameter name.
	 */
	private final Map<String, Map<String, OfficeFloorCommandParameter>> parameters = new HashMap<String, Map<String, OfficeFloorCommandParameter>>();

	/**
	 * Indicates if only a single command.
	 */
	private boolean isSingle = false;

	/**
	 * Flag that testing single command.
	 */
	protected void flagSingle() {
		this.isSingle = true;
	}

	/**
	 * Undertakes the command parsing.
	 * 
	 * @param commandLine
	 *            Command line.
	 * @param expectedCommandNames
	 *            Names of the expected {@link OfficeFloorCommand} instances.
	 * @throws OfficeFloorCommandParseException
	 *             If fails to parse.
	 */
	protected void doTest(String commandLine, String... expectedCommandNames)
			throws OfficeFloorCommandParseException {

		// Split command line into arguments
		String[] args = commandLine.split("\\s+");

		// Obtain the listing of factories
		OfficeFloorCommandFactory[] factoryArray = this.factoryList
				.toArray(new OfficeFloorCommandFactory[0]);

		// Test
		this.replayMockObjects();

		// Create the parser
		OfficeFloorCommandParser parser;
		if (this.isSingle) {
			// Single command
			assertEquals("Expecting only single command", 1,
					factoryArray.length);
			parser = new OfficeFloorCommandParserImpl(factoryArray[0]);

		} else {
			// Multiple commands
			parser = new OfficeFloorCommandParserImpl(factoryArray);
		}

		// Parse the command line
		OfficeFloorCommand[] commands = parser.parseCommands(args);

		// Verify
		this.verifyMockObjects();

		// Ensure the correct commands returned
		assertEquals("Incorrect number of returned commands",
				expectedCommandNames.length, commands.length);
		for (int i = 0; i < expectedCommandNames.length; i++) {
			String expectedCommandName = expectedCommandNames[i];
			OfficeFloorCommand expectedCommand = this.commands
					.get(expectedCommandName);
			assertEquals("Incorrect expected command " + expectedCommandName,
					expectedCommand, commands[i]);
		}
	}

	/**
	 * Records creating the necessary {@link OfficeFloorCommandFactory}
	 * instances for the commands.
	 * 
	 * @param commandNames
	 *            {@link OfficeFloorCommand} names.
	 */
	protected void record_Factories(String... commandNames) {
		for (String commandName : commandNames) {

			// Ensure not already exist
			assertNull("Command already recorded", this.factories
					.get(commandName));

			// Record the command factory
			OfficeFloorCommandFactory factory = this
					.createMock(OfficeFloorCommandFactory.class);
			if (!this.isSingle) {
				// Only requires name if multiple commands
				this.recordReturn(factory, factory.getCommandName(),
						commandName);
			}

			// Register the factory
			this.factoryList.add(factory);
			this.factories.put(commandName, factory);
		}
	}

	/**
	 * Records creating the {@link OfficeFloorCommand} with the arguments.
	 * 
	 * @param factory
	 *            {@link OfficeFloorCommandFactory}.
	 * @param optionDescriptiveShortenedPairs
	 *            Option pairs for the {@link OfficeFloorCommandParameter}
	 *            instances.
	 */
	protected void record_Command(String commandName,
			String... optionDescriptiveShortenedPairs) {

		// Obtain the factory
		OfficeFloorCommandFactory factory = this.factories.get(commandName);
		assertNotNull("No factory recorded for command '" + commandName + "'",
				factory);

		// Record create the command
		final OfficeFloorCommand command = this
				.createMock(OfficeFloorCommand.class);
		this.recordReturn(factory, factory.createCommand(), command);

		// Register the command
		this.commands.put(commandName, command);

		// Create the parameters for the command
		OfficeFloorCommandParameter[] parameters = new OfficeFloorCommandParameter[optionDescriptiveShortenedPairs.length / 2];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.createMock(OfficeFloorCommandParameter.class);
		}

		// Record returning the parameter list
		this.recordReturn(command, command.getParameters(), parameters);

		// Record obtain details regarding the arguments
		for (int i = 0; i < optionDescriptiveShortenedPairs.length; i += 2) {
			String descriptiveName = optionDescriptiveShortenedPairs[i];
			String shortenedName = optionDescriptiveShortenedPairs[i + 1];

			// Record obtaining details from parameter
			final OfficeFloorCommandParameter parameter = parameters[i / 2];
			this.recordReturn(parameter, parameter.getName(), descriptiveName);
			this.recordReturn(parameter, parameter.getShortName(),
					shortenedName);

			// Register the argument
			Map<String, OfficeFloorCommandParameter> commandParams = this.parameters
					.get(commandName);
			if (commandParams == null) {
				commandParams = new HashMap<String, OfficeFloorCommandParameter>();
				this.parameters.put(commandName, commandParams);
			}
			commandParams.put(descriptiveName, parameter);
		}
	}

	/**
	 * Records loading an {@link OfficeFloorCommandParameter} value.
	 * 
	 * @param commandName
	 *            {@link OfficeFloorCommand} name.
	 * @param parameterName
	 *            {@link OfficeFloorCommandParameter} name.
	 * @param value
	 *            Value to load.
	 */
	protected void record_Argument(String commandName, String parameterName,
			String value) {

		// Obtain the command argument
		Map<String, OfficeFloorCommandParameter> commandParams = this.parameters
				.get(commandName);
		assertNotNull("Unknonwn command '" + commandName + "'", commandParams);
		OfficeFloorCommandParameter argument = commandParams.get(parameterName);
		assertNotNull("Unknown parameter '" + parameterName + "' on command "
				+ commandName, argument);

		// Record loading the value
		argument.addValue(value);
	}

}