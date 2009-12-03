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
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Office Building Manager.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManager implements OfficeBuildingManagerMBean {

	/**
	 * {@link ObjectName} for the {@link OfficeBuildingManagerMBean}.
	 */
	static ObjectName OFFICE_BUILDING_MANAGER_OBJECT_NAME;

	static {
		try {
			OFFICE_BUILDING_MANAGER_OBJECT_NAME = new ObjectName(
					"OfficeBuilding", "type", "OfficeBuildingManager");
		} catch (MalformedObjectNameException ex) {
			// This should never be the case
		}
	}

	/**
	 * Starts the Office Building.
	 * 
	 * @param port
	 *            Port for the {@link JMXConnectorServer}.
	 * @throws IOException
	 *             If fails to start the Office Building.
	 */
	public static void startOfficeBuilding(int port) throws IOException {

		// Obtain the MBean Server
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

		// Create the Registry on the port
		LocateRegistry.createRegistry(port);

		// Start the JMX connector server
		JMXServiceURL serviceUrl = getOfficeBuildingJmxServiceUrl(port);
		JMXConnectorServer connectorServer = JMXConnectorServerFactory
				.newJMXConnectorServer(serviceUrl, null, mbeanServer);
		connectorServer.start();
	}

	/**
	 * Obtains the {@link OfficeBuildingManager} {@link JMXConnectorServer}
	 * {@link JMXServiceURL}.
	 * 
	 * @param port
	 *            Port the {@link JMXConnectorServer} is residing on.
	 * @return {@link JMXServiceURL} to the {@link OfficeBuildingManager}
	 *         {@link JMXConnectorServer}.
	 * @throws IOException
	 *             If fails to obtain the {@link JMXServiceURL}.
	 */
	public static JMXServiceURL getOfficeBuildingJmxServiceUrl(int port)
			throws IOException {
		return new JMXServiceURL("service:jmx:rmi://localhost:" + port
				+ "/jndi/rmi://localhost:" + port + "/OfficeBuilding");
	}

	/**
	 * Obtains the MBean proxy.
	 * 
	 * @param mbeanName
	 *            {@link ObjectName} for the MBean.
	 * @param mbeanInterface
	 *            MBean interface of the MBean.
	 * @return Proxy to the MBean.
	 */
	public static <I> I getMBeanProxy(ObjectName mbeanName,
			Class<I> mbeanInterface) {

		// Obtain the MBean Server connection
		MBeanServerConnection connection = ManagementFactory
				.getPlatformMBeanServer();

		// Create and return the MBean proxy
		return JMX.newMBeanProxy(connection, mbeanName, mbeanInterface);
	}

}