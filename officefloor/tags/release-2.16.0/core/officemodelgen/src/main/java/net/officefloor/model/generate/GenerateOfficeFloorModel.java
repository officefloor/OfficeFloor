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
package net.officefloor.model.generate;

import java.io.File;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Generates the OfficeFloor model.
 *
 * @author Daniel Sagenschneider
 */
public class GenerateOfficeFloorModel {

	/**
	 * Allow invoking.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to generate the {@link OfficeFloor} models.
	 */
	public static void main(String[] args) throws Exception {

		// Model Project directory should be the only argument
		if (args.length != 2) {
			throw new IllegalArgumentException("USAGE: java ... "
					+ GenerateOfficeFloorModel.class.getName()
					+ " <raw file directory> <output directory>");
		}

		// Obtain the raw directory (ensuring it exists)
		File rawDir = new File(args[0]);
		if (!(rawDir.exists())) {
			throw new IllegalArgumentException("Raw directory not found  - "
					+ rawDir.getAbsolutePath());
		}

		// Obtain the output directory
		File outputDir = new File(args[1]);

		// Notify of changes
		System.out.println("Generating all objects");
		System.out.println("    Raw: " + rawDir.getAbsolutePath());
		System.out.println("    Output: " + outputDir.getAbsolutePath());

		// Generate all objects
		new GraphGenerator().generate(rawDir, outputDir);
	}

}