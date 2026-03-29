/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link Class} path scanner to aid discovering resources.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorClassPathScanner {

	/**
	 * Convenience method to translates the {@link Package} name to class path entry
	 * path.
	 * 
	 * @param packageName {@link Package} name.
	 * @return Class path entry path for {@link Package}.
	 */
	public static String translatePackageToPath(String packageName) {
		return packageName.replace('.', '/');
	}

	/**
	 * Translates the resource {@link URL} to the JAR file path.
	 * 
	 * @param resourceUrl Resource {@link URL}.
	 * @return {@link File} path.
	 */
	public static String translateResourceUrlToFilePath(URL resourceUrl) {
		return resourceUrl.getFile().replaceFirst("\\.jar\\!.*", ".jar").substring("file:".length());
	}

	/**
	 * {@link ClassPathScanner} that attempts to resolve to files on disk to then
	 * interrogate directories/jars for the class path entries.
	 */
	private static final ClassPathScanner FILE_SYSTEM_CLASS_PATH_SCANNER = (context) -> {

		// Obtain the package path
		String packagePath = translatePackageToPath(context.getPackageName());

		// Obtain package resources (may be multiple jars/directories for package)
		Enumeration<URL> packageUrls = context.getClassLoader().getResources(packagePath);
		if (packageUrls == null) {
			return; // package seems to not exist
		}

		// Iterate over URLs to load the entry paths
		while (packageUrls.hasMoreElements()) {
			URL packageUrl = packageUrls.nextElement();

			// Attempt to determine if directory
			File directory;
			try {
				directory = new File(packageUrl.toURI());
			} catch (URISyntaxException | IllegalArgumentException e) {
				// Not directory
				directory = null;
			}

			// Handle if directory
			if (directory != null) {

				// Include all files within directory
				String[] files = directory.list();
				if (files != null) {
					for (String file : files) {

						// Create file entry path
						String fileEntryPath = packagePath + "/" + file;

						// Add the entry
						context.addEntry(fileEntryPath);
					}
				}

				// Return as loaded for directory
				return;
			}

			// Obtain the file path
			String urlFilePath = packageUrl.getFile();

			// Determine if JAR file
			if ((urlFilePath.startsWith("file:")) && (urlFilePath.endsWith(".jar!/" + packagePath))) {

				// Extract jar file name
				String jarFilePath = translateResourceUrlToFilePath(packageUrl);

				// Scan the jar for package entries
				try (JarFile jarFile = new JarFile(jarFilePath)) {
					Enumeration<JarEntry> jarEntries = jarFile.entries();
					NEXT_ENTRY: while (jarEntries.hasMoreElements()) {
						JarEntry jarEntry = jarEntries.nextElement();

						// Determine if entry for package
						String entryName = jarEntry.getName();
						if (!entryName.startsWith(packagePath)) {
							continue NEXT_ENTRY; // not package
						}
						if (entryName.indexOf('/', packagePath.length() + "/".length()) > 0) {
							continue NEXT_ENTRY; // sub package of target package
						}

						// Found the entry
						context.addEntry(entryName);
					}
				}

				// Return as loaded for JAR file
				return;
			}
		}
	};

	/**
	 * <p>
	 * {@link ClassPathScanner} that attempts to use the resource stream (sometimes
	 * supported by {@link ClassLoader} instances) to obtain listing of class path
	 * entries.
	 * <p>
	 * This is a fallback attempt after the {@link FileSystemClassPathScanner}.
	 */
	private static final ClassPathScanner CLASS_LOADER_CLASS_PATH_SCANNER = (context) -> {

		// Obtain the package path
		String packagePath = translatePackageToPath(context.getPackageName());

		// Obtain the package input stream
		InputStream packageInput = context.getClassLoader().getResourceAsStream(packagePath);
		if (packageInput == null) {
			return; // unable to load
		}

		// Read in entries of package
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(packageInput))) {
			String entry;
			while ((entry = reader.readLine()) != null) {
				String entryPath = packagePath + "/" + entry;
				context.addEntry(entryPath);
			}
		}
	};

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext context;

	/**
	 * {@link ClassPathScanner} instances.
	 */
	private final List<ClassPathScanner> classPathScanners = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param context {@link SourceContext}.
	 */
	public OfficeFloorClassPathScanner(SourceContext context) {
		this.context = context;

		// Add the default class path scanners
		this.classPathScanners.add(FILE_SYSTEM_CLASS_PATH_SCANNER);
		this.classPathScanners.add(CLASS_LOADER_CLASS_PATH_SCANNER);

		// Load the class path scanners services to enhance scanning
		for (ClassPathScanner scanner : context.loadOptionalServices(ClassPathScannerServiceFactory.class)) {
			this.classPathScanners.add(scanner);
		}
	}

	/**
	 * Scans the package for {@link Class} path entries.
	 * 
	 * @param packageName Package name.
	 * @return {@link Class} path entries for the package.
	 * @throws IOException If fails to scan the {@link Class} path.
	 */
	public Set<String> scan(String packageName) throws IOException {

		// Create the context to load the entries
		Set<String> entryPaths = new HashSet<>();
		ClassPathScannerContext context = new ClassPathScannerContext() {
			@Override
			public String getPackageName() {
				return packageName;
			}

			@Override
			public ClassLoader getClassLoader() {
				return OfficeFloorClassPathScanner.this.context.getClassLoader();
			}

			@Override
			public void addEntry(String entryPath) {
				entryPaths.add(entryPath);
			}
		};

		// Load the class path entries
		for (ClassPathScanner scanner : this.classPathScanners) {
			scanner.scan(context);
		}

		// Return the class path entries
		return entryPaths;
	}

	/**
	 * Scans the package for just {@link Class} fully qualified names.
	 * 
	 * @param packageName Package name.
	 * @return Fully qualified {@link Class} names for the package.
	 * @throws IOException If fails to scan the {@link Class} path.
	 */
	public Set<String> scanClasses(String packageName) throws IOException {

		// Scan the entries
		Set<String> entries = this.scan(packageName);

		// Filter out non-classes and transform to class names
		Set<String> classNames = new HashSet<>();
		NEXT_ENTRY: for (String entry : entries) {

			// Ignore if not class
			if (!entry.endsWith(".class")) {
				continue NEXT_ENTRY;
			}

			// Transform to class name
			String className = entry.substring(0, entry.length() - ".class".length());
			className = className.replace('/', '.');

			// Include the class name
			classNames.add(className);
		}

		// Return the class names
		return classNames;
	}

}
