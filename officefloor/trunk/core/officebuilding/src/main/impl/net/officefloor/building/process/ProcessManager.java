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
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import mx4j.tools.remote.proxy.RemoteMBeanProxy;

/**
 * Manages a {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessManager implements ProcessManagerMBean {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(ProcessManager.class
			.getName());

	/**
	 * {@link ObjectName} for the {@link ProcessManagerMBean}.
	 */
	static ObjectName PROCESS_MANAGER_OBJECT_NAME;

	static {
		try {
			PROCESS_MANAGER_OBJECT_NAME = new ObjectName("process", "type",
					"ProcessManager");
		} catch (MalformedObjectNameException ex) {
			// This should never be the case
		}
	}

	/**
	 * Default {@link ManagedProcess} name.
	 */
	public static final String DEFAULT_PROCESS_NAME = "Process";

	/**
	 * System property to get the class path.
	 */
	private static final String SYSTEM_PROPERTY_CLASS_PATH = "java.class.path";

	/**
	 * Obtains the {@link ObjectName} for the {@link ProcessManagerMBean}.
	 * 
	 * @return {@link ObjectName} for the {@link ProcessManagerMBean}.
	 */
	public static ObjectName getProcessManagerObjectName() {
		return PROCESS_MANAGER_OBJECT_NAME;
	}

	/**
	 * Determines if the value is blank (<code>null</code> or empty string).
	 * 
	 * @param value
	 *            Value.
	 * @return <code>true</code> if blank.
	 */
	private static boolean isBlank(String value) {
		return ((value == null) || (value.trim().length() == 0));
	}

	/**
	 * Active set of MBean name spaces.
	 */
	private static final Set<String> activeMBeanNamespaces = new HashSet<String>();

	/**
	 * Runs the {@link ManagedProcess} locally within calling {@link Thread}
	 * (and subsequently current {@link Process}).
	 * 
	 * @param managedProcess
	 *            {@link ManagedProcess}.
	 * @param configuration
	 *            Optional {@link ProcessConfiguration}. May be
	 *            <code>null</code> to use defaults. Please note that most of
	 *            the attributes such as JVM options are ignored as the current
	 *            {@link Process} can not be altered.
	 * @throws ProcessException
	 *             if fails to run the {@link ManagedProcess}.
	 */
	public static void runProcess(ManagedProcess managedProcess,
			ProcessConfiguration configuration) throws ProcessException {

		// Ensure have configuration to prevent NPE
		if (configuration == null) {
			configuration = new ProcessConfiguration();
		}

		// Obtain the process class path
		final String processClassPath = getProcessClasspath(configuration);

		// Create the process class loader
		List<URL> processClassPathUrls = new LinkedList<URL>();
		try {
			for (String classPathEntry : processClassPath
					.split(File.pathSeparator)) {
				File classPathFile = new File(classPathEntry);
				URL classPathUrl = classPathFile.toURI().toURL();
				processClassPathUrls.add(classPathUrl);
			}
		} catch (MalformedURLException ex) {
			throw newProcessException(null, ex.getMessage(), ex);
		}
		try (URLClassLoader processClassLoader = new URLClassLoader(
				processClassPathUrls.toArray(new URL[processClassPathUrls
						.size()]))) {

			// Obtain the process name (also name space as not register MBeans)
			String processName = configuration.getProcessName();
			if (isBlank(processName)) {
				processName = DEFAULT_PROCESS_NAME;
			}
			final String processNamespace = processName;

			// Serialise managed process for use
			final ByteArrayOutputStream serialisedManagedProcess = new ByteArrayOutputStream();
			synchronized (serialisedManagedProcess) {
				try {
					ObjectOutputStream serialiser = new ObjectOutputStream(
							serialisedManagedProcess);
					serialiser.writeObject(managedProcess);
				} catch (IOException ex) {
					throw newProcessException(null, ex.getMessage(), ex);
				}
			}

			// Create the process manager
			ProcessManager processManager = new ProcessManager(processName,
					processName, null, null, null);

			// Maintain class path for resetting
			final String originalClassPath = System
					.getProperty(SYSTEM_PROPERTY_CLASS_PATH);
			try {

				// Specify the java class path
				System.setProperty(SYSTEM_PROPERTY_CLASS_PATH, processClassPath);

				// Notify starting listener (if one provided)
				ProcessStartListener startListener = configuration
						.getProcessStartListener();
				if (startListener != null) {
					startListener.processStarted(processManager);
				}

				// Run the process (in another thread to not change this thread)
				final boolean[] isComplete = new boolean[] { false };
				final Throwable[] failure = new Throwable[] { null };
				Thread localProcess = new Thread(new Runnable() {
					@Override
					public void run() {
						try {

							// Obtain the serialised managed process
							byte[] managedProcessBytes;
							synchronized (serialisedManagedProcess) {
								managedProcessBytes = serialisedManagedProcess
										.toByteArray();
							}

							// Create the process shell within class loader
							Class<?> shellClass = Thread.currentThread()
									.getContextClassLoader()
									.loadClass(ProcessShell.class.getName());

							// Run the process within shell
							Method runMethod = shellClass.getMethod("run",
									String.class, byte[].class);
							runMethod.invoke(null, processNamespace,
									managedProcessBytes);

						} catch (Throwable ex) {
							// Capture failure to propagate
							synchronized (isComplete) {
								if (ex instanceof InvocationTargetException) {
									// Provide run failure
									failure[0] = ex.getCause();
								} else {
									// Failure
									failure[0] = ex;
								}
							}
						} finally {
							// Flag process complete
							synchronized (isComplete) {
								isComplete[0] = true;

								// Notify complete
								isComplete.notify();
							}
						}
					}
				});

				// Execute the local process
				localProcess.setContextClassLoader(processClassLoader);
				localProcess.start();

				// Wait until process complete
				synchronized (isComplete) {
					for (;;) {
						// Propagate failure in process
						if (failure[0] != null) {
							throw newProcessException(null,
									"Failed to run ProcessShell for "
											+ managedProcess
											+ " ["
											+ managedProcess.getClass()
													.getName() + "]",
									failure[0]);
						}

						// Determine if complete
						if (isComplete[0]) {
							return; // completed so finish
						}

						// Wait some time for process to complete
						try {
							isComplete.wait(100);
						} catch (InterruptedException ex) {
							throw newProcessException(null, ex.getMessage(), ex);
						}
					}
				}

			} finally {
				// Reset to original class path
				System.setProperty(SYSTEM_PROPERTY_CLASS_PATH,
						originalClassPath);

				// Notify completion listener (if one provided)
				ProcessCompletionListener completionListener = configuration
						.getProcessCompletionListener();
				if (completionListener != null) {
					completionListener.processCompleted(processManager);
				}
			}

		} catch (IOException ex) {
			// Propagate failure
			throw newProcessException(null, ex.getMessage(), ex);
		}
	}

	/**
	 * Starts the {@link Process} for the {@link ManagedProcess} returning the
	 * managing {@link ProcessManager}.
	 * 
	 * @param managedProcess
	 *            {@link ManagedProcess}.
	 * @param configuration
	 *            Optional {@link ProcessConfiguration}. May be
	 *            <code>null</code> to use defaults.
	 * @return {@link ProcessManager}.
	 * @throws ProcessException
	 *             If fails to start the {@link Process}.
	 */
	public static ProcessManager startProcess(ManagedProcess managedProcess,
			ProcessConfiguration configuration) throws ProcessException {

		// Ensure have configuration to prevent NPE
		if (configuration == null) {
			configuration = new ProcessConfiguration();
		}

		// Obtain the java bin directory location
		String javaHome = System.getProperty("java.home");
		File javaBinDir = new File(javaHome, "bin");
		if (!javaBinDir.isDirectory()) {
			throw newProcessException(
					null,
					"Can not find java bin directory at "
							+ javaBinDir.getAbsolutePath(), null);
		}

		// Obtain the java executable
		File javaExecutable = new File(javaBinDir, "java");
		if (!javaExecutable.isFile()) {
			// Not unix/linux, so try for windows
			javaExecutable = new File(javaBinDir, "javaw.exe");
			if (!javaExecutable.isFile()) {
				// Not windows either, so can not find
				throw newProcessException(
						null,
						"Can not find java executable in "
								+ javaBinDir.getAbsolutePath(), null);
			}
		}

		// Obtain the class path
		String classPath = getProcessClasspath(configuration);

		// Obtain the JVM options
		String[] jvmOptions = configuration.getJvmOptions();

		// Create the command to invoke process
		List<String> command = new ArrayList<String>(4 + jvmOptions.length);
		command.add(javaExecutable.getAbsolutePath());
		command.add("-cp");
		command.add(classPath);
		command.addAll(Arrays.asList(jvmOptions));
		command.add(ProcessShell.class.getName());

		// Obtain the process name
		String processName = configuration.getProcessName();
		if (isBlank(processName)) {
			processName = DEFAULT_PROCESS_NAME;
		}

		// Obtain the MBean Server
		MBeanServer mbeanServer = configuration.getMbeanServer();
		if (mbeanServer == null) {
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
		}

		// Obtain the process output stream factory
		ProcessOutputStreamFactory outputStreamFactory = configuration
				.getProcessOutputStreamFactory();
		if (outputStreamFactory == null) {
			outputStreamFactory = new StdProcessOutputStreamFactory();
		}

		// Obtain the process completion listener
		ProcessCompletionListener completionListener = configuration
				.getProcessCompletionListener();

		// Obtain the unique MBean name space
		String mbeanNamespace = processName;
		synchronized (activeMBeanNamespaces) {
			int suffix = 1;
			while (activeMBeanNamespaces.contains(mbeanNamespace)) {
				suffix++;
				mbeanNamespace = processName + suffix;
			}

			// Reserve name space for the process
			activeMBeanNamespaces.add(mbeanNamespace);
		}

		// Start the process
		ProcessManager processManager;
		Process process = null;
		ObjectOutputStream toProcessPipe;
		ProcessNotificationHandler notificationHandler;
		try {

			// Create the process streams
			OutputStream outStream = outputStreamFactory
					.createStandardProcessOutputStream(mbeanNamespace,
							command.toArray(new String[command.size()]));
			OutputStream errStream = outputStreamFactory
					.createErrorProcessOutputStream(mbeanNamespace);

			// Start the process (first as Process required by Manager)
			ProcessBuilder builder = new ProcessBuilder(command);
			process = builder.start();

			// Gobble the stdout and stderr of the process
			new StreamGobbler(mbeanNamespace, process.getInputStream(),
					outStream).start();
			new StreamGobbler(mbeanNamespace, process.getErrorStream(),
					errStream).start();
			Thread.yield(); // give chance to start

			// Create the process manager for the process
			processManager = new ProcessManager(processName, mbeanNamespace,
					process, completionListener, mbeanServer);

			// Obtain pipe to process
			toProcessPipe = new ObjectOutputStream(process.getOutputStream());

			// Create process response handler
			notificationHandler = new ProcessNotificationHandler(
					processManager, toProcessPipe);

		} catch (Exception ex) {
			// Release process name space
			releaseMBeanNamespace(mbeanNamespace);

			// Propagate the failure
			throw newProcessException(process, ex.getMessage(), ex);
		}

		// Start the handling of the process.
		// (handler will release MBean name space)
		try {

			// Start handling process responses
			notificationHandler.start();
			Thread.yield(); // give chance to start

			// Obtain the from Process port
			int fromProcessPort = notificationHandler.getFromProcessPort();

			// Send managed process and response port to process
			toProcessPipe.writeObject(mbeanNamespace);
			toProcessPipe.writeObject(managedProcess);
			toProcessPipe.writeInt(fromProcessPort);
			toProcessPipe.flush();

			// Notify starting listener (if one provided)
			ProcessStartListener startListener = configuration
					.getProcessStartListener();
			if (startListener != null) {
				startListener.processStarted(processManager);
			}

		} catch (Exception ex) {
			// Propagate failure
			throw newProcessException(process, ex.getMessage(), ex);
		}

		try {
			synchronized (processManager) {
				// Wait until process is initialised (or complete)
				long endTime = System.currentTimeMillis() + 20000;
				while ((!processManager.isInitialised)
						&& (!processManager.isComplete)) {
					processManager.wait(100);

					// Time out waiting for process to start
					if (System.currentTimeMillis() > endTime) {
						throw newProcessException(process, "Took too long for "
								+ ProcessShell.class.getSimpleName() + " for "
								+ managedProcess + " ["
								+ managedProcess.getClass().getName() + "]",
								null);
					}
				}

				// Determine if failure in running ProcessShell
				if (!processManager.isInitialised) {
					// Failed to start the ProcessShell
					throw newProcessException(process, "Failed to start "
							+ ProcessShell.class.getSimpleName() + " for "
							+ managedProcess + " ["
							+ managedProcess.getClass().getName() + "]",
							processManager.processShellFailure);
				}
			}
		} catch (InterruptedException ex) {
			// Interrupted in beginning the process
			throw newProcessException(process, "Process start interruption", ex);
		}

		// Return the manager for the process
		return processManager;
	}

	/**
	 * Obtains the local {@link ObjectName} for the remote MBean in the
	 * {@link Process} being managed within the <code>MBean name space</code>.
	 * 
	 * @param processNamespace
	 *            Name space of the {@link Process}.
	 * @param objectName
	 *            {@link ObjectName} of the remote MBean.
	 * @return Local {@link ObjectName} for the remote MBean in the
	 *         {@link Process} being managed.
	 * @throws MalformedObjectNameException
	 *             If resulting local name is malformed.
	 */
	public static ObjectName getLocalObjectName(String processNamespace,
			ObjectName remoteObjectName) throws MalformedObjectNameException {
		return new ObjectName(processNamespace + "."
				+ remoteObjectName.getDomain(),
				remoteObjectName.getKeyPropertyList());
	}

	/**
	 * Obtains the class path for the {@link ManagedProcess}.
	 * 
	 * @param configuration
	 *            {@link ProcessConfiguration}.
	 * @return Class path for the {@link ManagedProcess}.
	 */
	private static String getProcessClasspath(ProcessConfiguration configuration) {

		// Obtain the current class path
		String classPath = System.getProperty(SYSTEM_PROPERTY_CLASS_PATH);

		// Add additional class path entries
		String additionalClassPath = configuration.getAdditionalClassPath();
		if (!isBlank(additionalClassPath)) {
			classPath = classPath + File.pathSeparator + additionalClassPath;
		}

		// Return the class path
		return classPath;
	}

	/**
	 * Releases the MBean name space from active list.
	 * 
	 * @param mbeanNamespace
	 *            MBean name space to remove from active list.
	 */
	private static void releaseMBeanNamespace(String mbeanNamespace) {

		// Remove MBean name space from being active
		synchronized (activeMBeanNamespaces) {
			activeMBeanNamespaces.remove(mbeanNamespace);
		}
	}

	/**
	 * <p>
	 * Creates the {@link ProcessException}.
	 * <p>
	 * In doing so, it ensures the {@link Process} is destroyed.
	 * 
	 * @param process
	 *            {@link Process} to be destroyed.
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause. May be <code>null</code>.
	 * @return {@link ProcessException}.
	 */
	private static ProcessException newProcessException(Process process,
			String message, Throwable cause) {

		// Ensure kill the process
		try {
			if (process != null) {
				process.destroy();
			}
		} catch (Throwable destroyEx) {
			// Ignore as best attempts, but log details of this
			LOGGER.log(Level.WARNING, "Failed to destroy process for issue "
					+ message, destroyEx);
		}

		// Return the process exception
		if (cause != null) {
			return ProcessException.propagate(message, cause);
		} else {
			return new ProcessException(message);
		}
	}

	/**
	 * Name of the {@link Process}.
	 */
	private final String processName;

	/**
	 * MBean name space for the {@link Process}.
	 */
	private final String mbeanNamespace;

	/**
	 * {@link Process} being managed.
	 */
	private final Process process;

	/**
	 * {@link ProcessCompletionListener}.
	 */
	private final ProcessCompletionListener completionListener;

	/**
	 * {@link MBeanServer}.
	 */
	private final MBeanServer mbeanServer;

	/**
	 * Listing of the {@link ObjectName} instances for the registered MBeans.
	 */
	private List<ObjectName> registeredMBeanNames = new LinkedList<ObjectName>();

	/**
	 * Host name of the {@link Process} being managed. This is provided once the
	 * {@link Process} has been started.
	 */
	private String processHostName = null;

	/**
	 * Port that the {@link Registry} containing the {@link MBeanServer} for the
	 * {@link Process} being managed resides on. This is provided once the
	 * {@link Process} has been started.
	 */
	private int processPort = -1;

	/**
	 * Flag indicating if the {@link Process} has been initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Failure of {@link ProcessShell} running the {@link ManagedProcess}.
	 */
	private Throwable processShellFailure = null;

	/**
	 * Flag indicating if the {@link Process} is complete.
	 */
	private boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param processName
	 *            Name identifying the {@link Process}.
	 * @param mbeanNamespace
	 *            MBean name space for the {@link Process}.
	 * @param process
	 *            {@link Process} being managed.
	 * @param completionListener
	 *            {@link ProcessCompletionListener}.
	 * @param mbeanServer
	 *            {@link MBeanServer}.
	 */
	private ProcessManager(String processName, String mbeanNamespace,
			Process process, ProcessCompletionListener completionListener,
			MBeanServer mbeanServer) {
		this.processName = processName;
		this.mbeanNamespace = mbeanNamespace;
		this.process = process;
		this.completionListener = completionListener;
		this.mbeanServer = mbeanServer;
	}

	/**
	 * Obtains the local {@link ObjectName} for the remote MBean in the
	 * {@link Process} being managed by this {@link ProcessManager}.
	 * 
	 * @param objectName
	 *            {@link ObjectName} of the remote MBean.
	 * @return Local {@link ObjectName} for the remote MBean in the
	 *         {@link Process} being managed.
	 * @throws MalformedObjectNameException
	 *             If resulting local name is malformed.
	 */
	public ObjectName getLocalObjectName(ObjectName objectName)
			throws MalformedObjectNameException {
		return getLocalObjectName(this.mbeanNamespace, objectName);
	}

	/**
	 * Specifies the host and port of the {@link Registry} containing the
	 * {@link MBeanServer} for the {@link Process} being managed.
	 * 
	 * @param hostName
	 *            Host name.
	 * @param port
	 *            Port.
	 */
	private synchronized void setProcessHostAndPort(String hostName, int port) {
		this.processHostName = hostName;
		this.processPort = port;
	}

	/**
	 * Registers the remote MBean locally.
	 * 
	 * @param remoteMBeanName
	 *            {@link ObjectName} of the MBean in the remote
	 *            {@link MBeanServer} in the {@link Process}.
	 * @param connection
	 *            {@link MBeanServerConnection} to the
	 *            {@link JMXConnectorServer} for the {@link Process}.
	 * @throws Exception
	 *             If fails to register the MBean.
	 */
	private void registerRemoteMBeanLocally(ObjectName remoteMBeanName,
			MBeanServerConnection connection) throws Exception {
		// Register the remote MBean locally
		RemoteMBeanProxy mbeanProxy = new RemoteMBeanProxy(remoteMBeanName,
				connection);
		this.registerMBean(mbeanProxy, remoteMBeanName);
	}

	/**
	 * Registers the MBean.
	 * 
	 * @param mbean
	 *            Mbean.
	 * @param name
	 *            {@link ObjectName} for the MBean.
	 * @throws Exception
	 *             If fails to register the MBean.
	 */
	private void registerMBean(Object mbean, ObjectName name) throws Exception {

		// Obtain details to register the MBean
		ObjectName localMBeanName;
		MBeanServer server;
		synchronized (this) {

			// Do not register if already complete
			if (this.isComplete) {
				return;
			}

			// Obtain the MBean name
			localMBeanName = this.getLocalObjectName(name);

			// Keep track of the registered MBeans
			this.registeredMBeanNames.add(localMBeanName);

			// Obtain access to mbean server
			server = this.mbeanServer;
		}

		// Register the MBean (outside lock as other thread may need access)
		server.registerMBean(mbean, localMBeanName);
	}

	/**
	 * Flags a failure in running the {@link ProcessShell}.
	 * 
	 * @param failure
	 *            Failure in running the {@link ProcessShell}.
	 */
	private synchronized void setProcessShellFailure(Throwable failure) {
		this.processShellFailure = failure;
	}

	/**
	 * Flags the {@link Process} is initialised.
	 */
	private synchronized void flagInitialised() {

		// Flag initialised
		this.isInitialised = true;

		// Notify immediately that initialised
		this.notify();
	}

	/**
	 * Flags the {@link Process} is complete.
	 * 
	 * @throws IOException
	 *             If
	 */
	private void flagComplete() {

		// Obtain details to flag complete
		MBeanServer server;
		List<ObjectName> objectNames;
		ProcessCompletionListener listener;
		synchronized (this) {

			// Do not run completion twice
			if (this.isComplete) {
				return;
			}

			// Flag completing
			this.isComplete = true;

			// Obtain thread safe details
			server = this.mbeanServer;
			objectNames = new ArrayList<ObjectName>(this.registeredMBeanNames);
			listener = this.completionListener;
		}

		// Unregister the MBeans
		for (ObjectName mbeanName : objectNames) {
			try {
				server.unregisterMBean(mbeanName);
			} catch (Throwable ex) {
				LOGGER.log(Level.WARNING, "Failed to unregister MBean "
						+ mbeanName, ex);
			}
		}

		// Release process name space
		releaseMBeanNamespace(this.mbeanNamespace);

		// Notify process complete
		if (listener != null) {
			try {
				listener.processCompleted(this);
			} catch (Throwable ex) {
				LOGGER.log(Level.WARNING,
						ProcessCompletionListener.class.getSimpleName() + " "
								+ listener.getClass().getName() + " failed", ex);
			}
		}
	}

	/*
	 * ================== ProcessManagerMBean ======================
	 */

	@Override
	public String getProcessName() {
		return this.processName;
	}

	@Override
	public String getProcessNamespace() {
		return this.mbeanNamespace;
	}

	@Override
	public synchronized String getProcessHostName() {
		return this.processHostName;
	}

	@Override
	public synchronized int getProcessPort() {
		return this.processPort;
	}

	@Override
	public void triggerStopProcess() throws ProcessException {

		// Obtain details to trigger stopping process
		MBeanServer server;
		synchronized (this) {

			// Do nothing if running locally (i.e. no process)
			if (this.process == null) {
				return;
			}

			// Ignore if already complete
			if (this.isComplete) {
				return;
			}

			// Obtain the Mbean Server
			server = this.mbeanServer;
		}

		try {
			// Obtain the process shell MBean name
			ObjectName name = this
					.getLocalObjectName(ProcessShell.PROCESS_SHELL_OBJECT_NAME);

			// Trigger stopping the process
			server.invoke(name, ProcessShell.TRIGGER_STOP_PROCESS_METHOD, null,
					null);

		} catch (Exception ex) {
			// Propagate failure
			throw newProcessException(this.process, ex.getMessage(), ex);
		}
	}

	@Override
	public void destroyProcess() {

		// Do nothing if running locally (i.e. no process)
		if (this.process == null) {
			return;
		}

		// Destroy the process
		this.process.destroy();
	}

	@Override
	public synchronized boolean isProcessComplete() {
		// Return whether process complete
		return this.isComplete;
	}

	/**
	 * Handler for the notifications from the {@link Process}.
	 */
	private static class ProcessNotificationHandler extends Thread {

		/**
		 * {@link ProcessManager}.
		 */
		private final ProcessManager processManager;

		/**
		 * Pipe to the {@link Process}.
		 */
		private final ObjectOutputStream toProcessPipe;

		/**
		 * {@link ServerSocket} to receive {@link ProcessResponse} instances.
		 */
		private final ServerSocket fromProcessServerSocket;

		/**
		 * {@link MBeanServerConnection} to the {@link ManagedProcess}
		 * {@link MBeanServer}. This is lazy created as needed.
		 */
		private MBeanServerConnection connection = null;

		/**
		 * Initiate.
		 * 
		 * @param processManager
		 *            {@link ProcessManager}.
		 * @param toProcessPipe
		 *            Pipe to the {@link Process}.
		 */
		public ProcessNotificationHandler(ProcessManager processManager,
				ObjectOutputStream toProcessPipe) throws IOException {
			this.processManager = processManager;
			this.toProcessPipe = toProcessPipe;

			// Create the from process communication pipe.
			// Must synchronise as used by another Thread.
			synchronized (this) {
				// Bind socket on any available port for listening to shell
				this.fromProcessServerSocket = new ServerSocket();
				this.fromProcessServerSocket.bind(null);
			}

			// Flag as daemon (should not stop process finishing)
			this.setDaemon(true);
		}

		/**
		 * Obtains the {@link ServerSocket} port to receive
		 * {@link ProcessResponse} instances.
		 * 
		 * @return {@link ServerSocket} port to receive {@link ProcessResponse}
		 *         instances.
		 */
		public synchronized int getFromProcessPort() {
			return this.fromProcessServerSocket.getLocalPort();
		}

		/*
		 * ================= Thread ======================
		 */

		@Override
		public void run() {

			// Ensure flag process completed
			try {

				// Establish the Process
				ObjectInputStream fromProcessPipe;
				try {
					// Accept only the first connection (from process).
					// (allow only 10 seconds to connect)
					ServerSocket serverSocket;
					synchronized (this) {
						serverSocket = this.fromProcessServerSocket;
					}
					serverSocket.setSoTimeout(10000);
					Socket socket = serverSocket.accept();

					// Register the Process Manager MBean
					this.processManager.registerMBean(this.processManager,
							ProcessManager.PROCESS_MANAGER_OBJECT_NAME);

					// Obtain pipe from process
					fromProcessPipe = new ObjectInputStream(
							socket.getInputStream());

					// Obtain connection details
					Object[] connectionDetails = (Object[]) fromProcessPipe
							.readObject();
					JMXServiceURL processServiceUrl = (JMXServiceURL) connectionDetails[0];
					String userName = (String) connectionDetails[1];
					String password = (String) connectionDetails[2];

					// Specify the host and port
					this.processManager.setProcessHostAndPort(
							processServiceUrl.getHost(),
							processServiceUrl.getPort());

					// Connect to the process
					Map<String, Object> env = new HashMap<String, Object>();
					env.put(JMXConnector.CREDENTIALS, new String[] { userName,
							password });
					JMXConnector connector = JMXConnectorFactory.connect(
							processServiceUrl, env);
					this.connection = connector.getMBeanServerConnection();

					// Provide notification that connected to process
					this.toProcessPipe.writeBoolean(true);
					this.toProcessPipe.flush();

				} catch (Throwable ex) {
					// Indicate failure, as process not established
					this.processManager.setProcessShellFailure(ex);
					return;
				}

				// Process established, listen for MBean registrations
				try {
					// Loop until pipe closes (or indicated to close)
					for (;;) {

						// Read in next Object
						Object object = fromProcessPipe.readObject();

						// Determine if exception
						if (object instanceof Throwable) {
							// Flag process shell failure
							Throwable failure = (Throwable) object;
							this.processManager.setProcessShellFailure(failure);

							// Failure indicates process complete.
							// (finally block handles completion)
							return;
						}

						// Determine if notification
						if (!(object instanceof MBeanRegistrationNotification)) {
							throw new IllegalArgumentException(
									"Unknown response: "
											+ object
											+ " ["
											+ (object == null ? "null" : object
													.getClass().getName()));
						}
						MBeanRegistrationNotification registration = (MBeanRegistrationNotification) object;

						// Register the remote MBean locally
						ObjectName remoteMBeanName = registration
								.getMBeanName();
						this.processManager.registerRemoteMBeanLocally(
								remoteMBeanName, this.connection);

						// Determine if process shell MBean.
						// Must be done after registering to ensure available.
						if (ProcessShell.PROCESS_SHELL_OBJECT_NAME
								.equals(remoteMBeanName)) {
							// Have process shell MBean so now initialised
							this.processManager.flagInitialised();

							// Notify process that registered initialised
							this.toProcessPipe.writeBoolean(true);
							this.toProcessPipe.flush();
						}
					}
				} catch (Throwable ex) {

					// Determine if potential wrapped EOF cause
					Throwable previousCause = null; // stops infinite loop
					Throwable currentCause = ex;
					while ((currentCause != null)
							&& (currentCause != previousCause)) {

						// Determine if EOF cause (assumed process completed)
						if (currentCause instanceof EOFException) {
							return; // EOF cause as process assumed complete
						}

						// Setup for next iteration
						previousCause = currentCause;
						currentCause = currentCause.getCause();
					}

					// Indicate failure, as not process complete
					this.processManager.setProcessShellFailure(ex);
				}

			} finally {
				// Flag that process is now complete
				this.processManager.flagComplete();

				// Close the socket as no longer listening
				try {
					synchronized (this) {
						this.fromProcessServerSocket.close();
					}
				} catch (Throwable ex) {
					// Indicate error
					LOGGER.log(Level.WARNING,
							ProcessNotificationHandler.class.getSimpleName()
									+ " error", ex);
				}
			}
		}
	}

	/**
	 * Gobbles the Stream.
	 */
	private static class StreamGobbler extends Thread {

		/**
		 * Name space of the {@link ManagedProcess} that gobbling content.
		 */
		private final String processNamespace;

		/**
		 * {@link InputStream} to gobble.
		 */
		private final InputStream input;

		/**
		 * {@link OutputStream} to send gobbled content.
		 */
		private final OutputStream output;

		/**
		 * Initiate.
		 * 
		 * @param processNamespace
		 *            Name space of the {@link ManagedProcess} that gobbling
		 *            content.
		 * @param input
		 *            {@link InputStream} to gobble.
		 * @param output
		 *            {@link OutputStream} to send gobbled content.
		 */
		public StreamGobbler(String processNamespace, InputStream input,
				OutputStream output) {

			// Must synchronize as used by another Thread
			synchronized (this) {
				this.processNamespace = processNamespace;
				this.input = input;
				this.output = output;
			}

			// Flag as deamon (should not stop process finishing)
			this.setDaemon(true);
		}

		/*
		 * ================= Thread ======================
		 */

		@Override
		public synchronized void run() {
			try {
				// Consume from stream until EOF
				for (int value = this.input.read(); value != -1; value = this.input
						.read()) {
					this.output.write(value);
				}

			} catch (Throwable ex) {
				// Provide stack trace and exit
				LOGGER.log(Level.WARNING, this.processNamespace + " "
						+ StreamGobbler.class.getSimpleName() + " error", ex);

			} finally {
				// Do not close System out/err
				if ((System.out == this.output) || (System.err == this.output)) {
					return;
				}

				// Ensure close output stream
				try {
					this.output.close();
				} catch (Throwable ex) {
					// Provide stack trace and exit
					LOGGER.log(Level.WARNING, this.processNamespace + " "
							+ StreamGobbler.class.getSimpleName()
							+ " failed closing stream", ex);
				}
			}
		}
	}

}