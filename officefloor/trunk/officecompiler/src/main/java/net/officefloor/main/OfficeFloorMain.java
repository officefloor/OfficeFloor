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

import net.officefloor.LoaderContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.classloader.ClassLoaderConfigurationContext;

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
					+ " <office floor configuration file>");
			return;
		}

		// Obtain the command line arguments
		String officeFloorConfigFile = args[0];

		// Create the office floor
		OfficeFloor officeFloor = createOfficeFloor(officeFloorConfigFile);

		// Open the office floor
		officeFloor.openOfficeFloor();
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

		// Obtain the builder factory
		BuilderFactory builderFactory = OfficeFrame.getInstance()
				.getBuilderFactory();

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

		// Compile the office floor
		OfficeFloor officeFloor = compiler.compileOfficeFloor(
				officeFloorConfigurationItem, builderFactory, loaderContext);

		// Return the office floor
		return officeFloor;
	}

}
