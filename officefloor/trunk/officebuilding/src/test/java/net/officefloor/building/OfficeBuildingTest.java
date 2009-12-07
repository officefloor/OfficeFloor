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
package net.officefloor.building;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.UndeclaredThrowableException;

import junit.framework.TestCase;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;

/**
 * Tests the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTest extends TestCase {

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
		OfficeBuilding.isTesting = true;

		// Collect output
		this._stdout = System.out;
		System.setOut(new PrintStream(this.stdout));
		this._stderr = System.err;
		System.setErr(new PrintStream(this.stderr));
	}

	@Override
	protected void tearDown() throws Exception {
		// Reinstate output streams
		System.setOut(this._stdout);
		System.setErr(this._stderr);
	}

	/**
	 * Ensure can start and then stop the {@link OfficeBuilding}.
	 */
	public void testOfficeBuildingLifecycle_start_stop() throws Throwable {

		// Provide Office Building Home
		File officeBuildingHomeDir = new File(".", "src/main/resources");
		System.setProperty(OfficeBuilding.OFFICE_BUILDING_HOME,
				officeBuildingHomeDir.getAbsolutePath());

		// Start the Office Building
		long beforeStartTime = System.currentTimeMillis();
		OfficeBuilding.main("start");
		long afterStartTime = System.currentTimeMillis();

		// Ensure started
		OfficeBuildingManagerMBean manager = OfficeBuildingManager
				.getOfficeBuildingManager(null, 13778);
		long startTime = manager.getStartTime().getTime();
		assertTrue(
				"Office Building should be just started",
				((beforeStartTime <= startTime) && (startTime <= afterStartTime)));

		// Stop the Office Building
		OfficeBuilding.main("stop");

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
		final int PORT = 13778;

		// Obtain the URL
		OfficeBuilding.main("url", HOST, String.valueOf(PORT));

		// Validate output URL
		String expectedUrl = OfficeBuildingManager
				.getOfficeBuildingJmxServiceUrl(HOST, PORT).toString()
				+ "\n";
		validateStreamContent("Incorrect URL", expectedUrl, this.stdout);
	}

	/**
	 * Ensure provides usage on no command.
	 */
	public void testUsageMessage() throws Throwable {

		// Attempt with no command
		try {
			OfficeBuilding.main();
			fail("Should not succeed as requires command");
		} catch (Error ex) {
			validateStreamContent("Should provide usage message",
					OfficeBuilding.USAGE_MESSAGE + "\n", this.stderr);
			assertEquals("Incorrect requires command cause", "Exit", ex
					.getMessage());
		}
	}

	/**
	 * Ensure provides usage on unknown command.
	 */
	public void testUnknownCommand() throws Throwable {

		// Attempt with unknown command
		try {
			OfficeBuilding.main("unknown");
			fail("Should not succeed as unknown command");
		} catch (Error ex) {
			String errorMessage = "ERROR: unknown command 'unknown'\n\n"
					+ OfficeBuilding.USAGE_MESSAGE + "\n";
			validateStreamContent("Should provide usage message", errorMessage,
					this.stderr);
			assertEquals("Incorrect unknown command cause", "Exit", ex
					.getMessage());
		}
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