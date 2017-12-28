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
package net.officefloor.web.resource.classpath;

import net.officefloor.web.resource.AbstractHttpDirectoryTestCase;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.classpath.ClasspathHttpDirectory;
import net.officefloor.web.resource.classpath.ClasspathHttpResourceFactory;

/**
 * Tests the {@link ClasspathHttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpDirectoryTest extends AbstractHttpDirectoryTestCase {

	/**
	 * Ensure correct toString().
	 */
	public void testToString() {
		assertEquals(
				"Incorrect toString details",
				"ClasspathHttpDirectory: /directory/ (Class path prefix: class/path/prefix)",
				new ClasspathHttpDirectory("/directory/", "class/path/prefix")
						.toString());
	}

	/*
	 * ==================== AbstractHttpDirectoryTestCase ===================
	 */

	@Override
	protected HttpDirectory createHttpDirectory(String resourcePath,
			String... defaultFileNames) {

		// Obtain class path prefix
		String classPathPrefix = AbstractHttpDirectoryTestCase.class
				.getPackage().getName().replace('.', '/');

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Ensure fresh resource factories
		ClasspathHttpResourceFactory.clearHttpResourceFactories();

		// Create the resource factory
		ClasspathHttpResourceFactory factory = ClasspathHttpResourceFactory
				.getHttpResourceFactory(classPathPrefix, classLoader,
						defaultFileNames);

		// Create and return the HTTP directory
		HttpResource resource;
		try {
			resource = factory.createHttpResource(resourcePath);
		} catch (Exception ex) {
			throw fail(ex);
		}
		return (HttpDirectory) resource;
	}

}