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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Properties;

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

		// Load the properties to generate the model
		File propertyFileLocation = new File(new File(System
				.getProperty("user.home")), new File("officefloor",
				"model-generator.properties").getPath());
		if (!propertyFileLocation.exists()) {
			throw new FileNotFoundException(
					"Can not find model generator property file : "
							+ propertyFileLocation.getAbsolutePath());
		}
		Properties properties = new Properties();
		properties.load(new FileInputStream(propertyFileLocation));

		// Obtain model project directory
		final String MODEL_PROJECT_DIR_PROPERTY_NAME = "model.project.dir";
		String modelProjectDir = properties
				.getProperty(MODEL_PROJECT_DIR_PROPERTY_NAME);
		if (modelProjectDir == null) {
			throw new IllegalStateException("Property '"
					+ MODEL_PROJECT_DIR_PROPERTY_NAME
					+ "' must be specified in property file: "
					+ propertyFileLocation.getAbsolutePath());
		}

		// Ensure the model project directory exists
		if (!(new File(modelProjectDir).exists())) {
			throw new FileNotFoundException(
					"Model project directory not exist - " + modelProjectDir);
		}

		// Obtain the raw directory
		File rawDir = new File(modelProjectDir, "src/raw");

		// Obtain the output directory
		File outputDir = new File(modelProjectDir, "src/auto");

		// Notify of changes
		System.out.println("Generating all objects");
		System.out.println("    Raw: " + rawDir.getAbsolutePath());
		System.out.println("    Output: " + outputDir.getAbsolutePath());
		System.out.println();
		System.out.print("Press [enter] to continue ... ");

		// Wait for confirmation to continue
		new BufferedReader(new InputStreamReader(System.in)).readLine();

		// Generate all objects
		new GraphGenerator().generate(rawDir, outputDir);
	}

}
