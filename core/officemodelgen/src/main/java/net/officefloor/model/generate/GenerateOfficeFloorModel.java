/*-
 * #%L
 * Model Generator
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.model.generate;

import java.io.File;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.compatibility.JavaFacet;

/**
 * Generates the OfficeFloor model.
 *
 * @author Daniel Sagenschneider
 */
public class GenerateOfficeFloorModel {

	/**
	 * Allow invoking.
	 * 
	 * @param args Command line arguments.
	 * @throws Exception If fails to generate the {@link OfficeFloor} models.
	 */
	public static void main(String[] args) throws Exception {

		// Model Project directory should be the only argument
		if (args.length != 2) {
			throw new IllegalArgumentException("USAGE: java ... " + GenerateOfficeFloorModel.class.getName()
					+ " <raw file directory> <output directory>");
		}

		// Obtain the raw directory (ensuring it exists)
		File rawDir = new File(args[0]);
		if (!(rawDir.exists())) {
			throw new IllegalArgumentException("Raw directory not found  - " + rawDir.getAbsolutePath());
		}

		// Obtain the output directory
		File outputDir = new File(args[1]);

		// Notify of changes
		int javaVersion = JavaFacet.getJavaFeatureVersion();
		System.out.println("Generating all objects (for Java " + javaVersion + ")");
		System.out.println("    Raw: " + rawDir.getAbsolutePath());
		System.out.println("    Output: " + outputDir.getAbsolutePath());

		// Generate all objects
		new GraphGenerator().generate(rawDir, outputDir);
	}

}
