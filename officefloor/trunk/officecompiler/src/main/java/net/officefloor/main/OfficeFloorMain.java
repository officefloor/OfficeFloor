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

package net.officefloor.main;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;

/**
 * Main class to compile and run an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMain {

	/**
	 * <p>
	 * Main method to compile and run an {@link OfficeFloor}.
	 * <p>
	 * Arguments:
	 * <ol>
	 * <li>Office Floor configuration file</li>
	 * </ol>
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String... args) {

		// Ensure have command line arguments
		if (args.length == 0) {
			System.err.println("USAGE: java ... "
					+ OfficeFloorMain.class.getName()
					+ " <office floor location> [<office> <work>]");
			return;
		}

		// Obtain the location of the office floor
		String officeFloorLocation = args[0];

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.addSystemProperties();

		// Compile the office floor
		System.out.println("Compiling office floor " + officeFloorLocation
				+ " ...");
		OfficeFloor officeFloor = compiler.compile(officeFloorLocation);
		if (officeFloor == null) {
			System.err.println("ERROR: Failed to compile office floor.");
			System.exit(1);
		}

		// Open the office floor
		System.out.println("Opening office floor ...");
		try {
			officeFloor.openOfficeFloor();
		} catch (Throwable ex) {
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			System.err.println("ERROR: Failed to open office floor.\n"
					+ stackTrace.toString());
			System.exit(1);
		}

		// Determine if invoke work
		if (args.length >= 3) {
			// Obtain office and work for invoking
			String officeName = args[1];
			String workName = args[2];
			System.out.println("Invoking work '" + workName + "' on office '"
					+ officeName + "'");

			// Obtain the Office
			Office office = officeFloor.getOffice(officeName);
			if (office == null) {
				System.err.println("ERROR: No office by name '" + officeName
						+ "'");
				System.exit(1);
			}

			// Obtain the Work
			WorkManager workManager;
			try {
				workManager = office.getWorkManager(workName);
			} catch (UnknownWorkException ex) {
				System.err.println("ERROR: No work by name '" + workName
						+ "' on office " + officeName);
				System.exit(1);
				return; // required for compiling
			}

			// Invoke the Work
			String parameter = (args.length > 4 ? args[3] : null);
			try {
				workManager.invokeWork(parameter);
			} catch (NoInitialTaskException ex) {
				System.err.println("ERROR: No initial task on work " + workName
						+ " of office " + officeName);
				System.exit(1);
			}
		}
	}

}