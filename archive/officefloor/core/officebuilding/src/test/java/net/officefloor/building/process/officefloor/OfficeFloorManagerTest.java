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
import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * Tests the {@link OfficeFloorManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagerTest extends OfficeFrameTestCase {

	/**
	 * {@link ProcessManager}.
	 */
	private ProcessManager processManager = null;

	@Override
	protected void tearDown() throws Exception {

		// Ensure stop the process
		if (this.processManager != null) {
			this.processManager.destroyProcess();
			OfficeBuildingTestUtil.waitUntilProcessComplete(this.processManager, null);
		}

		// Super tear down
		super.tearDown();
	}

	/**
	 * Ensures the {@link OfficeFloor} configuration is correct by running it.
	 */
	public void testEnsureOfficeFloorRuns() throws Exception {

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		compiler.addSourceAliases();
		compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
		compiler.setOfficeFloorLocation(this.getOfficeFloorLocation());
		compiler.addProperty("team.name", "TEAM");

		// Compile the test OfficeFloor
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Run function to write to temporary file
		File file = OfficeBuildingTestUtil.createTempFile(this);
		officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.writeMessage").invokeProcess(file.getAbsolutePath(),
				null);

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written", MockWork.MESSAGE, file);
	}

	/**
	 * Ensure can start an {@link OfficeFloor} to execute a
	 * {@link ManagedFunctionk}.
	 */
	public void testOfficeFloorManagedProcess() throws Throwable {

		// Create the OfficeFloor managed process
		File file = OfficeBuildingTestUtil.createTempFile(this);
		OfficeFloorManager managedProcess = new OfficeFloorManager(OfficeFloorModelOfficeFloorSource.class.getName(),
				this.getOfficeFloorLocation(), this.getOfficeFloorProperties());
		managedProcess.addExecuteFunction("OFFICE", "SECTION.writeMessage", file.getAbsolutePath());

		// Run process ensuring it completes
		this.processManager = ProcessManager.startProcess(managedProcess, null);
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.processManager, null);

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written", MockWork.MESSAGE, file);
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
		OfficeFloorManager managedProcess = new OfficeFloorManager(MockOfficeFloorSource.class.getName(),
				file.getAbsolutePath(), properties);

		// Start process (should be using alternate OfficeFloorSource)
		this.processManager = ProcessManager.startProcess(managedProcess, null);

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written", MESSAGE, file);
	}

	/**
	 * Ensure able to invoke {@link ManagedFunction} remotely.
	 */
	public void testInvokeFunctionRemotely() throws Exception {

		// Create the OfficeFloor managed process
		File file = OfficeBuildingTestUtil.createTempFile(this);
		OfficeFloorManager managedProcess = new OfficeFloorManager(OfficeFloorModelOfficeFloorSource.class.getName(),
				this.getOfficeFloorLocation(), this.getOfficeFloorProperties());

		// Create process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		configuration.setMbeanServer(mbeanServer);

		// Run the process
		this.processManager = ProcessManager.startProcess(managedProcess, configuration);

		// Ensure the OfficeFloor managed process MBean registered
		ObjectName officeFloorManagerName = this.processManager.getLocalObjectName(
				OfficeFloorManager.getOfficeFloorManagerObjectName(this.processManager.getProcessName()));
		assertTrue("OfficeFloor Manager MBean must be registered", mbeanServer.isRegistered(officeFloorManagerName));

		// Obtain the OfficeFloor Manager MBean
		OfficeFloorManagerMBean officeFloorManager = JMX.newMBeanProxy(mbeanServer, officeFloorManagerName,
				OfficeFloorManagerMBean.class);
		OfficeBuildingTestUtil.waitUntilOfficeFloorOpens(officeFloorManager, this.processManager, mbeanServer);

		// As OfficeFloor is open, should have registered OfficeFloor MBean
		ObjectName officeFloorName = this.processManager
				.getLocalObjectName(OfficeFloorManager.getOfficeFloorObjectName(this.processManager.getProcessName()));
		assertTrue("OfficeFloor MBean must be registered as OfficeFloor open",
				mbeanServer.isRegistered(officeFloorName));

		// Invoke the function within OfficeFloor
		OfficeFloorMBean officeFloor = JMX.newMBeanProxy(mbeanServer, officeFloorName, OfficeFloorMBean.class);
		officeFloor.invokeFunction("OFFICE", "SECTION.writeMessage", file.getAbsolutePath());

		// Stop the managed process
		this.processManager.triggerStopProcess();
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.processManager, null);

		// Validate content in file
		OfficeBuildingTestUtil.validateFileContent("Expecting content written", MockWork.MESSAGE, file);
	}

	/**
	 * Ensure listen to {@link OfficeFloor} for closing.
	 */
	public void testCloseViaOfficeFloor() throws Exception {

		// Create the OfficeFloor managed process
		OfficeFloorManager managedProcess = new OfficeFloorManager(OfficeFloorModelOfficeFloorSource.class.getName(),
				this.getOfficeFloorLocation(), this.getOfficeFloorProperties());

		// Create process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		configuration.setMbeanServer(mbeanServer);

		// Run the process
		this.processManager = ProcessManager.startProcess(managedProcess, configuration);

		// Obtain the OfficeFloor Manager MBean
		ObjectName officeFloorManagerName = this.processManager.getLocalObjectName(
				OfficeFloorManager.getOfficeFloorManagerObjectName(this.processManager.getProcessName()));
		OfficeFloorManagerMBean officeFloorManager = JMX.newMBeanProxy(mbeanServer, officeFloorManagerName,
				OfficeFloorManagerMBean.class);
		OfficeBuildingTestUtil.waitUntilOfficeFloorOpens(officeFloorManager, this.processManager, mbeanServer);

		// Obtain the OfficeFloor MBean
		ObjectName officeFloorName = this.processManager
				.getLocalObjectName(OfficeFloorManager.getOfficeFloorObjectName(this.processManager.getProcessName()));
		OfficeFloorMBean officeFloor = JMX.newMBeanProxy(mbeanServer, officeFloorName, OfficeFloorMBean.class);

		// Stop via the OfficeFloor (and wait until notifies process complete)
		officeFloor.closeOfficeFloor();
		OfficeBuildingTestUtil.waitUntilProcessComplete(this.processManager, null);
		assertTrue("Process should be complete", this.processManager.isProcessComplete());
	}

	/**
	 * Obtains the location of the {@link OfficeFloor} configuration.
	 * 
	 * @return Location of the {@link OfficeFloor} configuration.
	 */
	private String getOfficeFloorLocation() {
		String officeFloorLocation = this.getClass().getPackage().getName().replace('.', '/')
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