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
package net.officefloor.web.resource.direct;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.build.HttpFileDescriber;
import net.officefloor.web.resource.impl.AbstractHttpResource;
import net.officefloor.web.resource.impl.HttpResourceFactory;
import net.officefloor.web.resource.impl.NotExistHttpResource;

/**
 * {@link HttpResourceFactory} to keep {@link HttpFile} and
 * {@link HttpDirectory} instances in memory with direct {@link ByteBuffer} for
 * improved performance.
 * 
 * @author Daniel Sagenschneider
 */
public class DirectHttpResourceFactory implements HttpResourceFactory {

	/**
	 * Delegate {@link HttpResourceFactory}.
	 */
	private final HttpResourceFactory delegate;

	/**
	 * Default directory file names.
	 */
	private final String[] defaultDirectoryFileNames;

	/**
	 * Mapping of in memory {@link HttpResource} by its canonical resource path.
	 */
	private final Map<String, HttpResource> resources = new HashMap<String, HttpResource>();

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link HttpResourceFactory}.
	 * @param defaultDirectoryFileNames
	 *            Default directory file names.
	 */
	public DirectHttpResourceFactory(HttpResourceFactory delegate,
			String... defaultDirectoryFileNames) {
		this.delegate = delegate;
		this.defaultDirectoryFileNames = defaultDirectoryFileNames;
	}

	/*
	 * ===================== HttpResourceFactory ======================
	 */

	@Override
	public void addHttpFileDescriber(HttpFileDescriber httpFileDescriber) {
		this.delegate.addHttpFileDescriber(httpFileDescriber);
	}

	@Override
	public HttpResource createHttpResource(String applicationCanonicalPath)
			throws IOException {

		// Attempt to obtain in memory resource
		HttpResource resource;
		synchronized (this.resources) {
			resource = this.resources.get(applicationCanonicalPath);
		}
		if (resource != null) {
			// Use the in memory resource
			return resource;
		}

		// Obtain the resource from delegate
		resource = this.delegate.createHttpResource(applicationCanonicalPath);

		// Determine if HTTP File
		if (resource instanceof HttpFile) {
			// Create the in memory HTTP File
			HttpFile delegateFile = (HttpFile) resource;
			HttpFile file = new DirectHttpFile(applicationCanonicalPath,
					delegateFile);

			// Register for further look ups
			synchronized (this.resources) {
				this.resources.put(applicationCanonicalPath, file);
			}

			// Return the HTTP file
			return file;

		} else if (resource instanceof HttpDirectory) {
			// Obtain the default file for HTTP directory
			String directoryResourcePath = (applicationCanonicalPath
					.endsWith("/") ? applicationCanonicalPath
					: applicationCanonicalPath + "/");
			HttpFile defaultFile = null;
			FOUND_DEFAULT_FILE: for (String defaultFileName : this.defaultDirectoryFileNames) {

				// Obtain the path to the default file
				String defaultFileResourcePath = directoryResourcePath
						+ defaultFileName;

				// Look up the default file
				HttpResource defaultResource;
				synchronized (this.resources) {
					defaultResource = this.resources
							.get(defaultFileResourcePath);
				}
				if (defaultResource instanceof DirectHttpFile) {
					// Already have direct default file
					defaultFile = (DirectHttpFile) defaultResource;
					break FOUND_DEFAULT_FILE;
				}

				// Attempt to obtain default file
				defaultResource = this.delegate
						.createHttpResource(defaultFileResourcePath);
				if (defaultResource instanceof HttpFile) {
					// Create the direct HTTP file
					HttpFile delegateFile = (HttpFile) defaultResource;
					defaultFile = new DirectHttpFile(defaultFileResourcePath,
							delegateFile);

					// Register for further look ups
					synchronized (this.resources) {
						this.resources
								.put(defaultFileResourcePath, defaultFile);
					}

					// Have the default file
					break FOUND_DEFAULT_FILE;
				}
			}

			// Create the in memory HTTP Directory
			HttpDirectory delegateDirectory = (HttpDirectory) resource;
			HttpDirectory directory = new DirectHttpDirectory(
					directoryResourcePath, delegateDirectory, defaultFile);

			// Register for further look ups
			synchronized (this.resources) {
				this.resources.put(applicationCanonicalPath, directory);
			}

			// Return the HTTP directory
			return directory;
		}

		/*
		 * File not found. Do not keep in memory as should not be getting this
		 * error on a well built application often enough to require improved
		 * performance. Plus keeping in memory runs risk of Out of Memory error.
		 */
		return new NotExistHttpResource(applicationCanonicalPath);
	}

	/**
	 * Direct {@link HttpFile}.
	 */
	private static class DirectHttpFile extends AbstractHttpResource implements
			HttpFile {

		/**
		 * Delegate {@link HttpFile}.
		 */
		private final HttpFile delegate;

		/**
		 * Contents.
		 */
		private final ByteBuffer contents;

		/**
		 * Initiate.
		 * 
		 * @param resourcePath
		 *            Resource path.
		 * @param delegate
		 *            Delegate {@link HttpFile}.
		 */
		public DirectHttpFile(String resourcePath, HttpFile delegate) {
			super(resourcePath);
			this.delegate = delegate;

			// Provide direct memory for file contents
			ByteBuffer buffer = delegate.getContents().duplicate();
			ByteBuffer contents = ByteBuffer.allocateDirect(buffer.remaining());
			contents.put(buffer);
			contents.flip();
			this.contents = contents.asReadOnlyBuffer();
		}

		/*
		 * =========================== HttpFile ==============================
		 */

		@Override
		public boolean isExist() {
			return this.delegate.isExist();
		}

		@Override
		public String getContentEncoding() {
			return this.delegate.getContentEncoding();
		}

		@Override
		public String getContentType() {
			return this.delegate.getContentType();
		}

		@Override
		public Charset getCharset() {
			return this.delegate.getCharset();
		}

		@Override
		public ByteBuffer getContents() {
			return this.contents;
		}
	}

	/**
	 * Direct {@link HttpDirectory}.
	 */
	private static class DirectHttpDirectory extends AbstractHttpResource
			implements HttpDirectory {

		/**
		 * Delegate {@link HttpDirectory}.
		 */
		private final HttpDirectory delegate;

		/**
		 * Default {@link HttpFile}.
		 */
		private final HttpFile defaultFile;

		/**
		 * Initiate.
		 * 
		 * @param resourcePath
		 *            Resource path.
		 * @param delegate
		 *            Delegate {@link HttpDirectory}.
		 * @param defaultFile
		 *            Default {@link HttpFile}.
		 */
		public DirectHttpDirectory(String resourcePath, HttpDirectory delegate,
				HttpFile defaultFile) {
			super(resourcePath);
			this.delegate = delegate;
			this.defaultFile = defaultFile;
		}

		/*
		 * ======================= HttpDirectory ======================
		 */

		@Override
		public boolean isExist() {
			return this.delegate.isExist();
		}

		@Override
		public HttpFile getDefaultFile() {
			return this.defaultFile;
		}

		@Override
		public HttpResource[] listResources() {
			// Return as is due to not used by web application
			return this.delegate.listResources();
		}
	}

}