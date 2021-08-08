/*-
 * #%L
 * Maven WoOF Plugin
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
	 * @param pluginUrls      {@link URL} entries for plugin class path.
	 * @param isIncludeJavaFx Indicates whether to include the JavaFX classes.
	 * @param javaFxLibDir    Library directory for JavaFX install. May be
	 *                        <code>null</code>.
	 * @return {@link URL}s for class path entries.
	 * @throws Exception If fails to load the class path entries.
	 */
	public static URL[] getClassPathEntries(URL[] pluginUrls, boolean isIncludeJavaFx, File javaFxLibDir)
			throws Exception {

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

		// Determine if include JavaFX
		if (isIncludeJavaFx) {

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
