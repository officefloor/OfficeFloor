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
package net.officefloor.console;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;
import java.util.List;

import javax.management.InstanceNotFoundException;

import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Test the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTest extends AbstractConsoleMainTestCase {

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
				+ OfficeBuildingManager
						.getOfficeBuildingJmxServiceUrl(
								null,
								OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT)
						.toString();
	}

	/**
	 * Ensure can start and then stop the {@link OfficeBuilding}.
	 */
	public void testOfficeBuildingLifecycle_start_stop() throws Throwable {

		// Start the Office Building
		long beforeStartTime = System.currentTimeMillis();
		this.doMain("start");

		// Ensure started by obtaining manager
		OfficeBuildingManagerMBean manager = OfficeBuildingManager
				.getOfficeBuildingManager(
						null,
						OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT);

		// Ensure correct start time
		long afterStartTime = System.currentTimeMillis();
		long startTime = manager.getStartTime().getTime();
		assertTrue(
				"Office Building should be just started",
				((beforeStartTime <= startTime) && (startTime <= afterStartTime)));

		// Stop the Office Building
		this.doMain("stop");

		// Ensure no errors and correct output
		this.assertOut(this.officeBuildingStartLine, "OfficeBuilding stopped");
		this.assertErr();

		// Ensure stopped
		try {
			manager.getStartTime();
			fail("Office Building should be stopped");
		} catch (UndeclaredThrowableException ex) {
			// Ensure cause is IO failure as Office Building stopped
			Throwable cause = ex.getCause();
			assertTrue("Incorrect cause", (cause instanceof IOException));
		}
	}

	/**
	 * Ensure able to obtain URL.
	 */
	public void testUrl() throws Throwable {

		final String HOST = "server";

		// Obtain the URL
		this
				.doMain("--office-building-host server --office-building-port 13778 url");

		// Validate output URL
		String expectedUrl = OfficeBuildingManager
				.getOfficeBuildingJmxServiceUrl(
						HOST,
						OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT)
				.toString();
		this.assertOut(expectedUrl);
		this.assertErr();
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} from via an archive and list
	 * processes then tasks.
	 */
	public void testOpenOfficeFloorViaArchiveAndList() throws Throwable {

		final String PROCESS_NAME = "Process";

		// Obtain location of Jar file
		File jarFilePath = this.findFile("lib/MockCore.jar");

		// Expected output
		List<String> out = new LinkedList<String>();

		// Start the OfficeBuilding
		this.doMain("start");
		out.add(this.officeBuildingStartLine);

		// Open the OfficeFloor (via a Jar)
		this
				.doMain("--jar "
						+ jarFilePath.getParentFile().getAbsolutePath()
						+ " --officefloor net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
						+ " open");
		out.add("OfficeFloor open under process name space '" + PROCESS_NAME
				+ "'");

		// List the processes for the OfficeFloor
		this.doMain("list");
		out.add(PROCESS_NAME);

		// List the tasks for the OfficeFloor
		this.doMain("--process-name " + PROCESS_NAME + " list");
		out.add("OFFICE");
		out.add("\tSECTION.WORK");
		out.add("\t\twriteMessage (String)");

		// Close the OfficeFloor
		this.doMain("--process-name " + PROCESS_NAME + " close");
		out.add("Closed");

		// Stop the OfficeBuilding
		this.doMain("stop");
		out.add("OfficeBuilding stopped");

		// Validate no error and correct output
		this.assertErr();
		this.assertOut(out.toArray(new String[out.size()]));
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} from an artifact and invoke.
	 */
	public void testOpenOfficeFloorViaArtifactAndInvoke() throws Throwable {

		final String PROCESS_NAME = this.getName();
		final String OFFICE_FLOOR_VERSION = OfficeBuildingTestUtil
				.getOfficeFloorArtifactVersion("officecompiler");

		// Expected output
		List<String> out = new LinkedList<String>();

		// Start the OfficeBuilding
		this.doMain("start");
		out.add(this.officeBuildingStartLine);

		// Open the OfficeFloor (via an Artifact)
		String openCommand = "--artifact net.officefloor.core:officecompiler:"
				+ OFFICE_FLOOR_VERSION
				+ " --process-name "
				+ PROCESS_NAME
				+ " --officefloor net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
				+ " open";
		this.doMain(openCommand);
		out.add("OfficeFloor open under process name space '" + PROCESS_NAME
				+ "'");

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Run the Task (to ensure OfficeFloor is open by writing to file)
		this.doMain("--process-name " + PROCESS_NAME + " --office OFFICE"
				+ " --work SECTION.WORK" + " --task writeMessage"
				+ " --parameter " + tempFile.getAbsolutePath() + " invoke");
		out
				.add("Invoked work SECTION.WORK (task writeMessage) on office OFFICE with parameter "
						+ tempFile.getAbsolutePath());

		// Ensure message written to file (passive team so should be done)
		String fileContent = this.getFileContents(tempFile);
		assertEquals("Message should be written to file", MockWork.MESSAGE,
				fileContent);

		// Stop the OfficeBuilding (ensuring running processes are stopped)
		this.doMain("stop");
		out.add("Stopping processes:");
		out.add("\t" + PROCESS_NAME + " [" + PROCESS_NAME + "]");
		out.add("");
		out.add("OfficeBuilding stopped");

		// Validate no error and correct output
		this.assertErr();
		this.assertOut(out.toArray(new String[out.size()]));
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} invoking a {@link Task} and
	 * stops {@link Process} once {@link Task} complete.
	 */
	public void testOpenOfficeFloorInvokingTask() throws Throwable {

		final String PROCESS_NAME = this.getName();

		// Expected output
		List<String> out = new LinkedList<String>();

		// Start the OfficeBuilding
		this.doMain("start");
		out.add(this.officeBuildingStartLine);

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Open the OfficeFloor (via an Artifact).
		// Use system property to specify office.
		String openCommand = "--jvm-option -D"
				+ MockWork.INCLUDE_SYSTEM_PROPERTY
				+ "=SYS_PROP_TEST"
				+ " --process-name "
				+ PROCESS_NAME
				+ " --officefloor net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
				+ " --office OFFICE" + " --work SECTION.WORK"
				+ " --task writeMessage" + " --parameter "
				+ tempFile.getAbsolutePath() + " open";
		this.doMain(openCommand);
		out
				.add("OfficeFloor open under process name space '"
						+ PROCESS_NAME
						+ "' for work (office=OFFICE, work=SECTION.WORK, task=writeMessage, parameter="
						+ tempFile.getAbsolutePath() + ")");

		// Wait for office floor to complete
		try {
			ProcessManagerMBean manager = OfficeBuildingManager
					.getProcessManager(
							null,
							OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT,
							PROCESS_NAME);
			long startTime = System.currentTimeMillis();
			do {
				// Allow some time for task to run
				Thread.sleep(100);
				if ((System.currentTimeMillis() - startTime) > 5000) {
					assertOut("Timed out waiting for task to run");
					fail("Ensure failure as timed out");
				}

			} while (!manager.isProcessComplete());
		} catch (UndeclaredThrowableException ex) {
			// May have already finished and unregistered before check
			assertTrue(
					"Check failure should only be because finished and unregistered",
					ex.getCause() instanceof InstanceNotFoundException);
		}

		// Ensure message written to file (task ran)
		String fileContent = this.getFileContents(tempFile);
		assertEquals("Message should be written to file", MockWork.MESSAGE
				+ "SYS_PROP_TEST", fileContent);

		// Stop the OfficeBuilding (ensuring running processes are stopped)
		this.doMain("stop");
		out.add("OfficeBuilding stopped");

		// Validate no error and correct output
		this.assertErr();
		this.assertOut(out.toArray(new String[out.size()]));
	}

	/**
	 * Ensure correct Help.
	 */
	public void testHelp() throws Throwable {

		// Obtain the Help
		this.doMain("help");

		// Validate output Help
		this.assertErr();
		this
				.assertOut(
						"                                                         ",
						"usage: script [options] <commands>                       ",
						"                                                         ",
						"Commands:                                                ",
						"                                                         ",
						"start : Starts the OfficeBuilding                        ",
						"      Options:                                           ",
						"       -lr,--local-repository <arg>         Local repository for caching Artifacts",
						"       -p,--office-building-port <arg>      Port for the OfficeBuilding. Default is 13778",
						"       -rr,--remote-repository-urls <arg>   Remote repository URL to retrieve Artifacts",
						"                                                         ",
						"url : Obtains the URL for the OfficeBuilding             ",
						"    Options:                                             ",
						"     --office-building-host <arg>      OfficeBuilding Host. Default is localhost",
						"     -p,--office-building-port <arg>   Port for the OfficeBuilding. Default is 13778",
						"                                                         ",
						"open : Opens an OfficeFloor within the OfficeBuilding    ",
						"     Options:                                            ",
						"      -a,--artifact <arg>               Artifact to include on the class path",
						"      -cp,--classpath <arg>             Raw entry to include on the class path",
						"      -j,--jar <arg>                    Archive to include on the class path",
						"      --jvm-option <arg>                JVM option       ",
						"      -o,--office <arg>                 Name of the Office",
						"      -of,--officefloor <arg>           Location of the OfficeFloor",
						"      --office-building-host <arg>      OfficeBuilding Host. Default is localhost",
						"      -p,--office-building-port <arg>   Port for the OfficeBuilding. Default is 13778",
						"      --parameter <arg>                 Parameter for the Task",
						"      --process-name <arg>              Process name space. Default is Process",
						"      -t,--task <arg>                   Name of the Task ",
						"      -w,--work <arg>                   Name of the Work ",
						"                                                         ",
						"list : Lists details of the OfficeBuilding/OfficeFloor   ",
						"     Options:                                            ",
						"      --office-building-host <arg>      OfficeBuilding Host. Default is localhost",
						"      -p,--office-building-port <arg>   Port for the OfficeBuilding. Default is 13778",
						"      --process-name <arg>              Process name space. Default is Process",
						"                                                         ",
						"invoke : Invokes a Task within a running OfficeFloor     ",
						"       Options:                                          ",
						"        -o,--office <arg>                 Name of the Office",
						"        --office-building-host <arg>      OfficeBuilding Host. Default is localhost",
						"        -p,--office-building-port <arg>   Port for the OfficeBuilding. Default is 13778",
						"        --parameter <arg>                 Parameter for the Task",
						"        --process-name <arg>              Process name space. Default is Process",
						"        -t,--task <arg>                   Name of the Task",
						"        -w,--work <arg>                   Name of the Work",
						"                                                         ",
						"close : Closes an OfficeFloor within the OfficeBuilding  ",
						"      Options:                                           ",
						"       --office-building-host <arg>      OfficeBuilding Host. Default is localhost",
						"       -p,--office-building-port <arg>   Port for the OfficeBuilding. Default is 13778",
						"       --process-name <arg>              Process name space. Default is Process",
						"       --stop-max-wait-time <arg>        Maximum time in milliseconds to wait to stop. Default is 10000",
						"                                                         ",
						"stop : Stops the OfficeBuilding                          ",
						"     Options:                                            ",
						"      --office-building-host <arg>      OfficeBuilding Host. Default is localhost",
						"      -p,--office-building-port <arg>   Port for the OfficeBuilding. Default is 13778",
						"      --stop-max-wait-time <arg>        Maximum time in milliseconds to wait to stop. Default is 10000",
						"                                                         ",
						"help : This help message                                 ");
	}

}