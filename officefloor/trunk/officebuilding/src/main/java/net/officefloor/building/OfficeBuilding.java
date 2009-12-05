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
package net.officefloor.building;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Properties;

/**
 * <p>
 * Office Building.
 * <p>
 * This provides only the <code>main</code> method for starting the Office
 * Building and running commands.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuilding {

	/**
	 * Environment property to specify where the Office Building home directory
	 * is located.
	 */
	public static final String OFFICE_BUILDING_HOME = "OFFICE_BUILDING_HOME";

	/**
	 * Name of the property defining the host that the {@link OfficeBuilding} is
	 * residing on. This property is ignored if starting the
	 * {@link OfficeBuilding} as always run on localhost.
	 */
	public static final String PROPERTY_OFFICE_BUILDING_HOST = "office.building.host";

	/**
	 * Name of the property defining the port that the {@link OfficeBuilding} is
	 * to run or is running on.
	 */
	public static final String PROPERTY_OFFICE_BUILDING_PORT = "office.building.port";

	/**
	 * Main method for running the {@link OfficeBuilding}.
	 * 
	 * @param arguments
	 *            Command line arguments.
	 * @throws Throwable
	 *             If fails.
	 */
	public static void main(String... arguments) throws Throwable {

		// Obtain the Office Building Home (property then environment)
		String officeBuildingHomeValue = System
				.getProperty(OFFICE_BUILDING_HOME);
		if (isBlank(officeBuildingHomeValue)) {
			// Not property, so look into the environment
			officeBuildingHomeValue = System.getenv(OFFICE_BUILDING_HOME);
			if (isBlank(officeBuildingHomeValue)) {
				// Must have Office Building Home specified
				errorAndExit("Must specify " + OFFICE_BUILDING_HOME);
			}
		}

		// Ensure the Office Building Home exists
		File officeBuildingHome = new File(officeBuildingHomeValue);
		if (!officeBuildingHome.isDirectory()) {
			// Must be directory
			errorAndExit(OFFICE_BUILDING_HOME + " directory does not exist: "
					+ officeBuildingHomeValue);
		}

		// Create the property locator
		File propertiesFile = new File(officeBuildingHome,
				"config/OfficeBuilding.properties");
		PropertyLocator properties = new PropertyLocator(propertiesFile);

		// Obtain the Office Building host
		String officeBuildingHost = properties.getProperty(
				PROPERTY_OFFICE_BUILDING_HOST, null);
		if (isBlank(officeBuildingHost)) {
			// Default to local host
			officeBuildingHost = InetAddress.getLocalHost().getHostName();
		}

		// Obtain the port for the Office Building
		int officeBuildingPort = properties
				.getIntegerProperty(PROPERTY_OFFICE_BUILDING_PORT);

		// TODO process the command
		System.out.println("TODO process the command");
	}

	/**
	 * Determines if the value is blank (<code>null</code> or empty string).
	 * 
	 * @param value
	 *            Value.
	 * @return <code>true</code> if blank.
	 */
	private static boolean isBlank(String value) {
		return ((value == null) || (value.trim().length() == 0));
	}

	/**
	 * Flag to indicate that testing so not to exit process but rather throw an
	 * exception.
	 */
	static boolean isTesting = false;

	/**
	 * Provides error message and exits process.
	 * 
	 * @param message
	 *            Message to be displayed.
	 */
	private static void errorAndExit(String message) {

		// Provide error message
		System.err.println(message);

		// Handle if testing
		if (isTesting) {
			throw new Error("Exit");
		}

		// Not testing so exit process
		System.exit(1);
	}

	/**
	 * Provides error message and exists process should the <code>value</code>
	 * be <code>null</code>.
	 * 
	 * @param value
	 *            Value to check if blank.
	 * @param message
	 *            Message display if value is blank.
	 */
	private static void errorAndExitOnBlankValue(String value, String message) {
		if (isBlank(value)) {
			errorAndExit(message);
		}
	}

	/**
	 * Should only use the <code>main</code> method.
	 */
	private OfficeBuilding() {
	}

	/**
	 * Locates a property value.
	 */
	private static class PropertyLocator {

		/**
		 * Properties file.
		 */
		private final File propertiesFile;

		/**
		 * {@link Properties} from the properties file.
		 */
		private final Properties fileProperties = new Properties();

		/**
		 * Initiate.
		 * 
		 * @param propertiesFile
		 *            Properties file.
		 */
		public PropertyLocator(File propertiesFile) throws Exception {

			// Ensure have properties file
			this.propertiesFile = propertiesFile;
			if (!this.propertiesFile.isFile()) {
				OfficeBuilding.errorAndExit("Can not find properties file "
						+ this.propertiesFile.getAbsolutePath());
			}

			// Load the properties
			this.fileProperties.load(new FileReader(propertiesFile));
		}

		/**
		 * Obtains the property value.
		 * 
		 * @param name
		 *            Name of the property.
		 * @return Value for the property or if not value available the default
		 *         value.
		 */
		public String getProperty(String name, String defaultValue) {

			// System properties always override file properties
			String value = System.getProperty(name);
			if (OfficeBuilding.isBlank(value)) {
				value = this.fileProperties.getProperty(name);
			}

			// Return the property value (or default if no value)
			return (OfficeBuilding.isBlank(value) ? defaultValue : value);
		}

		/**
		 * Obtains the property value.
		 * 
		 * @param name
		 *            Name of the property.
		 * @return Value for the property.
		 */
		public String getProperty(String name) {

			// Obtain the property value
			String value = this.getProperty(name, null);

			// Ensure have the property value
			OfficeBuilding.errorAndExitOnBlankValue(value,
					"Must provide property value for '" + name + "' in "
							+ this.propertiesFile.getAbsolutePath());

			// Return the property value
			return value;
		}

		/**
		 * Obtains the integer property value.
		 * 
		 * @param name
		 *            Name of the property.
		 * @return Integer value for the property.
		 */
		public int getIntegerProperty(String name) {

			// Obtain the property value
			String value = this.getProperty(name);

			try {
				// Transform to integer
				int intValue = Integer.parseInt(value);

				// Have integer value so return it
				return intValue;

			} catch (NumberFormatException ex) {
				// Provide error on invalid property
				errorAndExit("Property " + name + " must be an integer");
				throw new IllegalStateException("Should error before here");
			}
		}
	}

}