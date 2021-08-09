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

package net.officefloor.compile.impl.classes;

import java.io.IOException;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.ClassPathScanner;
import net.officefloor.compile.classes.OfficeFloorClassPathScanner;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorClassPathScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorClassPathScannerTest extends OfficeFrameTestCase {

	/**
	 * Ensure can find this test.
	 */
	public void testFindThisTest() {
		assertClassesFoundInPackage(this.getClass().getPackage(), this.getClass());
	}

	/**
	 * Ensure can find {@link OfficeFloor}.
	 */
	public void testFindOfficeFloor() {
		assertClassesFoundInPackage(OfficeFloor.class.getPackage(), OfficeFloor.class);
	}

	/**
	 * Ensure can find {@link Test} containing in a Jar.
	 */
	public void testJarClass() {
		assertClassesFoundInPackage(Test.class.getPackage(), Test.class);
	}

	/**
	 * Unable to scan system classes.
	 */
	public void testSystemClass() {
		Set<String> entries = scanPath("java/lang", getClassLoader());
		assertEquals("Currently not sure on means to load system classes", 0, entries.size());
	}

	/**
	 * Ensure can find multiple entries.
	 */
	public void testFindMultiple() {
		assertClassesFoundInPackage(Test.class.getPackage(), Test.class, TestCase.class, TestFailure.class);
	}

	/**
	 * <p>
	 * Ensure can fallback to streaming in directory contents.
	 * <p>
	 * Note: not all {@link ClassLoader} variations support this. However, providing
	 * as last chance before not finding.
	 */
	public void testFallBackToStream() {
		assertEntriesFoundInPackage(createNewClassLoader(), CLASS_LOADER_EXTRA_PACKAGE_NAME,
				CLASS_LOADER_EXTRA_CLASS_NAME.replace('.', '/') + ".class");
	}

	/**
	 * Ensure can use {@link ClassPathScanner} to load additional class paths.
	 */
	public void testFindViaAddedService() {
		assertEntriesFoundInPackage(MockClassPathScanner.MOCK_PACKAGE_PATH, MockClassPathScanner.MOCK_ENTRY_PATH);
	}

	/**
	 * Ensure can scan {@link Class}.
	 */
	public void testScanClasses() throws IOException {
		OfficeFloorClassPathScanner scanner = createScanner(getClassLoader());
		Set<String> classNames = scanner.scanClasses(Test.class.getPackage().getName());
		assertTrue("Ensure returns class name " + Test.class.getName() + " (" + classNames + ")",
				classNames.contains(Test.class.getName()));
	}

	/**
	 * Asserts the classes found in package.
	 * 
	 * @param scanPackage {@link Package} to scan.
	 * @param classes     Expected {@link Class} instances to be found in
	 *                    {@link Package}.
	 */
	private static void assertClassesFoundInPackage(Package scanPackage, Class<?>... classes) {
		String scanPackageName = scanPackage.getName();
		String[] entryPaths = new String[classes.length];
		for (int i = 0; i < entryPaths.length; i++) {
			entryPaths[i] = classes[i].getName().replace('.', '/') + ".class";
		}
		assertEntriesFoundInPackage(scanPackageName, entryPaths);
	}

	/**
	 * Asserts the classes found in package.
	 * 
	 * @param scanPackageName {@link Package} name to scan.
	 * @param entryPaths      Entry paths.
	 */
	private static void assertEntriesFoundInPackage(String scanPackageName, String... entryPaths) {
		assertEntriesFoundInPackage(getClassLoader(), scanPackageName, entryPaths);
	}

	/**
	 * Asserts the classes found in package.
	 * 
	 * @param classLoader     {@link ClassLoader} to use.
	 * @param scanPackageName {@link Package} name to scan.
	 * @param entryPaths      Entry paths.
	 */
	private static void assertEntriesFoundInPackage(ClassLoader classLoader, String scanPackageName,
			String... entryPaths) {
		Set<String> entries = scanPath(scanPackageName, classLoader);
		for (String entryPath : entryPaths) {
			assertTrue("Scanned entries should contain " + entryPath + " (" + entries + ")",
					entries.contains(entryPath));
		}
	}

	/**
	 * Undertakes scanning {@link Class} path.
	 * 
	 * @return Scanned classes.
	 */
	private static Set<String> scanPath(String packageName, ClassLoader classLoader) {
		try {
			Set<String> entries = createScanner(classLoader).scan(packageName);
			return entries;
		} catch (IOException ex) {
			throw fail(ex);
		}
	}

	/**
	 * Creates a {@link OfficeFloorClassPathScanner}.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link OfficeFloorClassPathScanner}.
	 */
	private static OfficeFloorClassPathScanner createScanner(ClassLoader classLoader) {
		SourceContext context = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader).createRootSourceContext();
		return new OfficeFloorClassPathScanner(context);
	}

	/**
	 * Obtains the {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	private static ClassLoader getClassLoader() {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = OfficeFloorJavaCompilerTest.class.getClassLoader();
		}

		// Return the class loader
		return classLoader;
	}

}
