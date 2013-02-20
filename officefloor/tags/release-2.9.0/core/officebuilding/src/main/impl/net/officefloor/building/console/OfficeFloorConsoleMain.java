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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Provides the <code>main</code> entrance point to running
 * {@link OfficeFloorCommand}.
 * 
 * @author Daniel Sagenschneider
 */
public final class OfficeFloorConsoleMain {

	/**
	 * Environment property to specify where the {@link OfficeFloor} home
	 * directory is located.
	 */
	public static final String OFFICE_FLOOR_HOME = "OFFICE_FLOOR_HOME";

	/**
	 * Relative path from the {@link #OFFICE_FLOOR_HOME} to find the properties
	 * file.
	 */
	public static final String PROPERTIES_FILE_RELATIVE_PATH = "config/OfficeFloor.properties";

	/**
	 * <p>
	 * Flags that {@link System#exit(int)} is called on an error to indicate
	 * failure to calling script.
	 * <p>
	 * This is typically only used for testing to not cause the JVM to exit
	 * while running tests.
	 */
	public static boolean isExit = true;

	/**
	 * Entrance point for running an {@link OfficeFloorCommand}.
	 * 
	 * @param arguments
	 *            Command line arguments.
	 * @throws Throwable
	 *             If fails to run the {@link OfficeFloorCommand}.
	 */
	public static void main(String[] arguments) throws Throwable {

		// Obtain the environment properties.
		// Reverse load order so allow over writing for more specific values.
		Properties environment = new Properties();
		environment.putAll(System.getenv());
		environment.putAll(System.getProperties());

		// Obtain the OFFICE_FLOOR_HOME
		String officeFloorHome = environment.getProperty(OFFICE_FLOOR_HOME);
		if (officeFloorHome == null) {
			writeErrAndExit("ERROR: OFFICE_FLOOR_HOME not specified. Must be an environment variable pointing to the OfficeFloor install directory.");
		}

		// Ensure OFFICE_FLOOR_HOME exists
		File officeFloorHomeDir = new File(officeFloorHome);
		if (!officeFloorHomeDir.isDirectory()) {
			writeErrAndExit("ERROR: Can not find OFFICE_FLOOR_HOME directory "
					+ officeFloorHome);
		}

		// Message for invalid call from script
		final String[] INVALID_CALL_FROM_SCRIPT = new String[] {
				"ERROR: Invalid call from script.",
				"",
				"usage: java ... " + OfficeFloorConsoleMain.class.getName()
						+ " <script> <factory> <\"run\"|\"start\"> <args>" };

		// Ensure appropriate number of arguments to run/start console
		final int SCRIPT_INDEX = 0;
		final int FACTORY_CLASS_INDEX = 1;
		final int CONSOLE_INDEX = 2;
		final int REQUIRED_NUMBER_OF_ARGUMENTS = 3;
		if (arguments.length < REQUIRED_NUMBER_OF_ARGUMENTS) {
			writeErrAndExit(INVALID_CALL_FROM_SCRIPT);
		}

		// Obtain the console run/start arguments
		String scriptName = arguments[SCRIPT_INDEX];
		String factoryClassName = arguments[FACTORY_CLASS_INDEX];
		String consoleRunStart = arguments[CONSOLE_INDEX];

		// Instantiate the factory
		OfficeFloorConsoleFactory factory;
		try {
			factory = (OfficeFloorConsoleFactory) Thread.currentThread()
					.getContextClassLoader().loadClass(factoryClassName)
					.newInstance();
		} catch (Exception ex) {
			writeErrAndExit("ERROR: Invalid "
					+ OfficeFloorConsoleFactory.class.getSimpleName() + " "
					+ factoryClassName + " : " + ex.getMessage() + " ["
					+ ex.getClass().getSimpleName() + "]");
			return; // required for compilation
		}

		// Obtain the OFFICE_FLOOR_HOME properties
		File propertiesFile = new File(officeFloorHomeDir,
				PROPERTIES_FILE_RELATIVE_PATH);
		if (propertiesFile.isFile()) {
			// Load the properties to environment
			StringWriter propertiesFileContent = new StringWriter();
			Reader propertiesFileReader = new FileReader(propertiesFile);
			try {
				for (int character = propertiesFileReader.read(); character >= 0; character = propertiesFileReader
						.read()) {
					propertiesFileContent.write(character);
				}
			} finally {
				// Close the reader
				try {
					propertiesFileReader.close();
				} catch (IOException ex) {
					// Warn failed to close but carry on
					System.err
							.println("WARNING: Failed to close properties file.");
				}
			}

			// Replace tags in properties (e.g. allows OFFICE_FLOOR_HOME use)
			String propertiesContent = propertiesFileContent.toString();
			for (String tagName : environment.stringPropertyNames()) {
				String tag = "${" + tagName + "}";
				String tagValue = environment.getProperty(tagName);
				propertiesContent = propertiesContent.replace(tag, tagValue);
			}

			// Load the properties
			environment.load(new StringReader(propertiesContent));

		} else {
			// Warn no properties file available
			System.err
					.println("WARNING: Can not find OFFICE_FLOOR_HOME properties file.");
		}

		// Create the console
		OfficeFloorConsole console = factory.createOfficeFloorConsole(
				scriptName, environment);

		// Obtain arguments for execution
		String[] executeArguments = new String[arguments.length
				- REQUIRED_NUMBER_OF_ARGUMENTS];
		System.arraycopy(arguments, REQUIRED_NUMBER_OF_ARGUMENTS,
				executeArguments, 0, executeArguments.length);

		// Execute console
		if ("run".equalsIgnoreCase(consoleRunStart)) {
			// Run the console
			boolean isSuccessful = console.run(System.out, System.err, null,
					null, executeArguments);

			// Flag failure if not successful
			if (!isSuccessful) {
				writeErrAndExit();
			}

		} else if ("start".equalsIgnoreCase(consoleRunStart)) {
			// Start the console
			console.start(new InputStreamReader(System.in), System.out,
					System.err, executeArguments);

		} else {
			// Unknown execution mode
			writeErrAndExit(INVALID_CALL_FROM_SCRIPT);
		}
	}

	/**
	 * Write output to err and exit.
	 * 
	 * @param lines
	 *            Lines to write to err.
	 */
	private static void writeErrAndExit(String... lines) {

		// Write the lines
		for (String line : lines) {
			System.err.println(line);
		}

		// Determine if exit
		if (!isExit) {
			// Provide error rather than exit
			throw new Error("See stderr");
		}

		// Notify to calling script of failure
		System.exit(1);
	}

}