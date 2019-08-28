/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.maven.woof;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.compatibility.JavaFacetContext;

/**
 * Obtains the {@link URL} instances for adding JavaFx to class path.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaFxFacet implements JavaFacet {

	/**
	 * Obtains the {@link URL}s for class path entries.
	 * 
	 * @param pluginUrls   {@link URL} entries for plugin class path.
	 * @param javaFxLibDir Library directory for JavaFX install. May be
	 *                     <code>null</code>.
	 * @return {@link URL}s for class path entries.
	 * @throws Exception If fails to load the class path entries.
	 */
	public static URL[] getClassPathEntries(URL[] pluginUrls, File javaFxLibDir) throws Exception {

		// Obtain the java facet context
		JavaFxFacet facet = new JavaFxFacet();
		JavaFacet.isSupported(facet);

		// Separate JavaFX plugins out
		List<URL> classPathUrls = new ArrayList<>();
		List<URL> javaFxUrls = new ArrayList<>();
		for (URL pluginUrl : pluginUrls) {
			String filePath = pluginUrl.getFile();
			int fileIndex = filePath.lastIndexOf(File.separatorChar);
			String fileName = filePath.substring(fileIndex >= 0 ? fileIndex + "/".length() : 0);
			if (fileName.startsWith("javafx")) {
				javaFxUrls.add(pluginUrl);
			} else {
				classPathUrls.add(pluginUrl);
			}
		}

		// Determine if specifying JavaFX lib directory
		if (javaFxLibDir != null) {

			// Ensure exists and directory
			if (!javaFxLibDir.isDirectory()) {
				throw new FileNotFoundException("JavaFX lib directory not found: " + javaFxLibDir.getPath());
			}

			// Clear plugin JavaFX entries
			javaFxUrls.clear();

			// Load JavaFX entries from lib directory
			NEXT_FILE: for (File file : javaFxLibDir.listFiles()) {

				// Ensure jar
				if (!file.isFile()) {
					continue NEXT_FILE;
				}
				if (!file.getName().endsWith(".jar")) {
					continue NEXT_FILE;
				}

				// Add the jar
				classPathUrls.add(file.toURI().toURL());
			}
		}

		// Handle based on version
		switch (facet.javaFacetContext.getFeature()) {
		case 8:
			// Include JavaFX installed in JRE
			String javaHome = System.getProperty("java.home");
			File javaFxJar = new File(javaHome, "lib/ext/jfxrt.jar");
			classPathUrls.add(javaFxJar.toURI().toURL());
			break;
		case 9:
		case 10:
			// Should be included in JRE installation
			break;
		default:
			// Include JavaFX
			classPathUrls.addAll(javaFxUrls);
			break;
		}

		// Return the class path URLs
		return classPathUrls.toArray(new URL[classPathUrls.size()]);
	}

	/**
	 * {@link JavaFacetContext}.
	 */
	private JavaFacetContext javaFacetContext;

	/*
	 * ================ JavaFacet ================
	 */

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		this.javaFacetContext = context;
		return true;
	}

}