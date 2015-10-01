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
package net.officefloor.building.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.execute.MockCommand;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.building.util.FurtherDetails;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Provides abstract functionality for {@link OfficeFloorConsole} testing.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractConsoleTestCase extends OfficeFrameTestCase implements
		FurtherDetails {

	/**
	 * Data written to file to confirm the {@link ManagedProcess} is run.
	 */
	private static final String RUN_DATA = "TEST";

	/**
	 * Original stdout.
	 */
	private PrintStream stdout;

	/**
	 * Original stderr.
	 */
	private PrintStream stderr;

	/**
	 * Sends data in to the {@link OfficeFloorConsole}.
	 */
	private PrintWriter in;

	/**
	 * {@link OfficeFloorConsole} in.
	 */
	protected Reader consoleIn;

	/**
	 * Reads data from the {@link OfficeFloorConsole}.
	 */
	private BufferedReader out;

	/**
	 * {@link OfficeFloorConsole} out.
	 */
	protected PrintStream consoleOut;

	/**
	 * Reads data from the {@link OfficeFloorConsole}.
	 */
	private BufferedReader err;

	/**
	 * {@link OfficeFloorConsole} err.
	 */
	protected PrintStream consoleErr;

	@Override
	protected void setUp() throws Exception {

		// Record original streams
		this.stdout = System.out;
		this.stderr = System.err;

		// Console 'in'
		PipedWriter pipeIn = new PipedWriter();
		this.in = new PrintWriter(pipeIn);
		this.consoleIn = new PipedReader(pipeIn);

		// Console 'out' (make large buffer to stop blocking)
		PipedInputStream pipeOut = new PipedInputStream(1024 * 1024);
		this.out = new BufferedReader(new InputStreamReader(pipeOut));
		this.consoleOut = new PrintStream(new PipedOutputStream(pipeOut));
		System.setOut(this.consoleOut);

		// Console 'err' (make large buffer to stop blocking)
		PipedInputStream pipeErr = new PipedInputStream(1024 * 1024);
		this.err = new BufferedReader(new InputStreamReader(pipeErr));
		this.consoleErr = new PrintStream(new PipedOutputStream(pipeErr));
		System.setErr(this.consoleErr);
	}

	@Override
	protected void tearDown() throws Exception {
		// Reset state by reseting to original streams
		System.setOut(this.stdout);
		System.setErr(this.stderr);
	}

	/**
	 * Ensure the pipes working correctly for testing of the
	 * {@link OfficeFloorConsole}.
	 */
	public void testPiping() throws IOException {

		// Validate in pipe
		this.in.println("IN");
		assertEquals("IN", new BufferedReader(this.consoleIn).readLine());

		// Validate out pipe
		this.consoleOut.println("OUT");
		assertEquals("OUT", this.out.readLine());

		// Validate stdout pipe
		System.out.println("STDOUT");
		assertEquals("STDOUT", this.out.readLine());

		// Validate err pipe
		this.consoleErr.println("ERR");
		assertEquals("ERR", this.err.readLine());

		// Validate stderr pipe
		System.err.println("STDERR");
		assertEquals("STDERR", this.err.readLine());
	}

	/**
	 * Creates the {@link MockCommand}.
	 * 
	 * @param commandName
	 *            {@link OfficeFloorCommand} name.
	 * @param parameters
	 *            {@link OfficeFloorCommandParameter} names.
	 * @return {@link MockCommand} as {@link OfficeFloorCommandFactory}.
	 */
	protected MockCommand createCommand(String commandName,
			String... parameters) throws IOException {
		return new MockCommand(commandName, new MockManagedProcess(), null,
				parameters);
	}

	/**
	 * Specifies the wait file for the {@link MockCommand}.
	 * 
	 * @param command
	 *            {@link MockCommand}.
	 * @param waitFile
	 *            Wait file.
	 */
	protected static void setWaitFile(MockCommand command, File waitFile) {
		MockManagedProcess process = (MockManagedProcess) command
				.getManagedProcess();
		process.setWaitFile(waitFile);
	}

	/**
	 * <P>
	 * Specifies to output a particular System property to the result file.
	 * <p>
	 * Allows for testing JVM option.
	 * 
	 * @param command
	 *            {@link MockCommand}.
	 * @param systemPropertyName
	 *            Name of the system property.
	 * @param resultFile
	 *            Result file.
	 */
	protected static void setSystemPropertyCheck(MockCommand command,
			String systemPropertyName, File resultFile) {
		MockManagedProcess process = (MockManagedProcess) command
				.getManagedProcess();
		process.setSystemPropertyCheck(systemPropertyName, resultFile);
	}

	/**
	 * Indicates if the {@link MockCommand} is run.
	 * 
	 * @param command
	 *            {@link MockCommand} to assert is run.
	 * @return <code>true</code> if is run.
	 */
	protected static boolean isRun(MockCommand command) throws IOException {
		MockManagedProcess process = (MockManagedProcess) command
				.getManagedProcess();
		return process.isRun();
	}

	/**
	 * Mock {@link ManagedProcess}.
	 */
	private static class MockManagedProcess implements ManagedProcess {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Run {@link File} path.
		 */
		private String runFilePath;

		/**
		 * Fail to wait on (if specified).
		 */
		private String waitFilePath = null;

		/**
		 * Name of the System property to check (if specified).
		 */
		private String systemPropertyName = null;

		/**
		 * Name of file to write the result of the System property check.
		 */
		private String systemPropertyValueResultFilePath = null;

		/**
		 * Initiate.
		 */
		public MockManagedProcess() throws IOException {
			this.runFilePath = File.createTempFile(
					OfficeFloorConsoleTest.class.getSimpleName(), "test")
					.getAbsolutePath();
		}

		/**
		 * Specifies the file to wait on.
		 * 
		 * @param waitFile
		 *            File to wait on.
		 */
		public void setWaitFile(File waitFile) {
			this.waitFilePath = waitFile.getAbsolutePath();
		}

		/**
		 * Specifies to output a particular System property to the result file.
		 * 
		 * @param systemPropertyName
		 *            Name of the system property.
		 * @param resultFile
		 *            Result file.
		 */
		public void setSystemPropertyCheck(String systemPropertyName,
				File resultFile) {
			this.systemPropertyName = systemPropertyName;
			this.systemPropertyValueResultFilePath = resultFile
					.getAbsolutePath();
		}

		/**
		 * <p>
		 * Indicates if this {@link ManagedProcess} was run.
		 * <p>
		 * As the {@link ManagedProcess} is serialised to the {@link Process} to
		 * run, inter {@link Process} communication is required to check if run
		 * (in this case files on the file system).
		 * 
		 * @return <code>true</code> if the {@link ManagedProcess} was run.
		 */
		public boolean isRun() throws IOException {
			String fileContents = new OfficeFrameTestCase() {
			}.getFileContents(new File(this.runFilePath));
			return (RUN_DATA.equals(fileContents));
		}

		/*
		 * =================== ManagedProcess ==========================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Wait on file if wait file specified
			if (this.waitFilePath != null) {

				// Ensure the wait file exists
				assertTrue("Wait file exists",
						new File(this.waitFilePath).exists());

				// Wait for content in the wait file
				long startTime = System.currentTimeMillis();
				boolean isFinished = false;
				while (!isFinished) {

					// Allow some time for waiting
					Thread.sleep(10);
					TestCase.assertTrue("Timed out waiting",
							((System.currentTimeMillis() - startTime) < 5000));

					// Check if content to indicate finished waiting
					if (new OfficeFrameTestCase() {
					}.getFileContents(new File(this.waitFilePath)).length() > 0) {
						// Contents in file so finished waiting
						isFinished = true;
					}
				}
			}

			// Write the system property if required
			if (this.systemPropertyName != null) {

				// Obtain the system property value
				String systemPropertyValue = System.getProperty(
						this.systemPropertyName, "");

				// Write system property value to the result file
				FileWriter writer = new FileWriter(
						this.systemPropertyValueResultFilePath);
				writer.write(systemPropertyValue);
				writer.close();
			}

			// Write test content to run file
			FileWriter writer = new FileWriter(this.runFilePath);
			writer.write(RUN_DATA);
			writer.close();
		}
	}

	/**
	 * Asserts <code>out</code>.
	 * 
	 * @param lines
	 *            Lines of expected output.
	 */
	protected void assertOut(String... lines) throws IOException {
		this.assertPipeContent(this.consoleOut, this.out, lines);
	}

	/**
	 * Obtains the <code>out</code>.
	 * 
	 * @return Output content.
	 */
	protected String getOut() throws IOException {
		return this.getPipeContent(this.consoleOut, this.out);
	}

	/**
	 * Asserts <code>err</code>.
	 * 
	 * @param lines
	 *            Lines of expected output.
	 */
	protected void assertErr(String... lines) throws IOException {
		this.assertPipeContent(this.consoleErr, this.err, lines);
	}

	/**
	 * Asserts <code>err</code>.
	 * 
	 * @param console
	 *            {@link PrintStream}.
	 * @param pipe
	 *            Corresponding pipe for {@link PrintStream}.
	 * @param lines
	 *            Lines of expected output.
	 */
	private void assertPipeContent(PrintStream console, BufferedReader pipe,
			String[] lines) throws IOException {

		// Create the expected output
		final String EOLN = System.getProperty("line.separator");
		StringBuilder expected = new StringBuilder();
		for (String line : lines) {

			// Remove right format spacing for test readability
			final String TRAILING_SPACE = " ";
			while (line.endsWith(TRAILING_SPACE)) {
				line = line.substring(0,
						(line.length() - TRAILING_SPACE.length()));
			}

			// Add expected line
			expected.append(line);
			expected.append(EOLN);
		}

		// Obtain the actual output
		String actualOutput = this.getPipeContent(console, pipe);

		// Ignore any log lines
		StringBuilder actual = new StringBuilder();
		if (!"".equals(actualOutput)) {
			for (String line : actualOutput.split(EOLN)) {

				// Ignore log lines
				if (line.startsWith("SLF4J:")) {
					continue;
				}

				// Append the actual line
				actual.append(line);
				actual.append(EOLN);
			}
		}

		// Validate output
		assertEquals("Incorrect output", expected.toString(), actual.toString());
	}

	/**
	 * Obtains the Pipe content.
	 * 
	 * @param console
	 *            {@link PrintStream}.
	 * @param pipe
	 *            Corresponding pipe for {@link PrintStream}.
	 * @return Pipe content.
	 */
	private String getPipeContent(PrintStream console, BufferedReader pipe)
			throws IOException {

		// Close console to enable end of stream
		console.close();

		// Obtain the content
		StringWriter content = new StringWriter();
		for (int value = pipe.read(); value != -1; value = pipe.read()) {
			content.write(value);
		}

		// Return the content
		return content.toString();
	}

	/*
	 * ======================= FurtherDetails ==============================
	 */

	@Override
	public String getMessage() {
		try {
			// Obtain the message
			StringBuilder msg = new StringBuilder();
			msg.append("\nSTDOUT:\n");
			msg.append(this.getPipeContent(this.consoleOut, this.out));
			msg.append("\nSTDERR:\n");
			msg.append(this.getPipeContent(this.consoleErr, this.err));

			// Return the message
			return msg.toString();

		} catch (IOException ex) {
			throw fail(ex);
		}
	}

}