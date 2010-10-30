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

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.building.command.LocalRepositoryOfficeFloorCommandParameter;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.RemoteRepositoryUrlsOfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.execute.MockCommand;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessManagerMBean;

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
		assertErr();
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

		// Should be no processes started on run
		assertEquals("Should be no processes started on run", 0,
				this.processManagers.size());
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
		assertEquals("Parameter not loaded from environment", VALUE, command
				.getParameterValues().get(NAME));
	}

	/**
	 * Ensure extracts local and remote repositories from the
	 * {@link OfficeFloorCommand}.
	 */
	public void testExtractLocalAndRemoteRepositories() throws Exception {

		// Create the mocks
		LocalRepositoryOfficeFloorCommandParameter localRepository = this
				.createMock(LocalRepositoryOfficeFloorCommandParameter.class);
		RemoteRepositoryUrlsOfficeFloorCommandParameter remoteRepositoryUrls = this
				.createMock(RemoteRepositoryUrlsOfficeFloorCommandParameter.class);

		// Create command with local and remote repository parameters
		MockCommand command = this.createCommand("command");
		command.addParameter(localRepository);
		command.addParameter(remoteRepositoryUrls);

		// Recording initialising parser
		this.recordReturn(localRepository, localRepository.getName(),
				"local-repository");
		this
				.recordReturn(localRepository, localRepository.getShortName(),
						null);
		this.recordReturn(localRepository, localRepository.isRequireValue(),
				true);
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls.getName(),
				"remote-repository-urls");
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls
				.getShortName(), null);
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls
				.isRequireValue(), true);

		// Record parsing
		this.recordReturn(localRepository, localRepository.getName(),
				"local-repository");
		this
				.recordReturn(localRepository, localRepository.getShortName(),
						null);
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls.getName(),
				"remote-repository-urls");
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls
				.getShortName(), null);

		// Record obtaining both local and remote repositories
		this.recordReturn(localRepository,
				localRepository.getLocalRepository(), new File("."));
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls
				.getRemoteRepositoryUrls(), new String[0]);

		// Record environment loading onto parameters
		this.recordReturn(localRepository, localRepository.getName(),
				"local-repository");
		this.recordReturn(remoteRepositoryUrls, remoteRepositoryUrls.getName(),
				"remote-repository-urls");

		// Test
		this.replayMockObjects();

		// Run command
		this.run("", command);

		// Verify functionality
		this.verifyMockObjects();

		// Ensure run
		assertTrue("Process should be run", isRun(command));
	}

	/**
	 * Ensure can start {@link OfficeFloorCommand} within a spawned
	 * {@link Process} and notified of its start.
	 */
	public void testStartCommandWithListener() throws Exception {

		// Start
		this.doStartCommand();

		// Ensure registered the process manager
		assertEquals("Should have process manager registered", 1,
				this.processManagers.size());
		ProcessManagerMBean processManager = this.processManagers.get(0);
		assertEquals("Incorrect process manager", "command", processManager
				.getProcessName());
	}

	/**
	 * Ensure can start {@link OfficeFloorCommand} within a spawned
	 * {@link Process} and ignore starts.
	 */
	public void testStartCommandWithoutListener() throws Exception {

		// Clear the listener
		this.processStartListener = null;

		// Start
		this.doStartCommand();

		// Ensure test valid by no process manager registered
		assertEquals("Should have process manager registered", 0,
				this.processManagers.size());
	}

	/**
	 * Undertakes starting a {@link ManagedProcess}.
	 */
	private void doStartCommand() throws Exception {

		final File waitFile = File.createTempFile(OfficeFloorConsoleTest.class
				.getSimpleName(), "wait");

		// Run the command (within spawned process)
		MockCommand command = this.createCommand("command");
		command.setSpawn(true);
		setWaitFile(command, waitFile);
		this.run("", command);

		// Process should not be run
		assertFalse("Process should not be run", isRun(command));

		// Allow some time and should still be waiting on file
		Thread.sleep(100);
		assertFalse("Process should still not be run after some time",
				isRun(command));

		// Flag for process to run
		FileWriter writer = new FileWriter(waitFile);
		writer.write("run");
		writer.close();

		// Process should now run
		long startTime = System.currentTimeMillis();
		while (!isRun(command)) {
			// Allow some time for waiting
			Thread.sleep(10);
			TestCase.assertTrue("Timed out waiting", ((System
					.currentTimeMillis() - startTime) < 5000));
		}
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

		// Create the OfficeFloor console
		OfficeFloorConsole console = new OfficeFloorConsoleImpl(scriptName,
				commandFactories, this.environment, new OfficeFloorDecorator[0]);

		// Run from console
		console.run(this.consoleOut, this.consoleErr,
				this.processStartListener, arguments);
	}

}