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

package net.officefloor.building.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

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
	 * {@link ObjectName} for the {@link ProcessShellMBean}.
	 */
	static ObjectName PROCESS_SHELL_OBJECT_NAME;

	static {
		try {
			PROCESS_SHELL_OBJECT_NAME = new ObjectName("process", "type",
					"ProcessShell");
		} catch (MalformedObjectNameException ex) {
			// This should never be the case
		}
	}

	/**
	 * Obtains the {@link ObjectName} for the {@link ProcessShellMBean}.
	 * 
	 * @return {@link ObjectName} for the {@link ProcessShellMBean}.
	 */
	public static ObjectName getProcessShellObjectName() {
		return PROCESS_SHELL_OBJECT_NAME;
	}

	/**
	 * JMX communication protocol.
	 */
	private static final String JMX_COMMUNICATION_PROTOCOL = "rmi";

	/**
	 * Entrance point for running the {@link ManagedProcess}.
	 * 
	 * @param arguments
	 *            Arguments.
	 * @throws Throwable
	 *             If failure in running the {@link ManagedProcess}.
	 */
	public static void main(String[] arguments) throws Throwable {
		main(System.in, arguments);
	}

	/**
	 * Runs the process providing ability to specify the from parent pipe.
	 * 
	 * @param fromParentPipe
	 *            From parent pipe.
	 * @param arguments
	 *            Arguments.
	 * @throws Throwable
	 *             If failure in running the {@link ManagedProcess}.
	 */
	static void main(InputStream fromParentPipe, String... arguments)
			throws Throwable {

		// Obtain pipe from parent
		ObjectInputStream fromParentObjectPipe = new ObjectInputStream(
				fromParentPipe);

		// Attempt to run managed process
		JMXConnectorServer connectorServer = null;
		ObjectOutputStream toParentPipe = null;
		try {

			// Obtain the Managed Process (always first)
			Object object = fromParentObjectPipe.readObject();
			if (!(object instanceof ManagedProcess)) {
				throw new IllegalArgumentException("First object must be a "
						+ ManagedProcess.class.getName());
			}
			ManagedProcess managedProcess = (ManagedProcess) object;

			// Connect to parent to send notifications
			int parentPort = fromParentObjectPipe.readInt();
			Socket parentSocket = new Socket();
			parentSocket.connect(new InetSocketAddress(parentPort));
			toParentPipe = new ObjectOutputStream(parentSocket
					.getOutputStream());

			// Create the MBean Server
			MBeanServer mbeanServer = ManagementFactory
					.getPlatformMBeanServer();

			// Create the server socket for the JMX Connector Server
			final ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(null); // Any available port
			int serverPort = serverSocket.getLocalPort();

			// Set up environment for JMX Connector Server
			Map<String, Object> environment = new HashMap<String, Object>();
			environment.put(
					RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
					new RMIServerSocketFactory() {
						@Override
						public ServerSocket createServerSocket(int port)
								throws IOException {
							return serverSocket;
						}
					});

			// Create and start the JMX Connecter Server (also ensure shut down)
			connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
					new JMXServiceURL(JMX_COMMUNICATION_PROTOCOL, null,
							serverPort), environment, mbeanServer);
			connectorServer.start();

			// Create instance as context
			ProcessShell context = new ProcessShell(arguments, connectorServer,
					toParentPipe);

			// Initialise the managed process
			managedProcess.init(context);

			// Initialised so register the process shell MBean
			context.registerMBean(context, PROCESS_SHELL_OBJECT_NAME);

			// Run the managed process
			managedProcess.main();

		} catch (Throwable ex) {
			// Notify Process Manager of failure
			if (toParentPipe != null) {
				try {
					toParentPipe.writeObject(ex);
					toParentPipe.flush();
				} catch (Throwable notifyEx) {
					// Indicate failure to notify.
					System.err.println("Failed to notify "
							+ ProcessManager.class.getSimpleName()
							+ " of failure:");
					notifyEx.printStackTrace();

					// Carry on the propagate actual failure
				}
			}

			// Propagate the failure
			throw ex;

		} finally {
			// Ensure shut down JMX connector server
			if (connectorServer != null) {
				connectorServer.stop();
			}
		}
	}

	/**
	 * Command arguments.
	 */
	private final String[] commandArguments;

	/**
	 * {@link JMXConnectorServer}.
	 */
	private final JMXConnectorServer connectorServer;

	/**
	 * {@link ObjectOutputStream} to send notifications.
	 */
	private final ObjectOutputStream toParentPipe;

	/**
	 * Flag indicating if should continue processing.
	 */
	private volatile boolean isContinueProcessing = true;

	/**
	 * Initiate.
	 * 
	 * @param commandArguments
	 *            Command arguments.
	 * @param connectorServer
	 *            {@link JMXConnectorServers}.
	 * @param toParentPipe
	 *            {@link ObjectOutputStream} to send the notifications.
	 */
	public ProcessShell(String[] commandArguments,
			JMXConnectorServer connectorServer, ObjectOutputStream toParentPipe) {
		this.commandArguments = commandArguments;
		this.connectorServer = connectorServer;
		this.toParentPipe = toParentPipe;
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
	public void triggerStopProcess() {
		// Flag to stop processing
		this.isContinueProcessing = false;
	}

	/*
	 * ==================== ManagedProcessContext =========================
	 */

	@Override
	public String[] getCommandArguments() {
		return this.commandArguments;
	}

	@Override
	public void registerMBean(Object mbean, ObjectName name)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {

		// Register the MBean locally
		this.connectorServer.getMBeanServer().registerMBean(mbean, name);

		// Notify managing parent process of the MBean
		JMXServiceURL serviceUrl = this.connectorServer.getAddress();
		try {
			this.toParentPipe.writeObject(new MBeanRegistrationNotification(
					serviceUrl, name));
			this.toParentPipe.flush();
		} catch (IOException ex) {
			// Must be able to register with managing parent process
			throw new MBeanRegistrationException(ex);
		}
	}

	@Override
	public boolean continueProcessing() {
		return this.isContinueProcessing;
	}

}