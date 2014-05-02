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
package net.officefloor.building.manager;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.UnmarshalException;
import java.rmi.server.RMIClientSocketFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.building.process.ProcessShellMBean;
import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagerTest extends OfficeFrameTestCase {

	/**
	 * Next port to run next current test.
	 */
	private static int nextPort = 13778;

	/**
	 * Port to run the current test on.
	 */
	private int port;

	/**
	 * Trust store file.
	 */
	private File trustStore;

	/**
	 * Password to trust store.
	 */
	private String trustStorePassword;

	/**
	 * Username.
	 */
	private String username;

	/**
	 * Password.
	 */
	private String password;

	/**
	 * {@link MBeanServer}.
	 */
	private MBeanServer mbeanServer;

	/**
	 * Remote repository URLs.
	 */
	private String[] remoteRepositoryUrls;

	@Override
	protected void setUp() throws Exception {

		// Obtain the next port for this test
		this.port = nextPort;

		// Ensure stop office building
		OfficeBuildingTestUtil.ensureOfficeBuildingStopped(this.port);

		// Obtain the trust store for SSL to work
		this.trustStore = OfficeBuildingTestUtil.getKeyStore();
		this.trustStorePassword = OfficeBuildingTestUtil.getKeyStorePassword();

		// Obtain the credentials
		this.username = OfficeBuildingTestUtil.getLoginUsername();
		this.password = OfficeBuildingTestUtil.getLoginPassword();

		// Obtain the MBean Server
		this.mbeanServer = ManagementFactory.getPlatformMBeanServer();

		// Setup the remote repository URLs
		this.remoteRepositoryUrls = new String[] { "file://"
				+ OfficeBuildingTestUtil.getUserLocalRepository()
						.getAbsolutePath() };
	}
	
	

	@Override
	protected void tearDown() throws Exception {
		
		// Ensure stop office building
		OfficeBuildingTestUtil.ensureOfficeBuildingStopped(this.port);
	}



	/**
	 * Starts the Office Building for testing.
	 * 
	 * @return {@link OfficeBuildingManager}.
	 */
	private OfficeBuildingManagerMBean startOfficeBuilding() throws Exception {

		// Obtain key store for office building
		File keyStore = OfficeBuildingTestUtil.getKeyStore();
		String keyStorePassword = OfficeBuildingTestUtil.getKeyStorePassword();

		// Start the office building
		return OfficeBuildingManager.startOfficeBuilding(null, this.port,
				keyStore, keyStorePassword, this.username, this.password, null,
				false, new Properties(), this.mbeanServer, new String[0],
				false, this.remoteRepositoryUrls);
	}

	/**
	 * Ensure able to start the Office Building.
	 */
	public void testRunningOfficeBuilding() throws Exception {

		// Start the Office Building (recording times before/after)
		long beforeTime = System.currentTimeMillis();
		OfficeBuildingManagerMBean manager = this.startOfficeBuilding();
		long afterTime = System.currentTimeMillis();

		// Ensure OfficeBuilding is available
		assertTrue("OfficeBuilding should be available",
				OfficeBuildingManager.isOfficeBuildingAvailable(null,
						this.port, this.trustStore, this.trustStorePassword,
						this.username, this.password));

		// Ensure correct JMX Service URL
		String actualServiceUrl = manager.getOfficeBuildingJmxServiceUrl();
		String hostName = InetAddress.getLocalHost().getHostName();
		String expectedServiceUrl = "service:jmx:rmi://" + hostName + ":"
				+ this.port + "/jndi/rmi://" + hostName + ":" + this.port
				+ "/OfficeBuilding";
		assertEquals("Incorrect service url", expectedServiceUrl,
				actualServiceUrl);

		// Obtain the Office Building Manager MBean
		OfficeBuildingManagerMBean managerMBean = OfficeBuildingManager
				.getOfficeBuildingManager(hostName, this.port, this.trustStore,
						this.trustStorePassword, this.username, this.password);

		// Ensure start time is accurate
		long startTime = managerMBean.getStartTime().getTime();
		assertTrue("Start time recorded incorrectly",
				((beforeTime <= startTime) && (startTime <= afterTime)));

		// Ensure MBean reports correct service URL
		String mbeanReportedServiceUrl = managerMBean
				.getOfficeBuildingJmxServiceUrl();
		assertEquals("Incorrect MBean service URL", expectedServiceUrl,
				mbeanReportedServiceUrl);

		// Ensure correct host and port
		String mbeanReportedHostName = managerMBean.getOfficeBuildingHostName();
		assertEquals("Incorrect MBean host name", hostName,
				mbeanReportedHostName);
		int mbeanReportedPort = managerMBean.getOfficeBuildingPort();
		assertEquals("Incorrect MBean port", this.port, mbeanReportedPort);

		// Ensure no processes running
		String processNamespaces = managerMBean.listProcessNamespaces();
		assertEquals("Should be no processes running", "", processNamespaces);

		// OfficeBuilding should still be available
		assertTrue("OfficeBuilding should still be available",
				OfficeBuildingManager.isOfficeBuildingAvailable(null,
						this.port, this.trustStore, this.trustStorePassword,
						this.username, this.password));

		// Stop the Office Building
		String stopDetails = managerMBean.stopOfficeBuilding(10000);
		assertEquals("Incorrect stop details", "OfficeBuilding stopped",
				stopDetails);

		// OfficeBuilding now not be available
		assertFalse(
				"OfficeBuilding should be stopped and therefore unavailable",
				OfficeBuildingManager.isOfficeBuildingAvailable(null,
						this.port, this.trustStore, this.trustStorePassword,
						this.username, this.password));
	}

	/**
	 * Ensure able to open the configured {@link OfficeFloor}.
	 */
	public void testEnsureOfficeFloorOpens() throws Exception {
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.addSourceAliases();
		OfficeFloor officeFloor = compiler.compile(this
				.getOfficeFloorLocation());
		officeFloor.openOfficeFloor();
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} with a Jar.
	 */
	public void testOfficeFloorJarManagement() throws Exception {
		this.doOfficeFloorManagementTest(new OfficeFloorOpener() {
			@Override
			public String openOfficeFloor(String processName,
					String officeFloorLocation,
					OfficeBuildingManagerMBean buildingManager)
					throws Exception {
				OpenOfficeFloorConfiguration config = new OpenOfficeFloorConfiguration(
						officeFloorLocation);
				config.setProcessName(processName);
				config.addUploadArtifact(new UploadArtifact(
						OfficeBuildingTestUtil.getOfficeCompilerArtifactJar()));
				return buildingManager.openOfficeFloor(config);
			}
		});
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} for JMX.
	 */
	public void testOfficeFloorArtifactManagement() throws Exception {
		this.doOfficeFloorManagementTest(new OfficeFloorOpener() {
			@Override
			public String openOfficeFloor(String processName,
					String officeFloorLocation,
					OfficeBuildingManagerMBean buildingManager)
					throws Exception {
				OpenOfficeFloorConfiguration config = new OpenOfficeFloorConfiguration(
						officeFloorLocation);
				config.setProcessName(processName);
				config.addArtifactReference(new ArtifactReference(
						"net.officefloor.core", "officecompiler",
						OfficeBuildingTestUtil
								.getOfficeCompilerArtifactVersion(), null, null));
				config.addRemoteRepositoryUrl(OfficeBuildingTestUtil
						.getUserLocalRepository().toURI().toURL().toString());
				return buildingManager.openOfficeFloor(config);
			}
		});
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} with JMX string command.
	 */
	public void testOfficeFloorJmxManagement() throws Exception {
		this.doOfficeFloorManagementTest(new OfficeFloorOpener() {
			@Override
			public String openOfficeFloor(String processName,
					String officeFloorLocation,
					OfficeBuildingManagerMBean buildingManager)
					throws Exception {
				return buildingManager.openOfficeFloor("--officefloor "
						+ officeFloorLocation);
			}
		});
	}

	/**
	 * Opens the {@link OfficeFloor}.
	 */
	private static interface OfficeFloorOpener {

		/**
		 * Opens the {@link OfficeFloor} via the
		 * {@link OfficeBuildingManagerMBean}.
		 * 
		 * @param processName
		 *            Process name.
		 * @param officeFloorLocation
		 *            {@link OfficeFloor} location.
		 * @param buildingManager
		 *            {@link OfficeBuildingManagerMBean}.
		 * @return Process namespace.
		 * @throws Exception
		 *             If fails to open.
		 */
		String openOfficeFloor(String processName, String officeFloorLocation,
				OfficeBuildingManagerMBean buildingManager) throws Exception;
	}

	/**
	 * Ensure able to open the {@link OfficeFloor}.
	 * 
	 * @param opener
	 *            {@link OfficeFloorOpener}.
	 */
	private void doOfficeFloorManagementTest(OfficeFloorOpener opener)
			throws Exception {

		// Start the OfficeBuilding
		this.startOfficeBuilding();

		// Obtain the manager MBean
		OfficeBuildingManagerMBean buildingManager = OfficeBuildingManager
				.getOfficeBuildingManager(null, this.port, this.trustStore,
						this.trustStorePassword, this.username, this.password);

		// Open the OfficeFloor
		String officeFloorLocation = this.getOfficeFloorLocation();
		String processNamespace = opener.openOfficeFloor(this.getName(),
				officeFloorLocation, buildingManager);

		// Ensure process running
		String processNamespaces = buildingManager.listProcessNamespaces();
		assertEquals("Should be process running", processNamespace,
				processNamespaces);

		// Ensure OfficeFloor opened (obtaining local floor manager)
		OfficeFloorManagerMBean localFloorManager = OfficeBuildingManager
				.getOfficeFloorManager(null, this.port, processNamespace,
						this.trustStore, this.trustStorePassword,
						this.username, this.password);
		assertEquals("Incorrect OfficeFloor location", officeFloorLocation,
				localFloorManager.getOfficeFloorLocation());

		// Obtain the local Process Manager MBean
		ProcessManagerMBean processManager = OfficeBuildingManager
				.getProcessManager(null, this.port, processNamespace,
						this.trustStore, this.trustStorePassword,
						this.username, this.password);

		// Obtain the local Process Shell MBean
		ProcessShellMBean localProcessShell = OfficeBuildingManager
				.getProcessShell(null, this.port, processNamespace,
						this.trustStore, this.trustStorePassword,
						this.username, this.password);

		// Validate the process host and port
		String remoteHostName = processManager.getProcessHostName();
		int remotePort = processManager.getProcessPort();
		String serviceUrlValue = localProcessShell.getJmxConnectorServiceUrl();
		JMXServiceURL remoteServiceUrl = new JMXServiceURL(serviceUrlValue);
		assertEquals("Incorrect process host", remoteServiceUrl.getHost(),
				remoteHostName);
		assertEquals("Incorrect process port", remoteServiceUrl.getPort(),
				remotePort);

		// Ensure OfficeFloor running
		this.validateRemoteProcessRunning(remoteServiceUrl);

		// Ensure the tasks are available
		StringBuilder taskListing = new StringBuilder();
		taskListing.append("OFFICE\n");
		taskListing.append("\tSECTION.WORK\n");
		taskListing.append("\t\twriteMessage (" + String.class.getSimpleName()
				+ ")");
		assertEquals("Incorrect task listing", taskListing.toString(),
				localFloorManager.listTasks());

		// Invoke the work
		File file = OfficeBuildingTestUtil.createTempFile(this);
		localFloorManager.invokeTask("OFFICE", "SECTION.WORK", null,
				file.getAbsolutePath());

		// Ensure work invoked (content in file)
		OfficeBuildingTestUtil.validateFileContent("Work should be invoked",
				MockWork.MESSAGE, file);

		// Obtain expected details of stopping the OfficeBuilding
		String expectedStopDetails = "Stopping processes:\n\t"
				+ processManager.getProcessName() + " ["
				+ processManager.getProcessNamespace()
				+ "]\n\nOfficeBuilding stopped";

		// Stop the OfficeBuilding
		String stopDetails = buildingManager.stopOfficeBuilding(10000);
		assertEquals("Ensure correct stop details", expectedStopDetails,
				stopDetails);

		// Ensure the OfficeFloor process is also stopped
		this.validateRemoteProcessStopped(remoteServiceUrl);
	}

	/**
	 * Ensure can close the {@link OfficeFloor}.
	 */
	public void testCloseOfficeFloor() throws Exception {

		// Start the OfficeBuilding
		this.startOfficeBuilding();

		// Obtain the manager MBean
		OfficeBuildingManagerMBean buildingManager = OfficeBuildingManager
				.getOfficeBuildingManager(null, this.port, this.trustStore,
						this.trustStorePassword, this.username, this.password);

		// Open the OfficeFloor
		String officeFloorLocation = this.getOfficeFloorLocation();
		String processNamespace = buildingManager
				.openOfficeFloor(new OpenOfficeFloorConfiguration(
						officeFloorLocation));

		// Ensure OfficeFloor opened (obtaining local floor manager)
		OfficeFloorManagerMBean localFloorManager = OfficeBuildingManager
				.getOfficeFloorManager(null, this.port, processNamespace,
						this.trustStore, this.trustStorePassword,
						this.username, this.password);
		assertEquals("Incorrect OfficeFloor location", officeFloorLocation,
				localFloorManager.getOfficeFloorLocation());

		// Obtain the local process shell
		JMXConnector localConnector = connectToJmxAgent(new JMXServiceURL(
				buildingManager.getOfficeBuildingJmxServiceUrl()), true);
		MBeanServerConnection localMBeanServer = localConnector
				.getMBeanServerConnection();
		ProcessShellMBean localProcessShell = JMX.newMBeanProxy(
				localMBeanServer, ProcessManager.getLocalObjectName(
						processNamespace,
						ProcessShell.getProcessShellObjectName()),
				ProcessShellMBean.class);

		// Obtain the remote process JMX service URL
		JMXServiceURL remoteServiceUrl = new JMXServiceURL(
				localProcessShell.getJmxConnectorServiceUrl());

		// Ensure the OfficeFloor process is running
		this.validateRemoteProcessRunning(remoteServiceUrl);

		// Close the OfficeFloor
		String closeText = buildingManager.closeOfficeFloor(processNamespace,
				3000);
		assertEquals("Should be closed", "Closed", closeText);

		// Ensure the OfficeFloor process is closed
		this.validateRemoteProcessStopped(remoteServiceUrl);

		// Stop the building manager
		buildingManager.stopOfficeBuilding(10000);
	}

	/**
	 * Ensure able to spawn the {@link OfficeBuilding}.
	 */
	public void testSpawnOfficeBuilding() throws Exception {

		// Spawn the OfficeBuilding
		ProcessManager process = OfficeBuildingManager.spawnOfficeBuilding(
				null, this.port, this.trustStore, this.trustStorePassword,
				this.username, this.password, null, false, null, null, false,
				new String[] { OfficeBuildingTestUtil.getUserLocalRepository()
						.getAbsolutePath() }, null);
		try {

			// Ensure the OfficeBuilding is available
			assertTrue("OfficeBuilding should be available",
					OfficeBuildingManager.isOfficeBuildingAvailable(null,
							this.port, this.trustStore,
							this.trustStorePassword, this.username,
							this.password));

			// Stop the spawned OfficeBuilding
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.getOfficeBuildingManager(null, this.port, this.trustStore,
							this.trustStorePassword, this.username,
							this.password);
			manager.stopOfficeBuilding(1000);

			// Ensure the OfficeBuilding stopped
			assertFalse("OfficeBuilding should be stopped",
					OfficeBuildingManager.isOfficeBuildingAvailable(null,
							this.port, this.trustStore,
							this.trustStorePassword, this.username,
							this.password));

		} finally {
			// Ensure process stopped
			process.destroyProcess();
		}
	}

	/**
	 * Obtains the {@link OfficeFloor} location.
	 * 
	 * @return {@link OfficeFloor} location.
	 */
	private String getOfficeFloorLocation() {
		return this.getClass().getPackage().getName().replace('.', '/')
				+ "/TestOfficeFloor.officefloor";
	}

	/**
	 * Validates {@link ProcessShellMBean} is running.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL} to determine if running.
	 */
	private void validateRemoteProcessRunning(JMXServiceURL serviceUrl)
			throws IOException {
		try {
			this.connectToJmxAgent(serviceUrl, false);
			fail("Security should prevent connection to running remote process");
		} catch (SecurityException ex) {
			assertEquals("Incorrect cause", "Bad credentials", ex.getMessage());
		}
	}

	/**
	 * Validate {@link ProcessShellMBean} is stopped.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL} to determine if stopped.
	 */
	private void validateRemoteProcessStopped(JMXServiceURL serviceUrl)
			throws InterruptedException {

		// Allow time for process to stop (10 seconds)
		long endTime = System.currentTimeMillis() + 10000;
		while (System.currentTimeMillis() < endTime) {

			try {
				this.connectToJmxAgent(serviceUrl, true);
				fail("Should not connect to stopped remote process");

			} catch (NoSuchObjectException ex) {
				// In process of shutting down (keep waiting)

			} catch (ConnectException ex) {
				assertEquals("Incorrect cause", "Connection refused", ex
						.getCause().getMessage());
				return; // successfully identified as closed

			} catch (UnmarshalException ex) {
				assertTrue("Incorrect cause "
						+ ex.getCause().getClass().getName(),
						(ex.getCause() instanceof EOFException));
				return; // successfully identified as closed

			} catch (IOException ex) {
				fail("Should only be ConnectionException but was "
						+ ex.getMessage() + " [" + ex.getClass().getName()
						+ "]");
			}

			// Allow some time for process to complete
			Thread.sleep(100);
		}

		// As here process failed to stop in time
		fail("Process took too long to stop");
	}

	/**
	 * Connects to the JMX agent.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL}.
	 * @param isSecure
	 *            Indicates if to provide security details to connect.
	 * @return {@link JMXConnector}.
	 */
	private JMXConnector connectToJmxAgent(JMXServiceURL serviceUrl,
			boolean isSecure) throws IOException {
		Map<String, Object> environment = new HashMap<String, Object>();
		if (isSecure) {
			byte[] keyStoreContent = OfficeBuildingRmiServerSocketFactory
					.getKeyStoreContent(this.trustStore);
			RMIClientSocketFactory socketFactory = new OfficeBuildingRmiClientSocketFactory(
					OfficeBuildingManager.getSslProtocol(),
					OfficeBuildingManager.getSslAlgorithm(), keyStoreContent,
					this.trustStorePassword);
			environment.put("com.sun.jndi.rmi.factory.socket", socketFactory);
			environment.put(JMXConnector.CREDENTIALS, new String[] {
					this.username, this.password });
		}
		return JMXConnectorFactory.connect(serviceUrl, environment);
	}

}