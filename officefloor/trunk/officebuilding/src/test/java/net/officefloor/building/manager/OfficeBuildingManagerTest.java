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
package net.officefloor.building.manager;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * Tests the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagerTest extends TestCase {

	/**
	 * Ensure able to start the Office Building.
	 */
	public void testStartOfficeBuilding() throws Exception {

		final int PORT = 13078;

		// Start the Office Building
		OfficeBuildingManager.startOfficeBuilding(PORT);

		// Allow time to start
		Thread.sleep(1000);

		// Attempt to connect to Office Building Manager
		this.getOfficeBuildingConnection(PORT);

		// Obtain the Office Building Manager MBean
		OfficeBuildingManagerMBean mbean = OfficeBuildingManager.getMBeanProxy(
				OfficeBuildingManager.OFFICE_BUILDING_MANAGER_OBJECT_NAME,
				OfficeBuildingManagerMBean.class);
		assertNotNull("Must have Office Building Manager MBean", mbean);
	}

	/**
	 * Waits for the {@link JMXConnectorServer} to start and then obtains a
	 * {@link MBeanServerConnection} to it.
	 * 
	 * @param port
	 *            Port of the {@link JMXConnectorServer}.
	 * @return {@link MBeanServerConnection} to the {@link JMXConnectorServer}.
	 */
	private MBeanServerConnection getOfficeBuildingConnection(int port)
			throws IOException {

		// Obtain the service url
		JMXServiceURL serviceUrl = OfficeBuildingManager
				.getOfficeBuildingJmxServiceUrl(port);

		// Obtain and return the connection
		JMXConnector connector = JMXConnectorFactory.connect(serviceUrl);
		return connector.getMBeanServerConnection();
	}

}