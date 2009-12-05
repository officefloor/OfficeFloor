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
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.Date;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import net.officefloor.building.OfficeBuilding;

/**
 * {@link OfficeBuilding} Manager.
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
	 * Main for running the {@link OfficeBuilding}.
	 * 
	 * @param arguments
	 * @throws Exception
	 */
	public static void main(String[] arguments) throws Exception {

	}

	/**
	 * Starts the {@link OfficeBuilding}.
	 * 
	 * @param port
	 *            Port for the {@link OfficeBuilding}.
	 * @return {@link OfficeBuildingManager} managing the started Office
	 *         Building.
	 * @throws Exception
	 *             If fails to start the {@link OfficeBuilding}.
	 */
	public static OfficeBuildingManager startOfficeBuilding(int port)
			throws Exception {

		// Obtain the start time
		Date startTime = new Date(System.currentTimeMillis());

		// Obtain the MBean Server
		MBeanServer mbeanServer = getMBeanServer();

		// Create the Registry on the port
		LocateRegistry.createRegistry(port);

		// Start the JMX connector server (on local host)
		JMXServiceURL serviceUrl = getOfficeBuildingJmxServiceUrl(null, port);
		JMXConnectorServer connectorServer = JMXConnectorServerFactory
				.newJMXConnectorServer(serviceUrl, null, mbeanServer);
		connectorServer.start();

		// Create the Office Building Manager
		OfficeBuildingManager manager = new OfficeBuildingManager(startTime,
				serviceUrl, connectorServer);

		// Register the Office Building Manager
		mbeanServer.registerMBean(manager, OFFICE_BUILDING_MANAGER_OBJECT_NAME);

		// Return the Office Building Manager
		return manager;
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeBuildingManagerMBean} for the
	 * {@link OfficeBuilding}.
	 * <p>
	 * This a utility method to obtain the {@link OfficeBuildingManagerMBean} of
	 * an existing {@link OfficeBuilding}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuilding} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuilding} resides.
	 * @return {@link OfficeBuildingManagerMBean}.
	 * @throws IOException
	 *             If fails to obtain the {@link OfficeBuildingManagerMBean}.
	 */
	public static OfficeBuildingManagerMBean getOfficeBuildingManager(
			String hostName, int port) throws IOException {
		return getMBeanProxy(hostName, port,
				OFFICE_BUILDING_MANAGER_OBJECT_NAME,
				OfficeBuildingManagerMBean.class);
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeBuilding} {@link JMXConnectorServer}
	 * {@link JMXServiceURL}.
	 * <p>
	 * This a utility method to obtain the {@link JMXServiceURL} of an existing
	 * {@link OfficeBuilding}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuilding} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port the {@link OfficeBuilding} is residing on.
	 * @return {@link JMXServiceURL} to the {@link OfficeBuilding}
	 *         {@link JMXConnectorServer}.
	 * @throws IOException
	 *             If fails to obtain the {@link JMXServiceURL}.
	 */
	public static JMXServiceURL getOfficeBuildingJmxServiceUrl(String hostName,
			int port) throws IOException {

		// Ensure have the host name
		if ((hostName == null) || (hostName.trim().length() == 0)) {
			// Default to localhost (though by name to useful on the network)
			hostName = InetAddress.getLocalHost().getHostName();
		}

		// Create and return the JMX service URL
		return new JMXServiceURL("service:jmx:rmi://" + hostName + ":" + port
				+ "/jndi/rmi://" + hostName + ":" + port + "/OfficeBuilding");
	}

	/**
	 * <p>
	 * Obtains the MBean proxy.
	 * <p>
	 * This a utility method to obtain an MBean from an existing Office
	 * Building.
	 * 
	 * @param hostName
	 *            Host where the {@link OfficeBuilding} resides.
	 * @param port
	 *            Port where the {@link OfficeBuilding} resides.
	 * @param mbeanName
	 *            {@link ObjectName} for the MBean.
	 * @param mbeanInterface
	 *            MBean interface of the MBean.
	 * @return Proxy to the MBean.
	 * @throws IOException
	 *             If fails to obtain the MBean proxy.
	 */
	public static <I> I getMBeanProxy(String hostName, int port,
			ObjectName mbeanName, Class<I> mbeanInterface) throws IOException {

		// Obtain the MBean Server connection
		JMXServiceURL serviceUrl = getOfficeBuildingJmxServiceUrl(hostName,
				port);
		JMXConnector connector = JMXConnectorFactory.connect(serviceUrl);
		MBeanServerConnection connection = connector.getMBeanServerConnection();

		// Create and return the MBean proxy
		return JMX.newMBeanProxy(connection, mbeanName, mbeanInterface);
	}

	/**
	 * Obtains the {@link MBeanServer}.
	 * 
	 * @return {@link MBeanServer}.
	 */
	private static MBeanServer getMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}

	/**
	 * Start time of the {@link OfficeBuilding}.
	 */
	private final Date startTime;

	/**
	 * {@link OfficeBuilding} {@link JMXServiceURL}.
	 */
	private final JMXServiceURL officeBuildingServiceUrl;

	/**
	 * {@link JMXConnectorServer} for the {@link OfficeBuilding}.
	 */
	private final JMXConnectorServer connectorServer;

	/**
	 * May only create by starting.
	 * 
	 * @param startTime
	 *            Start time of the {@link OfficeBuilding}.
	 * @param officeBuildingServiceUrl
	 *            {@link OfficeBuilding} {@link JMXServiceURL}.
	 * @param connectorServer
	 *            {@link JMXConnectorServer} for the {@link OfficeBuilding}.
	 */
	private OfficeBuildingManager(Date startTime,
			JMXServiceURL officeBuildingServiceUrl,
			JMXConnectorServer connectorServer) {
		this.startTime = startTime;
		this.officeBuildingServiceUrl = officeBuildingServiceUrl;
		this.connectorServer = connectorServer;
	}

	/*
	 * ===================== OfficeBuildingManagerMBean =======================
	 */

	@Override
	public Date getStartTime() {
		return this.startTime;
	}

	@Override
	public String getOfficeBuildingJmxServiceUrl() {
		return this.officeBuildingServiceUrl.toString();
	}

	@Override
	public String getOfficeBuildingHostName() {
		return this.officeBuildingServiceUrl.getHost();
	}

	@Override
	public int getOfficeBuildingPort() {
		return this.officeBuildingServiceUrl.getPort();
	}

	@Override
	public void stopOfficeBuilding() throws Exception {
		// Stop the connector server
		this.connectorServer.stop();
	}

}