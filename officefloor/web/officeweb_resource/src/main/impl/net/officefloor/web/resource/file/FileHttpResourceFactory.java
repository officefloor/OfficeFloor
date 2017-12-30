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
package net.officefloor.web.resource.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.build.HttpFileDescription;
import net.officefloor.web.resource.impl.AbstractHttpFileDescription;
import net.officefloor.web.resource.impl.AbstractHttpResourceFactory;
import net.officefloor.web.resource.impl.NotExistHttpResource;

/**
 * Locates a {@link HttpFile} from a {@link File} directory.
 * 
 * @author Daniel Sagenschneider
 */
public class FileHttpResourceFactory extends AbstractHttpResourceFactory {

	/**
	 * {@link Logger}.
	 */
	private static Logger LOGGER = Logger.getLogger(FileHttpResourceFactory.class.getName());

	/**
	 * Obtains the contents of the {@link HttpResource} for the {@link File}.
	 * 
	 * @param file
	 *            {@link File}.
	 * @return Class path of the {@link HttpResource}.
	 */
	public static ByteBuffer getHttpResourceContents(File file) {

		// Obtain the input stream
		InputStream content = null;
		try {
			content = new FileInputStream(file);
		} catch (IOException ex) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Failed to read content from file " + file.getAbsolutePath(), ex);
			}
		}

		// Obtain resource contents for file
		return getHttpResourceContents(content, file.getPath());
	}

	/**
	 * Creates the {@link HttpResource}.
	 * 
	 * @param resource
	 *            {@link File}.
	 * @param resourcePath
	 *            Resource path.
	 * @param defaultDirectoryFileNames
	 *            Default directory file names.
	 * @return {@link HttpResource}.
	 */
	public static HttpResource createHttpResource(File resource, String resourcePath,
			String[] defaultDirectoryFileNames) {

		// Determine if resource exists
		if (!(resource.exists())) {
			// Resource not exist
			return new NotExistHttpResource(resourcePath);
		}

		// Determine if directory
		if (resource.isDirectory()) {
			// Create and return HTTP directory
			return new FileHttpDirectory(resourcePath, resource, defaultDirectoryFileNames);
		}

		// Describe the resource
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(resourcePath, resource);
		this.describeFile(description);

		// Return the file
		return new FileHttpFile(resourcePath, resource, description);
	}

	/**
	 * Root directory.
	 */
	private final File rootDir;

	/**
	 * Names of the default {@link HttpFile} instances in order of searching for
	 * the default {@link HttpFile}.
	 */
	private final String[] defaultDirectoryFileNames;

	/**
	 * Initiate.
	 * 
	 * @param rootDir
	 *            WAR directory.
	 * @param defaultDirectoryFileNames
	 *            Names of the default {@link HttpFile} instances in order of
	 *            searching for the default {@link HttpFile}.
	 */
	private FileHttpResourceFactory(File rootDir, String[] defaultDirectoryFileNames) {
		this.rootDir = rootDir;
		this.defaultDirectoryFileNames = defaultDirectoryFileNames;
	}

	/*
	 * =================== HttpResourceFactory =======================
	 */

	@Override
	public HttpResource getHttpResource(String applicationCanonicalPath) throws IOException {

		// Obtain the resource
		File resource = new File(this.rootDir, applicationCanonicalPath);

		// Create and return the HTTP resource
		return createHttpResource(resource, applicationCanonicalPath, this.defaultDirectoryFileNames);
	}

	/**
	 * {@link HttpFileDescription} implementation.
	 */
	private static class HttpFileDescriptionImpl extends AbstractHttpFileDescription {

		/**
		 * {@link File}.
		 */
		private final File file;

		/**
		 * Initiate.
		 * 
		 * @param resourcePath
		 *            {@link HttpResource} path.
		 * @param file
		 *            {@link File}.
		 */
		public HttpFileDescriptionImpl(String resourcePath, File file) {
			super(resourcePath);
			this.file = file;
		}

		/*
		 * ================== HttpFileDescription ============================
		 */

		@Override
		public ByteBuffer getContents() {
			return getHttpResourceContents(this.file);
		}
	}

}