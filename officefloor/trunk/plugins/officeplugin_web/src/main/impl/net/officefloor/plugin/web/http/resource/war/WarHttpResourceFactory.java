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
package net.officefloor.plugin.web.http.resource.war;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.plugin.web.http.resource.AbstractHttpFileDescription;
import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescription;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.NotExistHttpResource;

/**
 * <p>
 * Locates a {@link HttpFile} from a WAR directory or archive.
 * <p>
 * As per Servlet standards it will not provide access to files within the
 * <code>WEB-INF</code> directory.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpResourceFactory extends AbstractHttpResourceFactory {

	/**
	 * {@link Logger}.
	 */
	private static Logger LOGGER = Logger
			.getLogger(WarHttpResourceFactory.class.getName());

	/**
	 * <p>
	 * {@link WarHttpResourceFactory} instances by WAR canonical path.
	 * <p>
	 * Typically within an application there will only be one of these.
	 */
	private static final Map<String, WarHttpResourceFactory> factories = new HashMap<String, WarHttpResourceFactory>(
			1);

	/**
	 * Obtains the {@link WarHttpResourceFactory} for the WAR directory.
	 * 
	 * @param warDirectory
	 *            WAR directory.
	 * @param defaultDirectoryFileNames
	 *            Default directory file names. Should the
	 *            {@link WarHttpResourceFactory} already be created for the WAR
	 *            directory, these values are ignored.
	 * @return {@link WarHttpResourceFactory} for the WAR directory.
	 * @throws IOException
	 *             If fails to obtain {@link WarHttpResourceFactory}.
	 */
	public static WarHttpResourceFactory getHttpResourceFactory(
			File warDirectory, String... defaultDirectoryFileNames)
			throws IOException {

		// Obtain the identifier
		String warPath = warDirectory.getCanonicalPath();

		// Obtain and return the HTTP Resource factory
		return getHttpResourceFactory(warPath, warDirectory,
				defaultDirectoryFileNames);
	}

	/**
	 * Obtains the {@link WarHttpResourceFactory} for the WAR path. This is
	 * available for {@link WarHttpDirectory} and {@link WarHttpFile}.
	 * 
	 * @param warPath
	 *            WAR path.
	 * @param warDirectory
	 *            WAR directory.
	 * @param defaultDirectoryFileNames
	 *            Default directory file names. Should the
	 *            {@link WarHttpResourceFactory} already be created for the WAR
	 *            directory, these values are ignored.
	 * @return {@link WarHttpResourceFactory} for the WAR directory.
	 */
	static WarHttpResourceFactory getHttpResourceFactory(String warPath,
			File warDirectory, String... defaultDirectoryFileNames) {

		// Attempt to obtain existing factory
		WarHttpResourceFactory factory;
		synchronized (factories) {
			factory = factories.get(warPath);
			if (factory == null) {

				// Ensure have war directory (not supplied if looking up)
				if (warDirectory == null) {
					throw new IllegalStateException("Looking up unknown "
							+ WarHttpResourceFactory.class.getSimpleName()
							+ " '" + warPath + "'");
				}

				// Prepare default directory file names (remove leading '/')
				String[] defaultFileNames = new String[defaultDirectoryFileNames.length];
				System.arraycopy(defaultDirectoryFileNames, 0,
						defaultFileNames, 0, defaultFileNames.length);
				for (int i = 0; i < defaultFileNames.length; i++) {
					while (defaultFileNames[i].startsWith("/")) {
						defaultFileNames[i] = defaultFileNames[i].substring("/"
								.length());
					}
				}

				// Not exist, so create factory
				factory = new WarHttpResourceFactory(warPath, warDirectory,
						defaultFileNames);

				// Register the factory for the WAR directory
				factories.put(warPath, factory);
			}
		}

		// Return the factory
		return factory;
	}

	/**
	 * Allows clearing the {@link HttpResourceFactory} instances.
	 */
	public static void clearHttpResourceFactories() {
		synchronized (factories) {
			factories.clear();
		}
	}

	/**
	 * Obtains the contents of the {@link HttpResource} for the {@link File}.
	 * 
	 * @return Class path of the {@link HttpResource}.
	 */
	public static ByteBuffer getHttpResourceContents(File file) {

		// Obtain the input stream
		InputStream content = null;
		try {
			content = new FileInputStream(file);
		} catch (IOException ex) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Failed to read content from file "
						+ file.getAbsolutePath(), ex);
			}
		}

		// Obtain resource contents for file
		return getHttpResourceContents(content, file.getPath());
	}

	/**
	 * WAR identifier for this {@link WarHttpResourceFactory}.
	 */
	private final String warIdentifier;

	/**
	 * WAR directory.
	 */
	private final File warDir;

	/**
	 * Names of the default {@link HttpFile} instances in order of searching for
	 * the default {@link HttpFile}.
	 */
	private final String[] defaultDirectoryFileNames;

	/**
	 * Initiate.
	 * 
	 * @param warIdentifier
	 *            WAR identifier for this {@link WarHttpResourceFactory}.
	 * @param warDir
	 *            WAR directory.
	 * @param defaultDirectoryFileNames
	 *            Names of the default {@link HttpFile} instances in order of
	 *            searching for the default {@link HttpFile}.
	 */
	private WarHttpResourceFactory(String warIdentifier, File warDir,
			String[] defaultDirectoryFileNames) {
		this.warIdentifier = warIdentifier;
		this.warDir = warDir;
		this.defaultDirectoryFileNames = defaultDirectoryFileNames;
	}

	/**
	 * Creates the {@link HttpResource}.
	 * 
	 * @param resource
	 *            {@link File}.
	 * @param resourcePath
	 *            Resource path.
	 * @return {@link HttpResource}.
	 */
	public HttpResource createHttpResource(File resource, String resourcePath) {

		// Determine if resource exists
		if (!(resource.exists())) {
			// Resource not exist
			return new NotExistHttpResource(resourcePath);
		}

		// Determine if directory
		if (resource.isDirectory()) {
			// Create and return HTTP directory
			return new WarHttpDirectory(resourcePath, this.warIdentifier,
					resource, this.defaultDirectoryFileNames);
		}

		// Describe the resource
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(
				resourcePath, resource);
		this.describeFile(description);

		// Return the file
		return new WarHttpFile(resourcePath, resource, description);
	}

	/*
	 * =================== HttpResourceFactory =======================
	 */

	@Override
	public HttpResource createHttpResource(String applicationCanonicalPath)
			throws IOException {

		// Obtain the resource
		File resource = new File(this.warDir, applicationCanonicalPath);

		// Create and return the HTTP resource
		return this.createHttpResource(resource, applicationCanonicalPath);
	}

	/**
	 * {@link HttpFileDescription} implementation.
	 */
	private static class HttpFileDescriptionImpl extends
			AbstractHttpFileDescription {

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