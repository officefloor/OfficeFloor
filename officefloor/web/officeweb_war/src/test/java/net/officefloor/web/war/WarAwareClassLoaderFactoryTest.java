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
import net.officefloor.woof.WoofLoaderSettings;

/**
 * Tests the {@link WarAwareClassLoaderFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarAwareClassLoaderFactoryTest extends OfficeFrameTestCase {

	/**
	 * Application WoOF configuration file resource path.
	 */
	private static final String APPLICATION_WOOF_PATH = WoofLoaderSettings.getWoofLoaderConfiguration()
			.getApplicationWoofPath();

	/**
	 * WAR {@link File} to test with.
	 */
	private File warFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Locate the TransactionHttpServer WAR file
		// (note: dependency on it should build it first)
		final String TRANSACTION_HTTP_SERVER_NAME = "TransactionHttpServer";
		File currentDir = new File(".");
		File transactionHttpServerProjectDir = new File(currentDir, "../../tutorials/" + TRANSACTION_HTTP_SERVER_NAME);
		assertTrue(
				"INVALID TEST: can not find " + TRANSACTION_HTTP_SERVER_NAME + " project directory at "
						+ transactionHttpServerProjectDir.getAbsolutePath(),
				transactionHttpServerProjectDir.isDirectory());
		for (File checkFile : new File(transactionHttpServerProjectDir, "target").listFiles()) {
			String fileName = checkFile.getName();
			if (fileName.startsWith(TRANSACTION_HTTP_SERVER_NAME) && fileName.toLowerCase().endsWith(".war")) {
				this.warFile = checkFile;
			}
		}
		assertNotNull("INVALID TEST: can not find " + TRANSACTION_HTTP_SERVER_NAME + " war file", this.warFile);
	}

	/**
	 * Ensure can handle no WAR {@link File}.
	 */
	public void testNoWarFile() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory().createClassLoader(new URL[0]);
		assertNotNull("Should find this class due to default parent", classLoader.loadClass(this.getClass().getName()));
		assertNotNull("Should find OfficeFloor", classLoader.loadClass(OfficeFloor.class.getName()));
		assertNull("Should not find " + APPLICATION_WOOF_PATH, classLoader.getResourceAsStream(APPLICATION_WOOF_PATH));
	}

	/**
	 * Ensure can handle no WAR {@link File}.
	 */
	public void testNoWarFileWithBoot() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory(null).createClassLoader(new URL[0]);
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertClassNotFound(OfficeFloor.class.getName(), classLoader);
		assertNull("Should not find " + APPLICATION_WOOF_PATH, classLoader.getResourceAsStream(APPLICATION_WOOF_PATH));
	}

	/**
	 * Ensure can work with default parent {@link ClassLoader}.
	 */
	public void testDefaultParentClassLoader() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory()
				.createClassLoader(new URL[] { this.warFile.toURI().toURL() });
		assertNotNull("Should find this class due to default parent", classLoader.loadClass(this.getClass().getName()));
		assertNotNull("Should find " + APPLICATION_WOOF_PATH, classLoader.getResourceAsStream(APPLICATION_WOOF_PATH));
	}

	/**
	 * Ensure can work with boot {@link ClassLoader}.
	 */
	public void testBootClassLoader() throws Exception {
		ClassLoader classLoader = new WarAwareClassLoaderFactory(null)
				.createClassLoader(new URL[] { this.warFile.toURI().toURL() });
		assertClassNotFound(this.getClass().getName(), classLoader);
		assertNotNull("Should find " + APPLICATION_WOOF_PATH, classLoader.getResourceAsStream(APPLICATION_WOOF_PATH));
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
