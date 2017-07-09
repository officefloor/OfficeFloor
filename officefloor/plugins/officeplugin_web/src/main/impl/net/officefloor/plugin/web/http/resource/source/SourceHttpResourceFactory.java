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
package net.officefloor.plugin.web.http.resource.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.properties.PropertiesUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
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
 * {@link ManagedObjectSource} and {@link ManagedFunctionSource} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceHttpResourceFactory implements HttpResourceFactory {

	/**
	 * <p>
	 * Copies the {@link Property} values of interest.
	 * <p>
	 * This is to aid configuration within the {@link OfficeFloorDeployer} to be
	 * provided down to this.
	 * 
	 * @param source
	 *            {@link SourceProperties}.
	 * @param target
	 *            {@link PropertyConfigurable}.
	 */
	public static void copyProperties(SourceProperties source,
			PropertyConfigurable target) {
		PropertiesUtil.copyProperties(source, target,
				PROPERTY_CLASS_PATH_PREFIX, PROPERTY_RESOURCE_DIRECTORIES,
				PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
				PROPERTY_DIRECT_STATIC_CONTENT);
	}

	/**
	 * <p>
	 * Convenience method for programmatically loading configuration properties.
	 * <p>
	 * This handles converting to the necessary configuration {@link Property}
	 * values.
	 * 
	 * @param classPathPrefix
	 *            Class path prefix. May be <code>null</code> to use default.
	 * @param resourceDirectories
	 *            Resource directories. May be <code>null</code> or empty array
	 *            to not specify.
	 * @param defaultDirectoryFileNames
	 *            Default directory file names. May be <code>null</code> or
	 *            empty array to use default.
	 * @param isDirect
	 *            Indicates if {@link HttpResource} content is kept in memory
	 *            (direct {@link ByteBuffer} instances). May be
	 *            <code>null</code> to use default.
	 * @param target
	 *            {@link PropertyConfigurable}.
	 */
	public static void loadProperties(String classPathPrefix,
			File[] resourceDirectories, String[] defaultDirectoryFileNames,
			Boolean isDirect, PropertyConfigurable target) {

		// Load class path prefix (if provided)
		if ((classPathPrefix != null) && (classPathPrefix.length() > 0)) {
			target.addProperty(PROPERTY_CLASS_PATH_PREFIX, classPathPrefix);
		}

		// Load resource directories (if provided)
		if ((resourceDirectories != null) && (resourceDirectories.length > 0)) {
			StringBuilder value = new StringBuilder();
			boolean isFirst = true;
			for (File resourcedirectory : resourceDirectories) {
				if (!isFirst) {
					value.append(";");
				}
				isFirst = false;
				value.append(resourcedirectory.getAbsolutePath());
			}
			target.addProperty(PROPERTY_RESOURCE_DIRECTORIES, value.toString());
		}

		// Load default directory file names (if provided)
		if ((defaultDirectoryFileNames != null)
				&& (defaultDirectoryFileNames.length > 0)) {
			StringBuilder value = new StringBuilder();
			boolean isFirst = true;
			for (String fileName : defaultDirectoryFileNames) {
				if (!isFirst) {
					value.append(";");
				}
				isFirst = false;
				value.append(fileName);
			}
			target.addProperty(PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
					value.toString());
		}

		// Load direct content (if provided)
		if (isDirect != null) {
			target.addProperty(PROPERTY_DIRECT_STATIC_CONTENT,
					String.valueOf(isDirect));
		}
	}

	/**
	 * Property to specify the class path prefix.
	 */
	public static String PROPERTY_CLASS_PATH_PREFIX = "http.classpath.prefix";

	/**
	 * Property to specify the resource directories.
	 */
	public static String PROPERTY_RESOURCE_DIRECTORIES = "http.resource.directories";

	/**
	 * Property to specify the listing of default directory file names.
	 */
	public static String PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES = "http.default.directory.file.names";

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
		String warDirectories = context.getProperty(
				PROPERTY_RESOURCE_DIRECTORIES, null);
		String classPathPrefix = context.getProperty(
				PROPERTY_CLASS_PATH_PREFIX,
				WebArchitect.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX);
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
	public HttpResource createHttpResource(String applicationCanonicalPath)
			throws IOException {

		// Use HTTP Resource Factories to locate the resource
		for (HttpResourceFactory resourceFactory : this.httpResourceFactories) {

			// Attempt to obtain the resource
			HttpResource resource = resourceFactory
					.createHttpResource(applicationCanonicalPath);
			if (resource.isExist()) {
				// Found the resource
				return resource;
			}
		}

		// As here, the resource was not found
		return new NotExistHttpResource(applicationCanonicalPath);
	}

}