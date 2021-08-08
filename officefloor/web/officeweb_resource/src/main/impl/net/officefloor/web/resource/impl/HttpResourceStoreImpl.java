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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tika.Tika;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.FileCache;
import net.officefloor.web.resource.spi.FileCacheFactory;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerContext;
import net.officefloor.web.route.WebRouter;

/**
 * {@link HttpResourceStore} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceStoreImpl implements HttpResourceStore, ResourceSystemContext, Closeable {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpResourceStoreImpl.class.getName());

	/**
	 * All working files, so delete on close and only allow read access to contents.
	 */
	private static final OpenOption[] OPEN_OPTIONS = new OpenOption[] { StandardOpenOption.READ,
			StandardOpenOption.DELETE_ON_CLOSE };

	/**
	 * Default <code>Content-Type</code> should it not be determined for the
	 * resource.
	 */
	private static final String DEFAULT_CONTENT_TYPE = "application/octet";

	/**
	 * {@link Tika} to determine content type if Java defaults can not.
	 */
	private static volatile Tika tikaSingleton = null;

	/**
	 * {@link Map} of {@link HttpResource} path to the current
	 * {@link CreateSingleton} for the {@link HttpResource}.
	 */
	private final Map<String, CreateSingleton> resolvingSingletons = new HashMap<>();

	/**
	 * {@link Map} of {@link HttpResource} path to the {@link HttpResource}.
	 */
	private final Map<String, AbstractHttpResource> cache = new ConcurrentHashMap<>();

	/**
	 * Files to clean up on close of this {@link HttpResourceStore}.
	 */
	private final Deque<Path> closeCleanupFiles = new ConcurrentLinkedDeque<>();

	/**
	 * {@link HttpResourceCache}.
	 */
	private final HttpResourceCacheImpl httpResourceCache = new HttpResourceCacheImpl();

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * {@link ResourceSystem}.
	 */
	private final ResourceSystem resourceSystem;

	/**
	 * {@link ResourceTransformer} instances.
	 */
	private final ResourceTransformer[] transformers;

	/**
	 * Cache of the <code>Content-Type</code> {@link HttpHeaderValue} instances to
	 * reduce GC.
	 */
	private final Map<String, HttpHeaderValue> contentTypes = new ConcurrentHashMap<>();

	/**
	 * {@link Charset} for {@link ResourceSystem}.
	 */
	private volatile Charset charset = Charset.defaultCharset();

	/**
	 * {@link FileCache}.
	 */
	private final FileCache fileCache;

	/**
	 * Directory default resource names.
	 */
	private final String[] directoryDefaultResourceNames;

	/**
	 * Instantiate.
	 * 
	 * @param location                      Location for the
	 *                                      {@link ResourceSystemContext}.
	 * @param resourceSystemService         {@link ResourceSystemFactory}.
	 * @param fileCacheFactory              {@link FileCacheFactory}.
	 * @param transformers                  {@link ResourceTransformer} instances.
	 * @param directoryDefaultResourceNames Directory default resource names.
	 * @throws IOException If fails to instantiate the {@link HttpResourceStore}.
	 */
	public HttpResourceStoreImpl(String location, ResourceSystemFactory resourceSystemService,
			FileCacheFactory fileCacheFactory, ResourceTransformer[] transformers,
			String[] directoryDefaultResourceNames) throws IOException {
		this.location = location;

		// Specify the transformers
		this.transformers = transformers != null ? transformers : new ResourceTransformer[0];

		// Create the file cache (handle windows C:\dev)
		String cacheName = location.replace(':', '_').replace(File.separatorChar, '_');
		this.fileCache = fileCacheFactory.createFileCache(cacheName);

		// Create the directory default resource names
		this.directoryDefaultResourceNames = new String[directoryDefaultResourceNames.length];
		for (int i = 0; i < this.directoryDefaultResourceNames.length; i++) {
			String defaultName = directoryDefaultResourceNames[i];
			if (!defaultName.startsWith("/")) {
				defaultName = "/" + defaultName;
			}
			this.directoryDefaultResourceNames[i] = WebRouter.transformToCanonicalPath(defaultName);
		}

		// Create the resource system
		this.resourceSystem = resourceSystemService.createResourceSystem(this);
	}

	/**
	 * Obtains the {@link HttpResourceCache}.
	 * 
	 * @return {@link HttpResourceCache}.
	 */
	public HttpResourceCache getCache() {
		return this.httpResourceCache;
	}

	/*
	 * ==================== HttpResourceStore ======================
	 */

	@Override
	public HttpResource getHttpResource(String path) throws IOException {

		// Ensure path is canonical
		path = WebRouter.transformToCanonicalPath(path);

		// Determine if have cached
		HttpResource cachedResource = this.httpResourceCache.getSafeHttpResource(path);
		if (cachedResource != null) {
			return cachedResource;
		}

		// Obtain the create singleton
		CreateSingleton singleton;
		synchronized (this.resolvingSingletons) {

			// Determine if cached
			cachedResource = this.httpResourceCache.getSafeHttpResource(path);
			if (cachedResource != null) {
				return cachedResource;
			}

			// Not cached, so resolve singleton
			singleton = this.resolvingSingletons.get(path);
			if (singleton == null) {
				singleton = new CreateSingleton(path);
				this.resolvingSingletons.put(path, singleton);
			}
		}

		// Obtain the singleton instance
		AbstractHttpResource resource = singleton.createHttpResource();

		// Remove this singleton from currently resolving
		synchronized (this.resolvingSingletons) {

			// Cache the HTTP resource
			if (resource.isExist()) {
				this.cache.put(path, resource);
			}

			// As cached, now remove from resolving
			this.resolvingSingletons.remove(path);
		}

		// Return the resource
		return resource;
	}

	@Override
	public HttpFile getDefaultHttpFile(HttpDirectory directory) throws IOException {

		// Iterate over default file names to find file
		for (int i = 0; i < this.directoryDefaultResourceNames.length; i++) {
			String filePath = directory.getPath() + this.directoryDefaultResourceNames[i];
			HttpResource resource = this.getHttpResource(filePath);
			if ((resource != null) && (resource instanceof HttpFile)) {

				// Found the default HTTP file
				return (HttpFile) resource;
			}
		}

		// As here, no default HTTP file
		return null;
	}

	/*
	 * ================== ResourceSystemContext ====================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public String[] getDirectoryDefaultResourceNames() {
		return this.directoryDefaultResourceNames;
	}

	@Override
	public HttpResourceStore getHttpResourceStore() {
		return this;
	}

	@Override
	public Path createFile(String name) throws IOException {

		// Ensure safe name for file
		name = name.replace('/', '_');

		// Create and register the new file
		Path newFile = HttpResourceStoreImpl.this.fileCache.createFile(name);
		this.closeCleanupFiles.push(newFile);

		// Return the new file
		return newFile;
	}

	@Override
	public Path createDirectory(String name) throws IOException {

		// Ensure safe name for file
		name = name.replace('/', '_');

		// Create and register the new directory
		Path newDirectory = HttpResourceStoreImpl.this.fileCache.createDirectory(name);
		this.closeCleanupFiles.push(newDirectory);

		// Return the new directory
		return newDirectory;
	}

	@Override
	public void setCharset(Charset charset) {
		this.charset = charset != null ? charset : Charset.defaultCharset();
	}

	@Override
	public void notifyResourceChanged(String resourcePath) {

		// Determine if have path
		String httpPath;
		if (resourcePath == null) {
			httpPath = null;

		} else {
			// Ensure path is canonical
			httpPath = WebRouter.transformToCanonicalPath(resourcePath);
		}

		// Clear HTTP resources
		List<AbstractHttpResource> cleanupResources = new ArrayList<>(httpPath == null ? this.cache.size() : 1);
		synchronized (this.resolvingSingletons) {
			if (httpPath != null) {

				// Remove the specific entry from cache
				AbstractHttpResource resource = this.cache.remove(httpPath);
				if (resource != null) {
					cleanupResources.add(resource);
				}

			} else {

				// Remove all entries from cache
				this.cache.forEach((path, resource) -> {
					cleanupResources.add(resource);
					this.cache.remove(path);
				});
			}
		}
		for (AbstractHttpResource cleanupResource : cleanupResources) {
			try {
				cleanupResource.close();
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING,
						"Failed to clean up " + HttpResource.class.getSimpleName() + " " + cleanupResource.getPath(),
						ex);
			}
		}
	}

	/*
	 * ==================== Closeable ======================
	 */

	@Override
	public void close() throws IOException {

		// Close all the HTTP resources
		for (AbstractHttpResource resource : this.cache.values()) {
			resource.close();
		}

		// Clean up files
		Path file;
		while ((file = this.closeCleanupFiles.pollFirst()) != null) {
			Files.delete(file);
		}

		// Close the file cache
		this.fileCache.close();
	}

	/**
	 * {@link HttpResourceCache} implementation.
	 */
	private class HttpResourceCacheImpl implements HttpResourceCache {

		/**
		 * Obtains the {@link HttpResource} assuming input is canonical path.
		 * 
		 * @param canonicalPath Canonical path to the {@link HttpResource}.
		 * @return {@link HttpResource} or <code>null</code> if no resource at path.
		 */
		private HttpResource getSafeHttpResource(String canonicalPath) {
			return HttpResourceStoreImpl.this.cache.get(canonicalPath);
		}

		/*
		 * ================ HttpResourceCache ====================
		 */

		@Override
		public HttpResource getHttpResource(String path) throws IOException {

			// Obtain the canonical path
			String canonicalPath = WebRouter.transformToCanonicalPath(path);

			// Obtain and return the resource
			return this.getSafeHttpResource(canonicalPath);
		}
	}

	/**
	 * <p>
	 * Promise to create the {@link HttpResource}.
	 * <p>
	 * This avoids the {@link HttpResource} being created multiple times.
	 */
	private class CreateSingleton {

		/**
		 * Path for the resource within the {@link ResourceSystem}.
		 */
		private final String resourcePath;

		/**
		 * Singleton {@link AbstractHttpResource}.
		 */
		private AbstractHttpResource singletonHttpResource = null;

		/**
		 * Potential creation {@link IOException}.
		 */
		private IOException creationFailure = null;

		/**
		 * Instantiate.
		 * 
		 * @param resourcePath Path for the resource within the {@link ResourceSystem}.
		 */
		private CreateSingleton(String resourcePath) {
			this.resourcePath = resourcePath;
		}

		/**
		 * Creates the {@link AbstractHttpResource}.
		 * 
		 * @return {@link AbstractHttpResource}.
		 * @throws IOException If fails to create the {@link AbstractHttpResource}.
		 */
		@SuppressWarnings("resource")
		private synchronized AbstractHttpResource createHttpResource() throws IOException {

			// Determine if have resolved resource (by another thread)
			if (this.singletonHttpResource != null) {
				return this.singletonHttpResource;
			}

			// Determine if failure in resolving
			if (this.creationFailure != null) {
				throw this.creationFailure;
			}

			// Easy access to HTTP resource store
			HttpResourceStoreImpl store = HttpResourceStoreImpl.this;

			// Not yet resolved, so resolve in critical section
			try {

				// Obtain the path to the resource
				final Path resource = store.resourceSystem.getResource(this.resourcePath);

				// Determine if have resource
				if ((resource == null) || (Files.notExists(resource))) {
					return new NotExistHttpResource(this.resourcePath);
				}

				// Determine if directory
				if (Files.isDirectory(resource)) {
					this.singletonHttpResource = new HttpDirectoryImpl(this.resourcePath, store);
					return this.singletonHttpResource;
				}

				// Determine the content type of resource
				String contentType = null; // Files.probeContentType(resource);
				HttpHeaderValue contentTypeHeaderValue;
				if ((contentType == null) || DEFAULT_CONTENT_TYPE.equals(contentType)) {
					// Could not determine, so attempt with Tika
					Tika tika = tikaSingleton;
					if (tika == null) {
						// Ok to create duplicates, as last will be one to reuse
						tika = new Tika();
						tikaSingleton = tika;
					}
					contentType = tika.detect(resource);
					if (contentType == null) {
						// Not able to determine, so use default
						contentType = DEFAULT_CONTENT_TYPE;
					}
				}

				// Obtain the content type
				contentTypeHeaderValue = store.contentTypes.get(contentType);
				if (contentTypeHeaderValue == null) {
					// Not cached, so create and cache
					contentTypeHeaderValue = new HttpHeaderValue(contentType);
					store.contentTypes.put(contentType, contentTypeHeaderValue);
				}

				// Undertake transformation of resource
				// (typically file per transform)
				List<Path> cleanUpFiles = new ArrayList<>(
						store.transformers.length == 0 ? 1 : store.transformers.length);
				ResourceTransformerContextImpl context = new ResourceTransformerContextImpl(this.resourcePath, resource,
						contentTypeHeaderValue, cleanUpFiles);
				for (int i = 0; i < store.transformers.length; i++) {
					ResourceTransformer transformer = store.transformers[i];
					transformer.transform(context);
				}

				// Determine if resulted in the original resource
				if (Files.isSameFile(resource, context.resource)) {

					/*
					 * Must make copy of file. This is so the file channel is always backed by a
					 * file. Specifically, if the file is altered within the resource system outside
					 * the control of the HTTP resource store, then this needs to be managed rather
					 * than breaking the file channel.
					 * 
					 * Furthermore, it ensures that system does not accidently delete the source
					 * file.
					 */
					Path copy = context.createFile();
					Files.copy(context.resource, copy, StandardCopyOption.REPLACE_EXISTING);
					context.resource = copy;
				}

				// Clean unused files (in reverse order created)
				Collections.reverse(context.cleanupFiles);
				for (Path cleanUpFile : context.cleanupFiles) {
					if (!(Files.isSameFile(context.resource, cleanUpFile))) {
						Files.delete(cleanUpFile);
					}
				}

				// Create the HTTP file
				FileChannel fileChannel = FileChannel.open(context.resource, OPEN_OPTIONS);
				HttpFileImpl httpFile = new HttpFileImpl(this.resourcePath, context.resource, fileChannel,
						context.contentEncoding, context.contentType, context.getCharset());

				// Flag HTTP resource as resolved
				this.singletonHttpResource = httpFile;

				// Return the resolve HTTP resource
				return this.singletonHttpResource;

			} catch (IOException ex) {
				// Capture failure in resolving singleton
				this.creationFailure = ex;

				// Propagate the failure
				throw this.creationFailure;
			}
		}
	}

	/**
	 * {@link ResourceTransformerContext} implementation.
	 */
	private class ResourceTransformerContextImpl implements ResourceTransformerContext {

		/**
		 * Path of the {@link HttpResource} to use as name for cached files. This aids
		 * identifying the file for debugging.
		 */
		private final String resourcePath;

		/**
		 * Index of the next file. This aids identifying the file for debugging.
		 */
		private int fileIndex = 0;

		/**
		 * {@link Path} to the resource.
		 */
		private Path resource;

		/**
		 * <code>Content-Type</code> {@link HttpHeaderValue}.
		 */
		private HttpHeaderValue contentType;

		/**
		 * {@link Charset}.
		 */
		private Charset charset = null;

		/**
		 * <code>Content-Encoding</code> {@link HttpHeaderValue}.
		 */
		private HttpHeaderValue contentEncoding = null;

		/**
		 * {@link Path} instances to the files that require clean up.
		 */
		private final List<Path> cleanupFiles;

		/**
		 * Instantiate.
		 * 
		 * @param resourcePath Path to {@link HttpResource}.
		 * @param resource     {@link Path} to the {@link ResourceSystem} resource.
		 * @param contentType  <code>Content-Type</code> {@link HttpHeaderValue}.
		 * @param cleanupFiles {@link List} of cleanup files.
		 */
		private ResourceTransformerContextImpl(String resourcePath, Path resource, HttpHeaderValue contentType,
				List<Path> cleanupFiles) {
			this.resourcePath = resourcePath;
			this.resource = resource;
			this.contentType = contentType;
			this.cleanupFiles = cleanupFiles;
		}

		/*
		 * =============== ResourceTransformerContext ===================
		 */

		@Override
		public String getPath() {
			return this.resourcePath;
		}

		@Override
		public Path getResource() {
			return this.resource;
		}

		@Override
		public Path createFile() throws IOException {

			// Create the name for the file
			String name = "_" + this.fileIndex + "-" + this.resourcePath;
			this.fileIndex++;
			name = name.replace('/', '_');

			// Create and register the new file
			Path newFile = HttpResourceStoreImpl.this.fileCache.createFile(name);
			this.cleanupFiles.add(newFile);

			// Return the new file
			return newFile;
		}

		@Override
		public String getContentType() {
			return this.contentType.getValue();
		}

		@Override
		public Charset getCharset() {
			return this.charset != null ? this.charset : HttpResourceStoreImpl.this.charset;
		}

		@Override
		public void setContentType(HttpHeaderValue contentType, Charset charset) {
			if (contentType != null) {
				this.contentType = contentType;
			}
			this.charset = charset;
		}

		@Override
		public String getContentEncoding() {
			return this.contentEncoding != null ? this.contentEncoding.getValue() : null;
		}

		@Override
		public void setContentEncoding(HttpHeaderValue contentEncoding) throws IOException {
			if (contentEncoding != null) {
				this.contentEncoding = contentEncoding;
			}
		}

		@Override
		public void setTransformedResource(Path resource) {
			if (resource != null) {
				this.resource = resource;
			}
		}
	}

}
