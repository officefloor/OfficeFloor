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

import java.net.InetAddress;

import junit.framework.TestCase;

/**
 * Tests the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagerTest extends TestCase {

	/**
	 * Port to run the current test.
	 */
	private static int PORT = 13078;

	/**
	 * Ensure able to start the Office Building.
	 */
	public void testStartOfficeBuilding() throws Exception {

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
		managerMBean.stopOfficeBuilding();
	}

}