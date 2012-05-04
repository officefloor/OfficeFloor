/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.resource.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.HttpResourceUtil;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.NotExistHttpResource;
import net.officefloor.plugin.web.http.resource.classpath.ClasspathHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.direct.DirectHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.war.WarHttpResourceFactory;

/**
 * <p>
 * {@link HttpResourceFactory} initialised from {@link SourceProperties}.
 * <p>
 * It incorporates other {@link HttpResourceFactory} implementations configured
 * from the {@link SourceProperties} to achieve its functionality. This provides
 * reusable {@link HttpResourceFactory} capabilities for the various HTTP Web
 * {@link ManagedObjectSource} and {@link WorkSource} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceHttpResourceFactory implements HttpResourceFactory {

	/**
	 * <p>
	 * Target to add {@link Property} values.
	 * <p>
	 * This allows differing target types to receive the {@link Property}
	 * values.
	 */
	public static interface PropertyTarget {

		/**
		 * Adds the {@link Property}.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		void addProperty(String name, String value);
	}

	/**
	 * <p>
	 * Copies the {@link Property} values of interest.
	 * <p>
	 * This is to aid configuration within the {@link OfficeFloorDeployer} to be
	 * provided down to this.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param target
	 *            {@link PropertyTarget}.
	 */
	public static void copyProperties(SourceProperties properties,
			PropertyTarget target) {
		copyProperty(properties, PROPERTY_CLASS_PATH_PREFIX, target);
		copyProperty(properties, PROPERTY_WAR_DIRECTORY, target);
		copyProperty(properties, PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, target);
		copyProperty(properties, PROPERTY_DIRECT_STATIC_CONTENT, target);
	}

	/**
	 * Copies a particular {@link Property}.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param propertyName
	 *            Name of {@link Property} to copy.
	 * @param target
	 *            {@link PropertyTarget}.
	 */
	private static void copyProperty(SourceProperties properties,
			String propertyName, PropertyTarget target) {

		// Obtain the property value
		String propertyValue = properties.getProperty(propertyName, null);

		// Only copy if have a value
		if (propertyValue != null) {
			target.addProperty(propertyName, propertyValue);
		}
	}

	/**
	 * Property to specify the WAR directory.
	 */
	public static String PROPERTY_WAR_DIRECTORY = "http.war.directory";

	/**
	 * Property to specify the class path prefix.
	 */
	public static String PROPERTY_CLASS_PATH_PREFIX = "http.classpath.prefix";

	/**
	 * <p>
	 * Property to specify that all {@link HttpFile} and {@link HttpDirectory}
	 * instances are stored in memory with direct {@link ByteBuffer} content for
	 * performance.
	 * <p>
	 * Should there be changing static content then this should be set to
	 * <code>false</code>. Typically this is only during development as in
	 * production content is fixed.
	 */
	public static String PROPERTY_DIRECT_STATIC_CONTENT = "http.direct.static.content";

	/**
	 * Property to specify the listing of default directory file names.
	 */
	public static String PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES = "http.default.directory.file.names";

	/**
	 * Creates the {@link HttpResourceFactory}.
	 * 
	 * @param context
	 *            {@link SourceContext} to configure the
	 *            {@link HttpResourceFactory} (and its delegates).
	 * @return {@link HttpResourceFactory}.
	 * @throws IOException
	 *             Should the configured resources not be accessible or not
	 *             exist.
	 */
	public static HttpResourceFactory createHttpResourceFactory(
			SourceContext context) throws IOException {

		// Obtain the configuration for HTTP Resource Factory
		String warDirectories = context.getProperty(PROPERTY_WAR_DIRECTORY,
				null);
		String classPathPrefix = context.getProperty(
				PROPERTY_CLASS_PATH_PREFIX,
				WebAutoWireApplication.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX);
		ClassLoader classLoader = context.getClassLoader();
		boolean isDirect = Boolean.parseBoolean(context.getProperty(
				PROPERTY_DIRECT_STATIC_CONTENT, String.valueOf(Boolean.TRUE)));
		String defaultDirectoryFileNames = context.getProperty(
				PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, "index.html");

		// Obtain the listing of default directory file names
		List<String> defaultDirectoryFileNameListing = new LinkedList<String>();
		for (String defaultDirectoryFileName : defaultDirectoryFileNames
				.split(";")) {

			// Ignore blank entries
			defaultDirectoryFileName = defaultDirectoryFileName.trim();
			if (defaultDirectoryFileName.length() == 0) {
				continue; // ignore the blank entry
			}

			// Remove leading slashes from name
			while (defaultDirectoryFileName.startsWith("/")) {
				defaultDirectoryFileName = defaultDirectoryFileName
						.substring("/".length());
			}

			// Add the default directory file name
			defaultDirectoryFileNameListing.add(defaultDirectoryFileName);
		}
		String[] defaultFileNames = defaultDirectoryFileNameListing
				.toArray(new String[defaultDirectoryFileNameListing.size()]);

		// Provide listing of delegate HTTP resource factories
		List<HttpResourceFactory> factories = new LinkedList<HttpResourceFactory>();

		// Create the listing of war directories
		if (warDirectories != null) {
			for (String warDirectory : warDirectories.split(";")) {

				// Ignore blank entries
				warDirectory = warDirectory.trim();
				if (warDirectory.length() == 0) {
					continue; // ignore blank entry
				}

				// Locate the WAR directory
				File warDirectoryFile = new File(warDirectory);
				if (!(warDirectoryFile.isDirectory())) {
					throw new FileNotFoundException(
							"Can not find WAR directory '" + warDirectory + "'");
				}

				// Create and register the WAR HTTP Resource Factory
				HttpResourceFactory warFactory = WarHttpResourceFactory
						.getHttpResourceFactory(warDirectoryFile,
								defaultFileNames);
				factories.add(warFactory);
			}
		}

		// Create and register the Class path HTTP Resource Factory
		HttpResourceFactory classpathFactory = ClasspathHttpResourceFactory
				.getHttpResourceFactory(classPathPrefix, classLoader,
						defaultFileNames);
		factories.add(classpathFactory);

		// Create the Source HTTP Resource Factory
		HttpResourceFactory resourceFactory = new SourceHttpResourceFactory(
				factories.toArray(new HttpResourceFactory[factories.size()]));

		// Determine if wrap with Direct HTTP Resource Factory
		if (isDirect) {
			resourceFactory = new DirectHttpResourceFactory(resourceFactory,
					defaultFileNames);
		}

		// Return the HTTP Resource Factory
		return resourceFactory;
	}

	/**
	 * Clears the {@link HttpResourceFactory} instances. This is useful for
	 * testing to have a fresh {@link HttpResourceFactory}.
	 */
	public static void clearHttpResourceFactories() {
		WarHttpResourceFactory.clearHttpResourceFactories();
		ClasspathHttpResourceFactory.clearHttpResourceFactories();
	}

	/**
	 * {@link HttpResourceFactory} instances for delegation to obtain the
	 * {@link HttpResource} instances.
	 */
	private final HttpResourceFactory[] httpResourceFactories;

	/**
	 * Initiate.
	 * 
	 * @param httpResourceFactories
	 *            {@link HttpResourceFactory} instances for delegation to obtain
	 *            the {@link HttpResource} instances.
	 */
	private SourceHttpResourceFactory(
			HttpResourceFactory... httpResourceFactories) {
		this.httpResourceFactories = httpResourceFactories;
	}

	/*
	 * ==================== HttpResourceFactory =======================
	 */

	@Override
	public void addHttpFileDescriber(HttpFileDescriber httpFileDescriber) {
		for (HttpResourceFactory factory : this.httpResourceFactories) {
			factory.addHttpFileDescriber(httpFileDescriber);
		}
	}

	@Override
	public HttpResource createHttpResource(String requestUriPath)
			throws IOException, InvalidHttpRequestUriException {

		// Obtain the canoncial path
		String canonicalPath = HttpResourceUtil
				.transformToCanonicalPath(requestUriPath);

		// Use HTTP Resource Factories to locate the resource
		for (HttpResourceFactory resourceFactory : this.httpResourceFactories) {

			// Attempt to obtain the resource
			HttpResource resource = resourceFactory
					.createHttpResource(canonicalPath);
			if (resource.isExist()) {
				// Found the resource
				return resource;
			}
		}

		// As here, the resource was not found
		return new NotExistHttpResource(canonicalPath);
	}

}