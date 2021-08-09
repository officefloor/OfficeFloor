/*-
 * #%L
 * Web resources
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
