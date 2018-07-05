/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.resource.classpath;

import org.junit.Assert;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.impl.AbstractHttpResourceStoreTestCase;

/**
 * Tests the {@link ClasspathResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathResourceSystemTest extends AbstractHttpResourceStoreTestCase {

	/**
	 * Tests retrieving directory from a JAR (as most tests are using
	 * directories).
	 * 
	 * Note test expects JUnit on class path.
	 */
	public void testObtainDirectoryFromJar() throws Exception {

		// Derive the paths
		String directoryPath = "/" + Assert.class.getPackage().getName().replace('.', '/');
		String fileName = "/" + Assert.class.getSimpleName() + ".class";
		String filePath = directoryPath + fileName;

		// Setup to find Assert as default file
		this.setupNewHttpResourceStore("", null, fileName);

		// Test obtain directory from jar file
		HttpResource resource = this.getHttpResourceStore().getHttpResource(directoryPath);
		assertTrue("Should be directory", resource instanceof HttpDirectory);
		HttpDirectory directory = (HttpDirectory) resource;
		HttpFile defaultFile = directory.getDefaultHttpFile();
		assertEquals("Should have default file", filePath, defaultFile.getPath());
	}

	/**
	 * Tests retrieving file from a JAR (as most tests are using directories).
	 * 
	 * Note test expects JUnit 4.8.2 on class path.
	 */
	public void testObtainFileFromJar() throws Exception {

		// Derive the path
		String path = "/" + Assert.class.getName().replace('.', '/') + ".class";

		// Setup to find Assert file
		this.setupNewHttpResourceStore("", null);

		// Test obtain file from jar file
		HttpResource resource = this.getHttpResourceStore().getHttpResource(path);
		assertTrue("Should be file", resource instanceof HttpFile);
		HttpFile file = (HttpFile) resource;
		assertEquals("Incorrect file", path, file.getPath());
	}

	/*
	 * ============== AbstractHttpResourceStoreTestCase ==================
	 */

	@Override
	protected String getLocation() {
		return this.getStoreClassPath();
	}

	@Override
	protected Class<ClasspathResourceSystemService> getResourceSystemService() {
		return ClasspathResourceSystemService.class;
	}

}