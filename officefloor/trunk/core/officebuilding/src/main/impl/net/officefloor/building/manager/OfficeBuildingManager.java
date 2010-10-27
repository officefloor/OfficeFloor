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

package net.officefloor.building.manager;

import java.io.File;
import java.io.IOException;
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

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandParser;
import net.officefloor.building.command.OfficeFloorCommandParserImpl;
import net.officefloor.building.command.officefloor.OpenOfficeFloorCommand;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorServiceLoader;
import net.officefloor.building.execute.OfficeFloorExecutionUnit;
import net.officefloor.building.execute.OfficeFloorExecutionUnitFactory;
import net.officefloor.building.execute.OfficeFloorExecutionUnitFactoryImpl;
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.building.process.ProcessShellMBean;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.main.OfficeBuildingMain;

/**
 * {@link OfficeBuildingMain} Manager.
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
	 * Starts the {@link OfficeBuildingMain}.
	 * 
	 * @param port
	 *            Port for the {@link OfficeBuildingMain}.
	 * @param localRepositoryDirectory
	 *            Directory for the local repository. May be <code>null</code>.
	 *            User settings will typically also override this value.
	 * @param remoteRepositoryUrls
	 *            Remote repository URLs. May be <code>null</code> to not
	 *            resolve dependencies.
	 * @param environment
	 *            Environment {@link Properties}.
	 * @param mbeanServer
	 *            {@link MBeanServer}. May be <code>null</code> to use platform
	 *            {@link MBeanServer}.
	 * @return {@link OfficeBuildingManager} managing the started
	 *         {@link OfficeBuildingMain}.
	 * @throws Exception
	 *             If fails to start the {@link OfficeBuildingMain}.
	 */
	public static OfficeBuildingManager startOfficeBuilding(int port,
			File localRepositoryDirectory, String[] remoteRepositoryUrls,
			Properties environment, MBeanServer mbeanServer) throws Exception {

		// Obtain the start time
		Date startTime = new Date(System.currentTimeMillis());

		// Ensure remote repositories is not null
		remoteRepositoryUrls = (remoteRepositoryUrls == null ? new String[0]
				: remoteRepositoryUrls);

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
				serviceUrl, connectorServer, mbeanServer,
				localRepositoryDirectory, remoteRepositoryUrls, environment);

		// Register the Office Building Manager
		mbeanServer.registerMBean(manager, OFFICE_BUILDING_MANAGER_OBJECT_NAME);

		// Return the Office Building Manager
		return manager;
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeBuildingManagerMBean} for the
	 * {@link OfficeBuildingMain}.
	 * <p>
	 * This a utility method to obtain the {@link OfficeBuildingManagerMBean} of
	 * an existing {@link OfficeBuildingMain}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuildingMain} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuildingMain} resides.
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
	 * {@link OfficeBuildingMain}.
	 * <p>
	 * This a utility method to obtain the {@link ProcessManagerMBean} of an
	 * existing {@link Process} currently running within the
	 * {@link OfficeBuildingMain}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuildingMain} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuildingMain} resides.
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
	 * {@link OfficeBuildingMain}.
	 * <p>
	 * This a utility method to obtain the {@link ProcessShellMBean} of an
	 * existing {@link Process} currently being managed within the
	 * {@link OfficeBuildingMain}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuildingMain} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuildingMain} resides.
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
	 * {@link OfficeBuildingMain} managing the {@link OfficeFloor}
	 * {@link Process}. They are <i>not</i> of the specific {@link Process}
	 * containing the {@link OfficeFloor}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuildingMain} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port where the {@link OfficeBuildingMain} resides.
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
	 * Obtains the {@link OfficeBuildingMain} {@link JMXConnectorServer}
	 * {@link JMXServiceURL}.
	 * <p>
	 * This a utility method to obtain the {@link JMXServiceURL} of an existing
	 * {@link OfficeBuildingMain}.
	 * 
	 * @param hostName
	 *            Name of the host where the {@link OfficeBuildingMain} resides.
	 *            <code>null</code> indicates localhost.
	 * @param port
	 *            Port the {@link OfficeBuildingMain} is residing on.
	 * @return {@link JMXServiceURL} to the {@link OfficeBuildingMain}
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
	 *            Host where the {@link OfficeBuildingMain} resides.
	 * @param port
	 *            Port where the {@link OfficeBuildingMain} resides.
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
	 * {@link ProcessManager} instances of currently running {@link Process}
	 * instances.
	 */
	private final List<ProcessManager> processManagers = new LinkedList<ProcessManager>();

	/**
	 * Flags if the {@link OfficeBuildingMain} is open.
	 */
	private boolean isOfficeBuildingOpen = true;

	/**
	 * Start time of the {@link OfficeBuildingMain}.
	 */
	private final Date startTime;

	/**
	 * {@link OfficeBuildingMain} {@link JMXServiceURL}.
	 */
	private final JMXServiceURL officeBuildingServiceUrl;

	/**
	 * {@link JMXConnectorServer} for the {@link OfficeBuildingMain}.
	 */
	private final JMXConnectorServer connectorServer;

	/**
	 * {@link MBeanServer}.
	 */
	private final MBeanServer mbeanServer;

	/**
	 * Local repository directory.
	 */
	private final File localRepositoryDirectory;

	/**
	 * Remote repository URLs.
	 */
	private final String[] remoteRepositoryUrls;

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * May only create by starting.
	 * 
	 * @param startTime
	 *            Start time of the {@link OfficeBuildingMain}.
	 * @param officeBuildingServiceUrl
	 *            {@link OfficeBuildingMain} {@link JMXServiceURL}.
	 * @param connectorServer
	 *            {@link JMXConnectorServer} for the {@link OfficeBuildingMain}.
	 * @param mbeanServer
	 *            {@link MBeanServer}.
	 * @param localRepositoryDirectory
	 *            Local repository directory.
	 * @param remoteRepositoryUrls
	 *            Remote repository URLs.
	 * @param environment
	 *            Environment {@link Properties}.
	 */
	private OfficeBuildingManager(Date startTime,
			JMXServiceURL officeBuildingServiceUrl,
			JMXConnectorServer connectorServer, MBeanServer mbeanServer,
			File localRepositoryDirectory, String[] remoteRepositoryUrls,
			Properties environment) {
		this.startTime = startTime;
		this.officeBuildingServiceUrl = officeBuildingServiceUrl;
		this.connectorServer = connectorServer;
		this.mbeanServer = mbeanServer;
		this.localRepositoryDirectory = localRepositoryDirectory;
		this.remoteRepositoryUrls = remoteRepositoryUrls;
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
	public String openOfficeFloor(String processName, String jarName,
			String officeFloorLocation, String jvmOptions) throws Exception {

		// Create the arguments to open the OfficeFloor
		String[] arguments = OpenOfficeFloorCommand.createArguments(jarName,
				officeFloorLocation);

		// Open the OfficeFloor
		return this.openOfficeFloor(processName, arguments, jvmOptions);
	}

	@Override
	public String openOfficeFloor(String processName, String groupId,
			String artifactId, String version, String type, String classifier,
			String officeFloorLocation, String jvmOptions) throws Exception {

		// Create the arguments to open the OfficeFloor
		String[] arguments = OpenOfficeFloorCommand.createArguments(groupId,
				artifactId, version, type, classifier, officeFloorLocation);

		// Open the OfficeFloor
		return this.openOfficeFloor(processName, arguments, jvmOptions);
	}

	@Override
	public String openOfficeFloor(String processName, String[] arguments,
			String jvmOptions) throws Exception {

		// Ensure the OfficeBuilding is open
		synchronized (this) {
			if (!this.isOfficeBuildingOpen) {
				throw new IllegalStateException("OfficeBuilding closed");
			}
		}

		// Parse arguments to create command to open the OfficeFloor
		OfficeFloorCommandParser parser = new OfficeFloorCommandParserImpl(
				new OpenOfficeFloorCommand());
		OfficeFloorCommand[] commands = parser.parseCommands(arguments);

		// Obtain decorators from default class path
		OfficeFloorDecorator[] decorators = OfficeFloorDecoratorServiceLoader
				.loadOfficeFloorDecorators(null);

		// Create the execution unit
		OfficeFloorExecutionUnitFactory factory = new OfficeFloorExecutionUnitFactoryImpl(
				this.localRepositoryDirectory, this.remoteRepositoryUrls,
				this.environment, decorators);
		OfficeFloorExecutionUnit executionUnit = factory
				.createExecutionUnit(commands[0]);

		// Overwritten values for process configuration
		ProcessConfiguration configuration = executionUnit
				.getProcessConfiguration();
		if (processName != null) {
			configuration.setProcessName(processName);
		}
		if (jvmOptions != null) {
			configuration.setJvmOptions(jvmOptions);
		}

		// Run the OfficeFloor.
		// (outside lock as completion listener on failure requires locking)
		ProcessManager manager = ProcessManager.startProcess(executionUnit
				.getManagedProcess(), configuration);

		// Determine if process already complete
		boolean isProcessComplete;
		synchronized (manager) {
			isProcessComplete = manager.isProcessComplete();
		}

		synchronized (this) {
			// Only register if process not already complete
			if (!isProcessComplete) {
				// Register the manager for the running process
				this.processManagers.add(manager);
			}

			// Return the process name space
			return manager.getProcessNamespace();
		}
	}

	@Override
	public synchronized String listProcessNamespaces() {
		// Create listing of process name spaces
		StringBuilder namespaces = new StringBuilder();
		boolean isFirst = true;
		for (ProcessManager manager : this.processManagers) {
			String namespace = manager.getProcessNamespace();
			namespaces.append(namespace);
			if (!isFirst) {
				namespaces.append("\n");
			}
			isFirst = false;
		}

		// Return the listing of process name spaces
		return namespaces.toString();
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
			status.append(OfficeBuildingMain.class.getSimpleName()
					+ " stopped\n");
			return status.toString();

		} finally {
			// Stop the connector server
			this.connectorServer.stop();

			// Unregister the Office Building Manager MBean
			this.mbeanServer
					.unregisterMBean(OFFICE_BUILDING_MANAGER_OBJECT_NAME);
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