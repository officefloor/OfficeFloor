/*-
 * #%L
 * OfficeFloor WAR ClassLoader
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

package net.officefloor.web.war;

import java.io.File;
import java.net.URL;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WarAwareClassLoaderFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarAwareClassLoaderFactoryTest extends OfficeFrameTestCase {

	/**
	 * Serlet class within WAR>
	 */
	private static final String SIMPLE_SERVLET_CLASS_NAME = "net.officefloor.tutorial.warhttpserver.SimpleServlet";

	/**
	 * Obtains the location of the WAR file.
	 * 
	 * @return Location of the WAR file.
	 */
	public static File getWarFile() {

		// Locate the WarHttpServer WAR file
		// (note: dependency on it should build it first)
		final String WAR_HTTP_SERVER_NAME = "WarHttpServer";
		File currentDir = new File(".");
		File warHttpServerProjectDir = new File(currentDir, "../../tutorials/" + WAR_HTTP_SERVER_NAME);
		assertTrue("INVALID TEST: can not find " + WAR_HTTP_SERVER_NAME + " project directory at "
				+ warHttpServerProjectDir.getAbsolutePath(), warHttpServerProjectDir.isDirectory());
		File warFile = null;
		for (File checkFile : new File(warHttpServerProjectDir, "target").listFiles()) {
			String fileName = checkFile.getName();
			if (fileName.startsWith(WAR_HTTP_SERVER_NAME) && fileName.toLowerCase().endsWith(".war")) {
				warFile = checkFile;
			}
		}
		assertNotNull("INVALID TEST: can not find " + WAR_HTTP_SERVER_NAME + " war file", warFile);
		return warFile;
	}

	/**
	 * WAR {@link File} to test with.
	 */
	private File warFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.warFile = getWarFile();
	}

	/**
	 * Ensure can work with default parent {@link ClassLoader}.
	 */
	public void testDefaultParentClassLoader() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory(this.getClass().getClassLoader())
				.createClassLoader(new URL[] { this.warFile.toURI().toURL() });
		assertNotNull("Should find this class due to default parent", classLoader.loadClass(this.getClass().getName()));
		assertNotNull("Should find " + SIMPLE_SERVLET_CLASS_NAME, classLoader.loadClass(SIMPLE_SERVLET_CLASS_NAME));
	}

	/**
	 * Ensure can handle no WAR {@link File}.
	 */
	public void testNoWarFileWithBoot() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory(null).createClassLoader(new URL[0]);
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertClassNotFound(OfficeFloor.class.getName(), classLoader);
		assertClassNotFound(SIMPLE_SERVLET_CLASS_NAME, classLoader);
	}

	/**
	 * Ensure can work with boot {@link ClassLoader}.
	 */
	public void testBootClassLoader() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory(null)
				.createClassLoader(new URL[] { this.warFile.toURI().toURL() });
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertNotNull("Should find " + SIMPLE_SERVLET_CLASS_NAME, classLoader.loadClass(SIMPLE_SERVLET_CLASS_NAME));
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