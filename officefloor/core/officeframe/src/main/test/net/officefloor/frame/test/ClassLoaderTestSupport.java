/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.test;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ClassLoader} {@link TestSupport}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderTestSupport implements TestSupport {

	/**
	 * {@link Package} name of the extra class for the new {@link ClassLoader}.
	 */
	public static final String CLASS_LOADER_EXTRA_PACKAGE_NAME = "extra";

	/**
	 * {@link Class} name of the extra class for the new {@link ClassLoader}.
	 */
	public static final String CLASS_LOADER_EXTRA_CLASS_NAME = CLASS_LOADER_EXTRA_PACKAGE_NAME + ".MockExtra";

	/**
	 * Indicates if mock is created.
	 */
	private static boolean isMockCreated = false;

	/**
	 * {@link FileTestSupport}.
	 */
	private FileTestSupport fileTestSupport;

	/*
	 * ====================== TestSupport =======================
	 */

	/**
	 * Initialise.
	 * 
	 * @param fileTestSupport {@link FileTestSupport}.
	 */
	public ClassLoaderTestSupport(FileTestSupport fileTestSupport) {
		this.fileTestSupport = fileTestSupport;
	}

	public ClassLoaderTestSupport() {
		// Initialised via test support
	}

	@Override
	public void init(ExtensionContext context) throws Exception {
		this.fileTestSupport = TestSupportExtension.getTestSupport(FileTestSupport.class, context);
	}

	/**
	 * <p>
	 * Creates a new {@link ClassLoader} from current process's java class path.
	 * <p>
	 * {@link Class} instances loaded via this {@link ClassLoader} will be different
	 * to the current {@link ClassLoader}. This is to allow testing multiple
	 * {@link ClassLoader} environments (such as Eclipse plug-ins).
	 * 
	 * @return New {@link ClassLoader}.
	 */
	public ClassLoader createNewClassLoader() {
		try {

			// Provide additional class to this class loader
			// (only compiled once as does not change)
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File workingDir = new File(tempDir, "officefloor-extra-classpath");
			if (!workingDir.isDirectory()) {
				workingDir.mkdir();
			} else if (!isMockCreated) {
				// Must clear mock (may be newer incompatible JVM that created it)
				this.fileTestSupport.deleteDirectory(workingDir);
				workingDir.mkdir();
			}
			File extraPackageDir = new File(workingDir, "extra");
			if (!extraPackageDir.isDirectory()) {
				extraPackageDir.mkdir();
			}
			File extraClassSrc = new File(extraPackageDir, "MockExtra.java");
			if (!extraClassSrc.exists()) {
				// Write the source file
				FileWriter writer = new FileWriter(extraClassSrc);
				writer.write("package extra;\n");
				writer.write("public class MockExtra {}\n");
				writer.close();
			}
			File extraClass = new File(extraPackageDir, "MockExtra.class");
			if (!extraClass.exists()) {
				// Compile the source to class
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
					Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(extraClassSrc);
					compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
				}
			}

			// Determine if running Java 9 (or above)
			ClassLoader platformClassLoader;
			try {
				// Use Platform ClassLoader (for modules of Java 9 and above)
				Method getPlatformClassLoader = ClassLoader.class.getMethod("getPlatformClassLoader");
				platformClassLoader = (ClassLoader) getPlatformClassLoader.invoke(null);
			} catch (NoSuchMethodException ex) {
				// Use Java 8 boot class loader
				platformClassLoader = new ClassLoader(null) {
				};
			}

			// Ensure platform class loader not loading OfficeFloor
			boolean isOfficeFloorOnPlatformClassPath = true;
			try {
				platformClassLoader.loadClass(OfficeFloor.class.getName());
			} catch (ClassNotFoundException ex) {
				isOfficeFloorOnPlatformClassPath = false;
			}
			Assertions.assertFalse(isOfficeFloorOnPlatformClassPath,
					"Invalid test, as Platform ClassLoader has " + OfficeFloor.class.getName());

			// Create Class Loader for testing
			String[] classPathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
			URL[] urls = new URL[classPathEntries.length + 1]; // include extra class
			for (int i = 0; i < classPathEntries.length; i++) {
				String classPathEntry = classPathEntries[i];
				File possibleClassPathFile = new File(classPathEntry);
				urls[i] = (possibleClassPathFile.exists() ? possibleClassPathFile.toURI().toURL()
						: new URL(classPathEntry));
			}
			urls[classPathEntries.length] = workingDir.toURI().toURL();
			ClassLoader classLoader = new URLClassLoader(urls, platformClassLoader);

			// Flag mock now created for testing
			isMockCreated = true;

			// Return the class loader
			return classLoader;

		} catch (Exception ex) {
			return Assertions.fail(ex);
		}
	}

}
