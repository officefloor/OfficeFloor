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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.OfficeFloorCommandParserImpl;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;

/**
 * Help {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public final class HelpManagedProcess implements ManagedProcess {

	/**
	 * Write out the help.
	 * 
	 * @param out
	 *            Out stream to write help.
	 * @param scriptName
	 *            Name of the script invoking the {@link OfficeFloorCommand}.
	 *            <code>null</code> indicates from within
	 *            {@link OfficeFloorConsole}.
	 * @param commandFactories
	 *            {@link OfficeFloorCommandFactory} instances for help content
	 *            to be written.
	 */
	public void writeHelp(PrintStream out, String scriptName,
			OfficeFloorCommandFactory[] commandFactories) {
		if (commandFactories.length == 1) {
			// Single command
			this.writeSingleCommandHelp(out, scriptName, commandFactories[0]);
		} else {
			// Multiple commands
			this.writeMultipleCommandsHelp(out, scriptName, commandFactories);
		}
	}

	/**
	 * Writes the help for a single {@link OfficeFloorCommand}.
	 * 
	 * @param out
	 *            Out stream to write help.
	 * @param scriptName
	 *            Name of script.
	 * @param commandFactory
	 *            {@link OfficeFloorCommandFactory} for help content to be
	 *            written.
	 */
	private void writeSingleCommandHelp(PrintStream out, String scriptName,
			OfficeFloorCommandFactory commandFactory) {

		// Obtain the command
		OfficeFloorCommand command = commandFactory.createCommand();

		// Output description of the command
		out.println();
		out.println(command.getDescription());

		// Output usage of command
		out.println();
		out.println("usage: " + scriptName + " [options]");

		// Output command options
		out.println();
		this.writeCommandOptions(out, "", command);
	}

	/**
	 * Writes the help for multiple {@link OfficeFloorCommand} instances.
	 * 
	 * @param out
	 *            Out stream to write help.
	 * @param scriptName
	 *            Name of script.
	 * @param commandFactories
	 *            {@link OfficeFloorCommandFactory} instances for help content
	 *            to be written.
	 */
	private void writeMultipleCommandsHelp(PrintStream out, String scriptName,
			OfficeFloorCommandFactory[] commandFactories) {

		// Output usage of command
		out.println();
		out.println("usage: " + scriptName + " [options] <commands>");

		// Output each command
		out.println();
		out.println("Commands:");
		for (OfficeFloorCommandFactory commandFactory : commandFactories) {

			// Obtain the command
			OfficeFloorCommand command = commandFactory.createCommand();
			String commandName = commandFactory.getCommandName();

			// Output description of the command
			out.println();
			out.print(commandName);
			out.print(" : ");
			out.println(command.getDescription());

			// Calculate prefix for options
			StringBuilder optionPrefix = new StringBuilder();
			for (int i = 0; i < (commandName.length() + " ".length()); i++) {
				optionPrefix.append(" ");
			}

			// Output command options
			this.writeCommandOptions(out, optionPrefix.toString(), command);
		}
	}

	/**
	 * Writes the {@link OfficeFloorCommand} options.
	 * 
	 * @param out
	 *            Out stream to write help.
	 * @param linePrefix
	 *            Prefix on each option line.
	 * @param command
	 *            {@link OfficeFloorCommand} for help content to be written.
	 */
	private void writeCommandOptions(PrintStream out, String linePrefix,
			OfficeFloorCommand command) {

		// Ensure parameters to output
		OfficeFloorCommandParameter[] parameters = command.getParameters();
		if (parameters.length == 0) {
			return; // no parameters to output
		}

		// Create the listing of options for command
		Option[] options = new Option[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			options[i] = new Option(parameters[i]);
		}

		// Sort the options
		Arrays.sort(options, new Comparator<Option>() {
			@Override
			public int compare(Option a, Option b) {
				return String.CASE_INSENSITIVE_ORDER.compare(a.sortKey,
						b.sortKey);
			}
		});

		// Find largest length option detail
		int optionMaxLength = 0;
		for (Option option : options) {
			if (option.details.length() > optionMaxLength) {
				optionMaxLength = option.details.length();
			}
		}

		// Output command options
		out.print(linePrefix);
		out.println("Options:");

		// Output the options
		for (Option option : options) {
			out.print(linePrefix);
			out.print(" ");
			this.writeSpacePadded(out, option.details, optionMaxLength);
			out.print("   ");
			out.println(option.parameter.getDescription());
		}

	}

	/**
	 * Writes the message padded with spaces to the specified length.
	 * 
	 * @param out
	 *            Out stream to write help.
	 * @param message
	 *            Message.
	 * @param length
	 *            Length to pad output to by including out putting additional
	 *            spaces (if necessary).
	 */
	private void writeSpacePadded(PrintStream out, String message, int length) {
		out.print(message);
		for (int i = message.length(); i < length; i++) {
			out.print(" ");
		}
	}

	/*
	 * ====================== ManagedProcess =========================
	 */

	@Override
	public void init(ManagedProcessContext context) throws Throwable {
		// Should never be run
	}

	@Override
	public void main() throws Throwable {
		// Should never be run
	}

	/**
	 * Particular option for a command.
	 */
	private class Option {

		/**
		 * {@link OfficeFloorCommand}.
		 */
		public final OfficeFloorCommandParameter parameter;

		/**
		 * Key of this {@link Option} for sorting.
		 */
		public final String sortKey;

		/**
		 * Details.
		 */
		public final String details;

		/**
		 * Initiate.
		 * 
		 * @param parameter
		 *            {@link OfficeFloorCommandParameter}.
		 */
		public Option(OfficeFloorCommandParameter parameter) {
			this.parameter = parameter;

			// Generate the details
			StringWriter details = new StringWriter();
			PrintWriter out = new PrintWriter(details);

			// Write out the short name (if available)
			String shortName = parameter.getShortName();
			if (shortName != null) {
				out.print(OfficeFloorCommandParserImpl.OPTION_SHORT_PREFIX);
				out.print(shortName);
				out.print(",");
			}

			// Write out the name
			String name = parameter.getName();
			out.print(OfficeFloorCommandParserImpl.OPTION_PREFIX);
			out.print(name);

			// Write out if requires value
			if (parameter.isRequireValue()) {
				out.print(" <arg>");
			}

			// Specify details for parameter
			this.details = details.toString();

			// Specify the sort key (short before name if available)
			this.sortKey = (shortName != null ? shortName : name);
		}
	}

}