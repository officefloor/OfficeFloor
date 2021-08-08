/*-
 * #%L
 * Servlet
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
