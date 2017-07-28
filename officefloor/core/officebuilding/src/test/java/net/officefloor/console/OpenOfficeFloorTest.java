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
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

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
	 * Ensure able to open the {@link OfficeFloor} and invoke a
	 * {@link ManagedFunction}.
	 */
	public void testOpenOfficeFloorAndInvokeFunction() throws Throwable {

		final String PROCESS_NAME = this.getName();

		// Expected output
		List<String> out = new LinkedList<String>();

		// File
		File tempFile = File.createTempFile(this.getName(), "txt");

		// Run the OfficeFloor and invoke function
		String openCommand = "--officefloor_name " + PROCESS_NAME + " --office OFFICE"
				+ " --function SECTION.writeMessage" + " --parameter " + tempFile.getAbsolutePath()
				+ " --officefloorsource " + OfficeFloorModelOfficeFloorSource.class.getName()
				+ " --location net/officefloor/building/process/officefloor/TestOfficeFloor.officefloor"
				+ " --property team.name=TEAM";
		this.doMain(openCommand);
		out.add("Opening OfficeFloor '" + PROCESS_NAME
				+ "' for function (office=OFFICE, function=SECTION.writeMessage, parameter="
				+ tempFile.getAbsolutePath() + ")");
		out.add("OfficeFloor '" + PROCESS_NAME + "' closed");

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
				" -f,--function <arg>              Name of the Function                  ",
				" -h,--help                        This help message                     ",
				" -l,--location <arg>              Location of the OfficeFloor           ",
				" -n,--officefloor_name <arg>      OfficeFloor name. Default is OfficeFloor",
				" -o,--office <arg>                Name of the Office                    ",
				" -ofs,--officefloorsource <arg>   OfficeFloorSource                     ",
				" --parameter <arg>                Parameter for the Function            ",
				" --property <arg>                 Property for the OfficeFloor in the form of name=value");
	}

}