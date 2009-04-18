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

import java.util.Properties;

import net.officefloor.compile.impl.officefloor.OfficeFloorLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

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
		String officeFloorLocation = args[0];

		// Create the office floor
		System.out.println("Creating office floor '" + officeFloorLocation
				+ "'");
		OfficeFloor officeFloor = createOfficeFloor(officeFloorLocation);

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
	 * Creates the {@link OfficeFloor}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to create the {@link OfficeFloor}.
	 */
	public static OfficeFloor createOfficeFloor(String officeFloorLocation)
			throws Exception {

		// TODO obtain the office floor source
		Class<? extends OfficeFloorSource> officeFloorSourceClass = null;

		// Use class path configuration context
		ClassLoader classLoader = OfficeFloorMain.class.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Use the system properties as the properties
		PropertyList propertyList = new PropertyListImpl();
		Properties systemProperties = System.getProperties();
		for (String name : systemProperties.stringPropertyNames()) {
			String value = systemProperties.getProperty(name);
			propertyList.addProperty(name).setValue(value);
		}

		// Obtain the office frame
		OfficeFrame officeFrame = OfficeFrame.getInstance();

		// Create the office floor loader and load the office floor
		OfficeFloorLoader loader = new OfficeFloorLoaderImpl(
				officeFloorLocation);
		OfficeFloor officeFloor = loader.loadOfficeFloor(
				officeFloorSourceClass, configurationContext, propertyList,
				classLoader, new StderrCompilerIssues(), officeFrame);

		// Return the office floor
		return officeFloor;
	}

	/**
	 * {@link CompilerIssues} to write issues to {@link System#err}.
	 */
	private static class StderrCompilerIssues implements CompilerIssues {

		/*
		 * ================= CompilerIssues ==================================
		 */

		@Override
		public void addIssue(LocationType locationType, String location,
				AssetType assetType, String assetName, String issueDescription) {
			// TODO Implement
			throw new UnsupportedOperationException(
					"TODO implement CompilerIssues.addIssue");
		}

		@Override
		public void addIssue(LocationType locationType, String location,
				AssetType assetType, String assetName, String issueDescription,
				Throwable cause) {
			// TODO Implement
			throw new UnsupportedOperationException(
					"TODO implement CompilerIssues.addIssue");
		}
	}

}