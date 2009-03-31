/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.main;

import java.io.IOException;

import net.officefloor.compile.LoaderContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.classloader.ClassLoaderConfigurationContext;

/**
 * Starting point to compile and run an {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorMain {

	/**
	 * Main class name.
	 */
	private static final String OFFICE_FLOOR_MAIN = OfficeFloorMain.class
			.getName();

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
	public static void main(String... args) throws Exception {

		// Ensure have command line arguments
		if (args.length == 0) {
			System.err.println("USAGE: java ... " + OFFICE_FLOOR_MAIN
					+ " <office floor configuration file> [<office> <work>]");
			return;
		}

		// Obtain the command line arguments
		String officeFloorConfigFile = args[0];

		// Create the office floor
		System.out.println("Creating office floor '" + officeFloorConfigFile
				+ "'");
		OfficeFloor officeFloor = createOfficeFloor(officeFloorConfigFile);

		// Open the office floor
		System.out.println("Opening office floor");
		officeFloor.openOfficeFloor();

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
				System.err.println("No office by name '" + officeName + "'");
				return;
			}

			// Obtain the Work
			WorkManager workManager = office.getWorkManager(workName);
			if (workManager == null) {
				System.err.println("No work by name '" + workName
						+ "' on office " + officeName);
			}

			// Invoke the Work
			String parameter = (args.length > 4 ? args[3] : null);
			workManager.invokeWork(parameter);
		}
	}

	/**
	 * Creates the {@link OfficeFloor} from the input configuration file.
	 * 
	 * @param officeFloorConfigFile
	 *            Class path location of the {@link OfficeFloor} configuration
	 *            file.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to create the {@link OfficeFloor}.
	 */
	public static OfficeFloor createOfficeFloor(String officeFloorConfigFile)
			throws Exception {

		// Create the office floor compiler
		OfficeFloorCompiler compiler = new OfficeFloorCompiler();

		// Obtain the class loader
		ClassLoader classLoader = OfficeFloorMain.class.getClassLoader();

		// Create the loader context
		LoaderContext loaderContext = new LoaderContext(classLoader);

		// Obtain the office floor configuration
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				OFFICE_FLOOR_MAIN, classLoader);
		ConfigurationItem officeFloorConfigurationItem = configurationContext
				.getConfigurationItem(officeFloorConfigFile);
		if (officeFloorConfigurationItem == null) {
			throw new IOException(
					"Can not find office floor configuration file '"
							+ officeFloorConfigFile + "'");
		}

		// Obtain the office frame
		OfficeFrame officeFrame = OfficeFrame.getInstance();

		// Compile the office floor
		OfficeFloor officeFloor = compiler.compileOfficeFloor(
				officeFloorConfigurationItem, officeFrame, loaderContext);

		// Return the office floor
		return officeFloor;
	}

}