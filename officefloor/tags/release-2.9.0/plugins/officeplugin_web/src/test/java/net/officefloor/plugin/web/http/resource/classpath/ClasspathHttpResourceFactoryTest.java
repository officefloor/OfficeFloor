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
package net.officefloor.plugin.web.http.resource.classpath;

import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactoryTestCase;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;

/**
 * Tests the {@link ClasspathHttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpResourceFactoryTest extends
		AbstractHttpResourceFactoryTestCase {

	/**
	 * Tests retrieving directory from a JAR (as most tests are using
	 * directories).
	 * 
	 * Note test expects JUnit 4.8.2 on class path.
	 */
	public void testObtainDirectoryFromJar() throws Exception {
		HttpResourceFactory factory = this.createHttpResourceFactory("");
		HttpResource directory = factory.createHttpResource("/junit");
		this.assertHttpDirectory(directory, "/junit/", "/junit/extensions/",
				"/junit/framework/", "/junit/runner/", "/junit/textui/");
	}

	/**
	 * Tests retrieving file from a JAR (as most tests are using directories).
	 * 
	 * Note test expects JUnit 4.8.2 on class path.
	 */
	public void testObtainFileFromJar() throws Exception {
		HttpResourceFactory factory = this.createHttpResourceFactory("");
		HttpResource file = factory
				.createHttpResource("/junit/framework/Test.class");
		assertTrue("Expecting file", file instanceof HttpFile);
		assertEquals("Incorrect file path", "/junit/framework/Test.class",
				file.getPath());
		assertTrue("File should exist", file.isExist());
	}

	/*
	 * ============== AbstractHttpResourceFactoryTestCase ==================
	 */

	@Override
	protected HttpResourceFactory createHttpResourceFactory(String prefix) {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Clear to create new instance
		ClasspathHttpResourceFactory.clearHttpResourceFactories();

		// Create the factory to obtain files from test package
		HttpResourceFactory factory = ClasspathHttpResourceFactory
				.getHttpResourceFactory(prefix, classLoader, "index.html");

		// Return the factory
		return factory;
	}

}