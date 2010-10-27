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

import junit.framework.TestCase;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.execute.MockCommand;
import net.officefloor.building.util.OfficeBuildingTestUtil;

/**
 * Tests the {@link OfficeFloorConsole}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorConsoleTest extends AbstractConsoleTestCase {

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
	}

	/**
	 * Ensure can start {@link OfficeFloorCommand} within a spawned
	 * {@link Process}.
	 */
	public void testStartCommand() throws Exception {

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

}