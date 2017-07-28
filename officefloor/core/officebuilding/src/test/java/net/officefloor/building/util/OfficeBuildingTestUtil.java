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
package net.officefloor.building.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.TestCase;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Utility methods for testing the {@link OfficeBuilding} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTestUtil {

	/**
	 * Ensures the {@link OfficeBuildingManagerMBean} on the port is stopped.
	 * 
	 * @param port
	 *            Port.
	 */
	public static void ensureOfficeBuildingStopped(int port) {

		OfficeBuildingManagerMBean manager = null;
		try {
			// Attempt to connect to Office Building
			manager = OfficeBuildingManager.getOfficeBuildingManager(null, port, getTrustStore(),
					getTrustStorePassword(), getLoginUsername(), getLoginPassword());

		} catch (Exception ex) {
			// Assume not running
		}

		// Connected, so stop the Office Building
		if (manager != null) {
			try {
				manager.stopOfficeBuilding(1000);
			} catch (Exception ex) {
				throw OfficeFrameTestCase.fail(ex);
			}
		}
	}

	/**
	 * Convenience method to start the {@link OfficeBuildingManagerMBean} using
	 * details of this utility class.
	 * 
	 * @param port
	 *            Port.
	 * @return {@link OfficeBuildingManagerMBean}.
	 * @throws Exception
	 *             If fails to start the {@link OfficeBuildingManager}.
	 */
	public static OfficeBuildingManagerMBean startOfficeBuilding(int port) throws Exception {

		// Obtain the details for the Office Building
		File keyStore = getKeyStore();
		String keyStorePassword = getKeyStorePassword();
		String username = getLoginUsername();
		String password = getLoginPassword();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

		// Start the office building
		return OfficeBuildingManager.startOfficeBuilding(null, port, keyStore, keyStorePassword, username, password,
				null, false, new Properties(), mbeanServer, new String[0], false);
	}

	/**
	 * Obtains the login username.
	 */
	public static String getLoginUsername() {
		return "admin";
	}

	/**
	 * Obtains the login password.
	 */
	public static String getLoginPassword() {
		return "password";
	}

	/**
	 * Key store {@link File}.
	 */
	public static File getKeyStore() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/keystore.jks");
	}

	/**
	 * Password to the key store {@link File}.
	 */
	public static String getKeyStorePassword() {
		return "changeit";
	}

	/**
	 * Trust store {@link File}.
	 */
	public static File getTrustStore() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/keystore.jks");
	}

	/**
	 * Password to the trust store {@link File}.
	 */
	public static String getTrustStorePassword() {
		return "changeit";
	}

	/**
	 * access.properties file as per JMX specification.
	 */
	public static File getAccessPropertiesFile() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/access.properties");
	}

	/**
	 * password.properties file as per JMX specification.
	 */
	public static File getPasswordPropertiesFile() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/password.properties");
	}

	/**
	 * Creates a temporary file.
	 * 
	 * @param testCase
	 *            {@link TestCase} requiring the temporary file.
	 * @return Temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public static File createTempFile(TestCase testCase) throws IOException {

		// Obtain the file
		File file = File.createTempFile(testCase.getClass().getSimpleName(), testCase.getName());

		// Return the file
		return file;
	}

	/**
	 * Validates the contents of the file.
	 * 
	 * @param message
	 *            Message if contents are invalid.
	 * @param expectedContent
	 *            Expected content of the file.
	 * @param file
	 *            File to validate its content.
	 * @throws IOException
	 *             If fails to validate content.
	 */
	public static void validateFileContent(String message, String expectedContent, File file) throws IOException {

		// Obtain the content from file
		StringBuilder content = new StringBuilder();
		FileReader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}
		reader.close();

		// Ensure content in file
		TestCase.assertEquals("Content should be in file", expectedContent, content.toString());
	}

	/**
	 * Waits until the {@link OfficeFloor} is open.
	 * 
	 * @param officeFloorManager
	 *            {@link OfficeFloorManagerMBean}.
	 * @param processManager
	 *            {@link ProcessManagerMBean}.
	 * @param connection
	 *            {@link MBeanServerConnection}.
	 */
	public static void waitUntilOfficeFloorOpens(OfficeFloorManagerMBean officeFloorManager,
			ProcessManagerMBean processManager, MBeanServerConnection connection) throws Exception {

		// Maximum run time (allow reasonable time to close)
		final int MAX_RUN_TIME = 20000;

		// Obtain maximum finish time
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;

		// Wait until OfficeFloor opens
		while (!officeFloorManager.isOfficeFloorOpen()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				processManager.destroyProcess();
				TestCase.fail("Took too long waiting for OfficeFloor to open");
			}

			// Wait some time to open
			Thread.sleep(10);
		}

		// Local MBean instance is registered asynchronously (so must wait)
		ObjectName officeFloorName = processManager
				.getLocalObjectName(OfficeFloorManager.getOfficeFloorObjectName(processManager.getProcessName()));
		while (!connection.isRegistered(officeFloorName)) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				processManager.destroyProcess();
				TestCase.fail("Took too long waiting for OfficeFloor to register open");
			}

			// Wait some time to open
			Thread.sleep(10);
		}
	}

	/**
	 * Waits until the {@link Process} is complete (or times out).
	 * 
	 * @param manager
	 *            {@link ProcessManagerMBean} of {@link Process} to wait until
	 *            complete.
	 * @param details
	 *            Provides further details should {@link Process} time out. May
	 *            be <code>null</code>.
	 */
	public static void waitUntilProcessComplete(ProcessManagerMBean manager, FurtherDetails details) throws Exception {

		// Maximum run time (allow reasonable time to close)
		final int MAX_RUN_TIME = 20000;

		// Wait until process completes (or times out)
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				manager.destroyProcess();
				TestCase.fail("Processing took too long" + (details == null ? "" : ": " + details.getMessage()));
			}

			// Wait some time for further processing
			Thread.sleep(10);
		}
	}

	/**
	 * All access via static methods.
	 */
	private OfficeBuildingTestUtil() {
	}

}