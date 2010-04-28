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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Tests the {@link ProcessShell}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessShellTest extends TestCase {

	/**
	 * Flags whether the init for the {@link ManagedProcess} run.
	 */
	private static volatile boolean isInitRun = false;

	/**
	 * Flags whether the main for the {@link ManagedProcess} run.
	 */
	private static volatile boolean isMainRun = false;

	/**
	 * Ensure able to run {@link ProcessShell}.
	 */
	public void testRunProcess() throws Throwable {

		// Create the server socket
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(null); // Any available port
		int serverPort = serverSocket.getLocalPort();

		// Start the server on the server socket
		new ParentServer(serverSocket).start();

		// Provide configuration for shell
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(buffer);
		output.writeObject(new RunManagedProcess());
		output.writeInt(serverPort);
		output.flush();
		InputStream fromParentPipe = new ByteArrayInputStream(buffer
				.toByteArray());

		// Ensure flagged not run
		isInitRun = false;
		isMainRun = false;

		// Run the process
		ProcessShell.main(fromParentPipe);

		// Ensure the methods run
		assertTrue("init() method should be invoked", isInitRun);
		assertTrue("main() method should be inovked", isMainRun);
	}

	/**
	 * {@link ManagedProcess} to test running the {@link ProcessShell}.
	 */
	public static class RunManagedProcess implements ManagedProcess {

		/**
		 * {@link ObjectName} for the {@link Mock}.
		 */
		private ObjectName mockObjectName;

		/*
		 * ================== ManagedProcess =============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Register the MBean
			this.mockObjectName = new ObjectName("test", "type", "mock");
			context.registerMBean(new Mock(), this.mockObjectName);

			// Flag run
			isInitRun = true;
		}

		@Override
		public void main() throws Throwable {

			// Obtain the MBean Server
			MBeanServer mbeanServer = ManagementFactory
					.getPlatformMBeanServer();

			// Ensure the mock is registered
			ObjectInstance mockInstance = mbeanServer
					.getObjectInstance(this.mockObjectName);
			assertNotNull("Must have mock MBean", mockInstance);

			// Ensure the Process Shell is registered
			ObjectInstance processShellInstance = mbeanServer
					.getObjectInstance(ProcessShell.PROCESS_SHELL_OBJECT_NAME);
			assertNotNull("Must have Process Shell MBean", processShellInstance);

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

				// Loop until registered the Process Shell
				ObjectInputStream input = new ObjectInputStream(socket
						.getInputStream());
				for (;;) {
					Object object = input.readObject();
					MBeanRegistrationNotification notification = (MBeanRegistrationNotification) object;
					if (ProcessShell.PROCESS_SHELL_OBJECT_NAME
							.equals(notification.getMBeanName())) {
						return; // registered so no further need for parent
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