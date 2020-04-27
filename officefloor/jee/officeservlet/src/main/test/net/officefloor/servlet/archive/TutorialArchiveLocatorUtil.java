/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
public class TutorialArchiveLocatorUtil {

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

	/**
	 * All access via static methods.
	 */
	private TutorialArchiveLocatorUtil() {
	}

}
