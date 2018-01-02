/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.resource.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpResponseBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
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
	 * Obtains the {@link ResourceSystem}.
	 * 
	 * @return {@link ResourceSystem}.
	 */
	protected abstract ResourceSystemFactory getResourceSystemFactory();

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
	 * Sets up a new {@link HttpResourceStore} for the location.
	 * 
	 * @param location
	 *            Location.
	 * @param directoryDefaultFileNames
	 *            Directory default file names.
	 */
	protected void setupNewHttpResourceStore(String location, String... directoryDefaultFileNames) throws IOException {

		// Close the existing HTTP resource store
		if (this.store != null) {
			this.store.close();

			// Ensure all files are deleted after close
			for (Path path : this.tempPaths) {
				assertFalse("Should delete file on close " + path, Files.exists(path));
			}
		}

		// Set up the new HTTP resource store
		ResourceSystemFactory factory = this.getResourceSystemFactory();
		this.store = new HttpResourceStoreImpl(location, factory, (name) -> new MockFileCache(name),
				new ResourceTransformer[] { transformer }, directoryDefaultFileNames);
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
		this.setupNewHttpResourceStore(location, "index.html");
	}

	@Override
	protected void tearDown() throws Exception {

		// Close the HTTP resource store
		this.store.close();

		// Ensure all files are deleted after close
		for (Path path : this.tempPaths) {
			assertFalse("Should delete file on close " + path, Files.exists(path));
		}

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Obtains the file.
	 */
	public void testObtainHttpFile() throws IOException {
		HttpResource resource = this.store.getHttpResource("/index.html");
		assertEquals("Incorrect path", "/index.html", resource.getPath());
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

		// Should not be available in cache
		HttpResource resource = this.store.getCache().getHttpResource("/index.html");
		assertNull("Should not be availabe in cache yet", resource);

		// Ensure that cache resource
		resource = this.store.getHttpResource("/index.html");
		assertNotNull("Invalid test: should have resource", resource);
		HttpResource cached = this.store.getHttpResource("/index.html");
		assertSame("HttpFile should be cached", resource, cached);

		// Ensure that also now available in cache
		cached = this.store.getCache().getHttpResource("/index.html");
		assertSame("HttpFile should be available in cache", resource, cached);
	}

	/**
	 * Ensure can write the {@link HttpFile}.
	 */
	public void testWriteHttpFile() throws IOException {
		HttpFile file = (HttpFile) this.store.getHttpResource("/index.html");
		MockHttpResponseBuilder mock = MockHttpServer.mockResponse();
		file.writeTo(mock);
		MockHttpResponse response = mock.build();
		assertEquals("Incorrect content-encoding", "mock", response.getHeader("content-encoding").getValue());
		assertEquals("Incorrect content-type", "text/html", response.getHeader("content-type").getValue());
		assertEquals("Incorrect entity", "hello world", response.getEntity(null));
	}

	/**
	 * Obtain the directory.
	 */
	public void testObtainHttpDirectory() throws IOException {
		HttpResource resource = this.store.getHttpResource("/directory");
		assertEquals("Incorrect path", "/directory", resource.getPath());
		assertTrue("Resource should exist", resource.isExist());
		assertTrue("Should be directory", resource instanceof HttpDirectory);
		HttpDirectory directory = (HttpDirectory) resource;

		// Ensure able to obtain default directory resource
		HttpFile defaultFile = directory.getDefaultHttpFile();
		assertEquals("Incorrect default file path", "/directory/index.html", defaultFile.getPath());
		assertSame("Ensure same file when obtaining", defaultFile, this.store.getHttpResource(defaultFile.getPath()));
	}

	/**
	 * Ensure the {@link HttpDirectory} is being cached.
	 */
	public void testCacheHttpDirectory() throws IOException {

		// Should not be available in cache
		HttpResource resource = this.store.getCache().getHttpResource("/directory");
		assertNull("Should not be availabe in cache yet", resource);

		// Ensure that cache resource
		resource = this.store.getHttpResource("/directory");
		assertNotNull("Invalid test: should have resource", resource);
		HttpResource cached = this.store.getHttpResource("/directory");
		assertSame("HttpDirectory should be cached", resource, cached);

		// Ensure that also now available in cache
		cached = this.store.getCache().getHttpResource("/directory");
		assertSame("HttpDirectory should be available in cache", resource, cached);
	}

	/**
	 * Ensure able to obtain not exist {@link HttpResource}.
	 */
	public void testNotExistResource() throws IOException {
		HttpResource resource = this.store.getHttpResource("/not_exist");
		assertEquals("Incorrect path", "/not_exist", resource.getPath());
		assertFalse("Should not exist", resource.isExist());
		assertFalse("Should not be file", resource instanceof HttpFile);
		assertFalse("Should not be directory", resource instanceof HttpDirectory);
	}

	/**
	 * Ensure not cache not existing {@link HttpResource}. This avoids potential
	 * out of memory due to lots of requests for not existing resources.
	 */
	public void testNoteCacheNotExistingHttpResource() throws IOException {
		HttpResource resource = this.store.getHttpResource("/not_exist");
		HttpResource notCached = this.store.getHttpResource("/not_exist");
		assertNotSame("Not existing HttpResource should NOT be cached", resource, notCached);
		assertNull("Should not cache not exsting HttpResource", this.store.getCache().getHttpResource("/not_exist"));
	}

	/**
	 * Ensure handle no default {@link HttpResource} for {@link HttpDirectory}.
	 */
	public void testNoDirectoryDefaultFile() throws IOException {
		HttpDirectory directory = (HttpDirectory) this.store.getHttpResource("/directory/sub_directory");
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
			this.tempDirectory = Files.createTempDirectory(name,
					PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---")));
			AbstractHttpResourceStoreTestCase.this.tempPaths.add(this.tempDirectory);
		}

		@Override
		public Path createFile(String name) throws IOException {
			name = name.replace('/', '_');
			Path file = Files.createTempFile(this.tempDirectory, null, name,
					PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r-----")));
			AbstractHttpResourceStoreTestCase.this.tempPaths.add(file);
			return file;
		}

		@Override
		public Path createDirectory(String name) throws IOException {
			name = name.replace('/', '_');
			Path directory = Files.createTempDirectory(this.tempDirectory, name,
					PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---")));
			AbstractHttpResourceStoreTestCase.this.tempPaths.add(directory);
			return directory;
		}
	}

}