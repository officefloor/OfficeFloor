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

package net.officefloor.web.resource.impl;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;

/**
 * Tests the {@link HttpResourceStore} with mock {@link ResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpResourceStoreTest extends AbstractHttpResourceStoreTestCase implements ResourceSystemFactory {

	/**
	 * {@link ResourceSystemContext}.
	 */
	private ResourceSystemContext context;

	/**
	 * Mock {@link ResourceSystem}.
	 */
	private final ResourceSystem system = (path) -> {
		String directory = this.context.getLocation();
		return Paths.get(directory, path);
	};

	/**
	 * Ensure the system is accessing the test files.
	 */
	public void testAbleToGetFile() throws IOException {
		Path path = this.system.getResource("index.html");
		assertTrue("Should find index.html", Files.isReadable(path));
	}

	/**
	 * Ensure always creates cache copy of file (ensures never deleted and that
	 * {@link FileChannel} always backed by a file).
	 */
	public void testAlwaysCacheCopyOfFile() throws Exception {

		// Setup without transformers (so uses resource system path)
		this.setupNewHttpResourceStore(this.getLocation(), null, "index.html");

		// Obtain the resource system path
		Path path = this.system.getResource("index.html");
		assertTrue("Should find index.html", Files.isReadable(path));

		// Obtain the HTTP file
		HttpResource resource = this.getHttpResourceStore().getHttpResource(this.path("/index.html"));
		assertTrue("Should have resource", resource.isExist());

		// Close the store
		this.closeHttpResourceStore();

		// Ensure the resource system file still exists
		assertTrue("Should still have resource system file", Files.exists(path));
	}

	/**
	 * Ensure can trigger resource changed.
	 */
	public void testTriggerResourceChanged() throws IOException {
		String filePath = this.path("/index.html");
		String directoryPath = this.path("/directory");
		HttpResource file = this.getHttpResourceStore().getHttpResource(filePath);
		HttpResource directory = this.getHttpResourceStore().getHttpResource(directoryPath);
		this.context.notifyResourceChanged("/index.html");
		assertNull("File should no longer be cached", this.getHttpResourceCache().getHttpResource(filePath));
		assertSame("Directory should still be cached", directory,
				this.getHttpResourceCache().getHttpResource(directoryPath));
		HttpResource reloaded = this.getHttpResourceStore().getHttpResource(filePath);
		assertNotSame("Should be different loaded HTTP file", file, reloaded);
	}

	/**
	 * Ensure can trigger all resources changed.
	 */
	public void testTriggerAllResourcesChanged() throws IOException {
		String filePath = this.path("/index.html");
		String directoryPath = this.path("/directory");
		HttpResource file = this.getHttpResourceStore().getHttpResource(filePath);
		HttpResource directory = this.getHttpResourceStore().getHttpResource(directoryPath);
		this.context.notifyResourceChanged(null);
		assertNull("File should no longer be cached", this.getHttpResourceCache().getHttpResource(filePath));
		assertNull("Directory should no longer be cached", this.getHttpResourceCache().getHttpResource(directoryPath));
		assertNotSame("Should be different loaded HTTP file", file,
				this.getHttpResourceStore().getHttpResource(filePath));
		assertNotSame("Should be different loaded HTTP directory", directory,
				this.getHttpResourceStore().getHttpResource(directoryPath));
	}

	/*
	 * =========== AbstractHttpResourceStoreTestCase ============
	 */

	@Override
	protected String getLocation() {
		return this.getStoreFilePath();
	}

	@Override
	protected Class<? extends ResourceSystemFactory> getResourceSystemService() {
		fail("Should not obtain class");
		return null;
	}

	@Override
	protected ResourceSystemFactory createResourceSystemService() throws Exception {
		return this;
	}

	/*
	 * ================= ResourceSystemFactory ==================
	 */

	@Override
	public String getProtocolName() {
		return "mock";
	}

	@Override
	public ResourceSystem createResourceSystem(ResourceSystemContext context) {
		this.context = context;
		assertEquals("Incorrect location", this.getLocation(), context.getLocation());
		return this.system;
	}

}
