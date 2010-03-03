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

package net.officefloor.building.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import net.officefloor.building.OfficeBuilding;
import net.officefloor.building.process.ProcessManager;

/**
 * Utility methods for testing the {@link OfficeBuilding} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTestUtil {

	/**
	 * Creates a temporary file.
	 * 
	 * @param testCase
	 *            {@link TestCase} requiring the temporary file.
	 * @return Temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public static File createTempFile(TestCase testCase) throws IOException {

		// Obtain the file
		File file = File.createTempFile(testCase.getClass().getSimpleName(),
				testCase.getName());

		// Return the file
		return file;
	}

	/**
	 * Validates the contents of the file.
	 * 
	 * @param message
	 *            Message if contents are invalid.
	 * @param expectedContent
	 *            Expected content of the file.
	 * @param file
	 *            File to validate its content.
	 * @throws IOException
	 *             If fails to validate content.
	 */
	public static void validateFileContent(String message,
			String expectedContent, File file) throws IOException {

		// Obtain the content from file
		StringBuilder content = new StringBuilder();
		FileReader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}

		// Ensure content in file
		TestCase.assertEquals("Content should be in file", expectedContent,
				content.toString());
	}

	/**
	 * Waits until the {@link Process} is complete (or times out).
	 */
	public static void waitUntilProcessComplete(ProcessManager manager)
			throws Exception {

		// Obtain the maximum run time
		final int MAX_RUN_TIME = 5000;

		// Wait until process completes (or times out)
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				manager.destroyProcess();
				TestCase.fail("Processing took too long");
			}

			// Wait some time for further processing
			Thread.sleep(100);
		}
	}

	/**
	 * All access via static methods.
	 */
	private OfficeBuildingTestUtil() {
	}
}