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
package net.officefloor.building.process;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

import junit.framework.TestCase;

/**
 * Tests the management of a {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessManagerTest extends TestCase {

	/**
	 * Ensure able to invoke a {@link Process}.
	 */
	public void testInvokeProcess() throws Exception {

		final long MAX_RUN_TIME = 1000;
		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = File.createTempFile(this.getClass().getSimpleName(), "txt");

		// Create the process manager
		ProcessManager manager = new ProcessManager(new WriteToFileProcess(file
				.getAbsolutePath(), TEST_CONTENT));
		manager.startProcess();
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				fail("Processing took too long");
			}

			// Wait some time for further processing
			Thread.sleep(100);
		}

		// Obtain the content from file
		StringBuilder content = new StringBuilder();
		FileReader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}

		// Ensure content in file
		assertEquals("Content should be in file", TEST_CONTENT, content
				.toString());
	}

	/**
	 * {@link ManagedProcess} to write to a file.
	 */
	public static class WriteToFileProcess implements ManagedProcess {

		/**
		 * Path to the {@link File}.
		 */
		private final String filePath;

		/**
		 * Content to write to the {@link File}.
		 */
		private final String content;

		/**
		 * Initiate.
		 * 
		 * @param filePath
		 *            Path to the {@link File}.
		 * @param content
		 *            Content to write to the {@link File}.
		 */
		public WriteToFileProcess(String filePath, String content) {
			this.filePath = filePath;
			this.content = content;
		}

		/*
		 * =================== ManagedProcess ==============================
		 */

		@Override
		public void run(ManagedProcessContext context) throws Throwable {

			// Wait some time to ensure wait for process to complete
			Thread.sleep(100);

			// Obtain the file
			File file = new File(this.filePath);

			// Write the content to the file
			Writer writer = new FileWriter(file);
			writer.write(this.content);
			writer.close();
		}
	}

}