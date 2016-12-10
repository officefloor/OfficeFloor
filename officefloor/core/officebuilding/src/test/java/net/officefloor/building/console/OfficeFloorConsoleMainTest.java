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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessStartListener;
import net.officefloor.console.ConfigureOfficeFloor;

/**
 * Tests the {@link OfficeFloorConsoleMain}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorConsoleMainTest extends AbstractConsoleTestCase {

	@Override
	protected void setUp() throws Exception {

		// Setup up pipes to validate outputs
		super.setUp();

		// Do not exit JVM on testing
		OfficeFloorConsoleMain.isExit = false;

		// Reset mock console factory for next test
		MockConsoleFactory.reset();
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear OFFICE_FLOOR_HOME value
		System.clearProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME);

		// Clear the pipes
		super.tearDown();
	}

	/**
	 * Ensure issue if OFFICE_FLOOR_HOME not specified.
	 */
	public void testNoOfficeFloorHome() throws Throwable {

		// Run without OFFICE_FLOOR_HOME
		this.failMain(null, true, "");

		// Ensure error regarding no OFFICE_FLOOR_HOME
		this.assertOut();
		this.assertErr("ERROR: OFFICE_FLOOR_HOME not specified. Must be an environment variable pointing to the OfficeFloor install directory.");
	}

	/**
	 * Ensure issue if OFFICE_FLOOR_HOME not exist.
	 */
	public void testOfficeFloorHomeNotExist() throws Throwable {

		// Run with non-existent OFFICE_FLOOR_HOME
		System.setProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME,
				"<OFFICE_FLOOR_HOME not exists>");
		this.failMain(null, true, "");

		// Ensure error regarding no OFFICE_FLOOR_HOME
		this.assertOut();
		this.assertErr("ERROR: Can not find OFFICE_FLOOR_HOME directory <OFFICE_FLOOR_HOME not exists>");
	}

	/**
	 * Ensure issue if no script name.
	 */
	public void testNoScriptName() throws Throwable {

		// Run without script name (first argument)
		this.failMain("Simple", false, "");

		// Ensure error regarding no script name
		this.assertOut();
		this.assertErr(
				"ERROR: Invalid call from script.                                ",
				"                                                                ",
				"usage: java ... net.officefloor.building.console.OfficeFloorConsoleMain <script> <factory> <args>");
	}

	/**
	 * Ensure issue if no factory name.
	 */
	public void testNoFactoryName() throws Throwable {

		// Run without factory name (second argument)
		this.failMain("Simple", false, "script");

		// Ensure error regarding no script name
		this.assertOut();
		this.assertErr(
				"ERROR: Invalid call from script.                                ",
				"                                                                ",
				"usage: java ... net.officefloor.building.console.OfficeFloorConsoleMain <script> <factory> <args>");
	}

	/**
	 * Ensure issue if no flag to run/start.
	 */
	public void testInvalidFactory() throws Throwable {

		// Run without run/start (third argument)
		this.failMain("Simple", false, "script InvalidFactoryClass run");

		// Ensure error regarding no script name
		this.assertOut();
		this.assertErr("ERROR: Invalid OfficeFloorConsoleFactory InvalidFactoryClass : InvalidFactoryClass [ClassNotFoundException]");
	}

	/**
	 * Ensure warning if no properties file.
	 */
	public void testNoPropertiesFile() throws Throwable {

		// Run with no properties file
		this.runMain("NoPropertiesFile", true, "");

		// Ensure run
		this.assertOut("run");
		this.assertErr("WARNING: Can not find OFFICE_FLOOR_HOME properties file.");
	}

	/**
	 * Ensure loads properties from file.
	 */
	public void testPropertiesFromFile() throws Throwable {

		// Flag expected parameters
		MockConsoleFactory.expectedEnvironment.put("one", "A");
		MockConsoleFactory.expectedEnvironment.put("two", "B");

		// Run with properties in file
		this.runMain("PropertiesFromFile", true, "");

		// Ensure run
		this.assertOut("run");
		this.assertErr();
	}

	/**
	 * Ensure failure status on failing command.
	 */
	public void testFailingConsole() throws Throwable {

		// Run with failing command
		MockConsoleFactory.isSuccessful = false;
		this.failMain("Simple", true, "");

		// Ensure run with arguments
		this.assertOut("run");
		this.assertErr();
	}

	/**
	 * Ensure run with no arguments.
	 */
	public void testNoArguments() throws Throwable {

		// Run with no arguments
		this.runMain("Simple", true, "");

		// Ensure run with no arguments
		this.assertOut("run");
		this.assertErr();
	}

	/**
	 * Ensure loads arguments from command line.
	 */
	public void testArguments() throws Throwable {

		// Run with arguments
		this.runMain("Simple", true, "ONE TWO THREE");

		// Ensure run with arguments
		this.assertOut("run ONE TWO THREE");
		this.assertErr();
	}

	/**
	 * Ensure can load the open OfficeFloor configuration.
	 */
	public void testLoadOfficeBuildingOpenOfficeFloorConfiguration()
			throws Throwable {

		// Provides means to obtain the open OfficeFloor configuration
		ConfigureOfficeFloor configure = new ConfigureOfficeFloor();

		// Load the OfficFloor home
		this.loadOfficeFloorHomeSystemProperty("Configure");

		// Run the OfficeFloor console
		OfficeFloorConsoleMain.run("TEST", new String[0], configure,
				OfficeFloorConsoleMain.DEFAULT_ERROR_HANDLER);

		// Ensure no errors
		this.assertOut();
		this.assertErr();

		// Validate the configuration
		OpenOfficeFloorConfiguration configuration = configure
				.getOpenOfficeFloorConfiguration();
		assertEquals(
				"Should have OfficeFloor location loaded from properties file",
				"net.officefloor.test.TestOfficeFloor",
				configuration.getOfficeFloorLocation());
	}

	/**
	 * Ensure able to load {@link OpenOfficeFloorConfiguration} via
	 * {@link ConfigureOfficeFloor} convenience method.
	 */
	public void testConfigureOpenOfficeFloorConfiguration() throws Throwable {

		// Load the OfficeFloor home
		this.loadOfficeFloorHomeSystemProperty("Configure");

		// Obtain the open OfficeFloor configuration
		OpenOfficeFloorConfiguration configuration = ConfigureOfficeFloor
				.newOpenOfficeFloorConfiguration(OfficeFloorConsoleMain.DEFAULT_ERROR_HANDLER);

		// Ensure no errors
		this.assertOut();
		this.assertErr();

		// Validate the configuration
		assertEquals(
				"Should have OfficeFloor location loaded from properties file",
				"net.officefloor.test.TestOfficeFloor",
				configuration.getOfficeFloorLocation());
	}

	/**
	 * Convenience method to simplify writing command line for failing
	 * {@link OfficeFloorConsoleMain}.
	 * 
	 * @param officeFloorHome
	 *            OFFICE_FLOOR_HOME directory name.
	 * @param isPrefix
	 *            Flag to prefix command line with script and
	 *            {@link MockConsoleFactory}.
	 * @param commandLine
	 *            Command line to be split into arguments.
	 */
	private void failMain(String officeFloorHome, boolean isPrefix,
			String commandLine) throws Throwable {
		try {
			this.runMain(officeFloorHome, isPrefix, commandLine);
			fail("Should not be successful");
		} catch (Error exit) {
			assertEquals("Incorrect exit", "See stderr", exit.getMessage());
		}
	}

	/**
	 * Convenience method to simplify writing the command line for testing the
	 * {@link OfficeFloorConsoleMain}.
	 * 
	 * @param officeFloorHome
	 *            OFFICE_FLOOR_HOME directory name.
	 * @param isPrefix
	 *            Flag to prefix command line with script and
	 *            {@link MockConsoleFactory}.
	 * @param commandLine
	 *            Command line to be split into arguments.
	 */
	private void runMain(String officeFloorHome, boolean isPrefix,
			String commandLine) throws Throwable {

		// Determine if specify the OFFICE_FLOOR_HOME
		if (officeFloorHome != null) {
			this.loadOfficeFloorHomeSystemProperty(officeFloorHome);
		}

		// Determine arguments
		String argumentLine = (isPrefix ? "script "
				+ MockConsoleFactory.class.getName() + " " : "")
				+ commandLine;

		// Run office floor console main
		String[] arguments = argumentLine.split("\\s+");
		OfficeFloorConsoleMain.main(arguments);
	}

	/**
	 * Loads the {@link OfficeFloorConsoleMain#OFFICE_FLOOR_HOME} {@link System}
	 * property.
	 * 
	 * @param officeFloorHome
	 *            Relative OfficeFloor home.
	 */
	private void loadOfficeFloorHomeSystemProperty(String officeFloorHome)
			throws IOException {

		// Attempt to find the OfficeFloor home directory
		File officeFloorHomeDir = null;
		try {
			// Attempt to find by properties file first.
			File propertiesFile = this
					.findFile(
							this.getClass(),
							officeFloorHome
									+ "/"
									+ OfficeFloorConsoleMain.PROPERTIES_FILE_RELATIVE_PATH);

			// Obtain the OFFICE_FLOOR_HOME (home/config/properties-file)
			officeFloorHomeDir = propertiesFile.getParentFile().getParentFile();

		} catch (FileNotFoundException ex) {
			// Obtain by marker file
			File markerFile = this.findFile(this.getClass(), officeFloorHome
					+ "/OfficeFloorHome.marker");

			// Obtain the OFFICE_FLOOR_HOME (home/marker-file)
			officeFloorHomeDir = markerFile.getParentFile();
		}

		// Specify the OFFICE_FLOOR_HOME property for testing
		System.setProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME,
				officeFloorHomeDir.getAbsolutePath());
	}

	/**
	 * Mock {@link OfficeFloorConsoleFactory} for testing. This is instantiated
	 * from default constructor and records details of the
	 * {@link OfficeFloorConsole} via static reference.
	 */
	public static class MockConsoleFactory implements
			OfficeFloorConsoleFactory, OfficeFloorConsole {

		/**
		 * Flag indicating if run was successful.
		 */
		public static boolean isSuccessful = true;

		/**
		 * Expected {@link Properties}.
		 */
		public static final Properties expectedEnvironment = new Properties();

		/**
		 * Resets for next test.
		 */
		public static void reset() {
			isSuccessful = true;
			expectedEnvironment.clear();
		}

		/*
		 * =================== OfficeFloorConsoleFactory ==================
		 */

		@Override
		public OfficeFloorConsole createOfficeFloorConsole(String scriptName,
				Properties environment) throws Exception {

			// Ensure script name correct
			assertEquals("Incorrect script name", "script", scriptName);

			// Construct the expected environment
			Properties env = new Properties();
			env.putAll(System.getenv());
			env.putAll(System.getProperties());
			env.putAll(expectedEnvironment);

			// Validate the environment
			assertEquals("Incorrect number of environment properties",
					env.size(), environment.size());
			for (String name : env.stringPropertyNames()) {
				assertEquals("Incorrect value for property " + name,
						env.getProperty(name), environment.getProperty(name));
			}

			// Return this for testing
			return this;
		}

		/*
		 * ====================== OfficeFloorConsole ======================
		 */

		@Override
		public boolean run(PrintStream out, PrintStream err,
				ProcessStartListener startListener,
				ProcessCompletionListener completionListener,
				String... arguments) {

			// Flag running command
			out.print("run");
			for (String argument : arguments) {
				out.print(" ");
				out.print(argument);
			}
			out.println();

			// Return whether successful
			return isSuccessful;
		}
	}

}