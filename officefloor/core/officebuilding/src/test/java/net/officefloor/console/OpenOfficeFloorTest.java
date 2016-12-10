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
package net.officefloor.console;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Tests opening the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenOfficeFloorTest extends AbstractConsoleMainTestCase {

	/**
	 * Initiate.
	 */
	public OpenOfficeFloorTest() {
		super(OpenOfficeFloor.class, false);
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} and invoke a {@link Task}.
	 */
	public void testOpenOfficeFloorAndInvokeTask() throws Throwable {

		final String PROCESS_NAME = this.getName();

		// Expected output
		List<String> out = new LinkedList<String>();

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Run the OfficeFloor and invoke task
		String openCommand = "--process_name " + PROCESS_NAME + " --office OFFICE" + " --work SECTION.WORK"
				+ " --task writeMessage" + " --parameter " + tempFile.getAbsolutePath()
				+ " --officefloor net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
				+ " --property team.name=TEAM";
		this.doMain(openCommand);
		out.add("Opening OfficeFloor within process name space '" + PROCESS_NAME
				+ "' for work (office=OFFICE, work=SECTION.WORK, task=writeMessage, parameter="
				+ tempFile.getAbsolutePath() + ")");
		out.add("OfficeFloor within process name space '" + PROCESS_NAME + "' closed");

		// Ensure message written to file
		String fileContent = this.getFileContents(tempFile);
		assertEquals("Message should be written to file", MockWork.MESSAGE, fileContent);

		// Validate no error and correct output
		this.assertErr();
		this.assertOut(out.toArray(new String[out.size()]));
	}

	/**
	 * Ensure able to provide help.
	 */
	public void testHelp() throws Throwable {

		// Output help
		this.doMain("-h");

		// Validate no error and appropriate help message
		this.assertErr();
		this.assertOut("                                                                 ",
				"Opens an OfficeFloor                                             ",
				"                                                                 ",
				"usage: script [options]                                          ",
				"                                                                 ",
				"Options:                                                         ",
				" -cp,--classpath <arg>            Raw entry to include on the class path",
				" -h,--help                        This help message                     ",
				" -o,--office <arg>                Name of the Office                    ",
				" -of,--officefloor <arg>          Location of the OfficeFloor           ",
				" -ofs,--officefloorsource <arg>   OfficeFloorSource",
				" --parameter <arg>                Parameter for the Task                ",
				" --process_name <arg>             Process name space. Default is Process",
				" --property <arg>                 Property for the OfficeFloor in the form of name=value",
				" -t,--task <arg>                  Name of the Task                      ",
				" -w,--work <arg>                  Name of the Work                      ");
	}

}