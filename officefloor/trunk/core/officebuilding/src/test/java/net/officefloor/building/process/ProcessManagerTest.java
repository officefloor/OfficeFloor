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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.TestCase;
import net.officefloor.building.util.OfficeBuildingTestUtil;

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

	@Override
	protected void tearDown() throws Exception {
		// Ensure process is stopped
		if (this.manager != null) {
			this.manager.destroyProcess();
		}
	}

	/**
	 * Ensure able to start a {@link Process}.
	 */
	public void testStartProcess() throws Exception {

		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Start the process
		this.manager = ProcessManager.startProcess(new WriteToFileProcess(file
				.getAbsolutePath(), TEST_CONTENT), null);

		// Wait until process writes content to file
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				TEST_CONTENT, file);
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

		// Listen for stopping of process
		final boolean[] isStopped = new boolean[1];
		synchronized (isStopped) {
			isStopped[0] = false;
		}
		ProcessCompletionListener listener = new ProcessCompletionListener() {
			@Override
			public void notifyProcessComplete(ProcessManager manager) {
				// Flag complete
				synchronized (isStopped) {
					isStopped[0] = true;
				}

				// Ensure the correct manager
				assertSame("Incorrect manager",
						ProcessManagerTest.this.manager, manager);
			}
		};

		// Provide listener to process
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessCompletionListener(listener);

		// Start the process
		this.manager = ProcessManager.startProcess(new LoopUntilStopProcess(),
				configuration);

		// Flag to stop the process
		this.manager.triggerStopProcess();

		// Wait until process completes
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager);

		// Ensure listener notified of process stopped
		synchronized (isStopped) {
			assertTrue("Listener must be notified", isStopped[0]);
		}
	}

	/**
	 * Ensure on failing to init the {@link ManagedProcess} that the exception
	 * is feed back.
	 */
	public void testFailInitProcess() throws Exception {

		final String FAILURE_MESSAGE = "TEST FAILURE";
		final Throwable failure = new Throwable(FAILURE_MESSAGE);

		// Should fail to start
		ManagedProcess managedProcess = new FailInitProcess(failure);
		try {
			ProcessManager.startProcess(managedProcess, null);
			fail("Should fail to start");
		} catch (ProcessException ex) {
			// Ensure correct message
			assertEquals("Incorrect failure message",
					"Failed to start ProcessShell for " + managedProcess + " ["
							+ FailInitProcess.class.getName() + "]", ex
							.getMessage());

			// Ensure correct cause
			Throwable cause = ex.getCause();
			assertEquals("Incorrect exception", FAILURE_MESSAGE, cause
					.getMessage());
		}
	}

	/**
	 * {@link ManagedProcess} to fail init.
	 */
	private static class FailInitProcess implements ManagedProcess {

		/**
		 * Failure to propagate from the init method.
		 */
		private final Throwable failure;

		/**
		 * Initiate.
		 * 
		 * @param failure
		 *            Failure to propagate from the init method.
		 */
		public FailInitProcess(Throwable failure) {
			this.failure = failure;
		}

		/*
		 * =================== ManagedProcess =============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Propagate failure to init
			throw this.failure;
		}

		@Override
		public void main() throws Throwable {
			fail("Should not be invoked");
		}
	}

	/**
	 * Ensure JVM options set for process.
	 */
	public void testJvmOptions() throws Exception {

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Provide the JVM options
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration
				.setJvmOptions("-Dtest.property1=One -Dtest.property2=Two");

		// Ensure properties not available
		System.clearProperty("test.property1");
		System.clearProperty("test.property2");

		// Start the process
		this.manager = ProcessManager.startProcess(new JvmOptionsProcess(file
				.getAbsolutePath()), configuration);

		// Wait until process writes content to file
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				"Two", file);
	}

	/**
	 * {@link ManagedProcess} to check that JVM options available.
	 */
	private static class JvmOptionsProcess implements ManagedProcess {

		/**
		 * Location of file to write System property value.
		 */
		private final String filePath;

		/**
		 * Initiate.
		 * 
		 * @param filePath
		 *            Location of file to write System property value.
		 */
		public JvmOptionsProcess(String filePath) {
			this.filePath = filePath;
		}

		/*
		 * =================== ManagedProcess =============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {

			// Ensure the System property is specified
			String property = System.getProperty("test.property1");

			// Ensure correct property value
			assertEquals("Incorrect system property value", "One", property);
		}

		@Override
		public void main() throws Throwable {

			// Obtain the file
			File file = new File(this.filePath);

			// Obtain System property
			String property = System.getProperty("test.property2");

			// Write the value to the file
			Writer writer = new FileWriter(file);
			writer.write(property);
			writer.close();
		}
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
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager);
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
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager);

		// Ensure the MBeans are unregistered
		assertFalse("Mock MBean should be unregistered", server
				.isRegistered(mockLocalMBeanName));
		assertFalse("Process Shell MBean should be unregistered", server
				.isRegistered(processShellLocalMBeanName));
		assertFalse("Process Manager MBean should be unregistered", server
				.isRegistered(processManagerLocalMBeanName));
	}

}