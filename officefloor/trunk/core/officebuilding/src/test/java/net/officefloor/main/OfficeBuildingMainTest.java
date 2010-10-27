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

package net.officefloor.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.UndeclaredThrowableException;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeBuildingMain}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingMainTest extends OfficeFrameTestCase {

	/**
	 * Default {@link OfficeBuildingMain} port.
	 */
	public static final int DEFAULT_OFFICE_BUILDING_PORT = 13778;

	/**
	 * Capture of <code>stdout</code>.
	 */
	private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();

	/**
	 * <code>stdout</code> to reinstate after test.
	 */
	private PrintStream _stdout;

	/**
	 * Capture of <code>stderr</code>.
	 */
	private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

	/**
	 * <code>stderr</code> to reinstate after test.
	 */
	private PrintStream _stderr;

	@Override
	protected void setUp() throws Exception {

		// Flag that testing
		OfficeBuildingMain.isTesting = true;

		// Provide OfficeBuilding Home
		File officeBuildingHomeDir = this.findFile(
				OfficeBuildingMain.PROPERTIES_FILE_RELATIVE_PATH)
				.getParentFile().getParentFile();
		System.setProperty(OfficeBuildingMain.OFFICE_BUILDING_HOME,
				officeBuildingHomeDir.getAbsolutePath());

		// Provide the local repository
		System.setProperty(OfficeBuildingMain.PROPERTY_LOCAL_REPOSITORY_PATH,
				OfficeBuildingTestUtil.getLocalRepositoryDirectory()
						.getAbsolutePath());

		// Provide remote repository path (use local to stop download)
		String localRepositoryUrl = OfficeBuildingTestUtil
				.getLocalRepositoryDirectory().toURI().toURL().toString();
		System.setProperty(OfficeBuildingMain.PROPERTY_REMOTE_REPOSITORY_URL,
				localRepositoryUrl);

		// Collect output
		this._stdout = System.out;
		System.setOut(new PrintStream(this.stdout));
		this._stderr = System.err;
		System.setErr(new PrintStream(this.stderr));
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure stop the OfficeFloor
		try {
			OfficeBuildingMain.main("stop");
		} catch (Throwable ex) {
		}

		// Clear the system properties specified for test
		System.clearProperty(OfficeBuildingMain.OFFICE_BUILDING_HOME);
		System.clearProperty(OfficeBuildingMain.PROPERTY_LOCAL_REPOSITORY_PATH);
		System.clearProperty(OfficeBuildingMain.PROPERTY_REMOTE_REPOSITORY_URL);

		// Reinstate output streams
		System.setOut(this._stdout);
		System.setErr(this._stderr);
	}

	/**
	 * Ensure can start and then stop the {@link OfficeBuildingMain}.
	 */
	public void testOfficeBuildingLifecycle_start_stop() throws Throwable {

		// Ensure stopped
		try {
			OfficeBuildingMain.main("stop");
		} catch (IOException ex) {
		}

		// Start the Office Building
		long beforeStartTime = System.currentTimeMillis();
		OfficeBuildingMain.main("start");

		// Ensure started by obtaining manager
		OfficeBuildingManagerMBean manager = OfficeBuildingManager
				.getOfficeBuildingManager(null, DEFAULT_OFFICE_BUILDING_PORT);

		// Ensure correct start time
		long afterStartTime = System.currentTimeMillis();
		long startTime = manager.getStartTime().getTime();
		assertTrue(
				"Office Building should be just started",
				((beforeStartTime <= startTime) && (startTime <= afterStartTime)));

		// Stop the Office Building (pulls in default stop time)
		OfficeBuildingMain.main("stop");

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
	 * Ensure able to run <code>url</code> command.
	 */
	public void testUrl() throws Throwable {

		final String HOST = "server";

		// Obtain the URL
		OfficeBuildingMain.main("url", HOST, String
				.valueOf(DEFAULT_OFFICE_BUILDING_PORT));

		// Validate output URL
		String expectedUrl = OfficeBuildingManager
				.getOfficeBuildingJmxServiceUrl(HOST,
						DEFAULT_OFFICE_BUILDING_PORT).toString()
				+ "\n";
		validateStreamContent("Incorrect URL", expectedUrl, this.stdout);
	}

	/**
	 * Ensure provides usage on no command.
	 */
	public void testUsageMessage() throws Throwable {

		// Attempt with no command
		try {
			OfficeBuildingMain.main();
			fail("Should not succeed as requires command");
		} catch (Error ex) {
			validateStreamContent("Should provide usage message",
					OfficeBuildingMain.USAGE_MESSAGE + "\n", this.stderr);
			assertEquals("Incorrect requires command cause", "Exit: "
					+ OfficeBuildingMain.USAGE_MESSAGE, ex.getMessage());
		}
	}

	/**
	 * Ensure provides usage on unknown command.
	 */
	public void testUnknownCommand() throws Throwable {

		// Attempt with unknown command
		try {
			OfficeBuildingMain.main("unknown");
			fail("Should not succeed as unknown command");
		} catch (Error ex) {
			String errorMessage = "ERROR: unknown command 'unknown'\n\n"
					+ OfficeBuildingMain.USAGE_MESSAGE + "\n";
			validateStreamContent("Should provide usage message", errorMessage,
					this.stderr);
			assertEquals("Incorrect unknown command cause",
					"Exit: ERROR: unknown command 'unknown'\n\n"
							+ OfficeBuildingMain.USAGE_MESSAGE, ex.getMessage());
		}
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} from a Jar.
	 */
	public void testOpenOfficeFloor_Jar() throws Throwable {

		final String PROCESS_NAME = this.getName();

		// Obtain location of Jar file
		File jarFilePath = this.findFile("lib/MockCore.jar");

		// Start the OfficeBuilding
		OfficeBuildingMain.main("start");
		this.stdout.reset(); // ignore output

		// Open the OfficeFloor (via a Jar)
		OfficeBuildingMain
				.main("open", PROCESS_NAME, jarFilePath.getAbsolutePath(),
						"net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor");
		validateStreamContent("Should be no error output", "", this.stderr);
		validateStreamContent("OfficeFloor should be opened",
				"OfficeFloor open under process name space '" + PROCESS_NAME
						+ "'\n", this.stdout);
		this.stdout.reset(); // reset for listing

		// Create the expected listing of processes
		StringBuilder processes = new StringBuilder();
		processes.append(PROCESS_NAME + "\n");

		// List the tasks for the OfficeFloor
		OfficeBuildingMain.main("list");
		validateStreamContent("Incorrect process listing",
				processes.toString(), this.stdout);
		this.stdout.reset(); // reset for next listing

		// Create the expected listing of tasks
		StringBuilder tasks = new StringBuilder();
		tasks.append("OFFICE\n");
		tasks.append("\tSECTION.WORK\n");
		tasks.append("\t\twriteMessage (" + String.class.getSimpleName()
				+ ")\n");

		// List the tasks for the OfficeFloor
		OfficeBuildingMain.main("list", PROCESS_NAME);
		validateStreamContent("Incorrect task listing", tasks.toString(),
				this.stdout);

		// Stop the OfficeBuilding
		OfficeBuildingMain.main("stop");
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} from an artifact.
	 */
	public void testOpenOfficeFloor_Artifact() throws Throwable {

		final String PROCESS_NAME = this.getName();
		final String OFFICE_FLOOR_VERSION = OfficeBuildingTestUtil
				.getOfficeFloorArtifactVersion("officecompiler");

		// Start the OfficeBuilding
		OfficeBuildingMain.main("start");
		this.stdout.reset(); // ignore output

		// Open the OfficeFloor (via an Artifact)
		OfficeBuildingMain
				.main("open", PROCESS_NAME,
						"net.officefloor.core:officecompiler:"
								+ OFFICE_FLOOR_VERSION,
						"net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor");
		validateStreamContent("Should be no error output", "", this.stderr);
		validateStreamContent("OfficeFloor should be opened",
				"OfficeFloor open under process name space '" + PROCESS_NAME
						+ "'\n", this.stdout);

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Run the Task (to ensure OfficeFloor is open by writing to file)
		OfficeBuildingMain.main("invoke", PROCESS_NAME, "OFFICE",
				"SECTION.WORK", "writeMessage", tempFile.getAbsolutePath());
		validateStreamContent("Should be no errors", "", this.stderr);

		// Ensure message written to file
		String fileContent = this.getFileContents(tempFile).trim();
		assertEquals("Message should be written to file", MockWork.MESSAGE,
				fileContent);

		// Stop the OfficeBuilding
		OfficeBuildingMain.main("stop");
	}

	/**
	 * Validates the contents of the stream.
	 * 
	 * @param message
	 *            Message describing validation.
	 * @param expectedContent
	 *            Expected content of the stream.
	 * @param stream
	 *            Stream to validate its content.
	 */
	private static void validateStreamContent(String message,
			String expectedContent, ByteArrayOutputStream stream)
			throws Exception {
		String actualContent = getStreamContent(stream);

		// Ignore [WARNING]...IGNORING lines about no checksums
		int startPos = actualContent.indexOf("[WARNING]");
		if (startPos >= 0) {
			int endPos = actualContent.lastIndexOf("IGNORING")
					+ "IGNORING\n".length();
			actualContent = actualContent.substring(0, startPos)
					+ actualContent.substring(endPos);
		}

		// Ensure content matches
		assertEquals(message, expectedContent, actualContent);
	}

	/**
	 * Obtains the contents of the stream.
	 * 
	 * @param stream
	 *            Stream to obtain its contents.
	 * @return Contents of the stream.
	 */
	private static String getStreamContent(ByteArrayOutputStream stream)
			throws Exception {
		Reader reader = new InputStreamReader(new ByteArrayInputStream(stream
				.toByteArray()));
		StringBuilder content = new StringBuilder();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}
		return content.toString();
	}

}