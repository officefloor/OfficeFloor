/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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

import java.io.File;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import sun.tools.jconsole.OfficeConsole;

/**
 * Ensure able to connect to {@link OfficeBuildingManager} with similar
 * mechanism to JConsole.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeConsoleTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeBuildingManagerMBean}.
	 */
	private OfficeBuildingManagerMBean manager;

	@Override
	protected void tearDown() throws Exception {
		// Stop the office building
		if (this.manager != null) {
			this.manager.stopOfficeBuilding(1000);
		}
	}

	/**
	 * Ensure {@link OfficeConsole} can connect.
	 */
	public void testOfficeConsole() throws Exception {

		// Connection details
		int port = 13778;
		String username = OfficeBuildingTestUtil.getLoginUsername();
		String password = OfficeBuildingTestUtil.getLoginPassword();
		File trustStoreFile = OfficeBuildingTestUtil.getTrustStore();
		String trustStorePassword = OfficeBuildingTestUtil
				.getTrustStorePassword();

		// Start the Office Building
		this.manager = OfficeBuildingTestUtil.startOfficeBuilding(port);

		// Start the console connected to the Office Building
		OfficeConsole console = new OfficeConsole();
		try {
			console.run(null, port, username, password, trustStoreFile,
					trustStorePassword);

		} finally {
			// Ensure shutdown the console
			console.setVisible(false);
			console.dispose();
		}
	}

}