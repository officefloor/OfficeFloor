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

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ArchiveAwareClassLoaderFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ArchiveAwareClassLoaderFactoryTest extends OfficeFrameTestCase {

	/**
	 * Serlet class within WAR.
	 */
	private static final String SIMPLE_SERVLET_CLASS_NAME = "net.officefloor.tutorial.warapp.SimpleServlet";

	/**
	 * Spring simple controller within Spring JAR.
	 */
	private static final String SIMPLE_CONTROLLER_CLASS_NAME = "net.officefloor.tutorial.springapp.SimpleController";

	/**
	 * WAR classes prefix.
	 */
	private static final String WAR_CLASSES_PREFIX = "WEB-INF/classes/";

	/**
	 * WAR lib prefix.
	 */
	private static final String WAR_LIB_PREFIX = "WEB-INF/lib/";

	/**
	 * WAR {@link File} to test with.
	 */
	private File warFile;

	/**
	 * Spring JAR {@link File} to test with.
	 */
	private File springJarFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.warFile = TutorialArchiveLocatorUtil.getArchiveFile("WarApp", ".war");
		this.springJarFile = TutorialArchiveLocatorUtil.getArchiveFile("SpringWebMvcApp", "-execute.jar");
	}

	/**
	 * Ensure can work with default parent {@link ClassLoader}.
	 */
	public void testDefaultParentClassLoader() throws Exception {
		ClassLoader classLoader = new ArchiveAwareClassLoaderFactory(this.getClass().getClassLoader())
				.createClassLoader(this.warFile.toURI().toURL(), WAR_CLASSES_PREFIX, WAR_LIB_PREFIX);
		assertNotNull("Should find this class due to default parent", classLoader.loadClass(this.getClass().getName()));
		assertNotNull("Should find " + SIMPLE_SERVLET_CLASS_NAME, classLoader.loadClass(SIMPLE_SERVLET_CLASS_NAME));
	}

	/**
	 * Ensure can handle no WAR {@link File}.
	 */
	public void testNoWarFileWithBoot() throws Exception {
		ClassLoader classLoader = new ArchiveAwareClassLoaderFactory(null)
				.createClassLoader(this.springJarFile.toURI().toURL(), WAR_CLASSES_PREFIX, WAR_LIB_PREFIX);
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertClassNotFound(OfficeFloor.class.getName(), classLoader);
		assertClassNotFound(SIMPLE_SERVLET_CLASS_NAME, classLoader);
	}

	/**
	 * Ensure can work with boot {@link ClassLoader}.
	 */
	public void testBootClassLoader() throws Exception {
		ClassLoader classLoader = new ArchiveAwareClassLoaderFactory(null)
				.createClassLoader(this.warFile.toURI().toURL(), WAR_CLASSES_PREFIX, WAR_LIB_PREFIX);
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertNotNull("Should find " + SIMPLE_SERVLET_CLASS_NAME, classLoader.loadClass(SIMPLE_SERVLET_CLASS_NAME));
	}

	/**
	 * Ensure can load class from Spring JAR archive.
	 */
	public void testSpringJar() throws Exception {
		ClassLoader classLoader = new ArchiveAwareClassLoaderFactory(null)
				.createClassLoader(this.springJarFile.toURI().toURL(), "BOOT-INF/classes/", "BOOT-INF/lib/");
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertNotNull("Should find " + SIMPLE_CONTROLLER_CLASS_NAME,
				classLoader.loadClass(SIMPLE_CONTROLLER_CLASS_NAME));
	}

	private static void assertClassNotFound(String className, ClassLoader classLoader) {
		try {
			classLoader.loadClass(className);
			fail("Should not be successful in finding class " + className);
		} catch (ClassNotFoundException ex) {
			// Correctly indicates class not found
		}
	}

}
