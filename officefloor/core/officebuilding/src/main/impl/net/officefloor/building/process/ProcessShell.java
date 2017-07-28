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
package net.officefloor.building.process;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import mx4j.tools.remote.PasswordAuthenticator;

/**
 * Provides the <code>main</code> method of an invoked {@link Process} for a
 * {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessShell implements ManagedProcessContext, ProcessShellMBean {

	/**
	 * Name of the {@link #triggerStopProcess()} method to invoke via another
	 * MBean.
	 */
	public static final String TRIGGER_STOP_PROCESS_METHOD = "triggerStopProcess";

	/**
	 * Obtains the {@link ObjectName} for the {@link ProcessShellMBean}.
	 * 
	 * @param processName
	 *            Process name.
	 * @return {@link ObjectName} for the {@link ProcessShellMBean}.
	 */
	public static ObjectName getProcessShellObjectName(String processName) {
		try {
			return new ObjectName(
					"net.officefloor:type=" + ProcessShell.class.getSimpleName() + ",name=" + processName);
		} catch (MalformedObjectNameException ex) {
			// This should never be the case
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * JMX communication protocol.
	 */
	private static final String JMX_COMMUNICATION_PROTOCOL = "rmi";

	/**
	 * Entrance point for running the {@link ManagedProcess} in current
	 * {@link Process}.
	 * 
	 * @param processNamespace
	 *            {@link ManagedProcess} name space.
	 * @param serialisedManagedProcess
	 *            Serialised {@link ManagedProcess} to be run.
	 * @throws Throwable
	 *             If failure in running the {@link ManagedProcess}.
	 */
	public static void run(String processNamespace, byte[] serialisedManagedProcess) throws Throwable {

		// Obtain the managed process to run
		ObjectInputStream managedProcessInput = new ObjectInputStream(
				new ByteArrayInputStream(serialisedManagedProcess));
		ManagedProcess managedProcess = (ManagedProcess) managedProcessInput.readObject();

		// Create the context for the managed process
		ProcessShell context = new ProcessShell(processNamespace, ManagementFactory.getPlatformMBeanServer());
		try {

			// Run the process
			managedProcess.init(context);
			managedProcess.main();

		} finally {
			// Process complete so unregister MBean instances
			context.unregisterMBeans();
		}
	}

	/**
	 * Entrance point for running the {@link ManagedProcess}.
	 * 
	 * @param arguments
	 *            Arguments.
	 * @throws IOException
	 *             If failure in running the {@link ManagedProcess}.
	 */
	public static void main(String[] arguments) throws IOException {
		main(System.in, System.out, System.err, arguments);
	}

	/**
	 * Runs the process providing ability to specify the from parent pipe.
	 * 
	 * @param fromParentPipe
	 *            From parent pipe.
	 * @param logger
	 *            Logger.
	 * @param errorLogger
	 *            Logger for errors.
	 * @param arguments
	 *            Arguments.
	 * @throws IOException
	 *             If failure in running the {@link ManagedProcess}.
	 */
	static void main(InputStream fromParentPipe, PrintStream logger, PrintStream errorLogger, String... arguments)
			throws IOException {

		// Obtain pipe from parent
		ObjectInputStream fromParentObjectPipe = new ObjectInputStream(fromParentPipe);

		// Attempt to run managed process
		JMXConnectorServer connectorServer = null;
		ObjectOutputStream toParentPipe = null;
		boolean isInitilised = false;
		try {
			try {
				ProgressLogger.logInitialMessage("Process waiting on configuration", logger);

				// Obtain the name space (always first)
				Object object = fromParentObjectPipe.readObject();
				if (!(object instanceof String)) {
					throw new IllegalArgumentException(
							"First object must be a " + String.class.getName() + " (process name space)");
				}
				String processName = (String) object;

				// Obtain the Managed Process (always second)
				object = fromParentObjectPipe.readObject();
				if (!(object instanceof ManagedProcess)) {
					throw new IllegalArgumentException("Second object must be a " + ManagedProcess.class.getName());
				}
				ManagedProcess managedProcess = (ManagedProcess) object;

				// Obtain parent port (always third)
				int parentPort = fromParentObjectPipe.readInt();

				ProgressLogger.logCompleteMessage(
						"received configuration (namespace " + processName + ", management port " + parentPort + ")",
						logger);

				try (ProgressLogger progress = new ProgressLogger("Connecting to process management", "connected",
						logger)) {
					// Connect to parent to send notifications
					@SuppressWarnings("resource")
					Socket parentSocket = new Socket(); // socket closed by
														// shell
					parentSocket.connect(new InetSocketAddress(parentPort));
					toParentPipe = new ObjectOutputStream(parentSocket.getOutputStream());
				}

				try (ProgressLogger progress = new ProgressLogger("Providing process control details", "provided",
						logger)) {

					// Create the MBean Server
					MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

					// Create the server socket for the JMX Connector Server
					final ServerSocket serverSocket = new ServerSocket();
					serverSocket.bind(null); // Any available port
					int serverPort = serverSocket.getLocalPort();

					// Set up environment for JMX Connector Server
					Map<String, Object> environment = new HashMap<String, Object>();
					environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
							new RMIServerSocketFactory() {
								@Override
								public ServerSocket createServerSocket(int port) throws IOException {
									return serverSocket;
								}
							});

					/*
					 * Use password to secure JMX Connector Server.
					 * 
					 * No need to encrypt information (i.e. SSL not needed) as
					 * only the parent process will be provided the password and
					 * will be interprocess communication on the same machine.
					 */
					UUID passwordUuid = UUID.randomUUID();
					String userName = processName;
					String password = String.valueOf(passwordUuid.getMostSignificantBits())
							+ String.valueOf(passwordUuid.getLeastSignificantBits());
					InputStream passwordStream = new ByteArrayInputStream((userName + "=" + password).getBytes());
					environment.put(RMIConnectorServer.AUTHENTICATOR, new PasswordAuthenticator(passwordStream));

					// Start the JMX Connecter Server (also ensure shutdown)
					connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
							new JMXServiceURL(JMX_COMMUNICATION_PROTOCOL, null, serverPort), environment, mbeanServer);
					connectorServer.start();

					// Send connection details back to parent
					Object[] connectionDetails = new Object[3];
					connectionDetails[0] = connectorServer.getAddress();
					connectionDetails[1] = userName;
					connectionDetails[2] = password;
					toParentPipe.writeObject(connectionDetails);
					toParentPipe.flush();
				}

				try (ProgressLogger progress = new ProgressLogger("Waiting on management to connect", "connected",
						logger)) {
					// Wait on parent to connect
					fromParentObjectPipe.readBoolean();
				}

				// Create instance as context
				ProcessShell context = new ProcessShell(processName, connectorServer, toParentPipe);

				try (ProgressLogger progress = new ProgressLogger("Initialising the process", "initialised", logger)) {
					// Initialise the managed process
					managedProcess.init(context);
					isInitilised = true;
				}

				try (ProgressLogger progress = new ProgressLogger("Registering process MBean", "registered", logger)) {
					// Initialised so register the process shell MBean
					context.registerMBean(context, getProcessShellObjectName(processName));
				}

				try (ProgressLogger progress = new ProgressLogger("Waiting for startup to complete", "completed",
						logger)) {
					// Wait on parent to register initialised
					fromParentObjectPipe.readBoolean();
				}

				// Run the managed process
				logger.println("Running process");
				managedProcess.main();
				logger.println("Process finished");

			} catch (Throwable ex) {

				// Log the failure
				errorLogger.println(isInitilised ? "Process failure" : "Failed to initialise process");
				ex.printStackTrace(errorLogger);

				// Notify Process Manager of failure
				if (toParentPipe != null) {
					try {

						// Propagate process exception
						ProcessException propagation = ProcessException.propagate(ex);

						// Propagate exception to Process Manager
						toParentPipe.writeObject(propagation);
						toParentPipe.flush();
					} catch (Throwable notifyEx) {
						// Indicate failure to notify
						errorLogger.println("Failed to notify " + ProcessManager.class.getSimpleName() + " of failure");
						notifyEx.printStackTrace(errorLogger);
					}
				}

			} finally {
				// Ensure shut down JMX connector server
				if (connectorServer != null) {
					connectorServer.stop();
				}
			}

		} finally {
			// Ensure parent pipe closed (lets parent know complete)
			if (toParentPipe != null) {
				toParentPipe.close();
			}
		}
	}

	/**
	 * {@link ManagedProcess} name space.
	 */
	private final String processNamespace;

	/**
	 * {@link JMXConnectorServer}.
	 */
	private final JMXConnectorServer connectorServer;

	/**
	 * {@link ObjectOutputStream} to send notifications.
	 */
	private final ObjectOutputStream toParentPipe;

	/**
	 * {@link MBeanServer} for running locally.
	 */
	private final MBeanServer mbeanServer;

	/**
	 * {@link ObjectName} instances of the registered MBeans.
	 */
	private final List<ObjectName> registeredMbeanNames;

	/**
	 * Flag indicating if should continue processing.
	 */
	private volatile boolean isContinueProcessing = true;

	/**
	 * Initiate for {@link Process}.
	 * 
	 * @param processNamespace
	 *            {@link ManagedProcess} name space.
	 * @param connectorServer
	 *            {@link JMXConnectorServer}.
	 * @param toParentPipe
	 *            {@link ObjectOutputStream} to send the notifications.
	 */
	public ProcessShell(String processNamespace, JMXConnectorServer connectorServer, ObjectOutputStream toParentPipe) {
		this.processNamespace = processNamespace;
		this.connectorServer = connectorServer;
		this.toParentPipe = toParentPipe;
		this.mbeanServer = null;
		this.registeredMbeanNames = null;
	}

	/**
	 * Initiate for running locally.
	 * 
	 * @param processNamespace
	 *            {@link ManagedProcess} name space.
	 * @param mbeanServer
	 *            Local {@link MBeanServer}.
	 */
	public ProcessShell(String processNamespace, MBeanServer mbeanServer) {
		this.processNamespace = processNamespace;
		this.connectorServer = null;
		this.toParentPipe = null;
		this.mbeanServer = mbeanServer;
		this.registeredMbeanNames = new LinkedList<ObjectName>();
	}

	/**
	 * Unregisters the MBean instances to the {@link MBeanServer}.
	 */
	private void unregisterMBeans() {
		for (ObjectName name : this.registeredMbeanNames) {
			try {
				this.mbeanServer.unregisterMBean(name);
			} catch (Throwable ex) {
				// Ignore failure to unregister
			}
		}
	}

	/*
	 * ==================== ProcessShellMBean =============================
	 */

	@Override
	public String getJmxConnectorServiceUrl() {
		// Return the address of the JMX Connector
		return this.connectorServer.getAddress().toString();
	}

	@Override
	public synchronized void triggerStopProcess() {

		// Flag to stop processing
		this.isContinueProcessing = false;

		// Notify process stopped
		this.notifyAll();
	}

	/*
	 * ==================== ManagedProcessContext =========================
	 */

	@Override
	public String getProcessNamespace() {
		return this.processNamespace;
	}

	@Override
	public void registerMBean(Object mbean, ObjectName name)
			throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

		// Determine if running in own process
		if (this.connectorServer != null) {

			// Register the MBean locally
			this.connectorServer.getMBeanServer().registerMBean(mbean, name);

			// Notify managing parent process of the MBean
			JMXServiceURL serviceUrl = this.connectorServer.getAddress();
			try {
				this.toParentPipe.writeObject(new MBeanRegistrationNotification(serviceUrl, name));
				this.toParentPipe.flush();
			} catch (IOException ex) {
				// Must be able to register with managing parent process
				throw new MBeanRegistrationException(ex);
			}

		} else {
			// Running local process, so register locally
			this.mbeanServer.registerMBean(mbean, name);

			// Keep track of names for unregistering
			this.registeredMbeanNames.add(name);
		}
	}

	@Override
	public boolean continueProcessing() {
		return this.isContinueProcessing;
	}

}