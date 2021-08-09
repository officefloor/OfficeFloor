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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.filesystem.OfficeFloorFileAttributes;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpResponseBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.FileCache;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceTransformer;

/**
 * Tests the {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResourceStoreTestCase extends OfficeFrameTestCase {

	/**
	 * Default {@link FileAttribute} for direction.
	 */
	private static final FileAttribute<?>[] DIRECTORY_ATTRIBUTES = OfficeFloorFileAttributes
			.getDefaultDirectoryAttributes();

	/**
	 * Default {@link FileAttribute} for file.
	 */
	private static final FileAttribute<?>[] FILE_ATTRIBUTES = OfficeFloorFileAttributes.getDefaultFileAttributes();

	/**
	 * <p>
	 * Obtains the {@link ResourceSystemFactory} {@link Class}.
	 * <p>
	 * As the {@link ResourceSystemFactory} is loaded via a {@link ServiceFactory}
	 * this ensures it can be.
	 * 
	 * @return {@link ResourceSystemFactory}.
	 */
	protected abstract Class<? extends ResourceSystemFactory> getResourceSystemService();

	/**
	 * Obtains the location to configure the {@link ResourceSystem}.
	 * 
	 * @return Location to configure the {@link ResourceSystem}.
	 */
	protected abstract String getLocation();

	/**
	 * {@link HttpResourceStore} to test.
	 */
	private HttpResourceStoreImpl store;

	/**
	 * Creates the {@link ResourceSystemFactory}.
	 * 
	 * @return {@link ResourceSystemFactory}.
	 */
	protected ResourceSystemFactory createResourceSystemService() throws Exception {
		Class<? extends ResourceSystemFactory> serviceClass = this.getResourceSystemService();
		return serviceClass.getDeclaredConstructor().newInstance();
	}

	/**
	 * Obtains the file path to the store directory.
	 * 
	 * @return File path to the store directory.
	 */
	protected String getStoreFilePath() {
		try {
			return this.findFile(AbstractHttpResourceStoreTestCase.class, "index.html").getParentFile()
					.getCanonicalPath();
		} catch (IOException ex) {
			throw fail(ex);
		}
	}

	/**
	 * Obtains the class path to the store directory.
	 * 
	 * @return Class path to the store directory.
	 */
	protected String getStoreClassPath() {
		return this.getPackageRelativePath(AbstractHttpResourceStoreTestCase.class);
	}

	/**
	 * Obtains the {@link HttpResourceStore} for further testing.
	 * 
	 * @return {@link HttpResourceStore} for further testing.
	 */
	protected HttpResourceStore getHttpResourceStore() {
		return this.store;
	}

	/**
	 * Obtains the {@link HttpResourceCache} for further testing.
	 * 
	 * @return {@link HttpResourceCache} for further testing.
	 */
	protected HttpResourceCache getHttpResourceCache() {
		return this.store.getCache();
	}

	/**
	 * Obtains the path with possible context path prefix.
	 * 
	 * @param path Path.
	 * @return Path with context path prefix.
	 */
	protected String path(String path) {
		return path.startsWith("/") ? path : "/" + path;
	}

	/**
	 * Sets up a new {@link HttpResourceStore} for the location.
	 * 
	 * @param location                  Location.
	 * @param transformers              {@link ResourceTransformer} instances.
	 * @param directoryDefaultFileNames Directory default file names.
	 */
	protected void setupNewHttpResourceStore(String location, ResourceTransformer[] transformers,
			String... directoryDefaultFileNames) throws Exception {

		// Close the existing store
		if (this.store != null) {
			this.closeHttpResourceStore();
		}

		// Set up the new HTTP resource store
		ResourceSystemFactory factory = this.createResourceSystemService();
		this.store = new HttpResourceStoreImpl(location, factory, (name) -> new MockFileCache(name), transformers,
				directoryDefaultFileNames);
	}

	/**
	 * Closes the {@link HttpResourceStore}.
	 */
	protected void closeHttpResourceStore() throws IOException {

		// Close the existing HTTP resource store
		this.store.close();

		// Ensure all files are deleted after close
		for (Path path : this.tempPaths) {
			assertFalse("Should delete file on close " + path.toAbsolutePath(), Files.exists(path));
		}
		this.tempPaths.clear();

		// Clear the store
		this.store = null;
	}

	/**
	 * Temporary {@link Path} instances used that should be cleaned on close of
	 * {@link HttpResourceStore}.
	 */
	private final List<Path> tempPaths = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create the store
		String location = this.getLocation();
		this.setupNewHttpResourceStore(location, new ResourceTransformer[] { transformer, transformer }, "index.html");
	}

	@Override
	protected void tearDown() throws Exception {

		// Close the HTTP resource store
		if (this.store != null) {
			this.closeHttpResourceStore();
		}

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Obtains the file.
	 */
	public void testObtainHttpFile() throws IOException {
		String path = this.path("/index.html");
		HttpResource resource = this.store.getHttpResource(path);
		assertEquals("Incorrect path", path, resource.getPath());
		assertTrue("Resource should exist", resource.isExist());
		assertTrue("Should be file", resource instanceof HttpFile);
		HttpFile file = (HttpFile) resource;
		assertEquals("Incorrect content encoding", "mock", file.getContentEncoding().getValue());
		assertEquals("Incorrect content type", "text/html", file.getContentType().getValue());
		assertEquals("Incorrect charset", Charset.defaultCharset(), file.getCharset());
	}

	/**
	 * Ensure the {@link HttpFile} is being cached.
	 */
	public void testCacheHttpFile() throws IOException {
		String path = this.path("/index.html");

		// Should not be available in cache
		HttpResource resource = this.store.getCache().getHttpResource(path);
		assertNull("Should not be availabe in cache yet", resource);

		// Ensure that cache resource
		resource = this.store.getHttpResource(path);
		assertNotNull("Invalid test: should have resource", resource);
		HttpResource cached = this.store.getHttpResource(path);
		assertSame("HttpFile should be cached", resource, cached);

		// Ensure that also now available in cache
		cached = this.store.getCache().getHttpResource(path);
		assertSame("HttpFile should be available in cache", resource, cached);
	}

	/**
	 * Ensure can write the {@link HttpFile}.
	 */
	public void testWriteHttpFile() throws IOException {
		String path = this.path("/index.html");
		HttpFile file = (HttpFile) this.store.getHttpResource(path);
		MockHttpResponseBuilder mock = MockHttpServer.mockResponse();
		file.writeTo(mock);
		MockHttpResponse response = mock.build();
		assertEquals("Incorrect content-encoding", "mock", response.getHeader("content-encoding").getValue());
		assertEquals("Incorrect content-type", "text/html", response.getHeader("content-type").getValue());
		assertEquals("Incorrect entity", "<html><body>Hello World</body></html>", response.getEntity(null));
	}

	/**
	 * Ensure the {@link FileChannel} is not available after write.
	 */
	public void testFileChannelNotAvailableAfterComplete() throws IOException {

		// Write HTTP file to response
		String filePath = this.path("/index.html");
		HttpFile file = (HttpFile) this.getHttpResourceStore().getHttpResource(filePath);

		// Close the HTTP resource store
		this.closeHttpResourceStore();

		// Ensure file channel still available after close
		MockHttpResponseBuilder mockResponse = MockHttpServer.mockResponse();
		try {
			file.writeTo(mockResponse);
			fail("Should be closed, so can not write successfully");
		} catch (ClosedChannelException ex) {
			// Should be closed
		}
	}

	/**
	 * Ensure can re-use the {@link FileChannel} within the {@link HttpFile}.
	 */
	public void testReuseHttpFile() throws IOException {

		// Write HTTP file to response
		String filePath = this.path("/index.html");
		HttpFile file = (HttpFile) this.getHttpResourceStore().getHttpResource(filePath);

		// Write multiple times
		for (int i = 0; i < 100; i++) {
			MockHttpResponseBuilder mockResponse = MockHttpServer.mockResponse();
			file.writeTo(mockResponse);
			MockHttpResponse response = mockResponse.build();
			assertEquals("Should still have file channel", "<html><body>Hello World</body></html>",
					response.getEntity(null));
		}
	}

	/**
	 * Obtain the directory.
	 */
	public void testObtainHttpDirectory() throws IOException {
		String path = this.path("/directory");
		HttpResource resource = this.store.getHttpResource(path);
		assertEquals("Incorrect path", path, resource.getPath());
		assertTrue("Resource should exist", resource.isExist());
		assertTrue("Should be directory", resource instanceof HttpDirectory);
		HttpDirectory directory = (HttpDirectory) resource;

		// Ensure able to obtain default directory resource
		HttpFile defaultFile = directory.getDefaultHttpFile();
		assertNotNull("Should have default file", defaultFile);
		assertEquals("Incorrect default file path", path + "/index.html", defaultFile.getPath());
		assertSame("Ensure same file when obtaining", defaultFile, this.store.getHttpResource(defaultFile.getPath()));
	}

	/**
	 * Ensure the {@link HttpDirectory} is being cached.
	 */
	public void testCacheHttpDirectory() throws IOException {
		String path = this.path("/directory");

		// Should not be available in cache
		HttpResource resource = this.store.getCache().getHttpResource(path);
		assertNull("Should not be availabe in cache yet", resource);

		// Ensure that cache resource
		resource = this.store.getHttpResource(path);
		assertNotNull("Invalid test: should have resource", resource);
		HttpResource cached = this.store.getHttpResource(path);
		assertSame("HttpDirectory should be cached", resource, cached);

		// Ensure that also now available in cache
		cached = this.store.getCache().getHttpResource(path);
		assertSame("HttpDirectory should be available in cache", resource, cached);
	}

	/**
	 * Ensure able to obtain not exist {@link HttpResource}.
	 */
	public void testNotExistResource() throws IOException {
		String path = this.path("/not_exist");
		HttpResource resource = this.store.getHttpResource(path);
		assertEquals("Incorrect path", path, resource.getPath());
		assertFalse("Should not exist", resource.isExist());
		assertFalse("Should not be file", resource instanceof HttpFile);
		assertFalse("Should not be directory", resource instanceof HttpDirectory);
	}

	/**
	 * Ensure not cache not existing {@link HttpResource}. This avoids potential out
	 * of memory due to lots of requests for not existing resources.
	 */
	public void testNoteCacheNotExistingHttpResource() throws IOException {
		String path = this.path("/not_exist");
		HttpResource resource = this.store.getHttpResource(path);
		HttpResource notCached = this.store.getHttpResource(path);
		assertNotSame("Not existing HttpResource should NOT be cached", resource, notCached);
		assertNull("Should not cache not exsting HttpResource", this.store.getCache().getHttpResource(path));
	}

	/**
	 * Ensure handle no default {@link HttpResource} for {@link HttpDirectory}.
	 */
	public void testNoDirectoryDefaultFile() throws IOException {
		HttpDirectory directory = (HttpDirectory) this.store.getHttpResource(this.path("/directory/sub_directory"));
		HttpFile defaultFile = directory.getDefaultHttpFile();
		assertNull("Should be no default file", defaultFile);
	}

	/**
	 * Mock {@link ResourceTransformer}.
	 */
	private final ResourceTransformer transformer = (context) -> {
		Path newFile = context.createFile();
		Files.copy(context.getResource(), newFile, StandardCopyOption.REPLACE_EXISTING);
		context.setContentEncoding(new HttpHeaderValue("mock"));
		context.setTransformedResource(newFile);
	};

	private class MockFileCache implements FileCache {

		private final Path tempDirectory;

		public MockFileCache(String name) throws IOException {
			name = name.replace('/', '_');
			this.tempDirectory = Files.createTempDirectory(name + "-", DIRECTORY_ATTRIBUTES);
			AbstractHttpResourceStoreTestCase.this.tempPaths.add(this.tempDirectory);
		}

		@Override
		public Path createFile(String name) throws IOException {
			String suffix = name.replace('/', '_');
			Path file = Files.createTempFile(this.tempDirectory, null, "-" + suffix, FILE_ATTRIBUTES);
			assertTrue("Should have file " + name, Files.isRegularFile(file));
			AbstractHttpResourceStoreTestCase.this.tempPaths.add(file);
			return file;
		}

		@Override
		public Path createDirectory(String name) throws IOException {
			String prefix = name.replace('/', '_');
			Path directory = Files.createTempDirectory(this.tempDirectory, prefix + "-", DIRECTORY_ATTRIBUTES);
			assertTrue("Should have directory " + name, Files.isDirectory(directory));
			AbstractHttpResourceStoreTestCase.this.tempPaths.add(directory);
			return directory;
		}

		@Override
		public void close() throws IOException {

			// Ensure contains no files before deleting
			Files.list(this.tempDirectory)
					.forEach((file) -> fail("Should be empty file cache " + file.toAbsolutePath()));

			// Delete the temporary directory
			Files.delete(this.tempDirectory);
		}
	}

}
