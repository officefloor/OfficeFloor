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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the management of a {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessManagerTest extends OfficeFrameTestCase {

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
	public void test_start_Process() throws Exception {

		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Start the process
		this.manager = ProcessManager.startProcess(
				new WriteToFileProcess(file.getAbsolutePath(), TEST_CONTENT),
				null);

		// Wait until process writes content to file
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				TEST_CONTENT, file);
	}

	/**
	 * Ensure able to run a {@link Process}.
	 */
	public void test_run_Process() throws Exception {

		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Capture class path
		String classPath = System.getProperty("java.class.path");

		// Start the process
		ProcessManager.runProcess(new WriteToFileProcess(
				file.getAbsolutePath(), TEST_CONTENT), null);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				TEST_CONTENT, file);

		// Ensure class path not changed
		assertEquals("Class path should not be changed", classPath,
				System.getProperty("java.class.path"));
	}

	/**
	 * Ensure listeners notified for running a {@link Process}.
	 */
	public void test_run_ProcessWithListeners() throws Exception {

		// Listen for starting of process
		final boolean[] isStarted = new boolean[] { false };
		ProcessStartListener startListener = new ProcessStartListener() {
			@Override
			public void processStarted(ProcessManagerMBean processManager) {
				// Flag started
				synchronized (isStarted) {
					isStarted[0] = true;
				}

				// Flag started process
				ProcessManagerTest.this.manager = (ProcessManager) processManager;
			}
		};

		// Listen for stopping of process
		final boolean[] isStopped = new boolean[] { false };
		ProcessCompletionListener completionListener = new ProcessCompletionListener() {
			@Override
			public void processCompleted(ProcessManagerMBean manager) {
				// Flag complete
				synchronized (isStopped) {
					isStopped[0] = true;
				}

				// Ensure same completed process as started process
				assertSame("Incorrect manager",
						ProcessManagerTest.this.manager, manager);
			}
		};

		// Provide listeners to process
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessStartListener(startListener);
		configuration.setProcessCompletionListener(completionListener);

		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Start the process
		ProcessManager.runProcess(new WriteToFileProcess(
				file.getAbsolutePath(), TEST_CONTENT), configuration);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				TEST_CONTENT, file);

		// Ensure listeners notified
		synchronized (isStarted) {
			assertTrue("Start listener must be notified", isStarted[0]);
		}
		synchronized (isStopped) {
			assertTrue("Completion listener must be notified", isStopped[0]);
		}
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
	 * Ensure able to configure logger.
	 */
	public void test_start_ProcessOutput() throws Exception {

		final String STDOUT_CONTENT = "STDOUT";
		final String STDERR_CONTENT = "STDERR";

		// Output stream factory
		MockProcessOutputStreamFactory factory = new MockProcessOutputStreamFactory();

		// Create process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessOutputStreamFactory(factory);

		// Start the process
		this.manager = ProcessManager.startProcess(new OutputProcess(
				STDOUT_CONTENT, STDERR_CONTENT), configuration);

		// Wait until process writes content to file
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

		// Validate the output content
		assertEquals("Incorrect stdout content", STDOUT_CONTENT, factory
				.getOutContent().trim());
		assertEquals("Incorrect stderr content", STDERR_CONTENT, factory
				.getErrContent().trim());
	}

	/**
	 * Ensure able to configure default logger.
	 */
	public void test_start_DefaultProcessOutput() throws Exception {

		// Obtain the standard streams
		PrintStream defaultOut = System.out;
		PrintStream defaultErr = System.err;

		try {

			final String STDOUT_CONTENT = "STDOUT";
			final String STDERR_CONTENT = "STDERR";

			// Override the standard streams
			ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
			System.setOut(new PrintStream(bufferOut, true));
			ByteArrayOutputStream bufferErr = new ByteArrayOutputStream();
			System.setErr(new PrintStream(bufferErr, true));

			// Start the process
			this.manager = ProcessManager.startProcess(new OutputProcess(
					STDOUT_CONTENT, STDERR_CONTENT), null);

			// Wait until process writes content to file
			OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

			// Validate the output content
			assertEquals("Incorrect stdout content", STDOUT_CONTENT,
					new String(bufferOut.toByteArray()).trim());
			assertEquals("Incorrect stderr content", STDERR_CONTENT,
					new String(bufferErr.toByteArray()).trim());

		} finally {
			// Re-instate the defaults
			System.setOut(defaultOut);
			System.setErr(defaultErr);
		}
	}

	/**
	 * {@link ManagedProcess} to output content.
	 */
	public static class OutputProcess implements ManagedProcess {

		/**
		 * <code>stdout</code> content.
		 */
		private final String stdoutContent;

		/**
		 * <code>stderr</code> content.
		 */
		private final String stderrContent;

		/**
		 * Initiate.
		 * 
		 * @param stdoutContent
		 *            <code>stdout</code> content.
		 * @param stderrContent
		 *            <code>stderr</code> content.
		 */
		public OutputProcess(String stdoutContent, String stderrContent) {
			this.stdoutContent = stdoutContent;
			this.stderrContent = stderrContent;
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

			// Output the stdout content
			System.out.println(this.stdoutContent);

			// Output the stderr content
			System.err.println(this.stderrContent);
		}
	}

	/**
	 * Ensure able to configure the class path.
	 */
	public void test_start_Classpath() throws Exception {

		// Obtain the file on class path
		final String CLASS_PATH_FILE_NAME = "Test.txt";
		final String CLASS_PATH_FILE_PATH = "classpath/" + CLASS_PATH_FILE_NAME;
		File classpathFile = this.findFile(this.getClass(),
				CLASS_PATH_FILE_PATH);
		String testContent = this.getFileContents(classpathFile).trim();

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Create process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		File additionalClasspathDir = classpathFile.getParentFile();
		configuration.setAdditionalClassPath(additionalClasspathDir
				.getAbsolutePath());

		// Start the process
		this.manager = ProcessManager.startProcess(new ClassPathProcess(
				CLASS_PATH_FILE_NAME, file.getAbsolutePath()), configuration);

		// Wait until process writes content to file
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				testContent, file);
	}

	/**
	 * Ensure able to configure the class path.
	 */
	public void test_run_Classpath() throws Exception {

		// Obtain the file on class path
		final String CLASS_PATH_FILE_NAME = "Test.txt";
		final String CLASS_PATH_FILE_PATH = "classpath/" + CLASS_PATH_FILE_NAME;
		File classpathFile = this.findFile(this.getClass(),
				CLASS_PATH_FILE_PATH);
		String testContent = this.getFileContents(classpathFile).trim();

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Create process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		File additionalClasspathDir = classpathFile.getParentFile();
		configuration.setAdditionalClassPath(additionalClasspathDir
				.getAbsolutePath());

		// Run local process
		ProcessManager.runProcess(new ClassPathProcess(CLASS_PATH_FILE_NAME,
				file.getAbsolutePath()), configuration);

		// Ensure content in file
		OfficeBuildingTestUtil.validateFileContent("Content should be in file",
				testContent, file);
	}

	/**
	 * {@link ManagedProcess} to validate class path.
	 */
	public static class ClassPathProcess implements ManagedProcess {

		/**
		 * Path on class path to find {@link File}.
		 */
		private final String classpathFilePath;

		/**
		 * Path to the target {@link File} to write content.
		 */
		private final String targetFilePath;

		/**
		 * Initiate.
		 * 
		 * @param classpathFilePath
		 *            Path on class path to find {@link File}.
		 * @param targetFilePath
		 *            Path to the target {@link File} to write content.
		 */
		public ClassPathProcess(String classpathFilePath, String targetFilePath) {
			this.classpathFilePath = classpathFilePath;
			this.targetFilePath = targetFilePath;
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

			// Obtain the file content via class path
			InputStream fileContent = Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(this.classpathFilePath);
			assertNotNull("Should find file on class path (file="
					+ this.classpathFilePath + ")", fileContent);

			// Obtain the file
			File file = new File(this.targetFilePath);

			// Write the content to the file
			OutputStream output = new FileOutputStream(file);
			for (int value = fileContent.read(); value != -1; value = fileContent
					.read()) {
				output.write(value);
			}
			output.close();
		}
	}

	/**
	 * Ensure able to stop the {@link Process}.
	 */
	public void test_start_TriggerStopProcessWithListeners() throws Exception {

		// Listen for starting of process
		final boolean[] isStarted = new boolean[] { false };
		ProcessStartListener startListener = new ProcessStartListener() {
			@Override
			public void processStarted(ProcessManagerMBean processManager) {
				// Flag started
				synchronized (isStarted) {
					isStarted[0] = true;
				}

				// Ensure the correct manager
				assertSame("Incorrect manager",
						ProcessManagerTest.this.manager, manager);
			}
		};

		// Listen for stopping of process
		final boolean[] isStopped = new boolean[] { false };
		ProcessCompletionListener completionListener = new ProcessCompletionListener() {
			@Override
			public void processCompleted(ProcessManagerMBean manager) {
				// Flag complete
				synchronized (isStopped) {
					isStopped[0] = true;
				}

				// Ensure the correct manager
				assertSame("Incorrect manager",
						ProcessManagerTest.this.manager, manager);
			}
		};

		// Provide listeners to process
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessStartListener(startListener);
		configuration.setProcessCompletionListener(completionListener);

		// Start the process
		this.manager = ProcessManager.startProcess(new LoopUntilStopProcess(),
				configuration);

		// Flag to stop the process
		this.manager.triggerStopProcess();

		// Wait until process completes
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

		// Ensure listeners notified
		synchronized (isStarted) {
			assertTrue("Start listener must be notified", isStarted[0]);
		}
		synchronized (isStopped) {
			assertTrue("Completion listener must be notified", isStopped[0]);
		}
	}

	/**
	 * Ensure on failing to init the {@link ManagedProcess} that the exception
	 * is feed back.
	 */
	public void test_start_FailInitProcess() throws Exception {

		final String FAILURE_MESSAGE = "TEST FAILURE";
		final Throwable failure = new Throwable(FAILURE_MESSAGE);

		// Should log the failure
		MockProcessOutputStreamFactory factory = new MockProcessOutputStreamFactory();
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessOutputStreamFactory(factory);

		// Should fail to start
		ManagedProcess managedProcess = new FailInitProcess(failure);
		try {
			ProcessManager.startProcess(managedProcess, configuration);
			fail("Should fail to start");
		} catch (ProcessException ex) {
			// Ensure correct message
			assertEquals("Incorrect failure message",
					"Failed to start ProcessShell for " + managedProcess + " ["
							+ FailInitProcess.class.getName() + "]",
					ex.getMessage());

			// Ensure correct cause
			Throwable cause = ex.getCause();
			assertEquals("Incorrect exception", FAILURE_MESSAGE,
					cause.getMessage());
		}

		// Create the expected end of the log message
		StringWriter expectedLogMessage = new StringWriter();
		expectedLogMessage.append("WARNING: Failed to initialise process\n");
		failure.printStackTrace(new PrintWriter(expectedLogMessage, true));
		String expectedLogMessageText = expectedLogMessage.toString().trim();

		// Obtain the log message
		String actualLogMessage = factory.getErrContent().trim();

		// Ensure log failure to initialise
		assertTrue("Should log failure to initialise, but was:\n"
				+ actualLogMessage,
				actualLogMessage.endsWith(expectedLogMessageText));
	}

	/**
	 * Ensure on failing to init the {@link ManagedProcess} that the exception
	 * is feed back.
	 */
	public void test_run_FailInitProcess() throws Exception {

		final String FAILURE_MESSAGE = "TEST FAILURE";
		final Throwable failure = new Throwable(FAILURE_MESSAGE);

		// Should fail to start
		ManagedProcess managedProcess = new FailInitProcess(failure);
		try {
			ProcessManager.runProcess(managedProcess, null);
			fail("Should fail to start");
		} catch (ProcessException ex) {
			// Ensure correct message
			assertEquals("Incorrect failure message",
					"Failed to run ProcessShell for " + managedProcess + " ["
							+ FailInitProcess.class.getName() + "]",
					ex.getMessage());

			// Ensure correct cause
			Throwable cause = ex.getCause();
			assertEquals("Incorrect exception", FAILURE_MESSAGE,
					cause.getMessage());
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
	public void test_start_JvmOptions() throws Exception {

		// Obtain temporary file to write content
		File file = OfficeBuildingTestUtil.createTempFile(this);

		// Provide the JVM options
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.addJvmOption("-Dtest.property1=One");
		configuration.addJvmOption("-Dtest.property2=Two");

		// Ensure properties not available
		System.clearProperty("test.property1");
		System.clearProperty("test.property2");

		// Start the process
		this.manager = ProcessManager.startProcess(
				new JvmOptionsProcess(file.getAbsolutePath()), configuration);

		// Wait until process writes content to file
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

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
	public void test_start_DestroyProcess() throws Exception {

		// Start the process
		this.manager = ProcessManager.startProcess(new LoopUntilStopProcess(),
				null);

		// Destroy the process
		this.manager.destroyProcess();

		// Wait until process completes
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);
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
	public void test_start_MBeanRegistration() throws Exception {

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
				server.isRegistered(this.manager
						.getLocalObjectName(ProcessManager.PROCESS_MANAGER_OBJECT_NAME)));
	}

	/**
	 * Ensure MBeans are unregistered after the {@link ManagedProcess}
	 * completes.
	 */
	public void test_start_MBeansUnregistered() throws Exception {

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
		assertTrue("Mock MBean not registered",
				server.isRegistered(mockLocalMBeanName));
		assertTrue("Process Shell MBean not registered",
				server.isRegistered(processShellLocalMBeanName));
		assertTrue("Process Manager MBean not registered",
				server.isRegistered(processManagerLocalMBeanName));

		// Stop the process
		this.manager.triggerStopProcess();
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.manager, null);

		// Ensure the MBeans are unregistered
		assertFalse("Mock MBean should be unregistered",
				server.isRegistered(mockLocalMBeanName));
		assertFalse("Process Shell MBean should be unregistered",
				server.isRegistered(processShellLocalMBeanName));
		assertFalse("Process Manager MBean should be unregistered",
				server.isRegistered(processManagerLocalMBeanName));
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
	 * Ensure {@link ManagedProcess} can register a MBean.
	 */
	public void test_run_MBeanRegistration() throws Throwable {

		// Obtain the MBean Server
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Create the process configuration
		final ObjectName localMBeanName = new ObjectName("local", "type",
				"mock");
		final ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setMbeanServer(server);

		// Create location for temporary files
		String tmpDir = System.getProperty("java.io.tmpdir");
		assertNotNull("Missing tmp directory", tmpDir);
		File workDir = null;
		int index = 0;
		do {
			workDir = new File(tmpDir, "OfficeFloorProcessManagerTest"
					+ String.valueOf(index++));
		} while (workDir.exists());

		// Create paths for flag files
		workDir.mkdir();
		String startFilePath = new File(workDir, "start.txt").getAbsolutePath();
		String completeFilePath = new File(workDir, "complete.txt")
				.getAbsolutePath();

		// Managed process to run
		final LocalMBeanProcess managedProcess = new LocalMBeanProcess(
				localMBeanName, startFilePath, completeFilePath);

		// Run
		final boolean[] isComplete = new boolean[1];
		isComplete[0] = false;
		final Throwable[] failure = new Throwable[1];
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Run the processes
					ProcessManager.runProcess(managedProcess, configuration);
				} catch (Throwable ex) {
					synchronized (failure) {
						failure[0] = ex;
					}
				} finally {
					// Flag complete
					synchronized (isComplete) {
						isComplete[0] = true;
					}
				}
			}
		}).start();

		try {
			// Wait until process started
			long startTime = System.currentTimeMillis();
			while (!new File(startFilePath).exists()) {
				synchronized (isComplete) {
					assertFalse("Process should not yet be completed",
							isComplete[0]);
				}
				assertTrue("Timed out",
						(System.currentTimeMillis() - startTime) < 50000);
				Thread.sleep(10);
			}

			// Ensure can access mock MBean
			Object value = server.getAttribute(localMBeanName,
					Mock.TEST_VALUE_ATTRIBUTE_NAME);
			assertEquals("Incorrect test value", new Mock().getTestValue(),
					value);

			// Stop execution of the process
			new File(completeFilePath).createNewFile();

			// Wait until process stopped
			startTime = System.currentTimeMillis();
			boolean isFinished = false;
			while (!isFinished) {
				synchronized (isComplete) {
					isFinished = isComplete[0];
				}
				assertTrue("Timed out",
						(System.currentTimeMillis() - startTime) < 50000);
				Thread.sleep(10);
			}

			// Ensure mock MBean unregistered after process complete
			assertFalse("MBean should not be registered",
					server.isRegistered(localMBeanName));

			// Ensure process did not fail
			synchronized (failure) {
				if (failure[0] != null) {
					throw failure[0];
				}
			}

		} finally {
			// Ensure stop process for test
			if (!new File(completeFilePath).exists()) {
				new File(completeFilePath).createNewFile();
			}
		}
	}

	/**
	 * <p>
	 * {@link ManagedProcess} to register the MBean.
	 * <p>
	 * As {@link ManagedProcess} is serialised then must use files as flags.
	 */
	public static class LocalMBeanProcess extends MBeanProcess {

		/**
		 * Start {@link File} path.
		 */
		private final String startFilePath;

		/**
		 * Complete {@link File} path.
		 */
		private final String completeFilePath;

		/**
		 * Initiate.
		 * 
		 * @param objectName
		 *            {@link ObjectName}.
		 * @param startFilePath
		 *            Start {@link File} path.
		 * @param completeFilePath
		 *            Complete {@link File} path.
		 */
		public LocalMBeanProcess(ObjectName objectName, String startFilePath,
				String completeFilePath) {
			super(objectName);
			this.startFilePath = startFilePath;
			this.completeFilePath = completeFilePath;
		}

		/*
		 * =================== LoopUntilStopProcess =======================
		 */

		@Override
		public void main() throws Throwable {

			// Flag started
			new File(this.startFilePath).createNewFile();

			// Loop until completion file exists
			for (;;) {
				if (new File(this.completeFilePath).exists()) {
					return; // exists so complete
				}
				Thread.sleep(10);
			}
		}
	}

	/**
	 * Mock {@link ProcessOutputStreamFactory}.
	 */
	private static class MockProcessOutputStreamFactory implements
			ProcessOutputStreamFactory {

		/**
		 * Standard out.
		 */
		private final ByteArrayOutputStream out = new ByteArrayOutputStream();

		/**
		 * Standard error.
		 */
		private final ByteArrayOutputStream err = new ByteArrayOutputStream();

		/**
		 * Obtains the content written to standard out.
		 * 
		 * @return Content written to standard out.
		 */
		public String getOutContent() {
			return new String(this.out.toByteArray());
		}

		/**
		 * Obtains the content written to standard err.
		 * 
		 * @return Content written to standard err.
		 */
		public String getErrContent() {
			return new String(this.err.toByteArray());
		}

		/*
		 * ==================== ProcessOutputStreamFactory ===============
		 */

		@Override
		public OutputStream createStandardProcessOutputStream(
				String processNamespace, String[] command) throws IOException {
			return this.out;
		}

		@Override
		public OutputStream createErrorProcessOutputStream(
				String processNamespace) throws IOException {
			return this.err;
		}
	}

}