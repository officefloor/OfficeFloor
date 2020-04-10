package net.officefloor.servlet.archive;

import java.io.File;

/**
 * <p>
 * Locates an archive within the tutorials.
 * <p>
 * This only works with the source code.
 * 
 * @author Daniel Sagenschneider
 */
public class TutorialArchiveLocator {

	/**
	 * Obtains the location of the archive file.
	 * 
	 * @return Location of the archive file.
	 */
	public static File getArchiveFile(String artifactId, String suffix) {

		// Locate tutorial directory containing the archive
		// (note: dependency on it should build it first)
		File currentDir = new File(".");
		File archiveTutorialProjectDir = new File(currentDir, "../../tutorials/" + artifactId);
		if (!archiveTutorialProjectDir.isDirectory()) {
			throw new IllegalStateException("INVALID TEST: can not find " + artifactId + " project directory at "
					+ archiveTutorialProjectDir.getAbsolutePath());
		}

		// Locate the archive file
		for (File file : new File(archiveTutorialProjectDir, "target").listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith(artifactId) && fileName.toLowerCase().endsWith(suffix.toLowerCase())) {
				return file; // archive file
			}
		}

		// As here, did not find the archive
		throw new IllegalStateException("INVALID TEST: can not find " + artifactId + " archive file");
	}

}