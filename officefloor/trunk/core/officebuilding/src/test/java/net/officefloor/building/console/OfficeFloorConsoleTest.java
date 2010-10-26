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
import java.io.StringWriter;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.execute.MockCommand;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorConsole}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorConsoleTest extends OfficeFrameTestCase {

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
	private Reader consoleIn;

	/**
	 * Reads data from the {@link OfficeFloorConsole}.
	 */
	private BufferedReader out;

	/**
	 * {@link OfficeFloorConsole} out.
	 */
	private PrintStream consoleOut;

	/**
	 * Reads data from the {@link OfficeFloorConsole}.
	 */
	private BufferedReader err;

	/**
	 * {@link OfficeFloorConsole} err.
	 */
	private PrintStream consoleErr;

	@Override
	protected void setUp() throws Exception {

		// Record original streams
		this.stdout = System.out;
		this.stderr = System.err;

		// Console 'in'
		PipedWriter pipeIn = new PipedWriter();
		this.in = new PrintWriter(pipeIn);
		this.consoleIn = new PipedReader(pipeIn);

		// Console 'out'
		PipedInputStream pipeOut = new PipedInputStream();
		this.out = new BufferedReader(new InputStreamReader(pipeOut));
		this.consoleOut = new PrintStream(new PipedOutputStream(pipeOut));
		System.setOut(this.consoleOut);

		// Console 'err'
		PipedInputStream pipeErr = new PipedInputStream();
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
	 * Ensure output help information.
	 */
	public void testSingleCommandHelp() throws Exception {

		// Run help
		MockCommand command = this.createCommand("command", "alpha", "beta");
		command.setParameterAsFlag("beta");
		this.run("script --help", command);

		// Validate help message
		assertOut("                                             ",
				"command COMMAND                                ",
				"                                               ",
				"usage: script [options]                        ",
				"                                               ",
				"Options:                                       ",
				" -a,--alpha <arg>   parameter ALPHA            ",
				" -b,--beta          parameter BETA             ",
				" -h,--help          This help message          ");
	}

	/**
	 * Ensure output help information has options sorted.
	 */
	public void testSingleCommandHelpSorting() throws Exception {

		// Run help (with short option)
		MockCommand command = this.createCommand("command", "kilo", "beta",
				"gamma", "alpha");
		command.setParameterAsFlag("beta");
		this.run("script -h", command);

		// Validate help message
		assertOut("                                             ",
				"command COMMAND                                ",
				"                                               ",
				"usage: script [options]                        ",
				"                                               ",
				"Options:                                       ",
				" -a,--alpha <arg>   parameter ALPHA            ",
				" -b,--beta          parameter BETA             ",
				" -g,--gamma <arg>   parameter GAMMA            ",
				" -h,--help          This help message          ",
				" -k,--kilo <arg>    parameter KILO             ");
	}

	/**
	 * Ensure output help information for multiple commands.
	 */
	public void testMultipleCommandHelp() throws Exception {

		// Run help
		MockCommand one = this.createCommand("one", "alpha", "beta", "gamma");
		one.setParameterAsFlag("beta");
		MockCommand two = this.createCommand("two");
		MockCommand three = this.createCommand("three", "delta", "epsilon");
		three.setParameterAsFlag("delta");
		this.run("script help", one, two, three);

		// Validate help message
		assertOut("                                             ",
				"usage: script [options] <commands>             ",
				"                                               ",
				"Commands:                                      ",
				"                                               ",
				"one : command ONE                              ",
				"    Options:                                   ",
				"     -a,--alpha <arg>   parameter ALPHA        ",
				"     -b,--beta          parameter BETA         ",
				"     -g,--gamma <arg>   parameter GAMMA        ",
				"                                               ",
				"two : command TWO                              ",
				"                                               ",
				"three : command THREE                          ",
				"      Options:                                 ",
				"       -d,--delta           parameter DELTA    ",
				"       -e,--epsilon <arg>   parameter EPSILON  ",
				"                                               ",
				"help : This help message                       ");
	}

	/**
	 * Runs the {@link OfficeFloorConsole} for the {@link MockCommand}
	 * instances.
	 * 
	 * @param commandLine
	 *            Command line.
	 * @param commandFactories
	 *            {@link MockCommand} instances.
	 */
	private void run(String commandLine, MockCommand... commandFactories)
			throws Exception {

		// Obtain the arguments from command line
		String[] commandLineSplit = commandLine.split("\\s+");

		// First is script, then arguments
		String scriptName = commandLineSplit[0];
		String[] arguments = new String[commandLineSplit.length - 1];
		System.arraycopy(commandLineSplit, 1, arguments, 0, arguments.length);

		// Obtain details of repositories
		File localRepositoryDirectory = OfficeBuildingTestUtil
				.getLocalRepositoryDirectory();
		String[] remoteRepositoryUrls = OfficeBuildingTestUtil
				.getRemoteRepositoryUrls();

		// Create the OfficeFloor console
		OfficeFloorConsole console = new OfficeFloorConsoleImpl(scriptName,
				commandFactories, localRepositoryDirectory,
				remoteRepositoryUrls, new OfficeFloorDecorator[0]);

		// Run from console
		console.run(this.consoleOut, this.consoleErr, arguments);
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
	private MockCommand createCommand(String commandName, String... parameters)
			throws IOException {
		return new MockCommand(commandName, new MockManagedProcess(), null,
				parameters);
	}

	/**
	 * Asserts <code>out</code>.
	 * 
	 * @param lines
	 *            Lines of output.
	 */
	private void assertOut(String... lines) throws IOException {

		// Create the expected output
		final String EOLN = System.getProperty("line.separator");
		StringBuilder expected = new StringBuilder();
		for (String line : lines) {

			// Remove right format spacing for test readability
			final String TRAILING_SPACE = " ";
			while (line.endsWith(TRAILING_SPACE)) {
				line = line.substring(0, (line.length() - TRAILING_SPACE
						.length()));
			}

			// Add expected line
			expected.append(line);
			expected.append(EOLN);
		}

		// Obtain the actual output
		this.consoleOut.close();
		StringWriter actual = new StringWriter();
		for (int value = this.out.read(); value != -1; value = this.out.read()) {
			actual.write(value);
		}

		// Validate output
		assertEquals("Incorrect output", expected.toString(), actual.toString());
	}

	/**
	 * Asserts the {@link MockCommand} is run.
	 * 
	 * @param command
	 *            {@link MockCommand} to assert is run.
	 */
	private static void assertRun(MockCommand command) throws IOException {
		MockManagedProcess process = (MockManagedProcess) command
				.getManagedProcess();
		process.assertRun();
	}

	/**
	 * Mock {@link ManagedProcess}.
	 */
	private static class MockManagedProcess implements ManagedProcess {

		/**
		 * Run {@link File} path.
		 */
		private String runFilePath;

		/**
		 * Initiate.
		 */
		public MockManagedProcess() throws IOException {
			this.runFilePath = File.createTempFile(
					OfficeFloorConsoleTest.class.getSimpleName(), "test")
					.getAbsolutePath();
		}

		/**
		 * <p>
		 * Asserts that this {@link ManagedProcess} was run.
		 * <p>
		 * As the {@link ManagedProcess} is serialised to the {@link Process} to
		 * run, inter {@link Process} communication is required to check if run
		 * (in this case files on the file system).
		 */
		public void assertRun() throws IOException {
			String fileContents = new OfficeFrameTestCase() {
			}.getFileContents(new File(this.runFilePath));
			assertEquals("Managed process not run", RUN_DATA, fileContents);
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
			// Write test content to run file
			FileWriter writer = new FileWriter(this.runFilePath);
			writer.write(RUN_DATA);
			writer.close();
		}
	}

}