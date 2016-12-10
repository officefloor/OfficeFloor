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
package net.officefloor.building.process.officefloor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagerTest extends OfficeFrameTestCase {

	/**
	 * Ensures the {@link OfficeFloor} configuration is correct by running it.
	 */
	public void testEnsureOfficeFloorRuns() throws Exception {

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		compiler.addSourceAliases();
		compiler.addProperty("team.name", "TEAM");

		// Compile the test OfficeFloor
		OfficeFloor officeFloor = compiler.compile(this
				.getOfficeFloorLocation());

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Run work to write to temporary file
		File file = OfficeBuildingTestUtil.createTempFile(this);
		officeFloor.getOffice("OFFICE").getWorkManager("SECTION.WORK")
				.invokeWork(file.getAbsolutePath());

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written",
				MockWork.MESSAGE, file);
	}

	/**
	 * Ensure can start the {@link OfficeFloor} {@link Process} and invoke
	 * {@link Work}.
	 */
	public void testOfficeFloorManagedProcess() throws Throwable {

		// Create the OfficeFloor managed process
		File file = OfficeBuildingTestUtil.createTempFile(this);
		OfficeFloorManager managedProcess = new OfficeFloorManager(null,
				this.getOfficeFloorLocation(), this.getOfficeFloorProperties());
		managedProcess.invokeTask("OFFICE", "SECTION.WORK", "writeMessage",
				file.getAbsolutePath());

		// Run process ensuring it completes
		ProcessManager manager = ProcessManager.startProcess(managedProcess,
				null);
		OfficeBuildingTestUtil.waitUntilProcessComplete(manager, null);

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written",
				MockWork.MESSAGE, file);
	}

	/**
	 * Ensure use alternate {@link OfficeFloorSource}.
	 */
	public void testAlternateOfficeFloorSource() throws Throwable {

		final String MESSAGE = "Test OfficeFloorSource";

		// Create properties with message to write
		Properties properties = this.getOfficeFloorProperties();
		properties.put(MockOfficeFloorSource.PROPERTY_MESSAGE, MESSAGE);

		// Create the Mock OfficeFloorSource
		File file = OfficeBuildingTestUtil.createTempFile(this);
		OfficeFloorManager managedProcess = new OfficeFloorManager(
				MockOfficeFloorSource.class.getName(), file.getAbsolutePath(),
				properties);

		// Start process (should be using alternate OfficeFloorSource)
		ProcessManager manager = ProcessManager.startProcess(managedProcess,
				null);
		manager.destroyProcess();

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written",
				MESSAGE, file);
	}

	/**
	 * Ensure able to invoke {@link Work} remotely.
	 */
	public void testInvokeWorkRemotely() throws Exception {

		// Create the OfficeFloor managed process
		File file = OfficeBuildingTestUtil.createTempFile(this);
		OfficeFloorManager managedProcess = new OfficeFloorManager(null,
				this.getOfficeFloorLocation(), this.getOfficeFloorProperties());

		// Create process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		configuration.setMbeanServer(mbeanServer);

		// Run the process
		ProcessManager manager = ProcessManager.startProcess(managedProcess,
				configuration);

		// Ensure the OfficeFloor managed process MBean registered
		ObjectName mbeanName = manager
				.getLocalObjectName(OfficeFloorManager.OFFICE_FLOOR_MANAGER_OBJECT_NAME);
		assertTrue("OfficeFloor MBean must be registered",
				mbeanServer.isRegistered(mbeanName));

		// Obtain the OfficeFloor MBean
		OfficeFloorManagerMBean mbean = JMX.newMBeanProxy(mbeanServer,
				mbeanName, OfficeFloorManagerMBean.class);

		// Invoke the work
		mbean.invokeTask("OFFICE", "SECTION.WORK", null, file.getAbsolutePath());

		// Stop the managed process
		manager.triggerStopProcess();
		OfficeBuildingTestUtil.waitUntilProcessComplete(manager, null);

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written",
				MockWork.MESSAGE, file);
	}

	/**
	 * Obtains the location of the {@link OfficeFloor} configuration.
	 * 
	 * @return Location of the {@link OfficeFloor} configuration.
	 */
	private String getOfficeFloorLocation() {
		String officeFloorLocation = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/TestOfficeFloor.officefloor";
		return officeFloorLocation;
	}

	/**
	 * Obtains the {@link Properties} for the {@link OfficeFloor}.
	 * 
	 * @return {@link Properties} for the {@link OfficeFloor}.
	 */
	private Properties getOfficeFloorProperties() {
		Properties properties = new Properties();
		properties.setProperty("team.name", "TEAM");
		return properties;
	}

}