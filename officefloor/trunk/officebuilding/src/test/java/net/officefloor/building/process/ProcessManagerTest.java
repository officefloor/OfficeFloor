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
	 * {@link ProcessManager}.
	 */
	private ProcessManager manager;

	/**
	 * Maximum run time for tests.
	 */
	private static final long MAX_RUN_TIME = 1000;

	@Override
	protected void tearDown() throws Exception {
		// Ensure process is stopped
		this.manager.triggerStopProcess();
		this.waitUntilProcessComplete(this.manager);
	}

	/**
	 * Ensure able to start a {@link Process}.
	 */
	public void testStartProcess() throws Exception {

		final String TEST_CONTENT = "test content";

		// Obtain temporary file to write content
		File file = File.createTempFile(this.getClass().getSimpleName(), "txt");

		// Start the process
		this.manager = ProcessManager.startProcess(new WriteToFileProcess(file
				.getAbsolutePath(), TEST_CONTENT));

		// Wait until process writes content to file
		this.waitUntilProcessComplete(this.manager);

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

		@Override
		public Object doCommand(Object command) throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

	/**
	 * Ensure able to stop the {@link Process}.
	 */
	public void testStopProcess() throws Exception {

		// Start the process
		this.manager = ProcessManager.startProcess(new LoopUntilStopProcess());

		// Flag to stop the process
		this.manager.triggerStopProcess();

		// Wait until process completes
		this.waitUntilProcessComplete(this.manager);
	}

	/**
	 * {@link ManagedProcess} to loop until informed to stop.
	 */
	public static class LoopUntilStopProcess implements ManagedProcess {

		@Override
		public void run(ManagedProcessContext context) throws Throwable {
			// Loop until informed to stop
			for (;;) {
				if (context.continueProcessing()) {
					// Wait a little more until told to stop
					Thread.sleep(100);
				} else {
					// Informed to stop
					return;
				}
			}
		}

		@Override
		public Object doCommand(Object command) throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

	/**
	 * Ensure able to do a command by callback.
	 */
	public void testAsynchronousDoCommand() throws Exception {

		// Start the process
		this.manager = ProcessManager
				.startProcess(new IncrementNumberProcess());

		// Does a command on the process
		final Object[] result = new Object[1];
		this.manager.doCommand(new Integer(1), new CommandCallback() {

			@Override
			public void complete(Object response) {
				assertNotNull("Expecting response", response);
				synchronized (result) {
					result[0] = response;
				}
			}

			@Override
			public void failed(Throwable failure) {
				synchronized (result) {
					result[0] = failure;
				}
			}
		});

		// Wait until have result or timed out
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		for (;;) {

			// Determine if have result
			Object value;
			synchronized (result) {
				value = result[0];
			}

			// Handle if have result
			if (value != null) {
				Integer intValue = (Integer) value;
				assertEquals("Incorrect response", 2, intValue.intValue());
				return; // test successful
			}

			// No result, determine if still wait for result
			if (System.currentTimeMillis() > maxFinishTime) {
				fail("Processing took too long");
			}

			// Wait some time for further processing
			Thread.sleep(100);
		}
	}

	/**
	 * Ensure able to do a command by synchronous method.
	 */
	public void testSynchronousDoCommand() throws ProcessException {

		// Start the process
		this.manager = ProcessManager
				.startProcess(new IncrementNumberProcess());

		// Does a command on the process
		Object response = this.manager.doCommand(new Integer(1), 1000);
		Integer responseValue = (Integer) response;

		// Ensure value incremented
		assertEquals("Incorrect response", 2, responseValue.intValue());
	}

	/**
	 * {@link ManagedProcess} to provide command to increment a number.
	 */
	public static class IncrementNumberProcess extends LoopUntilStopProcess {
		@Override
		public Object doCommand(Object command) throws Throwable {

			// Obtain the integer
			Integer value = (Integer) command;

			// Return the value incremented by one
			return new Integer(value.intValue() + 1);
		}
	}

	/**
	 * Ensure times out command.
	 */
	public void testDoCommandTimeout() throws Exception {

		// Start the process
		this.manager = ProcessManager.startProcess(new TimeoutCommandProcess());

		// Do a command and validates it times out
		try {
			this.manager.doCommand("TEST", 100);
			fail("Should time out");
		} catch (ProcessException ex) {
			assertEquals("Should be timed out", "Command timed out", ex
					.getMessage());
		}
	}

	/**
	 * {@link ManagedProcess} to test timing out a command.
	 */
	public static class TimeoutCommandProcess extends LoopUntilStopProcess {

		/**
		 * {@link ManagedProcessContext}.
		 */
		private ManagedProcessContext context;

		/*
		 * ================ ManagedProcess =======================
		 */

		@Override
		public void run(ManagedProcessContext context) throws Throwable {
			// Store context
			synchronized (this) {
				this.context = context;
			}

			// Loop until stop process
			super.run(context);
		}

		@Override
		public Object doCommand(Object command) throws Throwable {
			// Loop until complete
			for (;;) {

				// Determine if complete
				synchronized (this) {
					if (this.context != null) {
						if (!this.context.continueProcessing()) {
							return null; // process complete
						}
					}
				}

				// Sleep some time
				Thread.sleep(100);
			}
		}
	}

	/**
	 * Waits until the {@link Process} is complete (or times out).
	 */
	private void waitUntilProcessComplete(ProcessManager manager)
			throws Exception {
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				fail("Processing took too long");
			}

			// Wait some time for further processing
			Thread.sleep(100);
		}
	}

}