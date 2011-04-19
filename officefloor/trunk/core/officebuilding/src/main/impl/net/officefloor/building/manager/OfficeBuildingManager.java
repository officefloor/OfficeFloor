/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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

import net.officefloor.building.console.OfficeFloorConsole;
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessException;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.building.process.ProcessShellMBean;
import net.officefloor.building.process.ProcessStartListener;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.console.OpenOfficeFloor;
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
	 * @param environment
	 *            Environment {@link Properties}.
	 * @param mbeanServer
	 *            {@link MBeanServer}. May be <code>null</code> to use platform
	 *            {@link MBeanServer}.
	 * @return {@link OfficeBuildingManager} managing the started
	 *         {@link OfficeBuilding}.
	 * @throws Exception
	 *             If fails to start the {@link OfficeBuilding}.
	 */
	public static OfficeBuildingManager startOfficeBuilding(int port,
			Properties environment, MBeanServer mbeanServer) throws Exception {

		// Obtain the start time
		Date startTime = new Date(System.currentTimeMillis());

		// Ensure have an the MBean Server
		if (mbeanServer == null) {
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
		}

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
				serviceUrl, connectorServer, mbeanServer, environment);

		// Register the Office Building Manager
		mbeanServer.registerMBean(manager, OFFICE_BUILDING_MANAGER_OBJECT_NAME);

		// Return the Office Building Manager
		return manager;
	}

	/**
	 * Determines if the {@link OfficeBuilding} is available for use.
	 * 
	 * @param hostName
	 *            Name of host where the {@link OfficeBuilding} should be
	 *            available.
	 * @param port
	 *            Port on which the {@link OfficeBuilding} should be available.
	 * @return <code>true</code> if the {@link OfficeBuilding} is available.
	 */
	public static boolean isOfficeBuildingAvailable(String hostName, int port) {
		try {
			// Obtain the OfficeBuilding manager
			OfficeBuildingManagerMBean manager = getOfficeBuildingManager(
					hostName, port);

			// Available if not stopped
			return (!manager.isOfficeBuildingStopped());
		} catch (Exception ex) {
			return false; // not available
		}
	}

	/**
	 * Spawns an {@link OfficeBuilding} in a new {@link Process}.
	 * 
	 * @param port
	 *            Port for the {@link OfficeBuilding}.
	 * @param environment
	 *            Environment {@link Properties}. May be <code>null</code>.
	 * @param configuration
	 *            {@link ProcessConfiguration}. May be <code>null</code>.
	 * @return {@link ProcessManager} managing the started
	 *         {@link OfficeBuilding}.
	 * @throws ProcessException
	 *             If fails to spawn the {@link OfficeBuilding}.
	 */
	public static ProcessManager spawnOfficeBuilding(int port,
			Properties environment, ProcessConfiguration configuration)
			throws ProcessException {

		// Ensure have environment
		if (environment == null) {
			environment = new Properties();
		}

		// Create the OfficeBuilding managed process
		OfficeBuildingManagedProcess managedProcess = new OfficeBuildingManagedProcess(
				port, environment);

		// Ensure have process configuration
		if (configuration == null) {
			configuration = new ProcessConfiguration();
		}

		// Spawn the OfficeBuilding
		ProcessManager manager = ProcessManager.startProcess(managedProcess,
				configuration);

		// Return the Process Manager
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
				processNamespace,
				OfficeFloorManager.getOfficeFloorManagerObjectName());
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
	 * {@link ProcessManagerMBean} instances of currently running
	 * {@link Process} instances.
	 */
	private final List<ProcessManagerMBean> processManagers = new LinkedList<ProcessManagerMBean>();

	/**
	 * Flags if the {@link OfficeBuilding} is open.
	 */
	private boolean isOfficeBuildingOpen = true;

	/**
	 * Flags if the {@link OfficeBuilding} has been stopped.
	 */
	private volatile boolean isOfficeBuildingStopped = false;

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
	 * {@link MBeanServer}.
	 */
	private final MBeanServer mbeanServer;

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * May only create by starting.
	 * 
	 * @param startTime
	 *            Start time of the {@link OfficeBuilding}.
	 * @param officeBuildingServiceUrl
	 *            {@link OfficeBuilding} {@link JMXServiceURL}.
	 * @param connectorServer
	 *            {@link JMXConnectorServer} for the {@link OfficeBuilding}.
	 * @param mbeanServer
	 *            {@link MBeanServer}.
	 * @param environment
	 *            Environment {@link Properties}.
	 */
	private OfficeBuildingManager(Date startTime,
			JMXServiceURL officeBuildingServiceUrl,
			JMXConnectorServer connectorServer, MBeanServer mbeanServer,
			Properties environment) {
		this.startTime = startTime;
		this.officeBuildingServiceUrl = officeBuildingServiceUrl;
		this.connectorServer = connectorServer;
		this.mbeanServer = mbeanServer;
		this.environment = environment;
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
	public String openOfficeFloor(String arguments) throws Exception {

		// Split out the arguments
		String[] argumentEntries = arguments.trim().split("\\s+");

		// Open the OfficeFloor
		return this.openOfficeFloor(argumentEntries);
	}

	@Override
	public String openOfficeFloor(String[] arguments) throws Exception {

		// Ensure the OfficeBuilding is open
		synchronized (this) {
			if (!this.isOfficeBuildingOpen) {
				throw new IllegalStateException("OfficeBuilding closed");
			}
		}

		// Create the console to open OfficeFloor in spawned process
		OfficeFloorConsole console = new OpenOfficeFloor(true)
				.createOfficeFloorConsole("JMX", this.environment);

		// Handle to load the process manager
		final ProcessManagerMBean[] manager = new ProcessManagerMBean[1];

		// Create the listener
		ProcessStartListener listener = new ProcessStartListener() {
			@Override
			public void processStarted(ProcessManagerMBean processManager) {

				// Register the process manager
				synchronized (manager) {
					manager[0] = processManager;
				}

				// Determine if process already complete
				boolean isProcessComplete;
				synchronized (processManager) {
					isProcessComplete = processManager.isProcessComplete();
				}

				synchronized (OfficeBuildingManager.this) {
					// Only register if process not already complete
					if (!isProcessComplete) {
						// Register the manager for the running process
						OfficeBuildingManager.this.processManagers
								.add(processManager);
					}
				}
			}
		};

		// Create streams for out and err
		ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(stdOut);
		ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
		PrintStream err = new PrintStream(stdErr);

		// Run the command
		boolean isSuccessful = console.run(out, err, listener, this, arguments);

		// Ensure successful
		if (!isSuccessful) {
			// Failed, so provide error
			String message = new String(stdErr.toByteArray());
			throw new Exception(message);
		}

		// Obtain the process manager
		ProcessManagerMBean processManager;
		synchronized (manager) {
			processManager = manager[0];
		}

		// Return the process name space
		return processManager.getProcessNamespace();
	}

	@Override
	public synchronized String listProcessNamespaces() {
		// Create listing of process name spaces
		StringBuilder namespaces = new StringBuilder();
		boolean isFirst = true;
		for (ProcessManagerMBean manager : this.processManagers) {

			// Separate name spaces by end of line
			if (!isFirst) {
				namespaces.append("\n");
			}
			isFirst = false;

			// Output the name space
			String namespace = manager.getProcessNamespace();
			namespaces.append(namespace);
		}

		// Return the listing of process name spaces
		return namespaces.toString();
	}

	@Override
	public synchronized String closeOfficeFloor(String processNamespace,
			long waitTime) throws Exception {

		// Find the process manager for the OfficeFloor
		ProcessManagerMBean processManager = null;
		for (ProcessManagerMBean manager : this.processManagers) {
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
				for (ProcessManagerMBean processManager : this.processManagers) {
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
				for (ProcessManagerMBean processManager : this.processManagers) {
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
					for (ProcessManagerMBean processManager : this.processManagers) {
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
			status.append(OfficeBuilding.class.getSimpleName() + " stopped");
			return status.toString();

		} finally {
			// Flag the OfficeBuilding as stopped
			this.isOfficeBuildingStopped = true;

			// Stop the connector server
			this.connectorServer.stop();

			// Unregister the Office Building Manager MBean
			this.mbeanServer
					.unregisterMBean(OFFICE_BUILDING_MANAGER_OBJECT_NAME);

			// Notify that stopped (responsive stop spawned OfficeBuilding)
			this.notifyAll();
		}
	}

	@Override
	public boolean isOfficeBuildingStopped() {
		return this.isOfficeBuildingStopped;
	}

	/*
	 * =================== ProcessCompletionListener ===========================
	 */

	@Override
	public synchronized void processCompleted(ProcessManagerMBean manager) {
		// Remove manager as process no longer running
		this.processManagers.remove(manager);

		// Notify that a process manager complete
		this.notify();
	}

}