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
package net.officefloor.demo;

import java.io.File;
import java.util.Properties;

import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * Tests having the {@link OfficeBuilding} run this application.
 * 
 * @author Daniel Sagenschneider
 */
public class SpawnDemoAppTest extends AbstractDemoAppTestCase {

	/**
	 * {@link OfficeBuildingManagerMBean}.
	 */
	private OfficeBuildingManagerMBean manager;

	/**
	 * {@link ProcessManager}.
	 */
	private ProcessManager processManager;

	@Override
	protected int startServer() throws Exception {

		// Details for starting the OfficeBuilding
		final String officeBuildingHost = "localhost";
		int officeBuildingPort = MockHttpServer.getAvailablePort();
		File keyStore = KeyStoreOfficeFloorCommandParameter
				.getDefaultKeyStoreFile();
		String keyStorePassword = KeyStorePasswordOfficeFloorCommandParameter.DEFAULT_KEY_STORE_PASSWORD;
		String userName = UsernameOfficeFloorCommandParameter.DEFAULT_USER_NAME;
		String password = PasswordOfficeFloorCommandParameter.DEFAULT_PASSWORD;

		// Start the OfficeBuilding
		this.processManager = OfficeBuildingManager.spawnOfficeBuilding(
				officeBuildingHost, officeBuildingPort, keyStore,
				keyStorePassword, userName, password, null, false,
				new Properties(), null, true, null, null);

		// Obtain the OfficeBuilding Manager
		this.manager = OfficeBuildingManager.getOfficeBuildingManager(
				officeBuildingHost, officeBuildingPort, keyStore,
				keyStorePassword, userName, password);

		// Obtain port for this application
		int applicationPort = MockHttpServer.getAvailablePort();

		// Obtain the WebApp directory
		File webAppDir = new File(".", "src/main/webapp");
		assertTrue("Ensure webapp directory is located (exists)",
				webAppDir.exists());

		// Open this application
		OpenOfficeFloorConfiguration configuration = new OpenOfficeFloorConfiguration(
				"woof");
		configuration.setOfficeFloorSourceClassName(WoofOfficeFloorSource.class
				.getName());
		configuration.addClassPathEntry(webAppDir.getAbsolutePath());
		configuration.addOfficeFloorProperty(
				WoofOfficeFloorSource.PROPERTY_HTTP_PORT,
				String.valueOf(applicationPort));
		String officeFloorProcessName = this.manager
				.openOfficeFloor(configuration);

		// Ensure the OfficeFloor is listed as open
		String listedProcessName = this.manager.listProcessNamespaces();
		assertEquals("OfficeFloor should be running", officeFloorProcessName,
				listedProcessName);

		// Return the application port
		return applicationPort;
	}

	@Override
	protected void stopServer() throws Exception {
		try {
			// Stop the OfficeBuilding
			this.manager.stopOfficeBuilding(10000);
		} finally {
			// Ensure stop OfficeBuilding
			this.processManager.destroyProcess();
		}
	}

}