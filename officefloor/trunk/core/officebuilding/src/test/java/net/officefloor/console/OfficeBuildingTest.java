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
package net.officefloor.console;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.management.InstanceNotFoundException;

import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessManagerTest;
import net.officefloor.building.process.officefloor.MockOfficeFloorSource;
import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Test the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTest extends AbstractConsoleMainTestCase {

	/**
	 * Obtains the working directory.
	 * 
	 * @return Working directory.
	 */
	public static File getWorkingDirectory() {
		return new File(new File(System.getProperty("java.io.tmpdir"), System.getProperty("user.name")),
				"officebuilding");
	}

	/**
	 * Start line for {@link OfficeBuilding}.
	 */
	private String officeBuildingStartLine;

	/**
	 * Initiate.
	 */
	public OfficeBuildingTest() {
		super(OfficeBuilding.class, true);
	}

	@Override
	protected void setUp() throws Exception {

		// Setup console main
		super.setUp();

		// Obtain the office building start line
		this.officeBuildingStartLine = "OfficeBuilding started at "
				+ OfficeBuildingManager.getOfficeBuildingJmxServiceUrl(null,
						OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT).toString();
	}

	/**
	 * Ensure can start and then stop the {@link OfficeBuilding}.
	 */
	public void testOfficeBuildingLifecycle_start_stop() throws Throwable {

		// Start the Office Building
		long beforeStartTime = System.currentTimeMillis();
		this.doSecureMain("start");

		// Ensure started by obtaining manager
		OfficeBuildingManagerMBean manager = OfficeBuildingManager.getOfficeBuildingManager(null,
				OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT,
				OfficeBuildingTestUtil.getTrustStore(), OfficeBuildingTestUtil.getTrustStorePassword(),
				OfficeBuildingTestUtil.getLoginUsername(), OfficeBuildingTestUtil.getLoginPassword());

		// Ensure correct start time
		long afterStartTime = System.currentTimeMillis();
		long startTime = manager.getStartTime().getTime();
		assertTrue("Office Building should be just started",
				((beforeStartTime <= startTime) && (startTime <= afterStartTime)));

		// Stop the Office Building
		this.doSecureMain("stop");

		// Ensure no errors and correct output
		this.assertOut(this.officeBuildingStartLine, "OfficeBuilding stopped");
		this.assertErr();

		// Ensure stopped
		try {
			manager.getStartTime();
			fail("Office Building should be stopped");
		} catch (IOException ex) {
			// Ensure cause is IO failure as Office Building stopped
			assertEquals("Incorrect cause", "no such object in table", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain URL.
	 */
	public void testUrl() throws Throwable {

		final String HOST = "server";

		// Obtain the URL
		this.doMain("--office_building_host server --office_building_port 13778 url");

		// Validate output URL
		String expectedUrl = OfficeBuildingManager.getOfficeBuildingJmxServiceUrl(HOST,
				OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT).toString();
		this.assertOut(expectedUrl);
		this.assertErr();
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} from via an
	 * {@link UploadArtifact} and list processes then tasks.
	 */
	public void testOpenOfficeFloorViaUploadArtifactAndList() throws Throwable {

		final String PROCESS_NAME = "officefloor";

		// Obtain location of Jar file
		File jarFilePath = this.findFile("lib/MockCore.jar");

		// Start the OfficeBuilding
		this.doSecureMain("start");
		String prefixOutput = this.officeBuildingStartLine;

		// Ensure MockCore.jar is not existing
		File mockJar = new File(getWorkingDirectory(), "officefloor1/MockCore.jar");
		assertFalse("MockCore.jar should not yet be uploaded", mockJar.exists());

		// Open the OfficeFloor (via an uploaded artifact)
		this.doSecureMain("--upload_artifact " + jarFilePath.getAbsolutePath()
				+ " --officefloor net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
				+ " --property team.name=TEAM" + " open");
		String startupOutput = "OfficeFloor open under process name space '" + PROCESS_NAME + "'\n";

		// List the processes for the OfficeFloor
		this.doSecureMain("list");
		String processOutput = PROCESS_NAME + "\n";

		// List the tasks for the OfficeFloor
		this.doSecureMain("--process_name " + PROCESS_NAME + " list");
		processOutput += "OFFICE SECTION.WORK writeMessage(java.lang.String)\n";

		// Validate the MockCore.jar is available
		assertTrue("MockCore.jar should be uploaded", mockJar.exists());

		// Close the OfficeFloor
		this.doSecureMain("--process_name " + PROCESS_NAME + " close");
		String suffixOutput = "Closed\n";

		// Validate the process work space cleaned up
		assertFalse("MockCore.jar should be cleaned up", mockJar.exists());

		// Stop the OfficeBuilding
		this.doSecureMain("stop");
		suffixOutput += "OfficeBuilding stopped\n";

		// Validate no error and correct output
		this.assertErr();
		ProcessManagerTest.assertProcessStartOutput(this.getOut(), true, prefixOutput, startupOutput, processOutput,
				suffixOutput);
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} invoking a {@link Task} and
	 * stops {@link Process} once {@link Task} complete.
	 */
	public void testOpenOfficeFloorInvokingTask() throws Throwable {

		final String PROCESS_NAME = this.getName();

		// Start the OfficeBuilding
		this.doSecureMain("start");
		String prefixOutput = this.officeBuildingStartLine;

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Open the OfficeFloor (via an Artifact).
		// Use system property to specify office.
		String openCommand = "--jvm_option -D" + MockWork.INCLUDE_SYSTEM_PROPERTY + "=SYS_PROP_TEST"
				+ " --process_name " + PROCESS_NAME
				+ " --officefloor net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
				+ " --office OFFICE" + " --work SECTION.WORK" + " --task writeMessage" + " --parameter "
				+ tempFile.getAbsolutePath() + " --property team.name=TEAM" + " open";
		this.doSecureMain(openCommand);
		String startupOutput = "OfficeFloor open under process name space '" + PROCESS_NAME
				+ "' for work (office=OFFICE, work=SECTION.WORK, task=writeMessage, parameter="
				+ tempFile.getAbsolutePath() + ")\n";

		// Wait for OfficeFloor to complete
		try {
			ProcessManagerMBean manager = OfficeBuildingManager.getProcessManager(null,
					OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT, PROCESS_NAME,
					OfficeBuildingTestUtil.getTrustStore(), OfficeBuildingTestUtil.getTrustStorePassword(),
					OfficeBuildingTestUtil.getLoginUsername(), OfficeBuildingTestUtil.getLoginPassword());
			OfficeBuildingTestUtil.waitUntilProcessComplete(manager, this);
		} catch (UndeclaredThrowableException ex) {
			// May have already finished and unregistered before check
			assertTrue("Check failure should only be because finished and unregistered",
					ex.getCause() instanceof InstanceNotFoundException);
		}

		// Ensure message written to file (task ran)
		String fileContent = this.getFileContents(tempFile);
		assertEquals("Message should be written to file", MockWork.MESSAGE + "SYS_PROP_TEST", fileContent);

		// Stop the OfficeBuilding (ensuring running processes are stopped)
		this.doSecureMain("stop");
		String suffixOutput = "OfficeBuilding stopped\n";

		// Validate no error and correct output
		this.assertErr();
		ProcessManagerTest.assertProcessStartOutput(this.getOut(), true, prefixOutput, startupOutput, null,
				suffixOutput);
	}

	/**
	 * Ensure able to open an alternate {@link OfficeFloorSource}.
	 */
	public void testAlternateOfficeFloorSource() throws Throwable {

		final String PROCESS_NAME = this.getName();
		final String MESSAGE = "MESSAGE";

		// Start the OfficeBuilding
		this.doSecureMain("start");
		String prefixOutput = this.officeBuildingStartLine;

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Run the OfficeFloor with alternate OfficeFloorSource
		String openCommand = "--process_name " + PROCESS_NAME + " --officefloorsource "
				+ MockOfficeFloorSource.class.getName() + " --officefloor " + tempFile.getAbsolutePath()
				+ " --property " + MockOfficeFloorSource.PROPERTY_MESSAGE + "=" + MESSAGE + " open";
		this.doSecureMain(openCommand);
		String startupOutput = "OfficeFloor open under process name space '" + PROCESS_NAME + "'\n";

		// Ensure message written to file
		OfficeBuildingTestUtil.validateFileContent("Message should be written to file", MESSAGE, tempFile);

		// Stop the OfficeBuilding (ensuring running processes are stopped)
		this.doSecureMain("stop");
		String suffixOutput = "Stopping processes:\n";
		suffixOutput += "\t" + PROCESS_NAME + " [" + PROCESS_NAME + "]\n";
		suffixOutput += "\n";
		suffixOutput += "OfficeBuilding stopped\n";

		// Validate no error and correct output
		this.assertErr();
		ProcessManagerTest.assertProcessStartOutput(this.getOut(), true, prefixOutput, startupOutput, null,
				suffixOutput);
	}

	/**
	 * Ensure correct Help.
	 */
	public void testHelp() throws Throwable {

		// Obtain the Help
		this.doMain("help");

		// Validate output Help
		this.assertErr();
		this.assertOut("                                                         ",
				"usage: script [options] <commands>                       ",
				"                                                         ",
				"Commands:                                                ",
				"                                                         ",
				"start : Starts the OfficeBuilding                        ",
				"      Options:                                           ",
				"       --isolate_processes <arg>        True to isolate the processes",
				"       --jvm_option <arg>               JVM option",
				"       -kp,--key_store_password <arg>   Password to the key store file",
				"       -ks,--key_store <arg>            Location of the key store file",
				"       --office_building_host <arg>     OfficeBuilding Host. Default is localhost",
				"       --office_building_port <arg>     Port for the OfficeBuilding. Default is 13778",
				"       -p,--password <arg>              Password",
				"       -u,--username <arg>              User name",
				"       --workspace <arg>                Workspace for the OfficeBuilding",
				"                                                         ",
				"url : Obtains the URL for the OfficeBuilding             ",
				"    Options:                                             ",
				"     --office_building_host <arg>   OfficeBuilding Host. Default is localhost",
				"     --office_building_port <arg>   Port for the OfficeBuilding. Default is 13778",
				"                                                         ",
				"open : Opens an OfficeFloor within the OfficeBuilding",
				"     Options:                                            ",
				"      --jvm_option <arg>               JVM option       ",
				"      -kp,--key_store_password <arg>   Password to the key store file",
				"      -ks,--key_store <arg>            Location of the key store file",
				"      -o,--office <arg>                Name of the Office",
				"      -of,--officefloor <arg>          Location of the OfficeFloor",
				"      --office_building_host <arg>     OfficeBuilding Host. Default is localhost",
				"      --office_building_port <arg>     Port for the OfficeBuilding. Default is 13778",
				"      -ofs,--officefloorsource <arg>   OfficeFloorSource",
				"      -p,--password <arg>              Password",
				"      --parameter <arg>                Parameter for the Task",
				"      --process_name <arg>             Process name space. Default is Process",
				"      --property <arg>                 Property for the OfficeFloor in the form of name=value",
				"      -t,--task <arg>                  Name of the Task ",
				"      -u,--username <arg>              User name",
				"      --upload_artifact <arg>          Artifact to be uploaded for inclusion on the class path",
				"      -w,--work <arg>                  Name of the Work ",
				"                                                         ",
				"list : Lists details of the OfficeBuilding/OfficeFloor   ",
				"     Options:                                            ",
				"      -kp,--key_store_password <arg>   Password to the key store file",
				"      -ks,--key_store <arg>            Location of the key store file",
				"      --office_building_host <arg>     OfficeBuilding Host. Default is localhost",
				"      --office_building_port <arg>     Port for the OfficeBuilding. Default is 13778",
				"      -p,--password <arg>              Password",
				"      --process_name <arg>             Process name space. Default is Process",
				"      -u,--username <arg>              User name",
				"                                                         ",
				"invoke : Invokes a Task within a running OfficeFloor     ",
				"       Options:                                          ",
				"        -kp,--key_store_password <arg>   Password to the key store file",
				"        -ks,--key_store <arg>            Location of the key store file",
				"        -o,--office <arg>                Name of the Office",
				"        --office_building_host <arg>     OfficeBuilding Host. Default is localhost",
				"        --office_building_port <arg>     Port for the OfficeBuilding. Default is 13778",
				"        -p,--password <arg>              Password",
				"        --parameter <arg>                Parameter for the Task",
				"        --process_name <arg>             Process name space. Default is Process",
				"        -t,--task <arg>                  Name of the Task",
				"        -u,--username <arg>              User name",
				"        -w,--work <arg>                  Name of the Work",
				"                                                         ",
				"close : Closes an OfficeFloor within the OfficeBuilding  ",
				"      Options:                                           ",
				"       -kp,--key_store_password <arg>   Password to the key store file",
				"       -ks,--key_store <arg>            Location of the key store file",
				"       --office_building_host <arg>     OfficeBuilding Host. Default is localhost",
				"       --office_building_port <arg>     Port for the OfficeBuilding. Default is 13778",
				"       -p,--password <arg>              Password",
				"       --process_name <arg>             Process name space. Default is Process",
				"       --stop_max_wait_time <arg>       Maximum time in milliseconds to wait to stop. Default is 10000",
				"       -u,--username <arg>              User name",
				"                                                         ",
				"stop : Stops the OfficeBuilding                          ",
				"     Options:                                            ",
				"      -kp,--key_store_password <arg>   Password to the key store file",
				"      -ks,--key_store <arg>            Location of the key store file",
				"      --office_building_host <arg>     OfficeBuilding Host. Default is localhost",
				"      --office_building_port <arg>     Port for the OfficeBuilding. Default is 13778",
				"      -p,--password <arg>              Password",
				"      --stop_max_wait_time <arg>       Maximum time in milliseconds to wait to stop. Default is 10000",
				"      -u,--username <arg>              User name",
				"                                                         ",
				"help : This help message                                 ");
	}

}