/*-
 * #%L
 * Web resources
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
	 * Tests retrieving directory from a JAR (as most tests are using directories).
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

	/**
	 * Tests fallback to {@link ClassLoader}.
	 */
	public void testFallbackToClassLoader() throws Exception {

		// Derive the path
		String[] paths = CLASS_LOADER_EXTRA_CLASS_NAME.split("\\.");
		String location = paths[0];
		String fileName = "/" + paths[1] + ".class";

		// Ensure not on java class path
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree(location);
		assertEquals("Invalid test as should not find location", 0, tree.getChildren().length);

		// Provide class loader
		ClassLoader existingClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(createNewClassLoader());

			// Setup to find extra class
			this.setupNewHttpResourceStore(location, null, fileName);

			// Test obtain file via class loader
			HttpResource resource = this.getHttpResourceStore().getHttpResource(fileName);
			assertTrue("Should be file", resource instanceof HttpFile);
			HttpFile file = (HttpFile) resource;
			assertEquals("Incorrect file", fileName, file.getPath());

			// Obtain default file via class loader
			resource = this.getHttpResourceStore().getHttpResource("/");
			assertTrue("Should be directory", resource instanceof HttpDirectory);
			HttpDirectory directory = (HttpDirectory) resource;
			file = directory.getDefaultHttpFile();
			assertEquals("Incorrect default file", fileName, file.getPath());

			// Ensure not find resource
			resource = this.getHttpResourceStore().getHttpResource("/not-found");
			assertFalse("Should not find resource", resource.isExist());

		} finally {
			Thread.currentThread().setContextClassLoader(existingClassLoader);
		}
	}

	/*
	 * ============== AbstractHttpResourceStoreTestCase =================
	 */

	@Override
	protected String getLocation() {
		return this.getStoreClassPath();
	}

	@Override
	protected Class<DefaultConstructorClasspathResourceSystemFactory> getResourceSystemService() {
		return DefaultConstructorClasspathResourceSystemFactory.class;
	}

	public static class DefaultConstructorClasspathResourceSystemFactory extends ClasspathResourceSystemFactory {

		public DefaultConstructorClasspathResourceSystemFactory() {
			super(Thread.currentThread().getContextClassLoader());
		}
	}

}
