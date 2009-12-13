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
package net.officefloor.building.manager;

import java.io.File;
import java.net.InetAddress;
import java.rmi.ConnectException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.building.process.ProcessShellMBean;
import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Tests the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagerTest extends TestCase {

	/**
	 * Port to run the current test.
	 */
	private static int PORT = 13778;

	/**
	 * Ensure able to start the Office Building.
	 */
	public void testRunningOfficeBuilding() throws Exception {

		// Start the Office Building (recording times before/after)
		long beforeTime = System.currentTimeMillis();
		OfficeBuildingManager manager = OfficeBuildingManager
				.startOfficeBuilding(PORT);
		long afterTime = System.currentTimeMillis();

		// Ensure correct JMX Service URL
		String actualServiceUrl = manager.getOfficeBuildingJmxServiceUrl();
		String hostName = InetAddress.getLocalHost().getHostName();
		String expectedServiceUrl = "service:jmx:rmi://" + hostName + ":"
				+ PORT + "/jndi/rmi://" + hostName + ":" + PORT
				+ "/OfficeBuilding";
		assertEquals("Incorrect service url", expectedServiceUrl,
				actualServiceUrl);

		// Obtain the Office Building Manager MBean
		OfficeBuildingManagerMBean managerMBean = OfficeBuildingManager
				.getOfficeBuildingManager(hostName, PORT);

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
		assertEquals("Incorrect MBean port", PORT, mbeanReportedPort);

		// Stop the Office Building
		String stopDetails = managerMBean.stopOfficeBuilding(10000);
		assertEquals("Incorrect stop details", "OfficeBuilding stopped\n",
				stopDetails);
	}

	/**
	 * Ensure able to open the configured {@link OfficeFloor}.
	 */
	public void testEnsureOfficeFloorOpens() throws Exception {
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.addSourceAliases();
		OfficeFloor officeFloor = compiler.compile(this
				.getOfficeFloorLocation());
		officeFloor.openOfficeFloor();
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Ensure able to open the {@link OfficeFloor}.
	 */
	public void testOfficeFloorManagement() throws Exception {

		// Start the OfficeBuilding
		OfficeBuildingManager.startOfficeBuilding(PORT);

		// Obtain the manager MBean
		OfficeBuildingManagerMBean buildingManager = OfficeBuildingManager
				.getOfficeBuildingManager(null, PORT);

		// Open the OfficeFloor
		String officeFloorLocation = this.getOfficeFloorLocation();
		String processNamespace = buildingManager.openOfficeFloor(this
				.getName(), "not-needed.jar", officeFloorLocation, null);

		// Ensure OfficeFloor opened (obtaining local floor manager)
		OfficeFloorManagerMBean localFloorManager = OfficeBuildingManager
				.getOfficeFloorManager(null, PORT, processNamespace);
		assertEquals("Incorrect OfficeFloor location", officeFloorLocation,
				localFloorManager.getOfficeFloorLocation());

		// Obtain the local Process Manager MBean
		ProcessManagerMBean processManager = OfficeBuildingManager
				.getProcessManager(null, PORT, processNamespace);

		// Obtain the local Process Shell MBean
		ProcessShellMBean localProcessShell = OfficeBuildingManager
				.getProcessShell(null, PORT, processNamespace);

		// Validate the process host and port
		String remoteHostName = processManager.getProcessHostName();
		int remotePort = processManager.getProcessPort();
		String serviceUrlValue = localProcessShell.getJmxConnectorServiceUrl();
		JMXServiceURL serviceUrl = new JMXServiceURL(serviceUrlValue);
		assertEquals("Incorrect process host", serviceUrl.getHost(),
				remoteHostName);
		assertEquals("Incorrect process port", serviceUrl.getPort(), remotePort);

		// Obtain the MBean Server connection direct to process
		JMXConnector connector = JMXConnectorFactory.connect(serviceUrl);
		MBeanServerConnection remoteMBeanServer = connector
				.getMBeanServerConnection();

		// Ensure OfficeFloor running locally
		assertTrue("OfficeFloor manager should be running locally",
				remoteMBeanServer.isRegistered(OfficeFloorManager
						.getOfficeFloorManagerObjectName()));

		// Invoke the work
		File file = OfficeBuildingTestUtil.createTempFile(this);
		localFloorManager.invokeWork("OFFICE", "SECTION.WORK", file
				.getAbsolutePath());

		// Ensure work invoked (content in file)
		OfficeBuildingTestUtil.validateFileContent("Work should be invoked",
				MockWork.MESSAGE, file);

		// Obtain the remote process shell
		ProcessShellMBean remoteProcessShell = JMX.newMBeanProxy(
				remoteMBeanServer, ProcessShell.getProcessShellObjectName(),
				ProcessShellMBean.class);

		// Obtain expected details of stopping the OfficeBuilding
		String expectedStopDetails = "Stopping processes:\n\t"
				+ processManager.getProcessName() + " ["
				+ processManager.getProcessNamespace()
				+ "]\n\nOfficeBuilding stopped\n";

		// Stop the OfficeBuilding
		String stopDetails = buildingManager.stopOfficeBuilding(10000);
		assertEquals("Ensure correct stop details", expectedStopDetails,
				stopDetails);

		// Ensure the OfficeFloor process is also stopped
		try {
			remoteProcessShell.triggerStopProcess();
			fail("Process should already be stopped");
		} catch (Exception ex) {
			// Ensure issue connecting
			assertTrue("Should have issue connecting as process stopped", (ex
					.getCause() instanceof ConnectException));
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

}