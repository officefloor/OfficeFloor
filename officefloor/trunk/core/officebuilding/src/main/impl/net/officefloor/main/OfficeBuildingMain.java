/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.main;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Properties;

import javax.management.remote.JMXServiceURL;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;

/**
 * <p>
 * OfficeBuilding.
 * <p>
 * This provides only the <code>main</code> method for starting the
 * OfficeBuilding and running commands.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingMain {

	/**
	 * Environment property to specify where the Office Building home directory
	 * is located.
	 */
	public static final String OFFICE_BUILDING_HOME = "OFFICE_BUILDING_HOME";

	/**
	 * Relative path from the {@link #OFFICE_BUILDING_HOME} to find the
	 * properties file.
	 */
	public static final String PROPERTIES_FILE_RELATIVE_PATH = "todo_remove/OfficeBuilding.properties";

	/**
	 * Name of the property defining the host that the
	 * {@link OfficeBuildingMain} is residing on. This property is ignored if
	 * starting the {@link OfficeBuildingMain} as always run on localhost.
	 */
	public static final String PROPERTY_OFFICE_BUILDING_HOST = "office.building.host";

	/**
	 * Name of the property defining the port that the
	 * {@link OfficeBuildingMain} is to run or is running on.
	 */
	public static final String PROPERTY_OFFICE_BUILDING_PORT = "office.building.port";

	/**
	 * Name of the property defining the path to the local repository.
	 */
	public static final String PROPERTY_LOCAL_REPOSITORY_PATH = "local.repository.path";

	/**
	 * Name of the property defining the URL to the remote repository.
	 */
	public static final String PROPERTY_REMOTE_REPOSITORY_URL = "remote.repository.url";

	/**
	 * Wait time to stop the {@link OfficeBuildingMain}.
	 */
	public static final String PROPERTY_STOP_WAIT_TIME = "stop.wait.time";

	/**
	 * Usage message.
	 */
	static final String USAGE_MESSAGE = "USAGE: java ... "
			+ OfficeBuildingMain.class.getName()
			+ " <command>\n"
			+ "\n"
			+ "commands:\n"
			+ "\tstart\tStarts OfficeBuilding\n"
			+ "\tstop [<timeout>]\tStops the OfficeBuilding\n"
			+ "\turl <host> <port>\tOutputs the URL for an OfficeBuilding at the host and port\n"
			+ "\topen <process name> <component name> <office floor location> [<JVM options>]\tOpens the OfficeFloor\n"
			+ "\tclose <process name space>\tCloses the OfficeFloor\n"
			+ "\tlist [<process name space>]\tLists the process name spaces or if process name space provided the tasks of the corresponding OfficeFloor\n"
			+ "\tinvoke <process name space> <office name> <work name> [<task name>] [<parameter>]\tInvokes the Task within the OfficeFloor\n";

	/**
	 * Main method for running the {@link OfficeBuildingMain}.
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
				PROPERTIES_FILE_RELATIVE_PATH);
		PropertyLocator properties = new PropertyLocator(propertiesFile);

		// Obtain the Office Building host.
		// Host name not provided as default to save reverse DNS lookup.
		String officeBuildingHost = properties.getProperty(
				PROPERTY_OFFICE_BUILDING_HOST, null);
		if (isBlank(officeBuildingHost)) {
			// Default to local host
			officeBuildingHost = InetAddress.getLocalHost().getHostName();
		}

		// Obtain the port for the Office Building
		int officeBuildingPort = properties
				.getIntegerProperty(PROPERTY_OFFICE_BUILDING_PORT);

		// Obtain the command
		String command = "";
		if (arguments.length >= 1) {
			command = arguments[0];
		}

		// Handle the command
		if ("start".equalsIgnoreCase(command)) {

			// Obtain the local repository path
			String localRepositoryPath = properties.getProperty(
					PROPERTY_LOCAL_REPOSITORY_PATH, null);

			// Obtain the remote repository URL
			String remoteRepositoryUrl = properties
					.getProperty(PROPERTY_REMOTE_REPOSITORY_URL);
			String[] remoteRepositoryUrls = remoteRepositoryUrl.split(",");
			for (int i = 0; i < remoteRepositoryUrls.length; i++) {
				remoteRepositoryUrls[i] = remoteRepositoryUrls[i].trim();
			}

			// Start the OfficeBuilding
			OfficeBuildingManager manager = OfficeBuildingManager
					.startOfficeBuilding(officeBuildingPort, new File(
							localRepositoryPath), remoteRepositoryUrls, System
							.getProperties(), null);

			// Indicate started and location
			String serviceUrl = manager.getOfficeBuildingJmxServiceUrl();
			System.out.println("OfficeBuilding started at " + serviceUrl);

		} else if ("stop".equalsIgnoreCase(command)) {
			// Obtain wait time to stop the OfficeBuilding
			long stopWaitTime;
			if (arguments.length > 1) {
				// Use the command line stop wait time
				try {
					stopWaitTime = Long.parseLong(arguments[1]);
				} catch (NumberFormatException ex) {
					errorAndExit("ERROR: Stop timeout must be a long");
					return; // Should not get here as exit
				}
			} else {
				// Obtain the default stop wait time
				stopWaitTime = properties
						.getLongProperty(PROPERTY_STOP_WAIT_TIME);
			}

			// Stop the OfficeBuilding
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.getOfficeBuildingManager(officeBuildingHost,
							officeBuildingPort);
			String stopDetails = manager.stopOfficeBuilding(stopWaitTime);

			// Provide details of stopping the OfficeBuilding
			System.out.println(stopDetails);

		} else if ("url".equalsIgnoreCase(command)) {
			// Providing URL so obtain host and port
			if (arguments.length < 3) {
				// Must have host and port
				errorAndExit("ERROR: Must provide host and port arguments for 'url' command");
			}
			String hostName = arguments[1];
			int port = Integer.parseInt(arguments[2]);

			// Obtain the URL and output
			JMXServiceURL serviceUrl = OfficeBuildingManager
					.getOfficeBuildingJmxServiceUrl(hostName, port);
			System.out.println(serviceUrl.toString());

		} else if ("open".equalsIgnoreCase(command)) {
			// Opening OfficeFloor so obtain arguments
			if (arguments.length < 4) {
				errorAndExit("ERROR: must provide details of OfficeFloor for 'open' command");
			}
			String processName = arguments[1];
			String componentName = arguments[2];
			String officeFloorLocation = arguments[3];
			String jvmOptions = (arguments.length > 4 ? arguments[4] : "");

			// Obtain the OfficeBuilding manager
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.getOfficeBuildingManager(officeBuildingHost,
							officeBuildingPort);
			String processNameSpace;

			// Determine if Jar or Artifact
			if (new File(componentName).exists()) {
				// Open as Jar
				processNameSpace = manager.openOfficeFloor(processName,
						componentName, officeFloorLocation, jvmOptions);

			} else if (componentName.contains(":")) {
				// Open as Artifact, so obtain fragments of artifact name
				String[] fragments = componentName.split(":");
				if (fragments.length < 3) {
					errorAndExit("ERROR: artifact must be named in form 'groupId:artifactId:version[:type[:classifier]]");
				}
				String groupId = fragments[0];
				String artifactId = fragments[1];
				String version = fragments[2];
				String type = (fragments.length > 3 ? fragments[3] : "jar");
				String classifier = (fragments.length > 4 ? fragments[4] : null);

				// Open as Artifact
				processNameSpace = manager.openOfficeFloor(processName,
						groupId, artifactId, version, type, classifier,
						officeFloorLocation, jvmOptions);

			} else {
				// Unknown component
				errorAndExit("Unknown component to open OfficeFloor: '"
						+ componentName + "'");
				processNameSpace = null;
			}

			// Provide details of starting OfficeFloor
			System.out.println("OfficeFloor open under process name space '"
					+ processNameSpace + "'");

		} else if ("close".equalsIgnoreCase(command)) {
			// Closing OfficeFloor so obtain its process name space
			if (arguments.length < 2) {
				errorAndExit("ERROR: must provide OfficeFloor process name space for 'close' command");
			}
			String processNameSpace = arguments[1];

			// Obtain wait time to close the OfficeFloor
			long closeWaitTime;
			if (arguments.length > 2) {
				// Use the command line stop wait time
				try {
					closeWaitTime = Long.parseLong(arguments[2]);
				} catch (NumberFormatException ex) {
					errorAndExit("ERROR: Close timeout must be a long");
					return; // Should not get here as exit
				}
			} else {
				// Obtain the default stop wait time
				closeWaitTime = properties
						.getLongProperty(PROPERTY_STOP_WAIT_TIME);
			}

			// Close the OfficeFloor
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.getOfficeBuildingManager(officeBuildingHost,
							officeBuildingPort);
			manager.closeOfficeFloor(processNameSpace, closeWaitTime);

			// Provide details of closing OfficeFloor
			System.out.println("OfficeFloor under process name space '"
					+ processNameSpace + "' closed");

		} else if ("list".equalsIgnoreCase(command)) {
			// Listing so determine if processes or tasks
			String processNamespace = (arguments.length > 1 ? arguments[1]
					: null);
			String listing;
			if (processNamespace == null) {
				// List the processes
				OfficeBuildingManagerMBean officeBuildingManager = OfficeBuildingManager
						.getOfficeBuildingManager(officeBuildingHost,
								officeBuildingPort);
				listing = officeBuildingManager.listProcessNamespaces();
			} else {
				// List the tasks of the process name space
				OfficeFloorManagerMBean officeFloorManager = OfficeBuildingManager
						.getOfficeFloorManager(officeBuildingHost,
								officeBuildingPort, processNamespace);
				listing = officeFloorManager.listTasks();
			}

			// Output the listing
			System.out.println(listing);

		} else if ("invoke".equalsIgnoreCase(command)) {
			// Invoking task so obtain arguments
			if (arguments.length < 4) {
				errorAndExit("ERROR: must provide details of Task to invoke");
			}
			String processNamespace = arguments[1];
			String officeName = arguments[2];
			String workName = arguments[3];
			String taskName = (arguments.length > 4 ? arguments[4] : null);
			String parameter = (arguments.length > 5 ? arguments[5] : null);

			// Obtain the OfficeFloor manager
			OfficeFloorManagerMBean officeFloorManager = OfficeBuildingManager
					.getOfficeFloorManager(officeBuildingHost,
							officeBuildingPort, processNamespace);

			// Invoke the Task within the OfficeFloor
			officeFloorManager.invokeTask(officeName, workName, taskName,
					parameter);

		} else {
			// Unknown or no command
			StringBuilder message = new StringBuilder();
			if (!isBlank(command)) {
				// Provide details of unknown command
				message.append("ERROR: unknown command '" + command + "'\n\n");
			}

			// Always provide usage
			message.append(OfficeBuildingMain.USAGE_MESSAGE);

			// Display usage (and possible error).
			// Never successful as checking code should be running commands.
			errorAndExit(message.toString());
		}
	}

	/**
	 * Determines if the value is blank (<code>null</code> or empty string).
	 * 
	 * @param value
	 *            Value.
	 * @return <code>true</code> if blank.
	 */
	public static boolean isBlank(String value) {
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
			throw new Error("Exit: " + message);
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
	private OfficeBuildingMain() {
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
				OfficeBuildingMain.errorAndExit("Can not find properties file "
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
			if (OfficeBuildingMain.isBlank(value)) {
				value = this.fileProperties.getProperty(name);
			}

			// Return the property value (or default if no value)
			return (OfficeBuildingMain.isBlank(value) ? defaultValue : value);
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
			OfficeBuildingMain.errorAndExitOnBlankValue(value,
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

		/**
		 * Obtains the long property value.
		 * 
		 * @param name
		 *            Name of the property.
		 * @return Integer value for the property.
		 */
		public long getLongProperty(String name) {

			// Obtain the property value
			String value = this.getProperty(name);

			try {
				// Transform to long
				long longValue = Long.parseLong(value);

				// Have long value so return it
				return longValue;

			} catch (NumberFormatException ex) {
				// Provide error on invalid property
				errorAndExit("Property " + name + " must be a long");
				throw new IllegalStateException("Should error before here");
			}
		}
	}

}