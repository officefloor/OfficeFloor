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
package net.officefloor.bootstrap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Bootstraps the applications within the deployment.
 * 
 * @author Daniel Sagenschneider
 */
public class Bootstrap {

	/**
	 * Environment property to specify where the OfficeFloor home directory is
	 * located.
	 */
	public static final String OFFICE_FLOOR_HOME = "OFFICE_FLOOR_HOME";

	/**
	 * Bootstraps the application within the deployment.
	 * 
	 * @param arguments
	 *            Command line arguments.
	 * @throws Throwable
	 *             If fails to bootstrap.
	 */
	public static void main(String... arguments) throws Throwable {

		// Create the class path entry listing
		ClassPathEntries entries = new ClassPathEntries();

		// Load in the boot java class path entries
		String bootClassPath = System.getProperty("java.class.path");
		for (String bootClassPathEntry : bootClassPath
				.split(File.pathSeparator)) {
			entries.addClassPathEntry(bootClassPathEntry);
		}

		// Obtain the environment properties
		Properties environment = new Properties();
		environment.putAll(System.getenv());
		environment.putAll(System.getProperties());

		// Obtain the OFFICE_FLOOR_HOME for boot libraries
		String officeFloorHomePath = environment.getProperty(OFFICE_FLOOR_HOME);
		if (officeFloorHomePath == null) {
			errorAndExit("ERROR: OFFICE_FLOOR_HOME not specified. Must be an environment variable pointing to the OfficeFloor install directory.");
		}
		File officeFloorHome = new File(officeFloorHomePath);
		ensureDirectoryExists(officeFloorHome);

		// Add the lib class path entries
		File libDir = new File(officeFloorHome, "lib");
		entries.loadDirectoryClassPathEntries(libDir, true, true);

		// Specify the java class path
		String javaClassPath = entries.getClassPath();
		System.setProperty("java.class.path", javaClassPath);

		// Specify the class loader
		ClassLoader classLoader = entries.getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		// Obtain the class to invoke
		String className = arguments[0];
		Class<?> clazz = Thread.currentThread().getContextClassLoader()
				.loadClass(className);

		// Remove class from arguments
		String[] commandLine = new String[arguments.length - 1];
		for (int i = 1; i < arguments.length; i++) {
			commandLine[i - 1] = arguments[i];
		}

		// Bootstrap the delegate class
		Method mainMethod = clazz.getMethod("main", new String[0].getClass());
		try {
			mainMethod.invoke(null, (Object) commandLine);
		} catch (InvocationTargetException ex) {
			// Propagate the cause
			throw ex.getCause();
		}
	}

	/**
	 * Ensures the directory exists.
	 * 
	 * @param directory
	 *            Directory to check if exists.
	 */
	private static void ensureDirectoryExists(File directory) {
		if (!directory.exists()) {
			errorAndExit("Can not find directory '" + directory.getPath() + "'");
		}
		if (!directory.isDirectory()) {
			errorAndExit("Must have directory at path '" + directory.getPath()
					+ "'");
		}
	}

	/**
	 * Flag to indicate that testing so not to exit process but rather throw an
	 * exception.
	 */
	static boolean isTesting = false;

	/**
	 * Provides error message and exits process.
	 * 
	 * @param message
	 *            Message to be displayed.
	 */
	private static void errorAndExit(String message) {

		// Provide error message
		System.err.println(message);

		// Handle if testing
		if (isTesting) {
			throw new Error("Exit: " + message);
		}

		// Not testing so exit process
		System.exit(1);
	}

	/**
	 * Contains the class path entries.
	 */
	private static class ClassPathEntries {

		/**
		 * Class path entries.
		 */
		private List<File> entries = new LinkedList<File>();

		/**
		 * Adds a class path entry.
		 * 
		 * @param path
		 *            Path for the class path entry.
		 */
		public void addClassPathEntry(String path) {
			this.addClassPathEntry(new File(path));
		}

		/**
		 * Adds a class path entry.
		 * 
		 * @param path
		 *            Path for the class path entry.
		 */
		public void addClassPathEntry(File path) {

			// Ensure the path exists
			if (!path.exists()) {
				System.err.println("Unknown path: " + path.getName());
				return;
			}

			// Determine if already have entry
			for (File existingPath : this.entries) {
				if (existingPath.equals(path)) {
					return; // already have path
				}
			}

			// Add the path
			this.entries.add(path);
		}

		/**
		 * Loads the class path entries contained in the directory.
		 * 
		 * @param directory
		 *            Directory to interrogate for class path entries.
		 * @param isIncludeJars
		 *            Flags to include JAR files.
		 * @param isIncludeDirectories
		 *            Flags to include directories.
		 */
		public void loadDirectoryClassPathEntries(File directory,
				boolean isIncludeJars, boolean isIncludeDirectories) {

			// Ensure the directory exists
			ensureDirectoryExists(directory);

			// Iterate over the files in the directory
			for (File dirFile : directory.listFiles()) {
				if (dirFile.isDirectory()) {
					if (isIncludeDirectories) {
						// Include the directory
						this.entries.add(dirFile);
					}

				} else if (dirFile.isFile()) {
					if (dirFile.getName().endsWith(".jar")) {
						if (isIncludeJars) {
							// Include the jar
							this.entries.add(dirFile);
						}
					}
				}
			}
		}

		/**
		 * Obtains the class path.
		 * 
		 * @return Class path.
		 */
		public String getClassPath() {
			StringBuilder classPath = new StringBuilder();

			// Generate the class path
			boolean isFirst = true;
			for (File entry : this.entries) {

				// Provide separator after first entry
				if (!isFirst) {
					classPath.append(File.pathSeparator);
				}

				// Add the entry to the class path
				classPath.append(entry.getPath());

				// No longer the first
				isFirst = false;
			}

			// Return the class path
			return classPath.toString();
		}

		/**
		 * Obtains the {@link ClassLoader}.
		 * 
		 * @return {@link ClassLoader}.
		 * @throws Exception
		 *             If fails to obtain the {@link ClassLoader}.
		 */
		public ClassLoader getClassLoader() throws Exception {

			// Create the listing of urls
			List<URL> urls = new LinkedList<URL>();
			for (File entry : this.entries) {
				URL url = entry.toURI().toURL();
				urls.add(url);
			}

			/*
			 * The class loader needs to be top level, as certain Java libraries
			 * obtain the class loader from the class (eg InitialContext). In
			 * this case, the InitialContext is obtained from the parent class
			 * loader and therefore the parent class loader is used. This
			 * results in the created class loader here not being used (search
			 * is only upwards through parents and not downwards through child
			 * class loaders). Subsequently, the bootstrapped class path of this
			 * child class loader is not used.
			 */

			// Create the class loader (at top level, no parent)
			URL[] urlArray = urls.toArray(new URL[0]);
			URLClassLoader classLoader = URLClassLoader.newInstance(urlArray,
					null);

			// Return the class loader
			return classLoader;
		}
	}

}