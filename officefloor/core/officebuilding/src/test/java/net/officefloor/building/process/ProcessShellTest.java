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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * Tests the {@link ProcessShell}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessShellTest extends TestCase {

	/**
	 * Flags whether the child {@link Process} was started.
	 */
	private static volatile boolean isStarted = false;

	/**
	 * Flags whether the init for the {@link ManagedProcess} run.
	 */
	private static volatile boolean isInitRun = false;

	/**
	 * Flags whether the {@link ProcessShellMBean} has been registered.
	 */
	private static volatile boolean isShellMBeanRegistered = false;

	/**
	 * Flags whether the main for the {@link ManagedProcess} run.
	 */
	private static volatile boolean isMainRun = false;

	/**
	 * Name space of the {@link ManagedProcess}.
	 */
	private static volatile String processNamespace = null;

	/**
	 * Ensure able to run {@link ProcessShell}.
	 */
	public void testRunProcess() throws Throwable {

		final String PROCESS_NAMESPACE = "Test";

		// Create the server socket
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(null); // Any available port
		int serverPort = serverSocket.getLocalPort();

		// Start the server on the server socket
		new ParentServer(serverSocket).start();

		// Provide configuration for shell
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(buffer);
		output.writeObject(PROCESS_NAMESPACE);
		output.writeObject(new RunManagedProcess());
		output.writeInt(serverPort);
		output.writeBoolean(true); // process started
		output.writeBoolean(true); // process initialised
		output.flush();
		InputStream fromParentPipe = new ByteArrayInputStream(buffer.toByteArray());

		// Provide logger
		ByteArrayOutputStream log = new ByteArrayOutputStream();
		PrintStream logger = new PrintStream(log);
		PrintStream errorLogger = new PrintStream(log);

		// Ensure flagged not run
		isStarted = false;
		isInitRun = false;
		isMainRun = false;
		processNamespace = null;

		// Run the process
		ProcessShell.main(fromParentPipe, logger, errorLogger);

		// Validate child started
		assertTrue("Child process should be started", isStarted);

		// Ensure the methods run
		assertTrue("init() method should be invoked", isInitRun);
		assertTrue("main() method should be inovked", isMainRun);

		// Ensure correct process name
		assertEquals("Incorrect process name space", PROCESS_NAMESPACE, processNamespace);
	}

	/**
	 * {@link ManagedProcess} to test running the {@link ProcessShell}.
	 */
	public static class RunManagedProcess implements ManagedProcess {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link ObjectName} for the {@link Mock}.
		 */
		private ObjectName mockObjectName;

		/*
		 * ================== ManagedProcess =============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {

			// Ensure the process has identified as being started
			long endTime = System.currentTimeMillis() + 10000; // 10 seconds
			while ((!ProcessShellTest.isStarted)) {

				// Determine if timed out waiting to start
				if (System.currentTimeMillis() > endTime) {
					fail("Process took too long to be identified as started");
				}

				// Allow some time to start
				Thread.sleep(100);
			}

			// Register the MBean
			this.mockObjectName = new ObjectName("test", "type", "mock");
			context.registerMBean(new Mock(), this.mockObjectName);

			// Flag run
			isInitRun = true;

			// Obtain the process name space
			processNamespace = context.getProcessNamespace();
		}

		@Override
		public void main() throws Throwable {

			// Obtain the MBean Server
			MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

			// Ensure the mock is registered
			ObjectInstance mockInstance = mbeanServer.getObjectInstance(this.mockObjectName);
			assertNotNull("Must have mock MBean", mockInstance);

			// Ensure the Process Shell is registered
			ObjectInstance processShellInstance = mbeanServer
					.getObjectInstance(ProcessShell.getProcessShellObjectName("Test"));
			assertNotNull("Must have Process Shell MBean", processShellInstance);

			// Ensure the process shell MBean registered with parent
			long endTime = System.currentTimeMillis() + 10000; // 10 seconds
			while ((!ProcessShellTest.isShellMBeanRegistered)) {

				// Determine if timed out waiting to start
				if (System.currentTimeMillis() > endTime) {
					fail("Process took too long to register shell");
				}

				// Allow some time to start
				Thread.sleep(100);
			}

			// Flag run
			isMainRun = true;
		}
	}

	/**
	 * Parent server for running the {@link ProcessShell}.
	 */
	public static class ParentServer extends Thread {

		/**
		 * {@link ServerSocket}.
		 */
		private final ServerSocket serverSocket;

		/**
		 * Initiate.
		 * 
		 * @param serverSocket
		 *            {@link ServerSocket}.
		 */
		public ParentServer(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}

		/*
		 * ==================== Thread ==============================
		 */

		@Override
		public void run() {
			try {

				// Accept the process shell connection
				Socket socket = this.serverSocket.accept();

				// Read connection details from child process
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				Object[] connectionDetails = (Object[]) input.readObject();
				assertEquals("Incorrect number of details", 3, connectionDetails.length);

				// Obtain the connection details
				JMXServiceURL serviceUrl = (JMXServiceURL) connectionDetails[0];
				String userName = (String) connectionDetails[1];
				String password = (String) connectionDetails[2];
				Map<String, Object> env = new HashMap<String, Object>();
				env.put(JMXConnector.CREDENTIALS, new String[] { userName, password });

				// Ensure require credentials to connect
				try {
					JMXConnectorFactory.connect(serviceUrl);
					fail("Should not successfully connect");
				} catch (SecurityException ex) {
					assertEquals("Incorrect connect failure", "Bad credentials", ex.getMessage());
				}

				// Ensure can connect to child
				JMXConnector connector = JMXConnectorFactory.connect(serviceUrl, env);
				MBeanServerConnection connection = connector.getMBeanServerConnection();
				String defaultDomain = connection.getDefaultDomain();
				assertEquals("Incorrect default domain to validate connection", "DefaultDomain", defaultDomain);

				// Child process has started
				ProcessShellTest.isStarted = true;

				// Loop until registered the Process Shell
				for (;;) {
					Object object = input.readObject();
					MBeanRegistrationNotification notification = (MBeanRegistrationNotification) object;
					if (ProcessShell.getProcessShellObjectName("Test").equals(notification.getMBeanName())) {

						// Flag registered
						ProcessShellTest.isShellMBeanRegistered = true;

						// Now registered so no further need for parent
						return;
					}

					// Wait some time
					Thread.sleep(100);
				}

			} catch (Exception ex) {
				fail("Failed to accept process shell connection");
			}
		}
	}

}