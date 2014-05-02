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
package net.officefloor.console;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.console.AbstractConsoleTestCase;
import net.officefloor.building.console.OfficeFloorConsoleFactory;
import net.officefloor.building.console.OfficeFloorConsoleMain;
import net.officefloor.building.util.OfficeBuildingTestUtil;

/**
 * Abstract functionality for testing {@link OfficeFloorConsoleMain}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConsoleMainTestCase extends
		AbstractConsoleTestCase {

	/**
	 * {@link OfficeFloorConsoleFactory} class.
	 */
	private final Class<? extends OfficeFloorConsoleFactory> consoleFactoryClass;

	/**
	 * Flag indicating if to ensure the {@link OfficeBuilding} is not running on
	 * setup.
	 */
	private final boolean isEnsureOfficeBuildingNotRunningForTest;

	/**
	 * Initiate.
	 * 
	 * @param consoleFactoryClass
	 *            {@link OfficeFloorConsoleFactory} class.
	 * @param isEnsureOfficeBuildingNotRunningForTest
	 *            Flag indicating if to ensure the {@link OfficeBuilding} is not
	 *            running on setup.
	 */
	public AbstractConsoleMainTestCase(
			Class<? extends OfficeFloorConsoleFactory> consoleFactoryClass,
			boolean isEnsureOfficeBuildingNotRunningForTest) {
		this.consoleFactoryClass = consoleFactoryClass;
		this.isEnsureOfficeBuildingNotRunningForTest = isEnsureOfficeBuildingNotRunningForTest;
	}

	@Override
	protected void setUp() throws Exception {

		// Flag verbose
		this.setVerbose(true);

		// Testing
		OfficeFloorConsoleMain.isExit = false;

		// Specify the OFFICE_FLOOR_HOME
		File propertiesFile = this.findFile(this.getClass(),
				OfficeFloorConsoleMain.PROPERTIES_FILE_RELATIVE_PATH);
		File officeFloorHome = propertiesFile.getParentFile().getParentFile();
		System.setProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME,
				officeFloorHome.getAbsolutePath());

		// Ensure OfficeBuilding not running (before output to pipes)
		if (this.isEnsureOfficeBuildingNotRunningForTest) {
			this.printMessage("Setting up test (ensuring OfficeBuilding not running)");
			PrintStream stdErr = System.err;
			PrintStream devNull = new PrintStream(new ByteArrayOutputStream());
			try {
				// Ignore errors in stopping OfficeFloor (as may not be running)
				System.setErr(devNull);

				this.doMain("stop");
			} catch (Throwable ex) {
				// Ignore failure to stop office
			} finally {
				// Reinstate stdErr
				System.setErr(stdErr);
			}
		}

		// Setup pipes
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear the OFFICE_FLOOR_HOME
		System.clearProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME);

		// Remove pipes
		super.tearDown();
	}

	/**
	 * Does the {@link OfficeFloorCommand} with security keys.
	 * 
	 * @param arguments
	 *            Arguments for {@link OfficeBuilding}.
	 */
	protected void doSecureMain(String arguments) throws Throwable {

		// Obtain the key store details
		String username = OfficeBuildingTestUtil.getLoginUsername();
		String password = OfficeBuildingTestUtil.getLoginPassword();
		File keyStore = OfficeBuildingTestUtil.getKeyStore();
		String keyStorePassword = OfficeBuildingTestUtil.getKeyStorePassword();
		String keyStoreArguments = "--key_store " + keyStore.getAbsolutePath()
				+ " --key_store_password " + keyStorePassword + " --username "
				+ username + " --password " + password;

		// Execure the secure command
		this.doMain(keyStoreArguments + " " + arguments);
	}

	/**
	 * Does the {@link OfficeFloorCommand}.
	 * 
	 * @param arguments
	 *            Arguments for {@link OfficeBuilding}.
	 */
	protected void doMain(String arguments) throws Throwable {

		// Create the command line
		String commandLine = "script " + this.consoleFactoryClass.getName()
				+ " run " + arguments;

		// Obtain the arguments
		String[] executeArguments = commandLine.split("\\s+");

		try {
			// Execute the command
			OfficeFloorConsoleMain.main(executeArguments);
		} catch (Error error) {
			// Cause EXIT error with details of failure
			this.assertErr("EXIT: " + error.getMessage());
			throw error;
		}
	}

}