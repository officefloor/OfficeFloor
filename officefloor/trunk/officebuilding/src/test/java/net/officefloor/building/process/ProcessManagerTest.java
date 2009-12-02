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
package net.officefloor.building.process;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Tests the management of a {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessManagerTest extends TestCase {

	/**
	 * {@link ProcessManager}.
	 */
	private ProcessManager manager;

	/**
	 * Maximum run time for tests.
	 */
	private static final long MAX_RUN_TIME = 1000;

	@Override
	protected void tearDown() throws Exception {
		// Ensure process is stopped
		this.manager.destroyProcess();
	}

	/**
	 * Ensure able to start a {@link Process}.
	 */
	public void testStartProcess() throws Exception {

		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = File.createTempFile(this.getClass().getSimpleName(), "txt");

		// Start the process
		this.manager = ProcessManager.startProcess(new WriteToFileProcess(file
				.getAbsolutePath(), TEST_CONTENT), null);

		// Wait until process writes content to file
		this.waitUntilProcessComplete(this.manager);

		// Obtain the content from file
		StringBuilder content = new StringBuilder();
		FileReader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}

		// Ensure content in file
		assertEquals("Content should be in file", TEST_CONTENT, content
				.toString());
	}

	/**
	 * {@link ManagedProcess} to write to a file.
	 */
	public static class WriteToFileProcess implements ManagedProcess {

		/**
		 * Path to the {@link File}.
		 */
		private final String filePath;

		/**
		 * Content to write to the {@link File}.
		 */
		private final String content;

		/**
		 * Initiate.
		 * 
		 * @param filePath
		 *            Path to the {@link File}.
		 * @param content
		 *            Content to write to the {@link File}.
		 */
		public WriteToFileProcess(String filePath, String content) {
			this.filePath = filePath;
			this.content = content;
		}

		/*
		 * =================== ManagedProcess ==============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Wait some time to ensure wait for process to complete
			Thread.sleep(100);

			// Obtain the file
			File file = new File(this.filePath);

			// Write the content to the file
			Writer writer = new FileWriter(file);
			writer.write(this.content);
			writer.close();
		}
	}

	/**
	 * Ensure able to stop the {@link Process}.
	 */
	public void testStopProcess() throws Exception {

		// Start the process
		this.manager = ProcessManager.startProcess(new LoopUntilStopProcess(),
				null);

		// Flag to stop the process
		this.manager.triggerStopProcess();

		// Wait until process completes
		this.waitUntilProcessComplete(this.manager);
	}

	/**
	 * Ensure able to destroy the {@link Process}.
	 */
	public void testDestroyProcess() throws Exception {

		// Start the process
		this.manager = ProcessManager.startProcess(new LoopUntilStopProcess(),
				null);

		// Destroy the process
		this.manager.destroyProcess();

		// Wait until process completes
		this.waitUntilProcessComplete(this.manager);
	}

	/**
	 * {@link ManagedProcess} to loop until informed to stop.
	 */
	public static class LoopUntilStopProcess implements ManagedProcess {

		/**
		 * {@link ManagedProcessContext}.
		 */
		protected ManagedProcessContext context;

		/*
		 * =================== ManagedProcess =============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			this.context = context;
		}

		@Override
		public void main() throws Throwable {
			// Loop until informed to stop
			for (;;) {
				if (this.context.continueProcessing()) {
					// Wait a little more until told to stop
					Thread.sleep(100);
				} else {
					// Informed to stop
					return;
				}
			}
		}
	}

	/**
	 * Ensure {@link ManagedProcess} can register a MBean.
	 */
	public void testMBeanRegistration() throws Exception {

		// Obtain the MBean Server
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Start the process
		final ObjectName remoteMBeanName = new ObjectName("remote", "type",
				"mock");
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessName("local");
		configuration.setMbeanServer(server);
		this.manager = ProcessManager.startProcess(new MBeanProcess(
				remoteMBeanName), configuration);

		// Ensure can access mock MBean
		ObjectName localMBeanName = this.manager
				.getLocalObjectName(remoteMBeanName);
		Object value = server.getAttribute(localMBeanName,
				Mock.TEST_VALUE_ATTRIBUTE_NAME);
		assertEquals("Incorrect test value", new Mock().getTestValue(), value);

		// Ensure can access Process Shell MBean
		ObjectInstance instance = server.getObjectInstance(this.manager
				.getLocalObjectName(ProcessShell.PROCESS_SHELL_OBJECT_NAME));
		assertNotNull("Should have Process Shell MBean", instance);

		// Ensure Process Manager MBean registered
		assertTrue(
				"Process Manager MBean should be registered",
				server
						.isRegistered(this.manager
								.getLocalObjectName(ProcessManager.PROCESS_MANAGER_OBJECT_NAME)));
	}

	/**
	 * {@link ManagedProcess} to register the MBean.
	 */
	public static class MBeanProcess extends LoopUntilStopProcess {

		/**
		 * {@link ObjectName}.
		 */
		private final ObjectName objectName;

		/**
		 * Initiate.
		 * 
		 * @param objectName
		 *            {@link ObjectName}.
		 */
		public MBeanProcess(ObjectName objectName) {
			this.objectName = objectName;
		}

		/*
		 * =================== LoopUntilStopProcess =======================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			this.context = context;

			// Register the mock MBean
			Mock mbean = new Mock();
			context.registerMBean(mbean, this.objectName);
		}
	}

	/**
	 * Ensure MBeans are unregistered after the {@link ManagedProcess}
	 * completes.
	 */
	public void testMBeansUnregistered() throws Exception {

		// Obtain the MBean Server
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Start the process
		final ObjectName mockRemoteMBeanName = new ObjectName("remote", "type",
				"mock");
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessName("local");
		configuration.setMbeanServer(server);
		this.manager = ProcessManager.startProcess(new MBeanProcess(
				mockRemoteMBeanName), configuration);

		// Obtain the local MBean names
		ObjectName mockLocalMBeanName = this.manager
				.getLocalObjectName(mockRemoteMBeanName);
		ObjectName processShellLocalMBeanName = this.manager
				.getLocalObjectName(ProcessShell.PROCESS_SHELL_OBJECT_NAME);
		ObjectName processManagerLocalMBeanName = this.manager
				.getLocalObjectName(ProcessManager.PROCESS_MANAGER_OBJECT_NAME);

		// Ensure the MBeans are registered
		assertTrue("Mock MBean not registered", server
				.isRegistered(mockLocalMBeanName));
		assertTrue("Process Shell MBean not registered", server
				.isRegistered(processShellLocalMBeanName));
		assertTrue("Process Manager MBean not registered", server
				.isRegistered(processManagerLocalMBeanName));

		// Stop the process
		this.manager.triggerStopProcess();
		this.waitUntilProcessComplete(this.manager);

		// Ensure the MBeans are unregistered
		assertFalse("Mock MBean should be unregistered", server
				.isRegistered(mockLocalMBeanName));
		assertFalse("Process Shell MBean should be unregistered", server
				.isRegistered(processShellLocalMBeanName));
		assertFalse("Process Manager MBean should be unregistered", server
				.isRegistered(processManagerLocalMBeanName));
	}

	/**
	 * Waits until the {@link Process} is complete (or times out).
	 */
	private void waitUntilProcessComplete(ProcessManager manager)
			throws Exception {
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				manager.destroyProcess();
				fail("Processing took too long");
			}

			// Wait some time for further processing
			Thread.sleep(100);
		}
	}

}