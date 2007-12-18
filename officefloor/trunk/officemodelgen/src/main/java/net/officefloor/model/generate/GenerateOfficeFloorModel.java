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
package net.officefloor.model.generate;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Generates the OfficeFloor model.
 * 
 * @author Daniel
 */
public class GenerateOfficeFloorModel {

	/**
	 * Allow invoking.
	 */
	public static void main(String[] args) throws Exception {

		// Model Project directory should be the only argument
		if (args.length == 0) {
			throw new IllegalArgumentException(
					"Must provide location of model project directory as first argument");
		}

		// Obtain model project directory file
		File modelProjectDir = new File(args[0]);

		// Ensure the model project directory exists
		if (!(modelProjectDir.exists())) {
			throw new FileNotFoundException(
					"Model project directory not exist - " + modelProjectDir);
		}

		// Obtain the raw directory
		File rawDir = new File(modelProjectDir, "src/raw");

		// Ensure the raw directory exists
		if (!(rawDir.exists())) {
			throw new IllegalStateException(
					"Raw directory not found within model project directory - "
							+ modelProjectDir.getAbsolutePath());
		}

		// Obtain the output directory
		File outputDir = new File(modelProjectDir, "src/auto");

		// Notify of changes
		System.out.println("Generating all objects");
		System.out.println("    Raw: " + rawDir.getAbsolutePath());
		System.out.println("    Output: " + outputDir.getAbsolutePath());

		// Generate all objects
		new GraphGenerator().generate(rawDir, outputDir);
	}

}
