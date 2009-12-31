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
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.building.process.ProcessShellMBean;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeBuilding} Manager.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManager implements OfficeBuildingManagerMBean,
		ProcessCompletionListener {

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
	 * Obtains {@link ObjectName} for the {@link OfficeBuildingManagerMBean}.
	 * 
	 * @return {@link ObjectName} for the {@link OfficeBuildingManagerMBean}.
	 */
	public static ObjectName getOfficeBuildingManagerObjectName() {
		return OFFICE_BUILDING_MANAGER_OBJECT_NAME;
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

		// Ensure have Registry on the port
		Registry registry = LocateRegistry.getRegistry(port);
		try {
			// Attempt to communicate to validate if registry exists
			registry.list();
		} catch (ConnectException ex) {
			// Registry not exist, so create it
			LocateRegistry.createRegistry(port);
		}

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
	 * @throws Exception
	 *             If fails to obtain the {@link OfficeBuildingManagerMBean}.
	 */
	public static OfficeBuildingManagerMBean getOfficeBuildingManager(
			String hostName, int port) throws Exception {
		return getMBeanProxy(hostName, port,
				OFFICE_BUILDING_MANAGER_OBJECT_NAME,
				OfficeBuildingManagerMBean.class);
	}

	/**
	 * <p>
	 * Obtains the {@link ProcessManagerMBean} by the process name for the
	 * {@link OfficeBuilding}.
	 * <p>
	 * This a utility method to obtain the {@link ProcessManagerMBean} of an
	 * existing {@link Process} currently running within the
	 * {@link OfficeBuilding}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuilding} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuilding} resides.
	 * @param processNamespace
	 *            Name of the {@link Process} to obtain its
	 *            {@link ProcessManagerMBean}.
	 * @return {@link ProcessManagerMBean}.
	 * @throws Exception
	 *             If fails to obtain the {@link ProcessManagerMBean}.
	 */
	public static ProcessManagerMBean getProcessManager(String hostName,
			int port, String processNamespace) throws Exception {
		ObjectName objectName = ProcessManager.getLocalObjectName(
				processNamespace, ProcessManager.getProcessManagerObjectName());
		return getMBeanProxy(hostName, port, objectName,
				ProcessManagerMBean.class);
	}

	/**
	 * <p>
	 * Obtains the {@link ProcessShellMBean} by the process name for the
	 * {@link OfficeBuilding}.
	 * <p>
	 * This a utility method to obtain the {@link ProcessShellMBean} of an
	 * existing {@link Process} currently being managed within the
	 * {@link OfficeBuilding}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuilding} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuilding} resides.
	 * @param processNamespace
	 *            Name of the {@link Process} to obtain its
	 *            {@link ProcessShellMBean}.
	 * @return {@link ProcessShellMBean}.
	 * @throws Exception
	 *             If fails to obtain the {@link ProcessShellMBean}.
	 */
	public static ProcessShellMBean getProcessShell(String hostName, int port,
			String processNamespace) throws Exception {
		ObjectName objectName = ProcessManager.getLocalObjectName(
				processNamespace, ProcessShell.getProcessShellObjectName());
		return getMBeanProxy(hostName, port, objectName,
				ProcessShellMBean.class);
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorManagerMBean}.
	 * <p>
	 * The <code>hostName</code> and <code>port</code> are of the
	 * {@link OfficeBuilding} managing the {@link OfficeFloor} {@link Process}.
	 * They are <i>not</i> of the specific {@link Process} containing the
	 * {@link OfficeFloor}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuilding} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuilding} resides.
	 * @param officeFloorManagerUrl
	 *            URL of the {@link OfficeFloorManagerMBean}.
	 * @return {@link OfficeFloorManagerMBean}.
	 * @throws Exception
	 *             If fails to obtain {@link OfficeFloorManagerMBean}.
	 */
	public static OfficeFloorManagerMBean getOfficeFloorManager(
			String hostName, int port, String processNamespace)
			throws Exception {
		ObjectName objectName = ProcessManager.getLocalObjectName(
				processNamespace, OfficeFloorManager
						.getOfficeFloorManagerObjectName());
		return getMBeanProxy(hostName, port, objectName,
				OfficeFloorManagerMBean.class);
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
	 * {@link ProcessManager} instances of currently running {@link Process}
	 * instances.
	 */
	private final List<ProcessManager> processManagers = new LinkedList<ProcessManager>();

	/**
	 * Flags if the {@link OfficeBuilding} is open.
	 */
	private boolean isOfficeBuildingOpen = true;

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
	public synchronized String openOfficeFloor(String processName,
			String jarName, String officeFloorLocation, String jvmOptions)
			throws Exception {

		// Ensure the OfficeBuilding is open
		if (!this.isOfficeBuildingOpen) {
			throw new IllegalStateException("OfficeBuilding closed");
		}

		// Create the configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessName(processName);
		configuration.setAdditionalClassPath(jarName);
		configuration.setJvmOptions(jvmOptions);

		// Create the OfficeFloor managed process
		OfficeFloorManager managedProcess = new OfficeFloorManager(
				officeFloorLocation);

		// Run the OfficeFloor
		ProcessManager manager = ProcessManager.startProcess(managedProcess,
				configuration);

		// Register the manager for the running process
		this.processManagers.add(manager);

		// Return the process name space
		return manager.getProcessNamespace();
	}

	@Override
	public synchronized String closeOfficeFloor(String processNamespace,
			long waitTime) throws Exception {

		// Find the process manager for the OfficeFloor
		ProcessManager processManager = null;
		for (ProcessManager manager : this.processManagers) {
			if (manager.getProcessNamespace().equals(processNamespace)) {
				processManager = manager;
			}
		}
		if (processManager == null) {
			// OfficeFloor not running
			return "OfficeFloor by process name space '" + processNamespace
					+ "' not running";
		}

		// Close the OfficeFloor
		processManager.triggerStopProcess();

		// Wait until processes complete (or time out waiting)
		long startTime = System.currentTimeMillis();
		for (;;) {

			// Wait some time for OfficeFloor to close
			this.wait(1000);

			// Determine if OfficeFloor closed
			if (processManager.isProcessComplete()) {
				return "Closed"; // OfficeFloor closed
			}

			// Determine if time out waiting
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > waitTime) {
				// Timed out waiting, so destroy processes
				processManager.destroyProcess();

				// Indicate failure in closing OfficeFloor
				return "Destroyed OfficeFloor '" + processNamespace
						+ "' as timed out waiting for close of " + waitTime
						+ " milliseconds";
			}
		}
	}

	@Override
	public synchronized String stopOfficeBuilding(long waitTime)
			throws Exception {

		// Flag no longer open
		this.isOfficeBuildingOpen = false;

		// Status of stopping
		StringBuilder status = new StringBuilder();
		try {

			// Stop the running processes (if any)
			if (this.processManagers.size() > 0) {
				status.append("Stopping processes:\n");
				for (ProcessManager processManager : this.processManagers) {
					try {
						// Stop process, keeping track if successful
						status.append("\t" + processManager.getProcessName()
								+ " [" + processManager.getProcessNamespace()
								+ "]");
						processManager.triggerStopProcess();
						status.append("\n");
					} catch (Throwable ex) {
						// Indicate failure in stopping process
						status.append(" failed: " + ex.getMessage() + " ["
								+ ex.getClass().getName() + "]\n");
					}
				}
				status.append("\n");
			}

			// Wait until processes complete (or time out waiting)
			long startTime = System.currentTimeMillis();
			WAIT_FOR_COMPLETION: for (;;) {

				// Determine if all processes complete
				boolean isAllComplete = true;
				for (ProcessManager processManager : this.processManagers) {
					if (!processManager.isProcessComplete()) {
						// Process still running so not all complete
						isAllComplete = false;
					}
				}

				// No further checking if all processes complete
				if (isAllComplete) {
					break WAIT_FOR_COMPLETION;
				}

				// Determine if time out waiting
				long currentTime = System.currentTimeMillis();
				if ((currentTime - startTime) > waitTime) {
					// Timed out waiting, so destroy processes
					status.append("\nStop timeout, destroying processes:\n");
					for (ProcessManager processManager : this.processManagers) {
						try {
							status.append("\t"
									+ processManager.getProcessName() + " ["
									+ processManager.getProcessNamespace()
									+ "]");
							processManager.destroyProcess();
							status.append("\n");
						} catch (Throwable ex) {
							// Indicate failure in stopping process
							status.append(" failed: " + ex.getMessage() + " ["
									+ ex.getClass().getName() + "]\n");
						}
					}
					status.append("\n");

					// Timed out so no further waiting
					break WAIT_FOR_COMPLETION;
				}

				// Wait some time for the processes to complete
				this.wait(1000);
			}

			// Return status of stopping
			status.append(OfficeBuilding.class.getSimpleName() + " stopped\n");
			return status.toString();

		} finally {
			// Stop the connector server
			this.connectorServer.stop();

			// Unregister the Office Building Manager MBean
			MBeanServer mbeanServer = getMBeanServer();
			mbeanServer.unregisterMBean(OFFICE_BUILDING_MANAGER_OBJECT_NAME);
		}
	}

	/*
	 * =================== ProcessCompletionListener ===========================
	 */

	@Override
	public synchronized void notifyProcessComplete(ProcessManager manager) {
		// Remove manager as process no longer running
		this.processManagers.remove(manager);

		// Notify that a process manager complete
		this.notify();
	}

}