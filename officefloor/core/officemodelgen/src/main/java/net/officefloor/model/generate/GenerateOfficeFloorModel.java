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