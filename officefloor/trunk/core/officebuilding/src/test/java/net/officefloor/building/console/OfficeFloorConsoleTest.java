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

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.execute.MockCommand;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessCompletionListener;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessStartListener;

/**
 * Tests the {@link OfficeFloorConsole}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorConsoleTest extends AbstractConsoleTestCase {

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment = new Properties();

	/**
	 * {@link ProcessManagerMBean} instances.
	 */
	private final List<ProcessManagerMBean> processManagers = new LinkedList<ProcessManagerMBean>();

	/**
	 * Completed {@link ProcessManagerMBean}.
	 */
	private final List<ProcessManagerMBean> completed = new LinkedList<ProcessManagerMBean>();

	/**
	 * {@link ProcessStartListener} for testing.
	 */
	private ProcessStartListener processStartListener = new ProcessStartListener() {
		@Override
		public void processStarted(ProcessManagerMBean processManager) {
			synchronized (OfficeFloorConsoleTest.this) {
				OfficeFloorConsoleTest.this.processManagers.add(processManager);
			}
		}
	};

	/**
	 * {@link ProcessCompletionListener} for testing.
	 */
	private ProcessCompletionListener processCompletionListener = new ProcessCompletionListener() {
		@Override
		public void processCompleted(ProcessManagerMBean manager) {
			synchronized (OfficeFloorConsoleTest.this) {
				OfficeFloorConsoleTest.this.completed.add(manager);
			}
		}
	};

	/**
	 * Ensure output help information.
	 */
	public void testSingleCommandHelp() throws Exception {

		// Run help
		MockCommand command = this.createCommand("command", "alpha", "beta");
		command.setParameterAsFlag("beta");
		this.run("script --help", command);

		// Validate help message
		assertOut("                                             ", "command COMMAND                                ",
				"                                               ", "usage: script [options]                        ",
				"                                               ", "Options:                                       ",
				" -a,--alpha <arg>   parameter ALPHA            ", " -b,--beta          parameter BETA             ",
				" -h,--help          This help message          ");
		assertErr();
	}

	/**
	 * Ensure output help information has options sorted.
	 */
	public void testSingleCommandHelpSorting() throws Exception {

		// Run help (with short option)
		MockCommand command = this.createCommand("command", "kilo", "beta", "gamma", "alpha");
		command.setParameterAsFlag("beta");
		this.run("script -h", command);

		// Validate help message
		assertOut("                                             ", "command COMMAND                                ",
				"                                               ", "usage: script [options]                        ",
				"                                               ", "Options:                                       ",
				" -a,--alpha <arg>   parameter ALPHA            ", " -b,--beta          parameter BETA             ",
				" -g,--gamma <arg>   parameter GAMMA            ", " -h,--help          This help message          ",
				" -k,--kilo <arg>    parameter KILO             ");
		assertErr();
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
		assertOut("                                             ", "usage: script [options] <commands>             ",
				"                                               ", "Commands:                                      ",
				"                                               ", "one : command ONE                              ",
				"    Options:                                   ", "     -a,--alpha <arg>   parameter ALPHA        ",
				"     -b,--beta          parameter BETA         ", "     -g,--gamma <arg>   parameter GAMMA        ",
				"                                               ", "two : command TWO                              ",
				"                                               ", "three : command THREE                          ",
				"      Options:                                 ", "       -d,--delta           parameter DELTA    ",
				"       -e,--epsilon <arg>   parameter EPSILON  ", "                                               ",
				"help : This help message                       ");
		assertErr();
	}

	/**
	 * Ensure can run {@link OfficeFloorCommand}.
	 */
	public void testRunCommand() throws Exception {

		// Run the command (within this process)
		MockCommand command = this.createCommand("command");
		this.run("", command);

		// Ensure run
		assertTrue("Process should be run", isRun(command));

		// Ensure notified of runs
		synchronized (this) {
			assertEquals("Should be notified of start", 1, this.processManagers.size());
			assertEquals("Should notified of completion", 1, this.completed.size());
			assertEquals("Incorrect process manager", "command", this.processManagers.get(0).getProcessName());
			assertEquals("Ensure same process", this.processManagers.get(0), this.completed.get(0));
		}
	}

	/**
	 * Ensure can load {@link OfficeFloorCommandParameter} from environment.
	 */
	public void testLoadCommandParameterFromEnvironment() throws Exception {

		final String NAME = "env-name";
		final String VALUE = "env-value";

		// Setup environment property
		this.environment.put(NAME, VALUE);

		// Run the command (within this process)
		MockCommand command = this.createCommand("command", NAME);
		this.run("", command);

		// Ensure run
		assertTrue("Process should be run", isRun(command));

		// Ensure parameter value loaded
		assertEquals("Parameter not loaded from environment", VALUE, command.getParameterValues().get(NAME));
	}

	/**
	 * Ensure can start {@link OfficeFloorCommand} within a spawned
	 * {@link Process} and notified of its start.
	 */
	public void testStartCommandWithListener() throws Exception {

		// Start
		this.doStartCommand();

		synchronized (this) {
			// Ensure only notified of start of process
			assertEquals("Should have process manager registered", 1, this.processManagers.size());
			ProcessManagerMBean processManager = this.processManagers.get(0);
			assertEquals("Incorrect process manager", "command", processManager.getProcessName());

			// Ensure notified of completion of process.
			// Need to wait on process completion to check this.
			long endTime = System.currentTimeMillis() + 10000;
			while (this.completed.size() == 0) {
				// Wait some time for completion
				this.wait(100);

				// Determine if timed out waiting for complete
				assertTrue("Timed out waiting for completion", (System.currentTimeMillis() < endTime));
			}
		}

	}

	/**
	 * Ensure can start {@link OfficeFloorCommand} within a spawned
	 * {@link Process} and ignore starts.
	 */
	public void testStartCommandWithoutListener() throws Exception {

		// Clear the listeners
		this.processStartListener = null;
		this.processCompletionListener = null;

		// Start
		this.doStartCommand();

		// Ensure test valid by no process manager registered
		synchronized (this) {
			assertEquals("Should not have started process notification", 0, this.processManagers.size());
			assertEquals("Should not have completed process notification", 0, this.completed.size());
		}
	}

	/**
	 * Undertakes starting a {@link ManagedProcess}.
	 */
	private void doStartCommand() throws Exception {

		final File waitFile = File.createTempFile(this.getName(), ".wait");
		final File resultFile = File.createTempFile(this.getName(), ".result");

		// Run the command (within spawned process and JVM option)
		MockCommand command = this.createCommand("command");
		command.setSpawn(true);
		setWaitFile(command, waitFile);
		setSystemPropertyCheck(command, "test-jvm-option", resultFile);
		command.addJvmOption("-Dtest-jvm-option=available");
		this.run("", command);

		// Process should not be run
		assertFalse("Process should not be run", isRun(command));

		// Allow some time and should still be waiting on file
		Thread.sleep(100);
		assertFalse("Process should still not be run after some time", isRun(command));

		// Flag for process to run
		FileWriter writer = new FileWriter(waitFile);
		writer.write("run");
		writer.close();

		// Process should now run
		long startTime = System.currentTimeMillis();
		while (!isRun(command)) {
			// Allow some time for waiting
			Thread.sleep(10);
			TestCase.assertTrue("Timed out waiting", ((System.currentTimeMillis() - startTime) < 5000));
		}

		// Ensure JVM option made available
		String jvmOptionValue = this.getFileContents(resultFile);
		assertEquals("Should have JVM option system property value", "available", jvmOptionValue);
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
	private void run(String commandLine, MockCommand... commandFactories) throws Exception {

		// Obtain the arguments from command line
		String[] commandLineSplit = commandLine.split("\\s+");

		// First is script, then arguments
		String scriptName = commandLineSplit[0];
		String[] arguments = new String[commandLineSplit.length - 1];
		System.arraycopy(commandLineSplit, 1, arguments, 0, arguments.length);

		// Create the OfficeFloor console
		OfficeFloorConsole console = new OfficeFloorConsoleImpl(scriptName, commandFactories, this.environment,
				new OfficeFloorDecorator[0]);

		// Run from console
		console.run(this.consoleOut, this.consoleErr, this.processStartListener, this.processCompletionListener,
				arguments);
	}

}